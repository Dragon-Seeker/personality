package io.blodhgarm.personality.misc.config;

import io.blodhgarm.personality.PersonalityMod;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Modmenu(modId = PersonalityMod.MODID)
@Config(name = PersonalityMod.MODID, wrapperName = "PersonalityConfig")
public class PersonalityConfigModel {

    //Character Configuration
    @SectionHeader("character_configuration")
    @Sync(value = Option.SyncMode.OVERRIDE_CLIENT)
    @RangeConstraint(min = 0, max = Integer.MAX_VALUE)
    public int defaultMaximumAge = 80;

    @Nest public GradualValue fasterExhaustion = new GradualValue(17, 24, 1.5F, 0, Curve.LINEAR);
    @Nest public GradualValue fasterHealing = new GradualValue(17, 24, 2F, 0, Curve.LINEAR);
    @Nest public GradualValue minimumHungerToHeal = new GradualValue(17, 24, 10F, 14F, Curve.LINEAR);

    @Nest public GradualValue agingSlowness = new GradualValue(60, 110, 0, 0.45F, Curve.LINEAR);

    @Sync(value = Option.SyncMode.OVERRIDE_CLIENT)
    @Nest public GradualValue agingBlurriness = new GradualValue(60, 110, 0, 16F, Curve.LINEAR);

    @Sync(value = Option.SyncMode.OVERRIDE_CLIENT)
    @Nest public ExtraLife extraAgeConfiguration = new ExtraLife(17, 110, 1, 1, Curve.NONE);

    @Sync(value = Option.SyncMode.OVERRIDE_CLIENT)
    @RangeConstraint(min = 0, max = Integer.MAX_VALUE)
    public int maximumExtraAge = 30;

    //Command Configurations

    @SectionHeader("command_authorization_configuration")
    // --- Moderation Level Command List (Similar to Whitelist with Player Names) ---
    public List<String> moderationList = new ArrayList<>();

    // --- Administration Level Command List (Similar to Whitelist with Player Names) ---
    public List<String> administrationList = new ArrayList<>();

    //Addon Options

    // --- Config Option for Pehkui ---
    @SectionHeader("addon_configuration")
    @Sync(value = Option.SyncMode.OVERRIDE_CLIENT)
    @Hook public boolean adjustWidthAndHeightOnly = true;

    //Client Side Only

    // --- Used to change the Theme for the UI ---
    @SectionHeader("client")
    public ThemeMode themeMode = ThemeMode.SYSTEM;

    // --- Basically enabling advanced tooltip for chat messages ---
    public boolean showPlayerNameWhenHoveringChat = true;

    // --- Used to change the position of the players' nameplate ---
    public boolean showPlayerNamePlateAtChestLevel = false;

    //-----------------------------------------------

    public enum ThemeMode {
        LIGHT_MODE,
        DARK_MODE,
        SYSTEM
    }

    public enum Curve { NONE, LINEAR, QUADRATIC, SQRT, EXPONENTIAL, LOGARITHMIC, EXPONENTIAL_EXTREME, LOGARITHMIC_EXTREME }

    public static class GradualValue {
        @RangeConstraint(min = 0, max = 1000)
        public int minAge;

        @RangeConstraint(min = 0, max = 1000)
        public int maxAge;

        @RangeConstraint(min = 0, max = 100F)
        public float startingValue;

        @RangeConstraint(min = 0, max = 100F)
        public float endingValue;

        public Curve calculationCurve;

        public GradualValue(int minAge, int maxAge, float startValue, float endValue, Curve curve) {
            this.minAge = minAge;
            this.maxAge = maxAge;
            startingValue = startValue;
            endingValue = endValue;
            calculationCurve = curve;
        }


    }

    public static class ExtraLife {

        @RangeConstraint(min = 0, max = 1000)
        public int minAge;

        @RangeConstraint(min = 0, max = 1000)
        public int maxAge;

        @RangeConstraint(min = 0, max = Float.MAX_VALUE)
        public float minimumHoursForExtraLife;

        @RangeConstraint(min = 0, max = 100F)
        public float multiplier;

        public Curve calculationCurve;

        public ExtraLife(int minAge, int maxAge, float minimumHoursForExtraLife, float multiplier, Curve calculationCurve) {
            this.minAge = minAge;
            this.maxAge = maxAge;

            this.minimumHoursForExtraLife = minimumHoursForExtraLife;
            this.multiplier = multiplier;
            this.calculationCurve = calculationCurve;
        }

    }
}
