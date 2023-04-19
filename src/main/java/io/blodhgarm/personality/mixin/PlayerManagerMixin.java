package io.blodhgarm.personality.mixin;

import io.blodhgarm.personality.Networking;
import io.blodhgarm.personality.client.gui.CharacterViewMode;
import io.blodhgarm.personality.server.ServerCharacters;
import io.blodhgarm.personality.packets.OpenPersonalityScreenS2CPacket;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    /**
     * This Mixin is useful to send a packet to open the screen, It is not needed if origin is installed due canceling of their screen to show ours
     */
    @Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;onSpawn()V", shift = At.Shift.AFTER))
    private void sendOpenScreenPacket(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci){
        if(!FabricLoader.getInstance().isModLoaded("origins")) {
            if(!ServerCharacters.INSTANCE.getCharacterUUID(player.getUuid().toString()).equals("INVALID")) return;

            Networking.CHANNEL.serverHandle(player).send(new OpenPersonalityScreenS2CPacket(CharacterViewMode.CREATION, "personality$packet_target"));
        }
    }
}
