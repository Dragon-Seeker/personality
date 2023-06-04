package io.blodhgarm.personality.server;

import com.google.common.collect.HashBiMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import io.blodhgarm.personality.Networking;
import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.api.character.CharacterManager;
import io.blodhgarm.personality.api.character.ServerCharacter;
import io.blodhgarm.personality.api.events.OnWorldSaveEvent;
import io.blodhgarm.personality.api.reveal.InfoLevel;
import io.blodhgarm.personality.api.utils.PlayerAccess;
import io.blodhgarm.personality.api.addon.AddonRegistry;
import io.blodhgarm.personality.api.addon.BaseAddon;
import io.blodhgarm.personality.api.events.FinalizedPlayerConnectionEvent;
import io.blodhgarm.personality.api.reveal.KnownCharacter;
import io.blodhgarm.personality.misc.pond.OfflineStatExtension;
import io.blodhgarm.personality.packets.SyncS2CPackets;
import io.blodhgarm.personality.utils.CharacterReferenceData;
import io.blodhgarm.personality.utils.DebugCharacters;
import io.blodhgarm.personality.utils.gson.WrappedTypeToken;
import io.wispforest.owo.Owo;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.Stats;
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
import java.util.concurrent.Future;
import java.util.function.*;

/**
 * Client Specific Implementation of {@link CharacterManager}
 */
public class ServerCharacters extends CharacterManager<ServerPlayerEntity, ServerCharacter> implements FinalizedPlayerConnectionEvent.Finish, ServerLifecycleEvents.ServerStarted, ServerLifecycleEvents.ServerStopped, OnWorldSaveEvent.Save {

    public static ServerCharacters INSTANCE = new ServerCharacters();

    private static final Type REF_MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();
    private static final Type EDITOR_MAP_TYPE = new TypeToken<Map<String, ArrayList<EditorHistory>>>() {}.getType();

    //-----------------

    private final Gson GSON = PersonalityMod.GSON.newBuilder()
            .registerTypeAdapter(ServerCharacter.class, (InstanceCreator<ServerCharacter>) type -> {
                Character c = CharacterReferenceData.attemptGetCharacter(type);

                if(c == null) c = new ServerCharacter("", "", "", "", "", -1);

                return (ServerCharacter) c.setCharacterManager(this);
            })
            .registerTypeAdapter(KnownCharacter.class, (InstanceCreator<KnownCharacter>) type -> {
                return (KnownCharacter) new KnownCharacter("", "")
                        .setCharacterManager(this);
            })
            .create();

    private Map<String, List<EditorHistory>> characterUUIDToEditInfo = new HashMap<>();

    private static Path BASE_PATH;
    private static Path CHARACTER_PATH;
    private static Path REFERENCE_PATH;
    private static Path EDITOR_PATH;

    public boolean saveCharacterReference = false;

    public ServerCharacters() {
        super("server");
    }

