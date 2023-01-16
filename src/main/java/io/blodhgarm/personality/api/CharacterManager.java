package io.blodhgarm.personality.api;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.blodhgarm.personality.api.addon.AddonRegistry;
import io.blodhgarm.personality.impl.RevelCharacterInfo;
import io.blodhgarm.personality.utils.DebugCharacters;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;

/**
 * Base interface for storing and managing Character data
 * @param <P> Is a version of player either {@link ClientPlayerEntity} or {@link ServerPlayerEntity}
 */
public abstract class CharacterManager<P extends PlayerEntity> implements RevelCharacterInfo<P> {

    private static final Map<String, CharacterManager<?>> MANAGER_REGISTRY = new HashMap<>();

    protected static Supplier<Character> getClientCharacterFunc = () -> null;

    protected BiMap<String, String> playerIDToCharacterID = HashBiMap.create();
    protected Map<String, Character> characterIDToCharacter = new HashMap<>();

    public CharacterManager(String mangerId){
        MANAGER_REGISTRY.put(mangerId, this);
    }

    //-----------------------------------------------------------------------------------------

    @Nullable
    public static <P extends PlayerEntity> CharacterManager<P> getManger(PlayerEntity entity){
        return entity.getWorld() != null ? getManger(entity.getWorld()) : null;
    }

    @Nullable
    public static <P extends PlayerEntity> CharacterManager<P> getManger(World world){
        return getManger(world.isClient() ? "client" : "server");
    }

    @Nullable
    public static <P extends PlayerEntity> CharacterManager<P> getManger(String managerId){
        return (CharacterManager<P>) MANAGER_REGISTRY.get(managerId);
    }

    @Nullable
    public static Character getClientCharacter(){
        return getClientCharacterFunc.get();
    }

    //-----------------------------------------------------------------------------------------

    /**
     * Map that holds the reference of player and character
     */
    @Nonnull
    public BiMap<String, String> playerToCharacterReferences(){
        return playerIDToCharacterID;
    }

    /**
     * A Map holding the Character Reference to its UUID
     */
    @Nonnull
    public Map<String, Character> characterLookupMap(){
        return characterIDToCharacter;
    }

    /**
     * Attempt to get the Character based on the player's possible character reference UUID
     *
     * @param player The Possible Player for a Character
     * @return If a Character for a player or null if none are found
     */
    @Nullable
    public Character getCharacter(P player){
        String cUUID = null;

        if(player != null) cUUID = getCharacterUUID(player.getUuid().toString());

        return cUUID != null ? getCharacter(cUUID) : null;
    }

    /**
     * Attempt to get the Character based on the player's UUID
     *
     * @param UUID The Possible UUID of a player for a Character
     * @return If a Character for a player or null if none are found
     */
    @Nullable
    public Character getCharacter(String UUID){
        Character character = characterLookupMap().get(UUID);

        if(FabricLoader.getInstance().isDevelopmentEnvironment() && character == null){
            character = DebugCharacters.DEBUG_CHARACTERS_MAP.get(UUID);
        }

        return character;
    }

    /**
     * Attempt to get the Character based on the player's UUID
     *
     * @param UUID The Possible UUID of a player for a Character
     * @return If a Character for a player or null if none are found
     */
    public String getCharacterUUID(String UUID){
        return playerToCharacterReferences().getOrDefault(UUID, "INVALID");
    }

    /**
     * Attempt to get the Player based on the Character
     *
     * @param c The Character that may be connected to a player
     * @return A Player if a Character is associated to such or null if none are found
     */
    public PlayerAccess<P> getPlayer(Character c){
        return c == null ? (PlayerAccess<P>) PlayerAccess.EMPTY : getPlayer(c.getUUID());
    }

    /**
     * Attempt to get the Player based on the Characters UUID
     *
     * @param UUID A characters UUID
     * @return A Player if a Character is associated to such or null if none are found
     */
    public PlayerAccess<P> getPlayer(String UUID){
        return (PlayerAccess<P>) PlayerAccess.EMPTY;
    }

    /**
     * Attempt to get the Player's UUID based on the Character
     *
     * @param c A character
     * @return A Player's UUID if a Character is associated to such or null if none are found
     */
    @Nullable
    public String getPlayerUUID(Character c){
        return c == null ? null : getPlayerUUID(c.getUUID());
    }

    /**
     * Attempt to get the Player's UUID based on the Character's UUID
     *
     * @param cUUID A character's UUID
     * @return A Player's UUID if a Character is associated to such or null if none are found
     */
    @Nullable
    public String getPlayerUUID(String cUUID){
        return playerToCharacterReferences().inverse().get(cUUID);
    }

    // Manage Characters method

    public void associateCharacterToPlayer(String cUUID, String playerUUID){
        playerToCharacterReferences().put(playerUUID, cUUID);

        applyAddons(cUUID);
    }

    public void applyAddons(String characterUUID){
        PlayerAccess<P> playerAccess = getPlayer(characterUUID);

        if(playerAccess.player() != null) applyAddons(playerAccess.player());
    }

    public void applyAddons(P player){
        Character c = getCharacter(player);

        if(c != null){
            c.getAddons().values().forEach(addon -> {
                if(!addon.isEqualToPlayer(player) && addon.getAddonEnvironment().shouldApply(player.getWorld())){
                    addon.applyAddon(player);
                }
            });

            c.getKnownCharacters().forEach((s, knownCharacter) -> {
                knownCharacter.setCharacterManager(this);
            });
        } else {
            AddonRegistry.INSTANCE.checkAndDefaultPlayerAddons(player);
        }
    }

    /**
     *
     * @return Player UUID
     */
    public String dissociateUUID(String UUID, boolean isCharacterUUID){
        if(isCharacterUUID){
            return playerToCharacterReferences().inverse().remove(UUID);
        } else {
            playerToCharacterReferences().remove(UUID);

            return UUID;
        }
    }

    public void removeCharacter(String characterUUID){
        dissociateUUID(characterUUID, true);
        characterLookupMap().remove(characterUUID);
    }

}
