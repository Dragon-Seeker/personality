package io.blodhgarm.personality.api.character;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.logging.LogUtils;
import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.addon.AddonRegistry;
import io.blodhgarm.personality.api.utils.PlayerAccess;
import io.blodhgarm.personality.api.reveal.RevelInfoManager;
import io.blodhgarm.personality.utils.DebugCharacters;
import io.blodhgarm.personality.utils.ReflectionUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;

/**
 * Base interface for storing and managing Character data
 * @param <P> Is a version of player either {@link ClientPlayerEntity} or {@link ServerPlayerEntity}
 */
public abstract class CharacterManager<P extends PlayerEntity> implements RevelInfoManager<P> {

    protected static final Logger LOGGER = LogUtils.getLogger();

    private static final Map<String, CharacterManager<?>> MANAGER_REGISTRY = new HashMap<>();

    protected static Supplier<Character> getClientCharacterFunc = () -> null;

    protected BiMap<String, String> playerIDToCharacterID = HashBiMap.create();
    protected ListOrderedMap<String, Character> characterIDToCharacter = new ListOrderedMap<>();

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

    public static boolean hasModerationPermissions(PlayerEntity player){
        return hasAdministrationPermissions(player)
                || PersonalityMod.CONFIG.moderationList().contains(player.getGameProfile().toString());
    }

    public static boolean hasAdministrationPermissions(PlayerEntity player){
        return player.hasPermissionLevel(3)
                || PersonalityMod.CONFIG.administrationList().contains(player.getGameProfile().toString());
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
    public ListOrderedMap<String, Character> characterLookupMap(){
        return characterIDToCharacter;
    }

    public void clearRegistries(){
        this.characterIDToCharacter.clear();
        this.playerIDToCharacterID.clear();
    }

    public void sortCharacterLookupMap(){
        ReflectionUtils.getMapInsertOrder(characterLookupMap())
                .sort(Comparator.comparing(o -> getCharacter(o).getName(), Comparator.naturalOrder()));
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
     * @param UUID The Possible UUID of a Character
     * @return A Character linked to the given UUID
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


    /**
     * Method used to associate a Player to a given character within the Reference Map
     *
     * @param cUUID Characters UUID
     * @param playerUUID Player UUID
     *
     * @return if the character was found within the {@link #characterLookupMap()}
     */
    public boolean associateCharacterToPlayer(String cUUID, String playerUUID){
        if(!characterLookupMap().containsKey(cUUID)) return false;

        playerToCharacterReferences().put(playerUUID, cUUID);

        applyAddons(cUUID);

        return true;
    }

    public void applyAddons(String characterUUID){
        PlayerAccess<P> playerAccess = getPlayer(characterUUID);

        if(playerAccess.player() != null) applyAddons(playerAccess.player());
    }

    /**
     * Method used to apply addons to the given player if they have a character
     */
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
     * Remove the given UUID (Player or Character) from the Player Character Reference Map
     *
     * @return Player UUID removed if found within the map
     */
    @Nullable
    public String dissociateUUID(String UUID, boolean isCharacterUUID){
        BiMap<String, String> map = isCharacterUUID ? playerToCharacterReferences().inverse() : playerToCharacterReferences();

        if(!map.containsKey(UUID)) return null;

        String valueRemoved = map.remove(UUID);

        return isCharacterUUID ? valueRemoved : UUID;
    }

    public void removeCharacter(String characterUUID){
        dissociateUUID(characterUUID, true);
        characterLookupMap().remove(characterUUID);
    }

}
