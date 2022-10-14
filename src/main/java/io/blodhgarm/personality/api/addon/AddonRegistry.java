package io.blodhgarm.personality.api.addon;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.impl.ServerCharacters;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class AddonRegistry<A extends BaseAddon> implements ServerLifecycleEvents.EndDataPackReload, ServerLifecycleEvents.ServerStarted {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    //--------------------------------------------------------------------------------

    public static final AddonRegistry<BaseAddon> INSTANCE = new AddonRegistry<>();

    //--------------------------------------------------------------------------------

    private final List<Consumer<AddonRegistry<A>>> DELAYED_REGISTERY = new ArrayList<>();

    private final Map<Identifier, AddonLoaderStorage<A>> LOADERS = new HashMap<>();

    //--------------------------------------------------------------------------------

    public void registerAddon(Identifier addonId, Class<A> addonClass, Supplier<A> defaultAddon, Predicate<A> addonValidator){
        if(!LOADERS.containsKey(addonId)){
            LOADERS.put(addonId, new AddonLoaderStorage<>(addonId, addonClass, defaultAddon, addonValidator));
        }
    }

    public void registerDelayedAddon(Consumer<AddonRegistry<A>> registerConsumer){
        DELAYED_REGISTERY.add(registerConsumer);
    }

    //--------------------------------------------------------------------------------

    public Class<A> getAddonClass(Identifier addonId){
        return LOADERS.get(addonId).addonClass();
    }

    public List<A> getDefaultAddons(){
        return LOADERS.values().stream().map(loaderStorage -> loaderStorage.defaultAddon.get()).toList();
    }

    public A validateOrDefault(Identifier addonId, @Nullable A addonClass){
        AddonLoaderStorage<A> loaderStorage = LOADERS.get(addonId);

        if(addonClass != null){
            boolean valid = loaderStorage.addonValidator().test(addonClass);

            if(valid) {
                return addonClass;
            } else {
                LOGGER.warn("[AddonValidation] A characters addon from {} was found to be invalid, will be replaced with the default value", addonId);
            }
        }

        return loaderStorage.defaultAddon().get();
    }

    //--------------------------------------------------------------------------------

    public Map<Identifier, String> loadAddonsForCharacter(Character c){
        Path characterFolder = ServerCharacters.getSpecificCharacterPath(c.getUUID());

        Map<Identifier, String> addonData = new HashMap<>();

        LOADERS.forEach((s, registryHelper) -> {
            Path currentAddonFolder = characterFolder.resolve("addons/" + s.getNamespace() + "/" + s.getPath() + ".json");

            A addon;

            try {
                String addonJson = Files.readString(currentAddonFolder);

                addonData.put(s, addonJson);

                addon = GSON.fromJson(addonJson, registryHelper.addonClass());
            } catch (IOException e){
                LOGGER.error("[AddonLoading] {} addon for [Name: {}, UUID: {}] was unable to be loaded from the Disc, setting such to default.", s, c.getName(), c.getUUID());

                addon = registryHelper.defaultAddon.get();

                e.printStackTrace();
            } catch (JsonSyntaxException e){
                LOGGER.error("[AddonLoading] {} addon for [Name: {}, UUID: {}] was unable to be serialized, setting such to default.", s, c.getName(), c.getUUID());

                addon = registryHelper.defaultAddon.get();
                addon.improperLoad();

                e.printStackTrace();
            }

            c.characterAddons.put(s, addon);
        });

        return addonData;
    }

    //--------------------------------------------------------------------------------

    @Override
    public void endDataPackReload(MinecraftServer server, LifecycledResourceManager resourceManager, boolean success) {
        DELAYED_REGISTERY.forEach(addonRegistryConsumer -> addonRegistryConsumer.accept(this));
    }

    @Override
    public void onServerStarted(MinecraftServer server) {
        DELAYED_REGISTERY.forEach(addonRegistryConsumer -> addonRegistryConsumer.accept(this));
    }

    public static record AddonLoaderStorage<A extends BaseAddon>(Identifier addonId, Class<A> addonClass, Supplier<A> defaultAddon, Predicate<A> addonValidator){}
}
