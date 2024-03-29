package io.blodhgarm.personality.compat.pehkui;

import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.PersonalityEntrypoint;
import io.blodhgarm.personality.api.addon.AddonRegistry;
import io.blodhgarm.personality.api.addon.BaseAddon;
import io.blodhgarm.personality.api.reveal.InfoLevel;
import io.blodhgarm.personality.api.reveal.InfoRevealRegistry;
import net.minecraft.util.Identifier;
import virtuoel.pehkui.api.*;

public class PehkuiAddonRegistry implements PersonalityEntrypoint {

    public static PehkuiAddonRegistry INSTANCE = new PehkuiAddonRegistry();

    public static final ScaleModifier CHARACTER_MODIFIER = ScaleRegistries.register(
            ScaleRegistries.SCALE_MODIFIERS,
            PersonalityMod.id("character_modifier"),
            new TypedScaleModifier(() -> PehkuiAddonRegistry.CHARACTER_TYPE, (a, b) -> (Double.sum(a, b) - 1))
    );

    public static final ScaleType CHARACTER_TYPE = registerType();

    private static ScaleType registerType() {
        final ScaleType type = ScaleRegistries.register(
                ScaleRegistries.SCALE_TYPES,
                PersonalityMod.id("character_type"),
                ScaleType.Builder.create()
                        .defaultBaseScale(1)
                        .defaultTickDelay(100)
                        .affectsDimensions()
                        .defaultPersistence(true)
                        .addDependentModifier(CHARACTER_MODIFIER)
                        .build()
        );

        PersonalityMod.CONFIG.subscribeToAdjustWidthAndHeightOnly(resizeBoundsOnly -> {
            ScaleTypes.WIDTH.getDefaultBaseValueModifiers().remove(CHARACTER_MODIFIER);
            ScaleTypes.HEIGHT.getDefaultBaseValueModifiers().remove(CHARACTER_MODIFIER);
            ScaleTypes.BASE.getDefaultBaseValueModifiers().remove(CHARACTER_MODIFIER);

            if (resizeBoundsOnly) { //PersonalityMod.CONFIG.RE.get()
                ScaleTypes.WIDTH.getDefaultBaseValueModifiers().add(CHARACTER_MODIFIER);
                ScaleTypes.HEIGHT.getDefaultBaseValueModifiers().add(CHARACTER_MODIFIER);
            } else {
                ScaleTypes.BASE.getDefaultBaseValueModifiers().add(CHARACTER_MODIFIER);
            }
        });

        if (PersonalityMod.CONFIG.adjustWidthAndHeightOnly()) { //PersonalityMod.CONFIG.RE.get()
            ScaleTypes.WIDTH.getDefaultBaseValueModifiers().add(CHARACTER_MODIFIER);
            ScaleTypes.HEIGHT.getDefaultBaseValueModifiers().add(CHARACTER_MODIFIER);
        } else {
            ScaleTypes.BASE.getDefaultBaseValueModifiers().add(CHARACTER_MODIFIER);
        }

        return type;
    }

    public static final Identifier addonId = new Identifier("pehkui", "height_modifier");

    public <T extends BaseAddon> void addonRegistry(AddonRegistry<T> registry) {
        registry.registerAddon(addonId, (Class<T>) ScaleAddon.class, () -> (T) new ScaleAddon(0f), t -> true);
    }

    @Override
    public void infoRevealRegistry(InfoRevealRegistry registry) {
        registry.registerValueForRevealing(InfoLevel.GENERAL, addonId, () -> new ScaleAddon(-1).shouldShowHeight(false));
    }
}
