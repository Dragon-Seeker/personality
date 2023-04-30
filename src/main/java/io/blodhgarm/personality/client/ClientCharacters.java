package io.blodhgarm.personality.client;

import com.google.common.collect.HashBiMap;
import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.addon.BaseAddon;
import io.blodhgarm.personality.api.character.BaseCharacter;
import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.api.character.CharacterManager;
import io.blodhgarm.personality.api.utils.PlayerAccess;
import io.blodhgarm.personality.api.addon.AddonRegistry;
import io.blodhgarm.personality.api.core.KnownCharacterLookup;
import io.blodhgarm.personality.api.reveal.KnownCharacter;
import io.blodhgarm.personality.misc.pond.CharacterToPlayerLink;
import io.blodhgarm.personality.packets.SyncS2CPackets;
import io.blodhgarm.personality.utils.DebugCharacters;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Client Specific Implementation of {@link CharacterManager}
 */
public class ClientCharacters extends CharacterManager<AbstractClientPlayerEntity> implements KnownCharacterLookup, ClientPlayConnectionEvents.Disconnect {

    public static ClientCharacters INSTANCE = new ClientCharacters();

    public Map<String, BaseCharacter> clientKnownCharacterMap = new HashMap<>();

    public ClientCharacters() {
        super("client");

        CharacterManager.getClientCharacterFunc = () -> this.getCharacter(MinecraftClient.getInstance().player);
    }

    public PlayerAccess<AbstractClientPlayerEntity> getPlayer(String uuid) {
        String playerUUID = playerToCharacterReferences().inverse().get(uuid);

        if(playerUUID != null) {
            AbstractClientPlayerEntity player = MinecraftClient.getInstance().world.getPlayers()
                    .stream()
                    .filter(abstractClientPlayerEntity -> Objects.equals(abstractClientPlayerEntity.getUuidAsString(), playerUUID)) //playerUUID
                    .findFirst()
                    .orElse(null);

            return new PlayerAccess<>(playerUUID, player);
        }

        return super.getPlayer(uuid);
    }

    /**
     * Method used to initialize the client's Character Manager
     */
    @ApiStatus.Internal
    public void init(List<SyncS2CPackets.CharacterData> characters, Map<String, String> associations) {
        playerIDToCharacterID = HashBiMap.create(associations);
        characterIDToCharacter.clear();

        for (SyncS2CPackets.CharacterData entry : characters) {
            Character c = PersonalityMod.GSON.fromJson(entry.characterData(), Character.class);

            Map<Identifier, BaseAddon> addonMap = AddonRegistry.INSTANCE.deserializesAddons(c, entry.addonData(), false);

            c.getAddons().putAll(addonMap);

            characterIDToCharacter.put(c.getUUID(), c);
        }

        if(FabricLoader.getInstance().isDevelopmentEnvironment()) DebugCharacters.loadDebugCharacters(this);

        clientKnownCharacterMap.clear();
    }

    //---------------

    @Override
    public boolean associateCharacterToPlayer(String cUUID, String playerUUID) {
        if(!super.associateCharacterToPlayer(cUUID, playerUUID)) return false;

        PlayerAccess<AbstractClientPlayerEntity> playerAssociated = this.getPlayer(playerUUID);

        if (playerAssociated.player() != null) ((CharacterToPlayerLink<?>) playerAssociated.player()).setCharacter(this.getCharacter(cUUID));

        setKnownCharacters(playerAssociated, cUUID);

        return true;
    }

    public void setKnownCharacters(PlayerAccess<AbstractClientPlayerEntity> playerAccess, String cUUID){
        Character clientC = this.getCharacter(MinecraftClient.getInstance().player);

        if(playerAccess != null && clientC != null) {
            if(playerAccess.player() != null && playerAccess.player() == MinecraftClient.getInstance().player) {
                clientC.getKnownCharacters().forEach((s, knownCharacter) -> {
                    knownCharacter.setCharacterManager(this);

                    PlayerAccess<AbstractClientPlayerEntity> otherP = this.getPlayer(knownCharacter.getWrappedCharacter());

                    if (otherP.valid()) this.addKnownCharacter(otherP.UUID(), knownCharacter);
                });
            } else {
                if (clientC.getKnownCharacters().containsKey(cUUID)) {
                    KnownCharacter knownCharacter = clientC.getKnownCharacters().get(cUUID);

                    knownCharacter.setCharacterManager(this);

                    this.addKnownCharacter(playerAccess.UUID(), knownCharacter);
                }
            }
        }
    }

    @Override
    @Nullable
    public String dissociateUUID(String UUID, boolean isCharacterUUID) {
        PlayerAccess<AbstractClientPlayerEntity> playerDissociated;
        Character oldC;

        if(isCharacterUUID){
            oldC = this.getCharacter(UUID);
            playerDissociated = this.getPlayer(oldC);
        } else {
            playerDissociated = this.getPlayer(this.getCharacterUUID(UUID));
            oldC = this.getCharacter(this.getCharacterUUID(playerDissociated.UUID()));
        }

        if (playerDissociated.player() != null) ((CharacterToPlayerLink<?>) playerDissociated.player()).setCharacter(null);

        revokeKnownCharacters(playerDissociated, oldC);

        return super.dissociateUUID(UUID, isCharacterUUID);
    }

    public void revokeKnownCharacters(PlayerAccess<AbstractClientPlayerEntity> playerAccess, Character oldC){
        Character clientC = this.getCharacter(MinecraftClient.getInstance().player);

        if(playerAccess != null && clientC != null) {
            if(playerAccess.player() != null && playerAccess.player() == MinecraftClient.getInstance().player) {
                clientC.getKnownCharacters().forEach((s, knownCharacter) -> {
                    PlayerAccess<AbstractClientPlayerEntity> otherP = this.getPlayer(knownCharacter.getWrappedCharacter());

                    if (otherP.valid()) this.removeKnownCharacter(otherP.UUID());
                });
            } else {
                if (oldC != null && this.clientKnownCharacterMap.containsKey(playerAccess.UUID())) {

                    this.removeKnownCharacter(playerAccess.UUID());
                }
            }
        }
    }

    //---------------

    @Override
    public void addKnownCharacter(String playerUUID, BaseCharacter character) {
        this.clientKnownCharacterMap.put(playerUUID, character);
    }

    @Override
    public void removeKnownCharacter(String playerUUID) {
        this.clientKnownCharacterMap.remove(playerUUID);
    }

    @Override
    @Nullable
    public BaseCharacter getKnownCharacter(String UUID) {
        return this.clientKnownCharacterMap.get(UUID);
    }

    //---------------

    @Override
    public void onPlayDisconnect(ClientPlayNetworkHandler handler, MinecraftClient client) {
        this.clearRegistries();

        clientKnownCharacterMap.clear();

        if(FabricLoader.getInstance().isDevelopmentEnvironment()) LOGGER.info("[Client-CharacterManager]: Manager has been cleared!");
    }
}
