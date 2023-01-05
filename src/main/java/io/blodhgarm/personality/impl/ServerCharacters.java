package io.blodhgarm.personality.impl;

import com.google.common.collect.HashBiMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import io.blodhgarm.personality.Networking;
import io.blodhgarm.personality.api.reveal.KnownCharacter;
import io.blodhgarm.personality.api.addon.AddonRegistry;
import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.api.CharacterManager;
import io.blodhgarm.personality.api.addon.BaseAddon;
import io.blodhgarm.personality.api.reveal.InfoRevealLevel;
import io.blodhgarm.personality.packets.IntroductionPacket;
import io.blodhgarm.personality.packets.SyncS2CPackets;
import io.blodhgarm.personality.utils.DebugCharacters;
import io.blodhgarm.personality.utils.ServerAccess;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
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
import java.util.HashMap;
import java.util.Map;

public class ServerCharacters extends CharacterManager<ServerPlayerEntity> implements ServerPlayConnectionEvents.Join, ServerWorldEvents.Load {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type REF_MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();

    public static ServerCharacters INSTANCE = new ServerCharacters();

    private static Path BASE_PATH;
    private static Path CHARACTER_PATH;
    private static Path REFERENCE_PATH;

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

                Networking.sendToAll(new SyncS2CPackets.SyncCharacter(characterJson, AddonRegistry.INSTANCE.loadAddonsForCharacter(c)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return c;
    }

    @Nullable
    @Override
    public ServerPlayerEntity getPlayer(String characterUUID) {
        return ServerAccess.getPlayer(playerToCharacterReferences().inverse().get(characterUUID));
    }

    @Override
    public void associateCharacterToPlayer(String characterUUID, String playerUUID){
        super.associateCharacterToPlayer(characterUUID, playerUUID);

        Networking.sendToAll(new SyncS2CPackets.Association(characterUUID, playerUUID));

        saveCharacterReference();
    }

    @Override
    public String dissociateUUID(String UUID, boolean isCharacterUUID) {
        ServerPlayerEntity player = ServerAccess.getPlayer(super.dissociateUUID(UUID, isCharacterUUID));

        Networking.sendToAll(new SyncS2CPackets.Dissociation(UUID, isCharacterUUID));

        AddonRegistry.INSTANCE.checkAndDefaultPlayerAddons(player);

        saveCharacterReference();

        return player.getUuidAsString();
    }

    @Override
    public void removeCharacter(String characterUUID) {
        Networking.sendToAll(new SyncS2CPackets.RemoveCharacter(characterUUID));

        saveCharacterReference();

        super.removeCharacter(characterUUID);
    }

    //----------------------------------------------------

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
    public void revealCharacterInfo(Character source, ServerPlayerEntity target, Character targetCharacter, InfoRevealLevel level) {
        if (!targetCharacter.getKnownCharacters().containsKey(source.getUUID())) {
            targetCharacter.getKnownCharacters().put(source.getUUID(), new KnownCharacter(source.getUUID()));

            saveCharacter(targetCharacter);

            Networking.sendS2C(target, new IntroductionPacket(source.getUUID()));
        }
    }

    @Override
    public void revealCharacterInfo(ServerPlayerEntity source, int range, InfoRevealLevel level) {
        Character sourceCharacter = this.getCharacter(source);

        if(sourceCharacter == null) return;

        for (ServerPlayerEntity otherPlayer : source.getWorld().getPlayers()) {
            if (source.getPos().distanceTo(otherPlayer.getPos()) <= range) {
                Character targetCharacter = this.getCharacter(otherPlayer);

                if(targetCharacter != null) revealCharacterInfo(sourceCharacter, otherPlayer, targetCharacter, level);
            }
        }
    }

    //----------------------------------------------------

    public void saveCharacter(Character character) {
        saveCharacter(character, true);
    }

    public void saveCharacter(Character character, boolean syncCharacter) {
        character.beforeSaving();

        String characterJson = GSON.toJson(character);

        if(syncCharacter) Networking.sendToAll(new SyncS2CPackets.SyncCharacter(characterJson, Map.of()));

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

    public void loadCharacterReference() {
        BASE_PATH = ServerAccess.getServer().getSavePath(WorldSavePath.ROOT).resolve("mod_data/personality");

        CHARACTER_PATH = BASE_PATH.resolve("characters");
        REFERENCE_PATH = BASE_PATH.resolve("reference.json");

        playerIDToCharacterID.clear();
        characterIDToCharacter.clear();

        try {
            if(!Files.exists(CHARACTER_PATH)) Files.createDirectories(CHARACTER_PATH);

            JsonObject o = GSON.fromJson(Files.readString(REFERENCE_PATH), JsonObject.class);
            playerIDToCharacterID = HashBiMap.create(GSON.fromJson(o.getAsJsonObject("player_to_character"), REF_MAP_TYPE));
        } catch (IOException e) {
            if (e instanceof NoSuchFileException) {
                saveCharacterReference();
            } else {
                e.printStackTrace();
            }
        }

        if(FabricLoader.getInstance().isDevelopmentEnvironment()){
            DebugCharacters.loadDebugCharacters(this);
        }
    }

    public void saveCharacterReference() {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("format", 1);
            json.add("player_to_character", GSON.toJsonTree(playerToCharacterReferences(), REF_MAP_TYPE));

            Files.writeString(REFERENCE_PATH, GSON.toJson(json));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //----------------------------------------------------------------------------

    @Override
    public void onWorldLoad(MinecraftServer server, ServerWorld world) {
        loadCharacterReference();
    }

    @Override
    public void onPlayReady(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        applyAddons(handler.player);

        Map<String, String> characters = new HashMap<>();

        for (Character c : characterLookupMap().values()){
            characters.put(GSON.toJson(c), GSON.toJson(c.getAddons()));
        }

        Networking.sendS2C(handler.player, new SyncS2CPackets.Initial(characters, playerToCharacterReferences()));
    }
}
