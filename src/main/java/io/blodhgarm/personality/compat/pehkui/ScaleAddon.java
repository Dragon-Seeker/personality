package io.blodhgarm.personality.compat.pehkui;

import io.blodhgarm.personality.api.addon.BaseAddon;
import net.minecraft.entity.player.PlayerEntity;
import virtuoel.pehkui.api.ScaleData;

public class ScaleAddon extends BaseAddon {

    private boolean shouldShowHeight = true;

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

    public final float getHeightOffset(){
        return heightOffset;
    }

    public ScaleAddon shouldShowHeight(boolean value){
        this.shouldShowHeight = value;

        return this;
    }

    public boolean shouldShowHeight(){
        return this.shouldShowHeight;
    }

    @Override
    public boolean isEqualToPlayer(PlayerEntity player) {
        ScaleData data = PehkuiAddonRegistry.CHARACTER_TYPE.getScaleData(player);

        return Math.abs(data.getScale() - (this.heightOffset / player.getHeight())) < 0.00001;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ScaleAddon addon){
            return Math.abs(addon.getHeightOffset() - this.getHeightOffset()) < 0.00001;
        }

        return false;
    }
}
