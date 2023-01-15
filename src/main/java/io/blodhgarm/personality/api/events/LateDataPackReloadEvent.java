package io.blodhgarm.personality.api.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.server.MinecraftServer;

public class LateDataPackReloadEvent {

    public static final Event<LateDataPackReload> LATEST_DATA_PACK_RELOAD = EventFactory.createArrayBacked(LateDataPackReload.class, callbacks -> (server, manager, success) -> {
        for (LateDataPackReload callback : callbacks) {
            callback.veryEndDataPackReload(server, manager, success);
        }
    });

    @FunctionalInterface
    public interface LateDataPackReload {
        void veryEndDataPackReload(MinecraftServer server, LifecycledResourceManager resourceManager, boolean success);
    }
}
