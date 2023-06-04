package io.blodhgarm.personality.client;

import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.mojang.authlib.GameProfile;
import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.addon.BaseAddon;
import io.blodhgarm.personality.api.character.BaseCharacter;
import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.api.character.CharacterManager;
import io.blodhgarm.personality.api.utils.PlayerAccess;
import io.blodhgarm.personality.api.addon.AddonRegistry;
import io.blodhgarm.personality.api.core.KnownCharacterLookup;
import io.blodhgarm.personality.api.reveal.KnownCharacter;
import io.blodhgarm.personality.mixin.PlayerEntityMixin;
import io.blodhgarm.personality.packets.SyncS2CPackets;
import io.blodhgarm.personality.utils.CharacterReferenceData;
import io.blodhgarm.personality.utils.DebugCharacters;
import io.blodhgarm.personality.utils.gson.ExtraTokenData;
import io.blodhgarm.personality.utils.gson.WrappedTypeToken;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Client Specific Implementation of {@link CharacterManager}
 */
public class ClientCharacters extends CharacterManager<AbstractClientPlayerEntity, Character> implements ClientPlayConnectionEvents.Disconnect {

    public static ClientCharacters INSTANCE = new ClientCharacters();

    //-----------------

    private final Gson GSON = PersonalityMod.GSON.newBuilder()
            .registerTypeAdapter(Character.class, (InstanceCreator<Character>) type -> {
                Character c = CharacterReferenceData.attemptGetCharacter(type);

                if(c == null) c = new Character("", "", "", "", "", -1);

                return (Character) c.setCharacterManager(this);
            })
            .registerTypeAdapter(KnownCharacter.class, (InstanceCreator<KnownCharacter>) type -> {
                return (KnownCharacter) new KnownCharacter("", "")
                        .setCharacterManager(this);
            })
            .create();

    public ClientCharacters() {
        super("client");
    }

    @Override
    protected WrappedTypeToken<Character> getToken() {
        return new WrappedTypeToken<>(){};
    }

    @Override
    public Gson getGson() {
        return GSON;
    }

    /**
     * Method used to get the clients current character for instances that don't reference
     * the main {@link ClientCharacters}. See example in {@link PlayerEntityMixin}
     *
     * @return the current clients character or null
     */
    @Nullable
    public Character getClientCharacter(){
        return getCharacter(MinecraftClient.getInstance().player);
    }

    @Override
    public PlayerAccess<AbstractClientPlayerEntity> getPlayer(@Nullable String pUUID) {
        if(pUUID != null) {
            AbstractClientPlayerEntity player = MinecraftClient.getInstance().world.getPlayers()
                    .stream()
                    .filter(abstractClientPlayerEntity -> Objects.equals(abstractClientPlayerEntity.getUuidAsString(), pUUID)) //playerUUID
                    .findFirst()
                    .orElse(null);

            return new PlayerAccess<>(pUUID, player);
        }

        return super.getPlayer(pUUID);
    }

    /**
     * Method used to initialize the client's Character Manager
     */
    @ApiStatus.Internal
    public void init(List<SyncS2CPackets.CharacterData> characters, Map<String, String> associations) {
        playerIDToCharacterID = HashBiMap.create(associations);
        characterIDToCharacter.clear();

        for (SyncS2CPackets.CharacterData entry : characters) {
            Character c = this.deserializeCharacter(entry.characterData());

            c.getAddons().putAll(AddonRegistry.INSTANCE.deserializesAddons(c, entry.addonData(), false));

            characterIDToCharacter.put(c.getUUID(), c);
        }

        if(FabricLoader.getInstance().isDevelopmentEnvironment()) DebugCharacters.loadDebugCharacters(this);
    }

    //---------------

    @Override
    public void onPlayDisconnect(ClientPlayNetworkHandler handler, MinecraftClient client) {
        this.clearRegistries();

        if(FabricLoader.getInstance().isDevelopmentEnvironment()) LOGGER.info("[Client-CharacterManager]: Manager has been cleared!");
    }
}
