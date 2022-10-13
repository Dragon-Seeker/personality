package io.blodhgarm.personality.api;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Table;
import io.blodhgarm.personality.api.addons.BaseAddon;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Base interface for storing and managing Character data
 * @param <P> Is a version of player either {@link ClientPlayerEntity} or {@link ServerPlayerEntity}
 */
public abstract class CharacterManager<P extends PlayerEntity> {

    protected BiMap<String, String> playerIDToCharacterID = HashBiMap.create();

    protected Map<String, Character> characterIDToCharacter = new HashMap<>();

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
        String cUUID = getCharacterUUID(player);

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
        return characterLookupMap().get(UUID);
    }

    /**
     * Attempt to get the Character based on the player's possible character reference UUID
     *
     * @param player The Possible Player for a Character
     * @return If a Character for a player or null if none are found
     */
    @Nullable
    public String getCharacterUUID(P player){
        return getCharacterUUID(player.getUuidAsString());
    }

    /**
     * Attempt to get the Character based on the player's UUID
     *
     * @param UUID The Possible UUID of a player for a Character
     * @return If a Character for a player or null if none are found
     */
    @Nullable
    public String getCharacterUUID(String UUID){
        return playerToCharacterReferences().get(UUID);
    }

    /**
     * Attempt to get the Player based on the Character
     *
     * @param c The Character that may be connected to a player
     * @return A Player if a Character is associated to such or null if none are found
     */
    @Nullable
    public P getPlayer(Character c){
        return getPlayer(c.getUUID());
    }

    /**
     * Attempt to get the Player based on the Characters UUID
     *
     * @param UUID A characters UUID
     * @return A Player if a Character is associated to such or null if none are found
     */
    @Nullable
    abstract public P getPlayer(String UUID);

    /**
     * Attempt to get the Player's UUID based on the Character
     *
     * @param c A character
     * @return A Player's UUID if a Character is associated to such or null if none are found
     */
    @Nullable
    public String getPlayerUUID(Character c){
        return getPlayerUUID(c.getUUID());
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
        playerToCharacterReferences().inverse().remove(cUUID);
        playerToCharacterReferences().put(playerUUID, cUUID);
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
        playerToCharacterReferences().inverse().remove(characterUUID);
        characterLookupMap().remove(characterUUID);
    }
}
