package io.blodhgarm.personality.server;

import com.google.common.collect.HashBiMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import io.blodhgarm.personality.Networking;
import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.api.character.CharacterManager;
import io.blodhgarm.personality.api.utils.PlayerAccess;
import io.blodhgarm.personality.api.addon.AddonRegistry;
import io.blodhgarm.personality.api.addon.BaseAddon;
import io.blodhgarm.personality.api.events.FinalizedPlayerConnectionEvent;
import io.blodhgarm.personality.api.reveal.InfoRevealLevel;
import io.blodhgarm.personality.api.reveal.KnownCharacter;
import io.blodhgarm.personality.packets.IntroductionPackets;
import io.blodhgarm.personality.packets.SyncS2CPackets;
import io.blodhgarm.personality.utils.DebugCharacters;
import io.wispforest.owo.Owo;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

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

                Networking.sendToAll(new SyncS2CPackets.SyncBaseCharacterData(characterJson, AddonRegistry.INSTANCE.loadAddonsFromDisc(c, true)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return c;
    }

    @Override
    public PlayerAccess<ServerPlayerEntity> getPlayer(String characterUUID) {
        String playerUUID = playerToCharacterReferences().inverse().get(characterUUID);

        if(playerUUID != null) {
            return new PlayerAccess<>(playerUUID, getPlayerFromServer(playerUUID));
        }

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

    public void killCharacter(String uuid) {
        killCharacter(characterIDToCharacter.get(uuid));
    }

    public void killCharacter(Character c) {
        this.removeCharacter(c.getUUID());

        c.setIsDead(true);

        saveCharacter(c, false);
    }

    public void deleteCharacter(Character character) {
        deleteCharacter(character.getUUID());
    }

    public void deleteCharacter(String uuid) {
        this.removeCharacter(uuid);

        try {
            Files.delete(getCharacterInfo(uuid));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //----------------------------------------------------

    @Override
    public void revealCharacterInfo(ServerPlayerEntity source, Collection<ServerPlayerEntity> targets, InfoRevealLevel level) {
        Character sourceCharacter = this.getCharacter(source);

        if (sourceCharacter == null) return;

        for (ServerPlayerEntity otherPlayer : targets) {
            Character targetCharacter = this.getCharacter(otherPlayer);

            if(targetCharacter != null) revealCharacterInfo(sourceCharacter, targetCharacter, level).accept(otherPlayer);
        }
    }

    @Override
    public Consumer<ServerPlayerEntity> revealCharacterInfo(Character sourceC, Character targetC, InfoRevealLevel level) {
        if (!targetC.getKnownCharacters().containsKey(sourceC.getUUID())) {
            KnownCharacter character = new KnownCharacter(targetC.getUUID(), sourceC.getUUID());

            character.updateInfoLevel(level);

            targetC.getKnownCharacters().put(sourceC.getUUID(), character);

            return player -> {
                saveCharacter(targetC);

                LOGGER.info("[ServerCharacter] A new Character (Character Name: {}) was revealed to {}", sourceC.getName(), targetC.getName());

                Networking.sendS2C(player, new IntroductionPackets.UnknownIntroduction(sourceC.getUUID()));
            };
        } else {
            KnownCharacter sourceKnownCharacter = targetC.getKnownCharacters().get(sourceC.getUUID());

            if(sourceKnownCharacter.level.shouldUpdateLevel(level)){
                sourceKnownCharacter.updateInfoLevel(level);

                return player -> {
                    saveCharacter(targetC);

                    LOGGER.info("[ServerCharacter] A already known Character (Character Name: {}) had more info revealed to {}", sourceC.getName(), targetC.getName());

                    Networking.sendS2C(player, new IntroductionPackets.UpdatedKnowledge(sourceC.getUUID()));
                };
            }
        }

        return target -> {};
    }

    //----------------------------------------------------

    public void saveCharacter(Character character) {
        saveCharacter(character, true);
    }

    public void saveCharacter(Character character, boolean syncCharacter) {
        character.beforeSaving();

        String characterJson = GSON.toJson(character);

        if(syncCharacter) Networking.sendToAll(new SyncS2CPackets.SyncBaseCharacterData(characterJson, Map.of()));

        try {
            File characterFile = getCharacterInfo(character.getUUID()).toAbsolutePath().toFile();

            characterFile.getParentFile().mkdirs();

            Files.writeString(characterFile.toPath(), characterJson);
        } catch (IOException e) {
            LOGGER.error("[ServerCharacters] A Character [Name:{}, UUID:{}] was unable to be saved to disc", character.getName(), character.getUUID());

            e.printStackTrace();
        }
    }

    public void saveAddonsForCharacter(Character c, boolean syncAddons){
        Map<Identifier, String> addonData = new HashMap<>();

        c.getAddons().keySet().forEach(s -> {
            String addonJson = saveAddonForCharacter(c, s, false);

            if(addonJson != null) addonData.put(s, addonJson);
        });

        if(syncAddons) Networking.sendToAll(new SyncS2CPackets.SyncAddonData(c.getUUID(), addonData));

    }

    public String saveAddonForCharacter(Character c, Identifier addonId, boolean syncAddons){
        BaseAddon addon = c.getAddons().get(addonId);

        if(addon != null){

            String addonJson = GSON.toJson(addon);

            try {
                if(addon.loadedProperly){
                    File addonFile = ServerCharacters.getSpecificCharacterPath(c.getUUID()).resolve("addons/" + addonId.getNamespace() + "/" + addonId.getPath() + ".json").toFile();

                    addonFile.getParentFile().mkdirs();

                    Files.writeString(addonFile.toPath(), addonJson);
                }

                if(syncAddons) Networking.sendToAll(new SyncS2CPackets.SyncAddonData(c.getUUID(), Map.of(addonId, addonJson)));

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

    public record EditorHistory(String editorUUID, LocalDateTime dateOfEdit, List<String> elementsChanges){}
}
