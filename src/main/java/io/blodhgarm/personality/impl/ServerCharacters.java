package io.blodhgarm.personality.impl;

import com.google.common.collect.HashBiMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.blodhgarm.personality.Networking;
import io.blodhgarm.personality.api.AddonRegistry;
import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.api.CharacterManager;
import io.blodhgarm.personality.api.addons.BaseAddon;
import io.blodhgarm.personality.packets.IntroductionPacket;
import io.blodhgarm.personality.packets.SyncS2CPackets;
import io.blodhgarm.personality.utils.ServerAccess;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerCharacters extends CharacterManager<ServerPlayerEntity> implements ServerPlayConnectionEvents.Join, ServerWorldEvents.Load {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type REF_MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();

    public static ServerCharacters INSTANCE = new ServerCharacters();

    private static Path BASE_PATH;
    private static Path CHARACTER_PATH;
    private static Path REFERENCE_PATH;

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

                Networking.sendToAll(new SyncS2CPackets.SyncCharacter(characterJson, GSON.toJson(AddonRegistry.INSTANCE.loadAddonsForCharacter(c))));
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

        applyAddons(characterUUID);

        Networking.sendToAll(new SyncS2CPackets.Association(characterUUID, playerUUID));

        saveCharacterReference();
    }

    @Override
    public String dissociateUUID(String UUID, boolean isCharacterUUID) {
        ServerPlayerEntity player = ServerAccess.getPlayer(super.dissociateUUID(UUID, isCharacterUUID));

        Networking.sendToAll(new SyncS2CPackets.Dissociation(UUID, isCharacterUUID));

        for (BaseAddon<?> defaultAddon : AddonRegistry.INSTANCE.getDefaultAddons()) defaultAddon.applyAddon(player);

        saveCharacterReference();

        return player.getUuidAsString();
    }

    public void applyAddons(String characterUUID){
        Character c = getCharacter(characterUUID);
        PlayerEntity player = getPlayer(characterUUID);

        if(c != null) {
            c.characterAddons.forEach((s, baseAddon) -> {
                baseAddon.applyAddon(player);
            });
        }
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

    public void revealToPlayersInRange(ServerPlayerEntity player, int range) {
        for (ServerPlayerEntity otherPlayer : player.getWorld().getPlayers()) {
            if (player.getPos().distanceTo(otherPlayer.getPos()) <= range) revealToPlayer(player, otherPlayer);
        }
    }

    public void revealToPlayer(ServerPlayerEntity revealed, ServerPlayerEntity revealedTo) {
        String revealedCharacterUUID = getCharacterUUID(revealed);
        Character revealedToCharacter = getCharacter(revealedTo);

        if (revealedToCharacter == null || revealedCharacterUUID == null) return;

        if (!revealedToCharacter.knowCharacters.contains(revealedCharacterUUID)) {
            revealedToCharacter.knowCharacters.add(revealedCharacterUUID);

            saveCharacter(revealedToCharacter);

            Networking.sendS2C(revealedTo, new IntroductionPacket(revealedCharacterUUID));
        }
    }

    //----------------------------------------------------

    public void saveCharacter(Character character) {
        saveCharacter(character, true);
    }

    public void saveCharacter(Character character, boolean syncCharacter) {
        String characterJson = GSON.toJson(character);

        if(syncCharacter) Networking.sendToAll(new SyncS2CPackets.SyncCharacter(characterJson, ""));

        try {
            Files.writeString(getCharacterInfo(character.getUUID()), characterJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Path getBasePath() {
        return BASE_PATH;
    }

    public static Path getCharacterPath() {
        return CHARACTER_PATH;
    }

    public static Path getReferencePath() {
        return REFERENCE_PATH;
    }

    public static Path getSpecificCharacterPath(String uuid) {
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
            Files.createDirectories(CHARACTER_PATH);

            JsonObject o = GSON.fromJson(Files.readString(REFERENCE_PATH), JsonObject.class);
            playerIDToCharacterID = HashBiMap.create(GSON.fromJson(o.getAsJsonObject("player_to_character"), REF_MAP_TYPE));
        } catch (IOException e) {
            if (e instanceof NoSuchFileException) {
                saveCharacterReference();
            } else {
                e.printStackTrace();
            }
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
        Map<String, String> characters = new HashMap<>();

        for (Character c : characterLookupMap().values()){
            characters.put(GSON.toJson(c), GSON.toJson(c.characterAddons));
        }

        Networking.sendS2C(handler.player, new SyncS2CPackets.Initial(characters, playerToCharacterReferences()));


    }
}
