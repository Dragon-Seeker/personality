package io.blodhgarm.personality.api.core;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.server.MinecraftServer;

public abstract class DelayedRegistry implements ServerLifecycleEvents.EndDataPackReload, ServerLifecycleEvents.ServerStarted {

    public DelayedRegistry() {
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(this);
        ServerLifecycleEvents.SERVER_STARTED.register(this);
    }

    public abstract void runDelayedRegistration();

    @Override
    public void endDataPackReload(MinecraftServer server, LifecycledResourceManager resourceManager, boolean success) {
        runDelayedRegistration();
    }

    @Override
    public void onServerStarted(MinecraftServer server) {
        runDelayedRegistration();
    }
}
