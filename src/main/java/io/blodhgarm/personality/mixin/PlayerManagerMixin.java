package io.blodhgarm.personality.mixin;

import com.mojang.authlib.GameProfile;
import io.blodhgarm.personality.Networking;
import io.blodhgarm.personality.client.gui.CharacterViewMode;
import io.blodhgarm.personality.misc.pond.OfflineStatExtension;
import io.blodhgarm.personality.server.ServerCharacters;
import io.blodhgarm.personality.packets.OpenPersonalityScreenS2CPacket;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.util.FileNameUtil;
import net.minecraft.util.WorldSavePath;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin implements OfflineStatExtension {

    @Shadow @Final private Map<UUID, ServerStatHandler> statisticsMap;

    @Shadow @Final private MinecraftServer server;

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

    @Override
    public ServerStatHandler loadStatHandler(GameProfile profile) {
        UUID uUID = profile.getId();
        ServerStatHandler serverStatHandler = (ServerStatHandler)this.statisticsMap.get(uUID);
        if (serverStatHandler == null) {
            File file = this.server.getSavePath(WorldSavePath.STATS).toFile();
            File file2 = new File(file, uUID + ".json");
            if (!file2.exists()) {
                File file3 = new File(file, profile.getName() + ".json");
                Path path = file3.toPath();
                if (FileNameUtil.isNormal(path) && FileNameUtil.isAllowedName(path) && path.startsWith(file.getPath()) && file3.isFile()) {
                    file3.renameTo(file2);
                }
            }

            serverStatHandler = new ServerStatHandler(this.server, file2);
            this.statisticsMap.put(uUID, serverStatHandler);
        }

        return serverStatHandler;
    }
}
