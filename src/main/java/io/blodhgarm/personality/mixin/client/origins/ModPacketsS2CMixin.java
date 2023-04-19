package io.blodhgarm.personality.mixin.client.origins;

import io.blodhgarm.personality.client.gui.CharacterViewMode;
import io.blodhgarm.personality.client.gui.screens.CharacterViewScreen;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.networking.ModPacketsS2C;
import io.github.apace100.origins.origin.OriginLayer;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;

@Mixin(value = ModPacketsS2C.class, remap = false)
@Pseudo
public abstract class ModPacketsS2CMixin {

    @Inject(method = "lambda$openOriginScreen$3(Lnet/minecraft/class_310;Z)V", at = @At(value = "TAIL"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private static void personality$cancelOpeningOriginScreen(MinecraftClient minecraftClient, boolean showDirtBackground, CallbackInfo ci, ArrayList<OriginLayer> layers, OriginComponent component){
        if(showDirtBackground){
            minecraftClient.setScreen(new CharacterViewScreen(CharacterViewMode.CREATION, minecraftClient.player.getGameProfile(), null));

            ci.cancel();
        }
    }
}
