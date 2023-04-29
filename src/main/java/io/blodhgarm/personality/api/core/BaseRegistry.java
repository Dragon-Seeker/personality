package io.blodhgarm.personality.api.core;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Helper Class for a basic Registry Implementation
 */
public abstract class BaseRegistry {

    protected static final Logger LOGGER = LogUtils.getLogger();

    public static Map<Identifier, BaseRegistry> REGISTRIES = new HashMap<>();

    public BaseRegistry(){
        REGISTRIES.put(this.getRegistryId(), this);
    }

    @Nullable
    public static BaseRegistry getRegistry(Identifier registryId){
        return REGISTRIES.get(registryId);
    }

    public abstract List<Identifier> getRegisteredIds();

    public abstract Identifier getRegistryId();

    public abstract void clearRegistry();

    public Text getTranslation(){
        return Text.translatable(
                getRegistryId().toString()
                        .replace(":", ".") + ".name"
        );
    }
}
