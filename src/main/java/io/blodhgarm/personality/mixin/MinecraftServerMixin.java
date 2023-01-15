package io.blodhgarm.personality.mixin;

import io.blodhgarm.personality.api.events.LateDataPackReloadEvent;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Mixin(value = MinecraftServer.class, priority = 1001)
public class MinecraftServerMixin {

    @Shadow private MinecraftServer.ResourceManagerHolder resourceManagerHolder;

    @Inject(method = "reloadResources", at = @At("TAIL"))
    private void personality$endResourceReload(Collection<String> collection, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        cir.getReturnValue().handleAsync((value, throwable) -> {
            // Hook into fail
            LateDataPackReloadEvent.LATEST_DATA_PACK_RELOAD.invoker().veryEndDataPackReload((MinecraftServer) (Object) this, this.resourceManagerHolder.resourceManager(), throwable == null);
            return value;
        }, (MinecraftServer) (Object) this);
    }
}