    /**
     * Similar method to {@link CharacterManager#getCharacter} but will attempt to load the character
     *
     * @param cUUID The Possible uuid of a Character
     * @return the character bond to the given uuid or null
     */
    @Nullable
    @Override
    public Character getCharacter(String cUUID) {
        Character c = super.getCharacter(cUUID);

        if(c == null) {
            try {
                Path path = getCharacterInfo(cUUID);

                if (!Files.exists(path)) return null;

                String characterJson = Files.readString(path);

                c = deserializeCharacter(characterJson);

                c.setCharacterManager(ServerCharacters.INSTANCE);

                characterLookupMap().put(cUUID, c);

                sortCharacterLookupMap();

                Networking.sendToAll(new SyncS2CPackets.SyncCharacterData(characterJson, AddonRegistry.INSTANCE.loadAddonsFromDisc(c, true)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return c;
    }

    @Override
    protected WrappedTypeToken<ServerCharacter> getToken() {
        return new WrappedTypeToken<>(){};
    }

    @Override
    public Gson getGson() {
        return GSON;
    }

    //--------------

    @Override
    public PlayerAccess<ServerPlayerEntity> getPlayer(@Nullable String pUUID) {
        if(pUUID != null) return new PlayerAccess<>(pUUID, getPlayerFromServer(pUUID));

        return super.getPlayer(pUUID);
    }

    @Nullable
    public StatHandler getStats(String playerUUID){
        PlayerAccess<ServerPlayerEntity> playerAccess = ServerCharacters.INSTANCE.getPlayer(playerUUID);

        if(playerAccess.isEmpty()) return null;

        return playerAccess.playerValid()
                ? ServerCharacters.INSTANCE.getStats(playerAccess.player())
                : ServerCharacters.INSTANCE.getStats(playerAccess.getProfile());
    }

    public StatHandler getStats(ServerPlayerEntity player) {
        return player.getStatHandler();
    }

    public StatHandler getStats(GameProfile profile) {
        return ((OfflineStatExtension) Owo.currentServer().getPlayerManager()).loadStatHandler(profile);
    }

    @Override
    public boolean associateCharacterToPlayer(String characterUUID, String playerUUID){
        //Make sure there is no association already existing and if so do the proper operations
        this.dissociateUUID(playerUUID, false);
        this.dissociateUUID(characterUUID, true);

        if(!super.associateCharacterToPlayer(characterUUID, playerUUID)) return false;

        StatHandler handler = getStats(playerUUID);

        if(handler != null) {
            ((ServerCharacter) this.getCharacter(characterUUID))
                    .setStartingPlaytime(handler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME)));
        }

        Networking.sendToAll(new SyncS2CPackets.Association(characterUUID, playerUUID));

        saveCharacterReference = true;

        return true;
    }

    @Override
    @Nullable
    public String dissociateUUID(String UUID, boolean isCharacterUUID) {
        String playerUUID = (isCharacterUUID) ? this.getPlayerUUID(UUID) : UUID;

        if(playerUUID == null) return null;

        Character character = this.getCharacter((isCharacterUUID) ? UUID : getCharacterUUID(UUID));

        if(character != null) character.beforeEvent("disassociate");

        super.dissociateUUID(UUID, isCharacterUUID);

        Networking.sendToAll(new SyncS2CPackets.Dissociation(UUID, isCharacterUUID));

        ServerPlayerEntity player = getPlayerFromServer(playerUUID);

        if (player != null) AddonRegistry.INSTANCE.checkAndDefaultPlayerAddons(player);

        return playerUUID;
    }

    @Override
    public void removeCharacter(String characterUUID) {
        Networking.sendToAll(new SyncS2CPackets.RemoveCharacter(characterUUID));

        saveCharacterReference = true;

        super.removeCharacter(characterUUID);
    }

    //----------------------------------------------------

    @Nullable
    public static ServerPlayerEntity getPlayerFromServer(String playerUUID) {
        return Owo.currentServer().getPlayerManager().getPlayer(UUID.fromString(playerUUID));
    }

    public void reviveCharacter(Character c, @Nullable String playerUUID) {
        c.setIsDead(false);

        if(playerUUID != null && !playerUUID.isBlank()) associateCharacterToPlayer(c.getUUID(), playerUUID);

        pushToSaveQueue(c, true);
    }

    /**
     * Method used to kill an input character on the server
     * @param c
     */
    public void killCharacter(Character c) {
        //TODO: Decide on a future way of handling Dead Players to prevent large amounts of memory usage if they won't be revived
        //this.removeCharacter(c.getUUID());

        c.setIsDead(true);

        pushToSaveQueue(c, true);
    }

    /**
     * Method that will permanently delete an input character from the Server
     */
    public void deleteCharacter(Character c) {
        String uuid = c.getUUID();

        this.removeCharacter(uuid);

        try {
            Files.delete(getCharacterPath(uuid));
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

                returnMessage = c.getName() + " is now " + action.formalActionName() + "! [uuid: " + c.getUUID() + "]";
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

    public interface ReturnPacketBuilder{
        Record getPacket(String message, String action);
    }

    //----------------------------------------------------

    @Override
    public void revealCharacterInfo(ServerPlayerEntity source, Collection<ServerPlayerEntity> targets, InfoLevel level) {
        Character sourceC = this.getCharacter(source);

        if (sourceC == null) return;

        for (ServerPlayerEntity targetP : targets) {
            Character targetC = this.getCharacter(targetP);

            if(targetC != null) revealCharacterInfo(sourceC, targetC, targetP, level, (message, action) -> new ReturnInformation(message, action, true));
        }
    }

    @Override
    public void revealCharacterInfo(Character sourceC, Character targetC, ServerPlayerEntity packetTarget, InfoLevel level, ReturnPacketBuilder returnPacketBuilder) {
        KnownCharacter wrappedSourceC = targetC.getKnownCharacter(sourceC);

        Record returnPacket = null;

        if (wrappedSourceC == null) {
            wrappedSourceC = (KnownCharacter) new KnownCharacter(targetC.getUUID(), sourceC.getUUID())
                    .setDiscoveredTime()
                    .updateInfoLevel(level)
                    .setCharacterManager(ServerCharacters.INSTANCE);

            targetC.addKnownCharacter(wrappedSourceC);

            LOGGER.info("[ServerCharacter] A new Character (Character Name: {}) was revealed to {}", sourceC.getName(), targetC.getName());

            returnPacket = returnPacketBuilder.getPacket(sourceC.getName() + " introduced themselves for the first time!", "New Character Introduced");
        } else if(wrappedSourceC.level.shouldUpdateLevel(level)) {
            wrappedSourceC.updateInfoLevel(level);

            LOGGER.info("[ServerCharacter] A already known Character (Character Name: {}) had more info revealed to {}", sourceC.getName(), targetC.getName());

            returnPacket = returnPacketBuilder.getPacket(sourceC.getName() + " told more about themselves!", "Known Character Revealed");
        } else {
            LOGGER.info("[ServerCharacter] A already known Character (Character Name: {}) didn't have anymore info revealed to {}", sourceC.getName(), targetC.getName());
        }

        if(returnPacket == null) return;

        pushToSaveQueue(targetC);

        Networking.sendS2C(packetTarget, returnPacket);
    }

    //----------------------------------------------------

    public void pushToSaveQueue(Character character) {
        pushToSaveQueue(character, true);
    }

    @Nullable
    public String pushToSaveQueue(Character character, boolean syncCharacter) {
        character.beforeEvent("save");

        String characterJson = GSON.toJson(character);

        if(syncCharacter) {
            this.attemptApplyAddonOnSave(character);

            Networking.sendToAll(new SyncS2CPackets.SyncCharacterData(characterJson, Map.of()));
        }

        characterSavingQueue.push(character.getUUID());

        return characterJson;
    }

    public void saveCharacter(Character character) {
        character.beforeEvent("save");

        String characterJson = GSON.toJson(character);

        try {
            File characterFile = getCharacterInfo(character.getUUID()).toAbsolutePath().toFile();

            characterFile.getParentFile().mkdirs();

            Files.writeString(characterFile.toPath(), characterJson);
        } catch (IOException e) {
            LOGGER.error("[ServerCharacters] A Character [Name:{}, uuid:{}] was unable to be saved to disc", character.getName(), character.getUUID());

            e.printStackTrace();
        }
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

        if(syncAddons) Networking.sendToAll(new SyncS2CPackets.SyncAddonData(c.getUUID(), addonData));

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
                LOGGER.error("[AddonLoading] {} addon for [Name: {}, uuid: {}] was unable to be save to Disc, data was not saved.", addonId, c.getName(), c.getUUID());

                e.printStackTrace();
            }
        } else {
            LOGGER.warn("A addon was attempted to be saved to disc but the character doesn't have such a addon");
        }

        return null;
    }

    public void attemptApplyAddonOnSave(Character character){
        PlayerAccess<ServerPlayerEntity> playerAccess = this.getPlayerFromCharacter(character);

        if(!playerAccess.playerValid()) return;

        this.applyAddons(playerAccess.player());
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

//            for (Map.Entry<String, List<EditorHistory>> entry : characterUUIDToEditInfo.entrySet()){
//                System.out.println(entry.getKey());
//                for (EditorHistory history : entry.getValue()) System.out.println("  ^--> " + history);
//                System.out.println();
//            }
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
        characterUUIDToEditInfo.computeIfAbsent(c.getUUID(), s -> new ArrayList<>())
                .add(new EditorHistory(editor.getUuidAsString(), LocalDateTime.now(), elementsChanges));

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

                Character c = deserializeCharacter(Files.readString(path));

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

        //Prevent Integrated player from receiving the deathMessage to load addon registries
        if(!server.isDedicated() && server.isHost(handler.player.getGameProfile())) loadRegistries = false;

        Networking.sendS2C(handler.player, new SyncS2CPackets.Initial(characters, playerToCharacterReferences(), loadRegistries));
    }

    @Override
    public void onServerStopped(MinecraftServer server) {
        this.clearRegistries();

        this.characterUUIDToEditInfo.clear();

        if(FabricLoader.getInstance().isDevelopmentEnvironment()) LOGGER.info("[Server-CharacterManager]: Manager has been cleared!");
    }

    //----------------

    private final Deque<String> characterSavingQueue = new ArrayDeque<>();

    @Nullable private Future<?> lastSaveFuture = null;

    @Override
    public void onSave(boolean suppressLogs, boolean flush, boolean force) {
        Runnable saveFunc = () -> {
            // Loop to save all active players given online counter stuff
            for(ServerPlayerEntity player : Owo.currentServer().getPlayerManager().getPlayerList()){
                Character c = ServerCharacters.INSTANCE.getCharacter(player);

                if(c == null) continue;

                ServerCharacters.INSTANCE.saveCharacter(c);
            }

            String currentCharacter = characterSavingQueue.poll();

            //Primary way for all characters who get modified to be saved
            while(currentCharacter != null){
                Character c = ServerCharacters.INSTANCE.getCharacter(currentCharacter);

                if(c == null) continue;

                ServerCharacters.INSTANCE.saveCharacter(c);

                currentCharacter = characterSavingQueue.poll();
            }
        };

        if(flush){
            if(lastSaveFuture != null && !lastSaveFuture.isDone()) lastSaveFuture.cancel(false);

            saveFunc.run();
        } else {
            lastSaveFuture = Util.getMainWorkerExecutor().submit(saveFunc);
        }

        if(saveCharacterReference){
            saveCharacterReference();

            saveCharacterReference = false;
        }
    };

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
