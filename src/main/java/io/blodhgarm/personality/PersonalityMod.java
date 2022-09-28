package io.blodhgarm.personality;

import com.jthemedetecor.OsThemeDetector;
import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.impl.CharacterTick;
import io.blodhgarm.personality.impl.ServerCharacters;
import io.blodhgarm.personality.misc.PersonalityCommands;
import io.blodhgarm.personality.misc.config.PersonalityConfig;
import io.blodhgarm.personality.utils.ServerAccess;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import virtuoel.pehkui.api.*;

import java.util.HashMap;
import java.util.Map;

public class PersonalityMod implements ModInitializer {

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

        ServerWorldEvents.LOAD.register(PersonalityMod.id("on_world_load"), ServerCharacters.INSTANCE);
        ServerWorldEvents.LOAD.register(PersonalityMod.id("on_world_load"), new ServerAccess());
        ServerWorldEvents.LOAD.register(PersonalityMod.id("on_world_load"), PersonalityMod::onWorldLoad);

        ServerPlayConnectionEvents.JOIN.register(PersonalityMod.id("on_player_join"), ServerCharacters.INSTANCE);
    }

    public static void onWorldLoad(MinecraftServer server, ServerWorld world) {
        ScaleTypes.BASE.getDefaultBaseValueModifiers().add(testModifier);
    }

    public static final ScaleModifier testModifier = ScaleRegistries.register(
            ScaleRegistries.SCALE_MODIFIERS,
            PersonalityMod.id("test"),
            new TypedScaleModifier(() -> ScaleTypes.BASE, (modifiedScale, scale) -> modifiedScale * 5)
    );

    public static Map<Character, ScaleModifier> modifiers = new HashMap<>();

    private static void setModifier(Character c, ScaleData data) {
        ScaleModifier modifier = ScaleRegistries.register(
                ScaleRegistries.SCALE_MODIFIERS,
                PersonalityMod.id(c.getUUID()),
                new TypedScaleModifier(() -> ScaleTypes.BASE,
                        (modifiedScale,scale) -> scale + c.getHeightOffset())
        );
        modifiers.put(c, modifier);
        data.getBaseValueModifiers().add(modifier);
    }

    private static void removeModifier(Character c, ScaleData data) {
        data.getBaseValueModifiers().remove(modifiers.get(c));
        modifiers.remove(c);
        ScaleRegistries.SCALE_MODIFIERS.remove(PersonalityMod.id(c.getUUID()));
    }

}
