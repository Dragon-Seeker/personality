package io.blodhgarm.personality.api.addons;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.PlayerEntity;

public abstract class BaseAddon<T> {

    public boolean loadedProperly = true;

    public abstract void applyAddon(PlayerEntity player);

    public void improperLoad(){
        loadedProperly = false;
    }

    public abstract boolean applyOnClient();
}
