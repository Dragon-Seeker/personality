package io.blodhgarm.personality.client;

import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.api.CharacterManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ClientCharacters extends CharacterManager<AbstractClientPlayerEntity> {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static ClientCharacters INSTANCE = new ClientCharacters();

    @Nullable
    public AbstractClientPlayerEntity getPlayer(String uuid) {
        return MinecraftClient.getInstance().world.getPlayers()
                .stream()
                .filter(abstractClientPlayerEntity -> Objects.equals(abstractClientPlayerEntity.getUuidAsString(), uuid))
                .findFirst()
                .orElse(null);
    }

    public void init(List<String> characters, Map<String,String> associations) {
        playerIDToCharacterID = HashBiMap.create(associations);
        characterIDToCharacter.clear();
        for (String characterJson : characters) {
            Character c = GSON.fromJson(characterJson, Character.class);
            characterIDToCharacter.put(c.getUUID(), c);
        }

    }

}
