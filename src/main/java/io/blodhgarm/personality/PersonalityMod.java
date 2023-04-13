package io.blodhgarm.personality;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.jthemedetecor.OsThemeDetector;
import io.blodhgarm.personality.api.PersonalityEntrypoint;
import io.blodhgarm.personality.api.addon.AddonRegistry;
import io.blodhgarm.personality.api.addon.BaseAddon;
import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.api.core.BaseRegistry;
import io.blodhgarm.personality.api.events.FinalizedPlayerConnectionEvent;
import io.blodhgarm.personality.api.reveal.InfoRevealLevel;
import io.blodhgarm.personality.api.reveal.InfoRevealRegistry;
import io.blodhgarm.personality.compat.origins.OriginsAddonRegistry;
import io.blodhgarm.personality.compat.pehkui.PehkuiAddonRegistry;
import io.blodhgarm.personality.compat.trinkets.TrinketsGlasses;
import io.blodhgarm.personality.item.WalkingStick;
import io.blodhgarm.personality.item.GlassesItem;
import io.blodhgarm.personality.misc.PersonalityCommands;
import io.blodhgarm.personality.misc.config.PersonalityConfig;
import io.blodhgarm.personality.server.CharacterTick;
import io.blodhgarm.personality.server.ServerCharacters;
import io.blodhgarm.personality.utils.DebugCharacters;
import io.wispforest.owo.registration.reflect.FieldRegistrationHandler;
import io.wispforest.owo.registration.reflect.ItemRegistryContainer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;

public class PersonalityMod implements ModInitializer, PersonalityEntrypoint, ItemRegistryContainer {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting()
            .registerTypeAdapter(Character.class, (InstanceCreator<Object>) type -> new Character("", "", "", "", "", -1, -1))
            .create();

    public static final PersonalityConfig CONFIG = PersonalityConfig.createAndLoad();
    public static final OsThemeDetector detector = OsThemeDetector.getDetector();

    public static final String MODID = "personality";

    public static Item WALKING_STICK = new WalkingStick(new Item.Settings().maxCount(1).group(ItemGroup.COMBAT));
    public static GlassesItem BASIC_GLASSES = new GlassesItem(new Item.Settings().maxCount(1).group(ItemGroup.TOOLS));

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    @Override
    public void onInitialize() {
        DebugCharacters.init();
        FieldRegistrationHandler.register(PersonalitySoundEvents.class, MODID, false);

        if(FabricLoader.getInstance().isModLoaded("trinkets")) TrinketsGlasses.init();

        FieldRegistrationHandler.register(PersonalityMod.class, MODID, false);

        PersonalityCommands.register();
        Networking.registerNetworking();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            loadRegistries("ServerStarted");

            ServerCharacters.INSTANCE.onServerStarted(server);
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(PersonalityMod.id("on_server_stop"), server -> {
            for (BaseRegistry value : BaseRegistry.REGISTRIES.values()) value.clearRegistry();

            ServerCharacters.INSTANCE.onServerStopped(server);
        });

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> loadRegistries("EndDataPackReload"));

        ServerTickEvents.END_WORLD_TICK.register(PersonalityMod.id("tick"), new CharacterTick());

        FinalizedPlayerConnectionEvent.CONNECTION_FINISHED.register(PersonalityMod.id("on_player_join"), ServerCharacters.INSTANCE);
    }

    public static void loadRegistries(String eventCall){
        if(FabricLoader.getInstance().isDevelopmentEnvironment()) System.out.println("Event[" + eventCall + "]: Registry[BaseRegistries]");

        FabricLoader.getInstance().getEntrypoints("personality", PersonalityEntrypoint.class).forEach(personalityEntrypoint -> {
            personalityEntrypoint.addonRegistry(AddonRegistry.INSTANCE);
            personalityEntrypoint.infoRevealRegistry(InfoRevealRegistry.INSTANCE);
        });
    }

    @Override
    public <T extends BaseAddon> void addonRegistry(AddonRegistry<T> registry) {
        if(FabricLoader.getInstance().isModLoaded("origins")) OriginsAddonRegistry.INSTANCE.addonRegistry(registry);
        if(FabricLoader.getInstance().isModLoaded("pehkui")) PehkuiAddonRegistry.INSTANCE.addonRegistry(registry);
    }

    @Override
    public void infoRevealRegistry(InfoRevealRegistry registry) {
        registry.registerValueForRevealing(InfoRevealLevel.GENERAL, PersonalityMod.id("description"), () -> "Unknown");
        registry.registerValueForRevealing(InfoRevealLevel.GENERAL, PersonalityMod.id("alias"), () -> "Unknown");

        registry.registerValueForRevealing(InfoRevealLevel.ASSOCIATE, PersonalityMod.id("gender"), () -> "Unknown");
        registry.registerValueForRevealing(InfoRevealLevel.ASSOCIATE, PersonalityMod.id("age"), () -> -1);

        registry.registerValueForRevealing(InfoRevealLevel.TRUSTED, PersonalityMod.id("biography"), () -> "Unknown");

        registry.registerValueForRevealing(InfoRevealLevel.CONFIDANT, PersonalityMod.id("name"), () -> "Unknown");

        if(FabricLoader.getInstance().isModLoaded("origins")) OriginsAddonRegistry.INSTANCE.infoRevealRegistry(registry);
        if(FabricLoader.getInstance().isModLoaded("pehkui")) PehkuiAddonRegistry.INSTANCE.infoRevealRegistry(registry);
    }
}
