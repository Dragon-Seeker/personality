package io.blodhgarm.personality.packets;

import io.blodhgarm.personality.api.core.DelayedRegistry;
import io.wispforest.owo.network.ClientAccess;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public record UpdateDelayedRegistry() {
    @Environment(EnvType.CLIENT)
    public static void update(UpdateDelayedRegistry message, ClientAccess access){
        DelayedRegistry.runAllDelayedRegistries();
    }
}
