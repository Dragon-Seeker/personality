package io.wispforest.personality.client;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.wispforest.personality.server.PersonalityServer;
import io.wispforest.personality.Character;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
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

}
