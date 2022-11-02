package io.blodhgarm.personality.compat.pehkui;

import io.blodhgarm.personality.api.addon.BaseAddon;
import net.minecraft.entity.player.PlayerEntity;
import virtuoel.pehkui.api.ScaleData;

public class ScaleAddon extends BaseAddon {

    private final float heightOffset;

    public ScaleAddon(float heightScale){
        this.heightOffset = heightScale;
    }

    @Override
    public void applyAddon(PlayerEntity player) {
        ScaleData data = PehkuiAddonRegistry.CHARACTER_TYPE.getScaleData(player);

        if(this.heightOffset == 0){
            data.resetScale();
        } else {
            data.setTargetScale((this.heightOffset / player.getHeight()));
        }
    }

    @Override
    public AddonEnvironment getAddonEnvironment() {
        return AddonEnvironment.SERVER;
    }

    @Override
    public String getInfo() {
        return "\n§lHeight§r: " + (String.format("%.2f", heightOffset + 1.8)) + "m";
    }
}
