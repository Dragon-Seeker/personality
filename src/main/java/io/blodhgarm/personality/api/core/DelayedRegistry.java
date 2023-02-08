package io.blodhgarm.personality.api.core;

import io.blodhgarm.personality.Networking;
import io.blodhgarm.personality.api.events.LateDataPackReloadEvent;
import io.blodhgarm.personality.packets.SyncC2SPackets;
import io.blodhgarm.personality.packets.UpdateDelayedRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.server.MinecraftServer;

public abstract class DelayedRegistry extends BaseRegistry implements ServerLifecycleEvents.EndDataPackReload, ServerLifecycleEvents.ServerStarted, LateDataPackReloadEvent.LateDataPackReload{

    public DelayedRegistry() {
        super();

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(this);
        ServerLifecycleEvents.SERVER_STARTED.register(this);
    }

    public abstract void runDelayedRegistration();

    //------------------------------------------------------------------

    @Environment(EnvType.CLIENT)
    public static void runAllDelayedRegistries(){
        BaseRegistry.REGISTRIES.forEach((identifier, baseRegistry) -> {
            System.out.println(identifier);

            if(baseRegistry instanceof DelayedRegistry delayedRegistry) {
                delayedRegistry.runDelayedRegistration();

                Networking.sendC2S(new SyncC2SPackets.RegistrySync(delayedRegistry.getRegistryId(), delayedRegistry.getRegisteredIds()));
            }
        });
    }

    //------------------------------------------------------------------

    @Override
    public void endDataPackReload(MinecraftServer server, LifecycledResourceManager resourceManager, boolean success) {
        runDelayedRegistration();

        System.out.println("EndDataPackReload");
    }

    @Override
    public void onServerStarted(MinecraftServer server) {
        runDelayedRegistration();
        System.out.println("ServerStarted");
    }

    @Override
    public void veryEndDataPackReload(MinecraftServer server, LifecycledResourceManager resourceManager, boolean success) {
        if(success) Networking.sendToAll(new UpdateDelayedRegistry());
    }
}
