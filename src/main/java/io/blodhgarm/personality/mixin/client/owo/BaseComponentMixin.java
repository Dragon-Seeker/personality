package io.blodhgarm.personality.mixin.client.owo;

import io.blodhgarm.personality.client.gui.utils.polygons.AbstractPolygon;
import io.blodhgarm.personality.misc.pond.owo.ExclusiveBoundingArea;
import io.blodhgarm.personality.misc.pond.owo.RefinedBoundingArea;
import io.wispforest.owo.ui.base.BaseComponent;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mixin(value = BaseComponent.class, remap = false)
public abstract class BaseComponentMixin {

    @Shadow protected int x;
    @Shadow protected int y;

    @Inject(method = {"updateX", "updateY"}, at = @At("HEAD"))
    private void updateBoundingArea(int value, CallbackInfo ci){
        List<AbstractPolygon> polygons = new ArrayList<>();

        if(this instanceof ExclusiveBoundingArea excludableBoundingArea){
            polygons.addAll(excludableBoundingArea.getExclusionZones());
        }

        if(this instanceof RefinedBoundingArea refinedBoundingArea && refinedBoundingArea.getRefinedBound() != null){
            polygons.add(refinedBoundingArea.getRefinedBound());
        }

        if(polygons.isEmpty()) return;

        boolean isUpdateX = Objects.equals(ci.getId(), "updateX");

        int diff = value - (isUpdateX ? this.x : this.y);

        if(diff != 0) {
            Vec3f vec3f = new Vec3f(
                    isUpdateX ? diff : 0,
                    isUpdateX ? 0 : diff,
                    0f
            );

            polygons.forEach(abstractPolygon -> abstractPolygon.movePolygon(vec3f, Vec3f::add));
        }
    }
}
