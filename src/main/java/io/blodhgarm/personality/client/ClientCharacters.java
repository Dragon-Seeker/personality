package io.blodhgarm.personality.client;

import com.google.common.collect.HashBiMap;
import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.api.CharacterManager;
import io.blodhgarm.personality.api.reveal.InfoRevealLevel;
import io.blodhgarm.personality.utils.DebugCharacters;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public class ClientCharacters extends CharacterManager<AbstractClientPlayerEntity> {

    public static ClientCharacters INSTANCE = new ClientCharacters();

    public ClientCharacters() {
        super("client");
    }

    @Nullable
    public AbstractClientPlayerEntity getPlayer(String uuid) {
        return MinecraftClient.getInstance().world.getPlayers()
                .stream()
                .filter(abstractClientPlayerEntity -> Objects.equals(abstractClientPlayerEntity.getUuidAsString(), playerToCharacterReferences().inverse().get(uuid)))
                .findFirst()
                .orElse(null);
    }

    public void init(Map<String, String> characters, Map<String, String> associations) {
        playerIDToCharacterID = HashBiMap.create(associations);
        characterIDToCharacter.clear();
        for (Map.Entry<String, String> entry : characters.entrySet()) {
            Character c = PersonalityMod.GSON.fromJson(entry.getKey(), Character.class);

            c.getAddons().putAll(PersonalityMod.GSON.fromJson(entry.getValue(), Character.REF_MAP_TYPE));

            characterIDToCharacter.put(c.getUUID(), c);
        }

        if(FabricLoader.getInstance().isDevelopmentEnvironment()){
            DebugCharacters.loadDebugCharacters(this);
        }
    }

    @Override
    public void revealCharacterInfo(Character source, AbstractClientPlayerEntity target, Character targetCharacter, InfoRevealLevel level) {

    }

    @Override
    public void revealCharacterInfo(AbstractClientPlayerEntity source, int range, InfoRevealLevel level) {

    }
}
