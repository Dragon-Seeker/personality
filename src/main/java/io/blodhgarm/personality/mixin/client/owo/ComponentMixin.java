package io.blodhgarm.personality.mixin.client.owo;

import io.blodhgarm.personality.client.gui.utils.polygons.AbstractPolygon;
import io.blodhgarm.personality.misc.pond.owo.CustomFocusHighlighting;
import io.blodhgarm.personality.misc.pond.owo.ExcludableBoundingArea;
import io.blodhgarm.personality.misc.pond.owo.RefinedBoundingArea;
import io.wispforest.owo.ui.core.Component;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Component.class)
public interface ComponentMixin{

    @Inject(method = "isInBoundingBox", at = @At("HEAD"), cancellable = true)
    private void checkIfInExclusionZones(double x, double y, CallbackInfoReturnable<Boolean> cir) {
        if(this instanceof ExcludableBoundingArea area && !area.getExclusionZones().isEmpty() && area.isWithinExclusionZone((float) x, (float) y)){
            cir.setReturnValue(false);
        }

        if(this instanceof RefinedBoundingArea area) {
            AbstractPolygon polygon = area.getRefinedBound();

            if(polygon != null) cir.setReturnValue(polygon.withinShape((float) x, (float) y));
        }
    }

    @Inject(method = "drawFocusHighlight", at = @At("HEAD"), cancellable = true)
    private void checkForCustomRender(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta, CallbackInfo ci){
        if(this instanceof CustomFocusHighlighting customFocusHighlighting && customFocusHighlighting.getEvent() != null){
            if(customFocusHighlighting.getEvent().drawHighlight(matrices, mouseX, mouseY, partialTicks, delta)) ci.cancel();
        }
    }
}
