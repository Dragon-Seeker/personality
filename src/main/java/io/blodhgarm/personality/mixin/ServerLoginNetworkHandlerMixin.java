package io.blodhgarm.personality.mixin;

import io.blodhgarm.personality.api.events.FinalizedPlayerConnectionEvent;
import net.fabricmc.fabric.impl.networking.server.ServerPlayNetworkAddon;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerLoginNetworkHandler.class, priority = 1001)
public class ServerLoginNetworkHandlerMixin {

    @Inject(at = @At("TAIL"), method = "addToServer")
    private void personality$latestInjectPoint(ServerPlayerEntity player, CallbackInfo ci) {
        FinalizedPlayerConnectionEvent.CONNECTION_FINISHED.invoker().onFinalize(player.networkHandler, new ServerPlayNetworkAddon(player.networkHandler, player.server), player.server);
    }
}
