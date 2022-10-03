package io.blodhgarm.personality.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import io.blodhgarm.personality.api.addons.BaseAddon;
import io.blodhgarm.personality.impl.ServerCharacters;
import io.wispforest.owo.offline.DataSavedEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.mixin.event.lifecycle.DataPackContentsMixin;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AddonRegistry<A extends BaseAddon<?>> implements ServerLifecycleEvents.EndDataPackReload {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    //--------------------------------------------------------------------------------

    public static final AddonRegistry<BaseAddon<?>> INSTANCE = new AddonRegistry<>();

    //--------------------------------------------------------------------------------

    private final List<Consumer<AddonRegistry<A>>> DELAYED_REGISTERY = new ArrayList<>();

    private final Map<String, RegistryHelper<A>> LOADERS = new HashMap<>();

    private final Map<String, Supplier<A>> DEFAULT_ADDONS = new HashMap<>();

    //--------------------------------------------------------------------------------

    public void registerAddon(String addonId, Class<A> addonClass, Supplier<A> defaultAddon){
        if(!LOADERS.containsKey(addonId)){
            DEFAULT_ADDONS.put(addonId, defaultAddon);

            LOADERS.put(addonId, new RegistryHelper<>(addonId, addonClass));
        }
    }

    public void registerDelayedAddon(Consumer<AddonRegistry<A>> registerConsumer){
        DELAYED_REGISTERY.add(registerConsumer);
    }

    //--------------------------------------------------------------------------------

    public Class<A> getAddonClass(String addonId){
        return LOADERS.get(addonId).addonClass();
    }

    public List<A> getDefaultAddons(){
        return DEFAULT_ADDONS.values().stream().map(Supplier::get).toList();
    }

    //--------------------------------------------------------------------------------

    public Map<String, String> loadAddonsForCharacter(Character c){
        Path characterFolder = ServerCharacters.getSpecificCharacterPath(c.getUUID());

        Map<String, String> addonData = new HashMap<>();

        LOADERS.forEach((s, registryHelper) -> {
            Path currentAddonFolder = characterFolder.resolve("addons/" + s + ".json");

            A addon;

            try {
                String addonJson = Files.readString(currentAddonFolder);

                addonData.put(s, addonJson);

                addon = GSON.fromJson(addonJson, registryHelper.addonClass());
            } catch (IOException e){
                LOGGER.error("[AddonLoading] {} addon for [Name: {}, UUID: {}] was unable to be loaded from the Disc, setting such to default.", s, c.getName(), c.getUUID());

                addon = DEFAULT_ADDONS.get(s).get();

                e.printStackTrace();
            } catch (JsonSyntaxException e){
                LOGGER.error("[AddonLoading] {} addon for [Name: {}, UUID: {}] was unable to be serialized, setting such to default.", s, c.getName(), c.getUUID());

                addon = DEFAULT_ADDONS.get(s).get();
                addon.improperLoad();

                e.printStackTrace();
            }

            c.characterAddons.put(s, addon);
        });

        return addonData;
    }

//    public Map<String, String> saveAddonsForCharacter(Character c){
//        Path characterFolder = ServerCharacters.getSpecificCharacterPath(c.getUUID());
//
//        Map<String, String> addonData = new HashMap<>();
//
//        LOADERS.forEach((s, registryHelper) -> {
//            Path currentAddonFolder = characterFolder.resolve("addons/" + s + ".json");
//
//            A addon;
//
//            try {
//                String addonJson = Files.readString(currentAddonFolder);
//
//                addonData.put(s, addonJson);
//
//                addon = GSON.fromJson(addonJson, registryHelper.addonClass());
//            } catch (IOException e){
//                LOGGER.error("[AddonLoading] {} addon for [Name: {}, UUID: {}] was unable to be loaded from the Disc, setting such to default.", s, c.getName(), c.getUUID());
//
//                addon = DEFAULT_ADDONS.get(s).get();
//
//                e.printStackTrace();
//            } catch (JsonSyntaxException e){
//                LOGGER.error("[AddonLoading] {} addon for [Name: {}, UUID: {}] was unable to be serialized, setting such to default.", s, c.getName(), c.getUUID());
//
//                addon = DEFAULT_ADDONS.get(s).get();
//                addon.improperLoad();
//
//                e.printStackTrace();
//            }
//
//            c.characterAddons.put(s, addon);
//        });
//
//        return addonData;
//    }

    //--------------------------------------------------------------------------------

    @Override
    public void endDataPackReload(MinecraftServer server, LifecycledResourceManager resourceManager, boolean success) {
        DELAYED_REGISTERY.forEach(addonRegistryConsumer -> addonRegistryConsumer.accept(this));
    }

    public static record RegistryHelper<A extends BaseAddon<?>>(String addonId, Class<A> addonClass){}
}
