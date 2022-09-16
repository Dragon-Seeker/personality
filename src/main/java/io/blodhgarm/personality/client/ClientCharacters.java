package io.blodhgarm.personality.client;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.blodhgarm.personality.server.PersonalityServer;
import io.blodhgarm.personality.Character;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientCharacters {

    public static BiMap<String, String> playerIDToCharacterID = HashBiMap.create();
    public static Map<String, Character> characterIDToCharacter = new HashMap<>();

    @Nullable
    public static Character getCharacter(ClientPlayerEntity player) {
        return getCharacter(playerIDToCharacterID.get(player.getUuidAsString()));
    }

    @Nullable
    public static Character getCharacter(String uuid) {
        Character c = characterIDToCharacter.get(uuid);

        if (c != null)
            return c;

        //TODO: Implement
        return null;
    }

    @Nullable
    public static ServerPlayerEntity getPlayer(Character c) {
        return getPlayer(c.getInfo());
    }

    @Nullable
    public static ServerPlayerEntity getPlayer(String uuid) {
        return PersonalityServer.server.getPlayerManager().getPlayer(uuid);
    }

    @Nullable
    public static String getPlayerUUID(Character c) {
        return getPlayerUUID(c.getUUID());
    }

    @Nullable
    public static String getPlayerUUID(String uuid) {
        return playerIDToCharacterID.inverse().get(uuid);
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static void init(List<String> characters, Map<String,String> associations) {
        playerIDToCharacterID = HashBiMap.create(associations);
        characterIDToCharacter.clear();
        for (String characterJson : characters) {
            Character c = GSON.fromJson(characterJson, Character.class);
            characterIDToCharacter.put(c.getUUID(), c);
        }

    }

}
