package io.blodhgarm.personality.api.addon;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public abstract class BaseAddon {

    public boolean loadedProperly = true;

    public final void improperLoad(){ loadedProperly = false; }

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

    public enum AddonEnvironment {
        BOTH,
        CLIENT,
        SERVER;

        public boolean shouldApply(World world){
            if(this == BOTH) return true;

            return (this == CLIENT && world.isClient()) || (this == SERVER && !world.isClient());
        }
    }

    public abstract boolean isEqualToPlayer(PlayerEntity player);

//    public abstract boolean isDefaultAddon(BaseAddon addon);
}
