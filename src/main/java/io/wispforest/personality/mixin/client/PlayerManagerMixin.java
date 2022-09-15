package io.wispforest.personality.mixin.client;

import io.wispforest.personality.Networking;
import io.wispforest.personality.packets.OpenCharacterCreationScreenPacket;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    /**
     * This Mixin is useful to send a packet to open the screen, It is not needed if origin is installed due canceling of their screen to show ours
     */
    @Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;onSpawn()V", shift = At.Shift.AFTER))
    private void sendOpenScreenPacket(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci){
        if(!FabricLoader.getInstance().isModLoaded("origins")) {
            Networking.CHANNEL.serverHandle(player).send(new OpenCharacterCreationScreenPacket());
        }
    }
}
