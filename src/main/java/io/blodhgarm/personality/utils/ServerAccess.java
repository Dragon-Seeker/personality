package io.blodhgarm.personality.utils;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class ServerAccess implements ServerWorldEvents.Load {

    public static MinecraftServer server = null;

    @Override
    public void onWorldLoad(MinecraftServer server, ServerWorld world) {
        ServerAccess.server = server;
    }

    public static ServerPlayerEntity getPlayer(String playerUUID) {
        return server.getPlayerManager().getPlayer(playerUUID);
    }

    public static MinecraftServer getServer(){
        return server;
    }
}
