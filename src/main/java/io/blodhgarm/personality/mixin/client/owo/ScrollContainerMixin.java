package io.blodhgarm.personality.mixin.client.owo;

import io.wispforest.owo.ui.container.ScrollContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ScrollContainer.class, remap = false)
public class ScrollContainerMixin {

    @Redirect(method = "onMouseDrag", at = @At(value = "INVOKE", target = "Ljava/lang/Double;isNaN(D)Z"))
    private boolean personality$preventInfinityCheck(double value){
        return !Double.isFinite(value);
    }
}
