package io.blodhgarm.personality.server.config;

import io.blodhgarm.personality.PersonalityMod;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.Nest;
import io.wispforest.owo.config.annotation.Sync;

@Modmenu(modId = PersonalityMod.MODID)
@Config(name = PersonalityMod.MODID, wrapperName = "PersonalityConfig")
public class PersonalityConfigModel {

    @Nest public GradualValue FASTER_EXHAUSTION     = new GradualValue(17, 24, 1.5F, 0, Curve.LINEAR);
    @Nest public GradualValue FASTER_HEAL           = new GradualValue(17, 24, 2F, 0, Curve.LINEAR);
    @Nest public GradualValue LOWER_HUNGER_MINIMUM  = new GradualValue(17, 24, 10F, 14F, Curve.LINEAR);
    @Nest public GradualValue NO_STICK_SLOWNESS     = new GradualValue(60, Integer.MAX_VALUE, 0, 0.45F, Curve.LINEAR);

    @Sync(value = Option.SyncMode.OVERRIDE_CLIENT)
    @Nest public GradualValue NO_GLASSES_BLURRINESS = new GradualValue(60, Integer.MAX_VALUE, 0, 16F, Curve.LINEAR);

    @Sync(value = Option.SyncMode.OVERRIDE_CLIENT)
    public int BASE_MAXIMUM_AGE = 80;

    @Sync(value = Option.SyncMode.OVERRIDE_CLIENT)
    @Nest public ExtraLife EXTRA_LIFE = new ExtraLife(17, 110, 1, 1, Curve.NONE);

    @Sync(value = Option.SyncMode.OVERRIDE_CLIENT)
    public int MAX_EXTRA_YEARS_OF_LIFE = 30;

    public ThemeMode THEME_MODE = ThemeMode.SYSTEM;

    public enum ThemeMode {
        LIGHT_MODE,
        DARK_MODE,
        SYSTEM
    }


    public enum Curve { NONE, LINEAR, QUADRATIC, SQRT, EXPONENTIAL, LOGARITHMIC, EXPONENTIAL_EXTREME, LOGARITHMIC_EXTREME }

    public static class GradualValue {
        public int MIN_AGE;
        public int MAX_AGE;
        public float START_VALUE;
        public float END_VALUE;
        public Curve CURVE;

        public GradualValue(int minAge, int maxAge, float startValue, float endValue, Curve curve) {
            MIN_AGE = minAge;
            MAX_AGE = maxAge;
            START_VALUE = startValue;
            END_VALUE = endValue;
            CURVE = curve;
        }
    }

    public static class ExtraLife {
        public int MIN_AGE;
        public int MAX_AGE;
        public float START_HOURS_PER_EXTRA_LIFE;
        public float CURVE_MULTIPLIER;
        public Curve CURVE;

        public ExtraLife(int minAge, int maxAge, float startHoursPerExtraLife, float curveMultiplier, Curve curve) {
            MIN_AGE = minAge;
            MAX_AGE = maxAge;
            START_HOURS_PER_EXTRA_LIFE = startHoursPerExtraLife;
            CURVE_MULTIPLIER = curveMultiplier;
            CURVE = curve;
        }

    }




}
