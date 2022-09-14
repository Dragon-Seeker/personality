package io.wispforest.personality.mixin.client.origins;

import io.github.apace100.origins.screen.ChooseOriginScreen;
import io.wispforest.personality.client.screens.ModifiedChooseOriginScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChooseOriginScreen.class)
public class ChooseOriginScreenMixin {

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lio/github/apace100/origins/screen/OriginDisplayScreen;init()V", shift = At.Shift.AFTER), cancellable = true)
    private void personality$cancelChooseOriginScreenInit(CallbackInfo ci){
        if(((Object)this) instanceof ModifiedChooseOriginScreen) ci.cancel();
    }

}
