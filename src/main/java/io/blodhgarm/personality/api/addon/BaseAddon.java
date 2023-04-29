package io.blodhgarm.personality.api.addon;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;

/**
 * Base Class to act as the frame for any Addons wanting to interact with Personality internal systems
 * and used to apply the addons effects and to check if the addon is currently applied
 */
public abstract class BaseAddon {

    /**
     * Enum used to declare if an addon should be handled on the Client or the Server or both
     */
    public enum AddonEnvironment {
        BOTH, CLIENT, SERVER;

        public boolean shouldApply(World world){
            return this == BOTH
                    || (this == CLIENT && world.isClient())
                    || (this == SERVER && !world.isClient());
        }
    }

    /**
     * Variable to indicated if an Addon has been loaded properly
     */
    @ApiStatus.Internal
    public boolean loadedProperly = true;

    //--------------------------------------

    /**
     * Method used to apply addon data when a character is associated with a player
     * @param player - Associated Player
     */
    public abstract void applyAddon(PlayerEntity player);

    /**
     * @return If the given addon should be applied on the Client
     */
    public abstract AddonEnvironment getAddonEnvironment();

    /**
     * @return Formatted String to be used in the character info command
     */
    public abstract String getInfo();

    /**
     * Method used to check if the addon is already applied to the given Player
     */
    public abstract boolean isEqualToPlayer(PlayerEntity player);

    //Just forcing everyone to make an implementation of equals
    @Override
    public abstract boolean equals(Object obj);
}
