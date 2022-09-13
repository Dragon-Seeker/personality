package io.wispforest.personality.mixin.client.origins;

import io.github.apace100.origins.screen.OriginDisplayScreen;
import io.wispforest.personality.screens.ModifiedChooseOriginScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(OriginDisplayScreen.class)
public class OriginDisplayScreenMixin {

    private static final int PERSONALITY$GUI_X_ADJUSTMENT = 160;

    @Shadow protected int guiLeft;

    @Inject(method = "init", at = @At(value = "TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void personality$shiftScreenRendering(CallbackInfo ci){
        if(((Object)this) instanceof ModifiedChooseOriginScreen){
            guiLeft += PERSONALITY$GUI_X_ADJUSTMENT;//120
        }
    }

    @ModifyArgs(method = "renderOriginWindow", at = @At(value = "INVOKE", target = "Lio/github/apace100/origins/screen/OriginDisplayScreen;drawCenteredText(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)V"))
    private void personality$adjustCenterXPosition(Args args){
        args.set(3, ((int)args.get(3)) + PERSONALITY$GUI_X_ADJUSTMENT);
    }
}
