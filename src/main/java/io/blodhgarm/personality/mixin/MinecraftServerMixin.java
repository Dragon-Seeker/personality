package io.blodhgarm.personality.mixin;

import io.blodhgarm.personality.api.events.OnWorldSaveEvent;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(method = "save", at = @At("HEAD"))
    private void personality$onServerSave(boolean suppressLogs, boolean flush, boolean force, CallbackInfoReturnable<Boolean> cir){
        OnWorldSaveEvent.ON_SAVE.invoker().onSave(suppressLogs, flush, force);
    }
}
