package io.blodhgarm.personality.api.addon;

import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.api.PersonalityEntrypoint;
import io.blodhgarm.personality.api.core.DelayedRegistry;
import io.blodhgarm.personality.impl.ServerCharacters;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * AddonRegistry is used internally to read and validate addons on both client and server
 */
public class AddonRegistry<A extends BaseAddon> extends DelayedRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final List<Consumer<AddonRegistry<A>>> DELAYED_REGISTERY = new ArrayList<>();

    private final Map<Identifier, AddonLoader<A>> LOADERS = new HashMap<>();

    //--------------------------------------------------------------------------------


    public AddonRegistry() { super(); }

    /**
     * The current Instant of the Addon Registry used for registering your addon within the {@link PersonalityEntrypoint}
     */
    public static final AddonRegistry<BaseAddon> INSTANCE = new AddonRegistry<>();

    /**
     * Method used to register your addon for Personality
     * @param addonId The Addons Identifier
     * @param addonClass The class that extends the BaseAddon
     * @param defaultAddon The default version of your given Addon
     * @param addonValidator Predicate used to confirm that the addon is valid during the creation of the character
     */
    public void registerAddon(Identifier addonId, Class<A> addonClass, Supplier<A> defaultAddon, Predicate<A> addonValidator){
        if(!LOADERS.containsKey(addonId)){
            LOADERS.put(addonId, new AddonLoader<>(addonId, addonClass, defaultAddon, addonValidator));
        } else {
            LOGGER.warn("[AddonRegistry]: The given Identifier [{}] is already found within the Loaders map meaning it will not be registered.", addonId);
        }
    }

    /**
     * Method allowing for delayed registry of Addons if your mod requires loading of DataPacks and such (See {@link #onServerStarted(MinecraftServer)}
     * @param registerConsumer A consumer with used to register your methods at a later time
     */
    public void registerDelayedAddon(Consumer<AddonRegistry<A>> registerConsumer){
        DELAYED_REGISTERY.add(registerConsumer);
    }

    //--------------------------------------------------------------------------------


    public Optional<AddonLoader<A>> getAddonLoader(Identifier addonId){
        return Optional.ofNullable(LOADERS.get(addonId));
    }

    @Nullable
    public Class<A> getAddonClass(Identifier addonId){
        Optional<AddonLoader<A>> loader = getAddonLoader(addonId);

        return loader.map(AddonLoader::addonClass).orElse(null);
    }

    public Map<Identifier, A> getDefaultAddons(){
        Map<Identifier, A> defaultAddonsMap = new HashMap<>();

        LOADERS.forEach((identifier, loaderStorage) -> defaultAddonsMap.put(identifier, loaderStorage.defaultAddon.get()));

        return defaultAddonsMap;
    }

    @Nullable
    public A validateOrDefault(Identifier addonId, @Nullable A addonClass){
        Optional<AddonLoader<A>> loaderStorage = getAddonLoader(addonId);

        if(loaderStorage.isPresent()) {
            if (addonClass != null) {
                boolean valid = loaderStorage.get().addonValidator().test(addonClass);

                if (valid) {
                    return addonClass;
                } else {
                    LOGGER.warn("[AddonValidation] A characters addon from {} was found to be invalid, will be replaced with the default value", addonId);
                }
            }

            return loaderStorage.get().defaultAddon().get();
        }

        return null;
    }

    public void checkAndDefaultPlayerAddons(PlayerEntity player){
        this.getDefaultAddons().forEach((identifier, addon) -> {
            if(!addon.isEqualToPlayer(player) && addon.getAddonEnvironment().shouldApply(player.getWorld())){
                addon.applyAddon(player);
            }
        });
    }

    //--------------------------------------------------------------------------------

    @Override
    public List<Identifier> getRegisteredIds(){
        return List.copyOf(this.LOADERS.keySet());
    }

    @ApiStatus.Internal
    public Map<Identifier, String> loadAddonsForCharacter(Character c){
        Path characterFolder = ServerCharacters.getSpecificCharacterPath(c.getUUID());

        Map<Identifier, String> addonData = new HashMap<>();

        LOADERS.forEach((s, registryHelper) -> {
            Path currentAddonFolder = characterFolder.resolve("addons/" + s.getNamespace() + "/" + s.getPath() + ".json");

            A addon;

            try {
                String addonJson = Files.readString(currentAddonFolder);

                addonData.put(s, addonJson);

                addon = PersonalityMod.GSON.fromJson(addonJson, registryHelper.addonClass());
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

            c.getAddons().put(s, addon);
        });

        return addonData;
    }

    public void deserializesAddons(Character c, Map<Identifier, String> addonData){
        Map<Identifier, BaseAddon> addons = new HashMap<>();

        addonData.forEach((addonId, s2) -> {
            AddonLoader<A> registryHelper = LOADERS.get(addonId);

            if(addonId == null){
                LOGGER.error("[AddonLoading] {} addon id was found to be invalid, ignoring addon", addonId);

                return;
            }

            if(registryHelper == null){
                LOGGER.error("[AddonLoading] {} addon for [Name: {}, UUID: {}] was unable to be found, ignoring addon", addonId, c.getName(), c.getUUID());

                return;
            }

            A addon;

            try {
                addon = PersonalityMod.GSON.fromJson(s2, registryHelper.addonClass());
            } catch (JsonSyntaxException e){
                LOGGER.error("[AddonLoading] {} addon for [Name: {}, UUID: {}] was unable to be serialized, setting such to default.", addonId, c.getName(), c.getUUID());

                addon = registryHelper.defaultAddon.get();
                addon.improperLoad();

                e.printStackTrace();
            }

            addons.put(addonId, addon);
        });

        c.getAddons().putAll(addons);
    }

    public Map<Identifier, String> serializesAddons(Character c){
        Map<Identifier, String> addonData = new HashMap<>();

        c.getAddons().forEach((identifier, addon) -> {
            addonData.put(identifier, PersonalityMod.GSON.toJson(addon));
        });

        return addonData;
    }

    @Override
    public Identifier getRegistryId() {
        return new Identifier(PersonalityMod.MODID, "addon_registry");
    }

    //--------------------------------------------------------------------------------

    @Override
    public void runDelayedRegistration(){
        DELAYED_REGISTERY.forEach(addonRegistryConsumer -> addonRegistryConsumer.accept(this));
    }

    public static record AddonLoader<A extends BaseAddon>(Identifier addonId, Class<A> addonClass, Supplier<A> defaultAddon, Predicate<A> addonValidator){ }
}
