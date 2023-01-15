package io.blodhgarm.personality.api.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public class FinalizedPlayerConnectionEvent {

    public static final Event<FinalizedPlayerConnectionEvent.Finish> CONNECTION_FINISHED = EventFactory.createArrayBacked(FinalizedPlayerConnectionEvent.Finish.class, callbacks -> (handler, sender, server) -> {
        for (FinalizedPlayerConnectionEvent.Finish callback : callbacks) {
            callback.onFinalize(handler, sender, server);
        }
    });

    @FunctionalInterface
    public interface Finish {
        void onFinalize(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server);
    }
}
