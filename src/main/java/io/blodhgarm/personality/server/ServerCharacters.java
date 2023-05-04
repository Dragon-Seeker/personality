package io.blodhgarm.personality.server;

import com.google.common.collect.HashBiMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.blodhgarm.personality.Networking;
import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.api.character.CharacterManager;
import io.blodhgarm.personality.api.reveal.InfoLevel;
import io.blodhgarm.personality.api.utils.PlayerAccess;
import io.blodhgarm.personality.api.addon.AddonRegistry;
import io.blodhgarm.personality.api.addon.BaseAddon;
import io.blodhgarm.personality.api.events.FinalizedPlayerConnectionEvent;
import io.blodhgarm.personality.api.reveal.KnownCharacter;
import io.blodhgarm.personality.packets.SyncS2CPackets;
import io.blodhgarm.personality.utils.DebugCharacters;
import io.wispforest.owo.Owo;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Client Specific Implementation of {@link CharacterManager}
 */
public class ServerCharacters extends CharacterManager<ServerPlayerEntity> implements FinalizedPlayerConnectionEvent.Finish, ServerLifecycleEvents.ServerStarted, ServerLifecycleEvents.ServerStopped {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .create();
    private static final Type REF_MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();

    public static ServerCharacters INSTANCE = new ServerCharacters();

    private static final Type EDITOR_MAP_TYPE = new TypeToken<Map<String, ArrayList<EditorHistory>>>() {}.getType();
    private Map<String, List<EditorHistory>> characterUUIDToEditInfo = new HashMap<>();

    private static Path BASE_PATH;
    private static Path CHARACTER_PATH;
    private static Path REFERENCE_PATH;
    private static Path EDITOR_PATH;

    public ServerCharacters() {
        super("server");
    }

