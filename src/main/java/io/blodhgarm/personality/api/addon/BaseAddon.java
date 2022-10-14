package io.blodhgarm.personality.api.addon;

import net.minecraft.entity.player.PlayerEntity;

public abstract class BaseAddon {

    public boolean loadedProperly = true;

    public abstract void applyAddon(PlayerEntity player);

    public void improperLoad(){
        loadedProperly = false;
    }

    public abstract boolean applyOnClient();
}
