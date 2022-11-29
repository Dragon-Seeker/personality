package io.blodhgarm.personality.mixin.client;

import io.blodhgarm.personality.client.gui.components.vanilla.BetterTextFieldWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextFieldWidget.class)
public abstract class TextFieldWidgetMixin {

    private boolean betterBackgroundCheck = false;

    @Shadow public abstract void setDrawsBackground(boolean drawsBackground);

    @Shadow protected abstract boolean drawsBackground();

    @Inject(method = "renderButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;drawsBackground()Z"))
    private void personality$disableTextFieldBackground(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci){
        if((Object)this instanceof BetterTextFieldWidget) {
            betterBackgroundCheck = this.drawsBackground();

            if(betterBackgroundCheck) {
                this.setDrawsBackground(false);
            }
        }
    }

    @Inject(method = "renderButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;drawsBackground()Z", shift = At.Shift.AFTER))
    private void personality$re_enableTextFieldBackground(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci){
        if((Object)this instanceof BetterTextFieldWidget) {
            this.setDrawsBackground(betterBackgroundCheck);
        }
    }
}