    /**
     * Similar method to {@link CharacterManager#getCharacter} but will attempt to load the character
     *
     * @param uuid The Possible UUID of a Character
     * @return the character bond to the given uuid or null
     */
    @Nullable
    @Override
    public Character getCharacter(String uuid) {
        Character c = super.getCharacter(uuid);

        if(c == null) {
            try {
                Path path = getCharacterInfo(uuid);

                if (!Files.exists(path)) return null;

                String characterJson = Files.readString(path);

                c = GSON.fromJson(characterJson, Character.class);

                characterLookupMap().put(uuid, c);

                sortCharacterLookupMap();

                Networking.sendToAll(new SyncS2CPackets.SyncCharacterData(characterJson, AddonRegistry.INSTANCE.loadAddonsFromDisc(c, true)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return c;
    }

    @Override
    public PlayerAccess<ServerPlayerEntity> getPlayer(String characterUUID) {
        String playerUUID = playerToCharacterReferences().inverse().get(characterUUID);

        if(playerUUID != null) return new PlayerAccess<>(playerUUID, getPlayerFromServer(playerUUID));

        return super.getPlayer(characterUUID);
    }

    @Override
    public boolean associateCharacterToPlayer(String characterUUID, String playerUUID){
        //Make sure there is no association already existing and if so do the proper operations
        this.dissociateUUID(playerUUID, false);
        this.dissociateUUID(characterUUID, true);

        if(!super.associateCharacterToPlayer(characterUUID, playerUUID)) return false;

        Networking.sendToAll(new SyncS2CPackets.Association(characterUUID, playerUUID));

        saveCharacterReference();

        return true;
    }

    @Override
    @Nullable
    public String dissociateUUID(String UUID, boolean isCharacterUUID) {
        String playerUUID = super.dissociateUUID(UUID, isCharacterUUID);

        if(playerUUID != null) {
            Networking.sendToAll(new SyncS2CPackets.Dissociation(UUID, isCharacterUUID));

            ServerPlayerEntity player = getPlayerFromServer(playerUUID);

            if (player != null) AddonRegistry.INSTANCE.checkAndDefaultPlayerAddons(player);

            saveCharacterReference();
        }

        return playerUUID;
    }

    @Override
    public void removeCharacter(String characterUUID) {
        Networking.sendToAll(new SyncS2CPackets.RemoveCharacter(characterUUID));

        saveCharacterReference();

        super.removeCharacter(characterUUID);
    }

    //----------------------------------------------------

    @Nullable
    public static ServerPlayerEntity getPlayerFromServer(String playerUUID) {
        return Owo.currentServer().getPlayerManager().getPlayer(UUID.fromString(playerUUID));
    }

    public void reviveCharacter(Character c, @Nullable String playerUUID) {
        //TODO: Decide on a future way of handling Dead Players to prevent large amounts of memory usage if they won't be revived
        //this.removeCharacter(c.getUUID());

        c.setIsDead(false);

        if(playerUUID != null && !playerUUID.isBlank()) associateCharacterToPlayer(c.getUUID(), playerUUID);

        saveCharacter(c, true);
    }

    /**
     * Method used to kill an input character on the server
     * @param c
     */
    public void killCharacter(Character c) {
        //TODO: Decide on a future way of handling Dead Players to prevent large amounts of memory usage if they won't be revived
        //this.removeCharacter(c.getUUID());

        c.setIsDead(true);

        saveCharacter(c, true);
    }

    /**
     * Method that will permanently delete an input character from the Server
     */
    public void deleteCharacter(Character c) {
        String uuid = c.getUUID();

        this.removeCharacter(uuid);

        try {
            Files.delete(getCharacterInfo(uuid));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //---

    private record CharacterAction(String formalActionName, BiConsumer<Character, @Nullable String> action, Predicate<Character> actionTest, String invalidMsg) implements BiConsumer<Character, @Nullable String> {
        public CharacterAction(String formalActionName, Consumer<Character> action){
            this(formalActionName, (c, s) -> action.accept(c), c -> true, "something has gone wrong!!!");
        }

        @Override
        public void accept(Character c, @Nullable String s) {
            action.accept(c, s);
        }
    }

    private static BiConsumer<Character, String> convert(Consumer<Character> c){
        return (t, o) -> c.accept(t);
    }

    private static final Map<String, CharacterAction> actions = Map.of(
            "revive", new CharacterAction("Alive", ServerCharacters.INSTANCE::reviveCharacter, Character::isDead, "The Character is already alive, meaning it can't be revived!"),
            "kill", new CharacterAction("Dead", convert(ServerCharacters.INSTANCE::killCharacter), Predicate.not(Character::isDead), "The Character is already dead, meaning it can't be killed!"),
            "delete", new CharacterAction("Deleted", ServerCharacters.INSTANCE::deleteCharacter)
    );

    public ReturnInformation attemptActionOn(List<String> characterUUID, String actionType, ServerPlayerEntity operator){
        return attemptActionOn(characterUUID, actionType, operator, null);
    }

    public ReturnInformation attemptActionOn(List<String> characterUUID, String actionType, ServerPlayerEntity operator, @Nullable String playerUUID){
        CharacterAction action = actions.get(actionType);

        String returnMessage = null;
        boolean success = false;

        if(action == null) {
            returnMessage = "THE GIVEN ACTION HAS NOT BEEN IMPLEMENTED WITHIN THE CharacterBasedAction Packet!";
        } else if(operator == null) {
            returnMessage = "This action can not performed without a link to a given Player!";
        } else if(!PrivilegeManager.getLevel(actionType).test(operator)) {
            returnMessage = "The given Operator dose not have the required permission to run this action!";
        }

        if(returnMessage != null) return new ReturnInformation(returnMessage, actionType, success);

        //----------------------

        if(characterUUID.size() == 1) {
            Character c = ServerCharacters.INSTANCE.getCharacter(characterUUID.get(0));

            if (c == null) {
                returnMessage = "Could not locate the Character though the given selection method";//errorNoCharacterMsg(context, context.getSource().getPlayer());
            } else if(!action.actionTest().test(c)){
                returnMessage = action.invalidMsg();
            } else {
                action.accept(c, playerUUID);

                returnMessage = c.getName() + " is now " + action.formalActionName() + "! [UUID: " + c.getUUID() + "]";
                success = true;
            }
        } else {
            Set<Character> charactersAffected = new HashSet<>();

            for (String uuid : characterUUID) {
                Character c = ServerCharacters.INSTANCE.getCharacter(uuid);

                if (c == null) continue;

                if(action.actionTest().test(c)){
                    action.accept(c, playerUUID);
                } else {
                    //TODO: HANDLE THIS MESSAGE OF SOME CHARACTERS WERE NOT EFFECTED
                }

                charactersAffected.add(c);
            }

            if(charactersAffected.isEmpty()) {
                returnMessage = "A list of characters were to be " + actionType + " but were not!";
            } else {
                String charactersNameList = charactersAffected.stream().map(Character::getName).toList().toString();
                String charactersUUIDList = charactersAffected.stream().map(Character::getUUID).toList().toString();

                returnMessage = charactersNameList  + " are now " + action.formalActionName() + "! [UUIDs: " + charactersUUIDList + "]";
                success = true;
            }
        }

        return new ReturnInformation(returnMessage, actionType, success);
    }

    public record ReturnInformation(String returnMessage, String action, boolean success){}

    //----------------------------------------------------

    @Override
    public void revealCharacterInfo(ServerPlayerEntity source, Collection<ServerPlayerEntity> targets, InfoLevel level) {
        Character sourceCharacter = this.getCharacter(source);

        if (sourceCharacter == null) return;

        for (ServerPlayerEntity otherPlayer : targets) {
            Character targetCharacter = this.getCharacter(otherPlayer);

            if(targetCharacter != null) revealCharacterInfo(sourceCharacter, targetCharacter, otherPlayer, level);
        }
    }

    @Override
    public void revealCharacterInfo(Character sourceC, Character targetC, ServerPlayerEntity packetTarget, InfoLevel level) {
        KnownCharacter sourceKnownCharacter = targetC.getKnownCharacters().get(sourceC.getUUID());

        ReturnInformation returnPacket = null;

        if (sourceKnownCharacter == null) {
            sourceKnownCharacter = new KnownCharacter(targetC.getUUID(), sourceC.getUUID());

            sourceKnownCharacter.updateInfoLevel(level);

            targetC.getKnownCharacters().put(sourceC.getUUID(), sourceKnownCharacter);

            LOGGER.info("[ServerCharacter] A new Character (Character Name: {}) was revealed to {}", sourceC.getName(), targetC.getName());

            returnPacket = new ReturnInformation(sourceC.getName() + " introduced themselves for the first time!", "New Character Introduced", true);
        } else if(sourceKnownCharacter.level.shouldUpdateLevel(level)) {
            sourceKnownCharacter.updateInfoLevel(level);

            LOGGER.info("[ServerCharacter] A already known Character (Character Name: {}) had more info revealed to {}", sourceC.getName(), targetC.getName());

            returnPacket = new ReturnInformation(sourceC.getName() + " told more about themselves!", "Known Character Revealed", true);
        } else {
            LOGGER.info("[ServerCharacter] A already known Character (Character Name: {}) didn't have anymore info revealed to {}", sourceC.getName(), targetC.getName());
        }

        if(returnPacket != null){
            saveCharacter(targetC);

            Networking.sendS2C(packetTarget, returnPacket);
        }
    }

    //----------------------------------------------------

    public void saveCharacter(Character character) {
        saveCharacter(character, true);
    }

    @Nullable
    public String saveCharacter(Character character, boolean syncCharacter) {
        character.beforeSaving();

        String characterJson = GSON.toJson(character);

        if(syncCharacter){
            this.attemptApplyAddonOnSave(character);

            Networking.sendToAll(new SyncS2CPackets.SyncCharacterData(characterJson, Map.of()));
        }

        try {
            File characterFile = getCharacterInfo(character.getUUID()).toAbsolutePath().toFile();

            characterFile.getParentFile().mkdirs();

            Files.writeString(characterFile.toPath(), characterJson);
        } catch (IOException e) {
            LOGGER.error("[ServerCharacters] A Character [Name:{}, UUID:{}] was unable to be saved to disc", character.getName(), character.getUUID());

            e.printStackTrace();

            return null;
        }

        return characterJson;
    }

    /**
     * Method that will attempt to save all addons and then sync such data to all players
     *
     * @param c Character of which addons should be saved
     * @param newAddons the addons that are seemingly new and need to be saved
     * @param syncAddons whether to send a packet to sync clients of the data change
     */
    public Map<Identifier, String> saveAddonsForCharacter(Character c, Map<Identifier, BaseAddon> newAddons, boolean syncAddons){
        Map<Identifier, String> addonData = Util.make(new HashMap<>(), map -> {
            newAddons.forEach((addonId, addon) -> {
                String addonJson = saveAddonForCharacter(c, addonId, addon);

                if (addonJson != null) map.put(addonId, addonJson);
            });
        });

        if(syncAddons){
            Networking.sendToAll(new SyncS2CPackets.SyncAddonData(c.getUUID(), addonData));
        }

        return addonData;
    }

    /**
     * Primary method used to save a given addon. Such method includes checks to see if the
     * addon attempting to be saved is the same one as the current characters addon
     *
     * @param c Character of which addons should be saved
     * @param addonId Current Addon ID that is being saved
     * @param addon Addon in question being saved
     * @return the deserialized form of the saved addon or null depending on if it fails or isn't any different from previous form
     */
    public String saveAddonForCharacter(Character c, Identifier addonId, @Nullable BaseAddon addon){
        if(addon != null){
            BaseAddon prevAddon = c.getAddon(addonId);

            if(addon.equals(prevAddon)) return null;

            String addonJson = GSON.toJson(addon);

            try {
                if(addon.loadedProperly){
                    File addonFile = ServerCharacters.getSpecificCharacterPath(c.getUUID()).resolve("addons/" + addonId.getNamespace() + "/" + addonId.getPath() + ".json").toFile();

                    addonFile.getParentFile().mkdirs();

                    Files.writeString(addonFile.toPath(), addonJson);
                }

                return addonJson;
            } catch (IOException e){
                LOGGER.error("[AddonLoading] {} addon for [Name: {}, UUID: {}] was unable to be save to Disc, data was not saved.", addonId, c.getName(), c.getUUID());

                e.printStackTrace();
            }
        } else {
            LOGGER.warn("A addon was attempted to be saved to disc but the character doesn't have such a addon");
        }

        return null;
    }

    public void attemptApplyAddonOnSave(Character character){
        PlayerAccess<ServerPlayerEntity> playerAccess = this.getPlayer(character);

        if(!playerAccess.valid() || playerAccess.player() == null) return;

        this.applyAddons(playerAccess.player());
    }

    public static Path getBasePath() {
        return BASE_PATH;
    }

    public static Path getReferencePath() {
        return REFERENCE_PATH;
    }

    public static Path getSpecificCharacterPath(String uuid) {
        return CHARACTER_PATH.resolve(uuid);
    }

    public static Path getCharacterPath(String uuid) {
        return CHARACTER_PATH.resolve(uuid);
    }

    private static Path getCharacterInfo(String uuid) {
        return CHARACTER_PATH.resolve(uuid + "/info.json");
    }

    /**
     * Method used to load both the character references and the edit history
     */
    public void loadGeneralInformation() {
        BASE_PATH = Owo.currentServer().getSavePath(WorldSavePath.ROOT).resolve("mod_data/personality");

        CHARACTER_PATH = BASE_PATH.resolve("characters");
        REFERENCE_PATH = BASE_PATH.resolve("reference.json");
        EDITOR_PATH = BASE_PATH.resolve("characterEditHistory.json");

        playerIDToCharacterID.clear();
        characterIDToCharacter.clear();
        characterUUIDToEditInfo.clear();

        try {
            if(!Files.exists(CHARACTER_PATH)) Files.createDirectories(CHARACTER_PATH);
        } catch (IOException e){ e.printStackTrace(); }

        try {
            JsonObject refJsonObject = GSON.fromJson(Files.readString(REFERENCE_PATH), JsonObject.class);
            playerIDToCharacterID = HashBiMap.create(GSON.fromJson(refJsonObject.getAsJsonObject("player_to_character"), REF_MAP_TYPE));
        }
        catch (NoSuchFileException fileException) { saveCharacterReference(); }
        catch (IOException e) { e.printStackTrace(); }

        try {
            JsonObject editorJsonObject = GSON.fromJson(Files.readString(EDITOR_PATH), JsonObject.class);
            characterUUIDToEditInfo = new HashMap<>(GSON.fromJson(editorJsonObject.getAsJsonObject("history"), EDITOR_MAP_TYPE));

            for (Map.Entry<String, List<EditorHistory>> entry : characterUUIDToEditInfo.entrySet()){
                System.out.println(entry.getKey());
                for (EditorHistory history : entry.getValue()) System.out.println("  ^--> " + history);
                System.out.println();
            }
        }
        catch (NoSuchFileException fileException) { saveEditorMap(); }
        catch (IOException e) { e.printStackTrace(); }

        if(FabricLoader.getInstance().isDevelopmentEnvironment()) DebugCharacters.loadDebugCharacters(this);
    }

    public void saveCharacterReference() {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("format", 1);
            json.add("player_to_character", GSON.toJsonTree(playerToCharacterReferences(), REF_MAP_TYPE));

            Files.writeString(REFERENCE_PATH, GSON.toJson(json));
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void saveEditorMap() {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("format", 1);
            json.add("history", GSON.toJsonTree(characterUUIDToEditInfo, EDITOR_MAP_TYPE));

            Files.writeString(EDITOR_PATH, GSON.toJson(json));
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void logCharacterEditing(ServerPlayerEntity editor, Character c, List<String> elementsChanges){
        List<EditorHistory> history = characterUUIDToEditInfo.computeIfAbsent(c.getUUID(), s -> new ArrayList<>());

        history.add(new EditorHistory(editor.getUuidAsString(), LocalDateTime.now(), elementsChanges));

        saveEditorMap();
    }

    //----------------------------------------------------------------------------

    @Override
    public void onServerStarted(MinecraftServer server) {
        if(FabricLoader.getInstance().isDevelopmentEnvironment()) System.out.println("Event[ServerStarted]: Loading Server Character Data");

        loadGeneralInformation();

        File[] folders = CHARACTER_PATH.toFile().listFiles();

        if(folders == null) return;

        for(final File folderEntry : folders){
            try {
                Path path = folderEntry.toPath().resolve("info.json");

                if (!Files.exists(path)) continue;

                Character c = GSON.fromJson(Files.readString(path), Character.class);

                characterLookupMap().put(c.getUUID(), c);

                AddonRegistry.INSTANCE.loadAddonsFromDisc(c, false);
            } catch (IOException e) {
                LOGGER.error("[Server Character]: A character was unable to be loaded from the disc, such will be skipped [Path: {}]", folderEntry.getPath());
                e.printStackTrace();
            }
        }

        sortCharacterLookupMap();
    }

    @Override
    public void onFinalize(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        applyAddons(handler.player);

        Map<String, Map<Identifier, String>> characters = new HashMap<>();

        for (Character c : characterLookupMap().values()){
            characters.put(GSON.toJson(c), AddonRegistry.INSTANCE.serializesAddons(c)); //GSON.toJson(c.getAddons())
        }

        boolean loadRegistries = true;

        //Prevent Integrated player from receiving the message to load addon registries
        if(!server.isDedicated() && server.isHost(handler.player.getGameProfile())) loadRegistries = false;

        Networking.sendS2C(handler.player, new SyncS2CPackets.Initial(characters, playerToCharacterReferences(), loadRegistries));
    }

    @Override
    public void onServerStopped(MinecraftServer server) {
        this.clearRegistries();

        this.characterUUIDToEditInfo.clear();

        if(FabricLoader.getInstance().isDevelopmentEnvironment()) LOGGER.info("[Server-CharacterManager]: Manager has been cleared!");
    }

    public static final class EditorHistory {
        private final String editorUUID;
        private final LocalDateTime dateOfEdit;
        private final List<String> elementsChanges;

        public EditorHistory(String editorUUID, LocalDateTime dateOfEdit, List<String> elementsChanges) {
            this.editorUUID = editorUUID;
            this.dateOfEdit = dateOfEdit;
            this.elementsChanges = elementsChanges;
        }

        public String editorUUID() {
            return editorUUID;
        }

        public LocalDateTime dateOfEdit() {
            return dateOfEdit;
        }

        public List<String> elementsChanges() {
            return elementsChanges;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (EditorHistory) obj;
            return Objects.equals(this.editorUUID, that.editorUUID) &&
                    Objects.equals(this.dateOfEdit, that.dateOfEdit) &&
                    Objects.equals(this.elementsChanges, that.elementsChanges);
        }

        @Override
        public int hashCode() {
            return Objects.hash(editorUUID, dateOfEdit, elementsChanges);
        }

        @Override
        public String toString() {
            return "EditorHistory[" +
                    "editorUUID=" + editorUUID + ", " +
                    "dateOfEdit=" + dateOfEdit + ", " +
                    "elementsChanges=" + elementsChanges + ']';
        }
    }
}
