package io.blodhgarm.personality;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jthemedetecor.OsThemeDetector;
import io.blodhgarm.personality.api.addon.AddonRegistry;
import io.blodhgarm.personality.api.PersonalityEntrypoint;
import io.blodhgarm.personality.api.addon.BaseAddon;
import io.blodhgarm.personality.compat.origins.OriginsAddonRegistry;
import io.blodhgarm.personality.compat.pehkui.PehkuiAddonRegistry;
import io.blodhgarm.personality.impl.CharacterTick;
import io.blodhgarm.personality.impl.ServerCharacters;
import io.blodhgarm.personality.misc.PersonalityCommands;
import io.blodhgarm.personality.misc.config.PersonalityConfig;
import io.blodhgarm.personality.utils.ServerAccess;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

public class PersonalityMod implements ModInitializer, PersonalityEntrypoint{

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final PersonalityConfig CONFIG = PersonalityConfig.createAndLoad();
    public static final OsThemeDetector detector = OsThemeDetector.getDetector();

    public static final String MODID = "personality";

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    @Override
    public void onInitialize() {
        PersonalityCommands.register();
        Networking.registerNetworking();

        ServerTickEvents.END_WORLD_TICK.register(PersonalityMod.id("tick"), new CharacterTick());

        ServerWorldEvents.LOAD.register(PersonalityMod.id("on_world_load"), new ServerAccess());
        ServerWorldEvents.LOAD.register(PersonalityMod.id("on_world_load"), ServerCharacters.INSTANCE);

        ServerPlayConnectionEvents.JOIN.register(PersonalityMod.id("on_player_join"), ServerCharacters.INSTANCE);

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(AddonRegistry.INSTANCE);
        ServerLifecycleEvents.SERVER_STARTED.register(AddonRegistry.INSTANCE);

        FabricLoader.getInstance().getEntrypoints("personality", PersonalityEntrypoint.class).forEach(personalityEntrypoint -> {
            personalityEntrypoint.addonRegistry(AddonRegistry.INSTANCE);
        });
    }

    @Override
    public <T extends BaseAddon> void addonRegistry(AddonRegistry<T> registry) {
        if(FabricLoader.getInstance().isModLoaded("origins")) OriginsAddonRegistry.INSTANCE.addonRegistry(registry);
        if(FabricLoader.getInstance().isModLoaded("pehkui")) PehkuiAddonRegistry.INSTANCE.addonRegistry(registry);
    }

        if(FabricLoader.getInstance().isModLoaded("pehkui")){
            PehkuiAddonRegistry.addonRegistry(registry);
        }
    }
}
