package io.blodhgarm.personality.mixin.client;

import io.blodhgarm.personality.PersonalityMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Shadow @Nullable protected MinecraftClient client;

    @Shadow public abstract void renderTooltip(MatrixStack matrices, List<Text> lines, int x, int y);

    @Inject(method = "renderTextHoverEffect", at = @At(value = "JUMP", opcode = Opcodes.IFEQ, ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void personality$renderAdvancedHoverTooltip(MatrixStack matrices, Style style, int x, int y, CallbackInfo ci, HoverEvent hoverEvent, HoverEvent.ItemStackContent itemStackContent, HoverEvent.EntityContent entityContent){
        if(PersonalityMod.CONFIG.showPlayerNameWhenHoveringChat() && !this.client.options.advancedItemTooltips) this.renderTooltip(matrices, entityContent.asTooltip(), x, y);
    }
}
