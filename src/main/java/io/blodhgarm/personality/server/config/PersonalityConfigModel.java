package io.blodhgarm.personality.server.config;

import io.blodhgarm.personality.PersonalityMod;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.Sync;

@Modmenu(modId = PersonalityMod.MODID)
@Config(name = PersonalityMod.MODID, wrapperName = "PersonalityConfig")
public class PersonalityConfigModel {

    public float YOUTH_EXHAUSTION_MULTIPLIER = 1.5F;
    public float YOUTH_HEAL_RATE_MULTIPLIER = 2F;
    public int YOUTH_HEAL_HUNGER_MINIMUM = 14;
    public int OLD_PERSON_SLOWNESS_WITHOUT_STICK = 3;

    @Sync(value = Option.SyncMode.OVERRIDE_CLIENT)
    public float OLD_PERSON_BLUR_WITHOUT_GLASSES_RADIUS = 80;

    @Sync(value = Option.SyncMode.OVERRIDE_CLIENT)
    public int BASE_MAXIMUM_AGE = 80;

    @Sync(value = Option.SyncMode.OVERRIDE_CLIENT)
    public int HOURS_PER_EXTRA_YEAR_OF_LIFE = 1;

    @Sync(value = Option.SyncMode.OVERRIDE_CLIENT)
    public int MAX_EXTRA_YEARS_OF_LIFE = 30;

    public ThemeMode THEME_MODE = ThemeMode.SYSTEM;

    public enum ThemeMode {
        LIGHT_MODE,
        DARK_MODE,
        SYSTEM
    }
}
