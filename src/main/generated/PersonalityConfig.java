package io.blodhgarm.personality.server.config;

import io.wispforest.owo.config.ConfigWrapper;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.util.Observable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class PersonalityConfig extends ConfigWrapper<io.blodhgarm.personality.server.config.PersonalityConfigModel> {

    private final Option<java.lang.Integer> FASTER_EXHAUSTION_MIN_AGE = this.optionForKey(new Option.Key("FASTER_EXHAUSTION.MIN_AGE"));
    private final Option<java.lang.Integer> FASTER_EXHAUSTION_MAX_AGE = this.optionForKey(new Option.Key("FASTER_EXHAUSTION.MAX_AGE"));
    private final Option<java.lang.Float> FASTER_EXHAUSTION_START_VALUE = this.optionForKey(new Option.Key("FASTER_EXHAUSTION.START_VALUE"));
    private final Option<java.lang.Float> FASTER_EXHAUSTION_END_VALUE = this.optionForKey(new Option.Key("FASTER_EXHAUSTION.END_VALUE"));
    private final Option<java.lang.Integer> FASTER_HEAL_MIN_AGE = this.optionForKey(new Option.Key("FASTER_HEAL.MIN_AGE"));
    private final Option<java.lang.Integer> FASTER_HEAL_MAX_AGE = this.optionForKey(new Option.Key("FASTER_HEAL.MAX_AGE"));
    private final Option<java.lang.Float> FASTER_HEAL_START_VALUE = this.optionForKey(new Option.Key("FASTER_HEAL.START_VALUE"));
    private final Option<java.lang.Float> FASTER_HEAL_END_VALUE = this.optionForKey(new Option.Key("FASTER_HEAL.END_VALUE"));
    private final Option<java.lang.Integer> LOWER_HUNGER_MINIMUM_MIN_AGE = this.optionForKey(new Option.Key("LOWER_HUNGER_MINIMUM.MIN_AGE"));
    private final Option<java.lang.Integer> LOWER_HUNGER_MINIMUM_MAX_AGE = this.optionForKey(new Option.Key("LOWER_HUNGER_MINIMUM.MAX_AGE"));
    private final Option<java.lang.Float> LOWER_HUNGER_MINIMUM_START_VALUE = this.optionForKey(new Option.Key("LOWER_HUNGER_MINIMUM.START_VALUE"));
    private final Option<java.lang.Float> LOWER_HUNGER_MINIMUM_END_VALUE = this.optionForKey(new Option.Key("LOWER_HUNGER_MINIMUM.END_VALUE"));
    private final Option<java.lang.Integer> NO_STICK_SLOWNESS_MIN_AGE = this.optionForKey(new Option.Key("NO_STICK_SLOWNESS.MIN_AGE"));
    private final Option<java.lang.Integer> NO_STICK_SLOWNESS_MAX_AGE = this.optionForKey(new Option.Key("NO_STICK_SLOWNESS.MAX_AGE"));
    private final Option<java.lang.Float> NO_STICK_SLOWNESS_START_VALUE = this.optionForKey(new Option.Key("NO_STICK_SLOWNESS.START_VALUE"));
    private final Option<java.lang.Float> NO_STICK_SLOWNESS_END_VALUE = this.optionForKey(new Option.Key("NO_STICK_SLOWNESS.END_VALUE"));
    private final Option<java.lang.Integer> NO_GLASSES_BLURRINESS_MIN_AGE = this.optionForKey(new Option.Key("NO_GLASSES_BLURRINESS.MIN_AGE"));
    private final Option<java.lang.Integer> NO_GLASSES_BLURRINESS_MAX_AGE = this.optionForKey(new Option.Key("NO_GLASSES_BLURRINESS.MAX_AGE"));
    private final Option<java.lang.Float> NO_GLASSES_BLURRINESS_START_VALUE = this.optionForKey(new Option.Key("NO_GLASSES_BLURRINESS.START_VALUE"));
    private final Option<java.lang.Float> NO_GLASSES_BLURRINESS_END_VALUE = this.optionForKey(new Option.Key("NO_GLASSES_BLURRINESS.END_VALUE"));
    private final Option<java.lang.Integer> BASE_MAXIMUM_AGE = this.optionForKey(new Option.Key("BASE_MAXIMUM_AGE"));
    private final Option<java.lang.Integer> EXTRA_LIFE_START_AT_AGE = this.optionForKey(new Option.Key("EXTRA_LIFE.START_AT_AGE"));
    private final Option<java.lang.Float> EXTRA_LIFE_START_HOURS_PER_EXTRA_LIFE = this.optionForKey(new Option.Key("EXTRA_LIFE.START_HOURS_PER_EXTRA_LIFE"));
    private final Option<io.blodhgarm.personality.server.config.PersonalityConfigModel.Curve> EXTRA_LIFE_CURVE = this.optionForKey(new Option.Key("EXTRA_LIFE.CURVE"));
    private final Option<java.lang.Integer> MAX_EXTRA_YEARS_OF_LIFE = this.optionForKey(new Option.Key("MAX_EXTRA_YEARS_OF_LIFE"));
    private final Option<io.blodhgarm.personality.server.config.PersonalityConfigModel.ThemeMode> THEME_MODE = this.optionForKey(new Option.Key("THEME_MODE"));

    private PersonalityConfig() {
        super(io.blodhgarm.personality.server.config.PersonalityConfigModel.class);
    }

    public static PersonalityConfig createAndLoad() {
        var wrapper = new PersonalityConfig();
        wrapper.load();
        return wrapper;
    }

    public final FASTER_EXHAUSTION FASTER_EXHAUSTION = new FASTER_EXHAUSTION();
    public class FASTER_EXHAUSTION implements GradualValue {
        public int MIN_AGE() {
            return FASTER_EXHAUSTION_MIN_AGE.value();
        }

        public void MIN_AGE(int value) {
            instance.FASTER_EXHAUSTION.MIN_AGE = value;
            FASTER_EXHAUSTION_MIN_AGE.synchronizeWithBackingField();
        }

        public int MAX_AGE() {
            return FASTER_EXHAUSTION_MAX_AGE.value();
        }

        public void MAX_AGE(int value) {
            instance.FASTER_EXHAUSTION.MAX_AGE = value;
            FASTER_EXHAUSTION_MAX_AGE.synchronizeWithBackingField();
        }

        public float START_VALUE() {
            return FASTER_EXHAUSTION_START_VALUE.value();
        }

        public void START_VALUE(float value) {
            instance.FASTER_EXHAUSTION.START_VALUE = value;
            FASTER_EXHAUSTION_START_VALUE.synchronizeWithBackingField();
        }

        public float END_VALUE() {
            return FASTER_EXHAUSTION_END_VALUE.value();
        }

        public void END_VALUE(float value) {
            instance.FASTER_EXHAUSTION.END_VALUE = value;
            FASTER_EXHAUSTION_END_VALUE.synchronizeWithBackingField();
        }

    }
    public final FASTER_HEAL FASTER_HEAL = new FASTER_HEAL();
    public class FASTER_HEAL implements GradualValue {
        public int MIN_AGE() {
            return FASTER_HEAL_MIN_AGE.value();
        }

        public void MIN_AGE(int value) {
            instance.FASTER_HEAL.MIN_AGE = value;
            FASTER_HEAL_MIN_AGE.synchronizeWithBackingField();
        }

        public int MAX_AGE() {
            return FASTER_HEAL_MAX_AGE.value();
        }

        public void MAX_AGE(int value) {
            instance.FASTER_HEAL.MAX_AGE = value;
            FASTER_HEAL_MAX_AGE.synchronizeWithBackingField();
        }

        public float START_VALUE() {
            return FASTER_HEAL_START_VALUE.value();
        }

        public void START_VALUE(float value) {
            instance.FASTER_HEAL.START_VALUE = value;
            FASTER_HEAL_START_VALUE.synchronizeWithBackingField();
        }

        public float END_VALUE() {
            return FASTER_HEAL_END_VALUE.value();
        }

        public void END_VALUE(float value) {
            instance.FASTER_HEAL.END_VALUE = value;
            FASTER_HEAL_END_VALUE.synchronizeWithBackingField();
        }

    }
    public final LOWER_HUNGER_MINIMUM LOWER_HUNGER_MINIMUM = new LOWER_HUNGER_MINIMUM();
    public class LOWER_HUNGER_MINIMUM implements GradualValue {
        public int MIN_AGE() {
            return LOWER_HUNGER_MINIMUM_MIN_AGE.value();
        }

        public void MIN_AGE(int value) {
            instance.LOWER_HUNGER_MINIMUM.MIN_AGE = value;
            LOWER_HUNGER_MINIMUM_MIN_AGE.synchronizeWithBackingField();
        }

        public int MAX_AGE() {
            return LOWER_HUNGER_MINIMUM_MAX_AGE.value();
        }

        public void MAX_AGE(int value) {
            instance.LOWER_HUNGER_MINIMUM.MAX_AGE = value;
            LOWER_HUNGER_MINIMUM_MAX_AGE.synchronizeWithBackingField();
        }

        public float START_VALUE() {
            return LOWER_HUNGER_MINIMUM_START_VALUE.value();
        }

        public void START_VALUE(float value) {
            instance.LOWER_HUNGER_MINIMUM.START_VALUE = value;
            LOWER_HUNGER_MINIMUM_START_VALUE.synchronizeWithBackingField();
        }

        public float END_VALUE() {
            return LOWER_HUNGER_MINIMUM_END_VALUE.value();
        }

        public void END_VALUE(float value) {
            instance.LOWER_HUNGER_MINIMUM.END_VALUE = value;
            LOWER_HUNGER_MINIMUM_END_VALUE.synchronizeWithBackingField();
        }

    }
    public final NO_STICK_SLOWNESS NO_STICK_SLOWNESS = new NO_STICK_SLOWNESS();
    public class NO_STICK_SLOWNESS implements GradualValue {
        public int MIN_AGE() {
            return NO_STICK_SLOWNESS_MIN_AGE.value();
        }

        public void MIN_AGE(int value) {
            instance.NO_STICK_SLOWNESS.MIN_AGE = value;
            NO_STICK_SLOWNESS_MIN_AGE.synchronizeWithBackingField();
        }

        public int MAX_AGE() {
            return NO_STICK_SLOWNESS_MAX_AGE.value();
        }

        public void MAX_AGE(int value) {
            instance.NO_STICK_SLOWNESS.MAX_AGE = value;
            NO_STICK_SLOWNESS_MAX_AGE.synchronizeWithBackingField();
        }

        public float START_VALUE() {
            return NO_STICK_SLOWNESS_START_VALUE.value();
        }

        public void START_VALUE(float value) {
            instance.NO_STICK_SLOWNESS.START_VALUE = value;
            NO_STICK_SLOWNESS_START_VALUE.synchronizeWithBackingField();
        }

        public float END_VALUE() {
            return NO_STICK_SLOWNESS_END_VALUE.value();
        }

        public void END_VALUE(float value) {
            instance.NO_STICK_SLOWNESS.END_VALUE = value;
            NO_STICK_SLOWNESS_END_VALUE.synchronizeWithBackingField();
        }

    }
    public final NO_GLASSES_BLURRINESS NO_GLASSES_BLURRINESS = new NO_GLASSES_BLURRINESS();
    public class NO_GLASSES_BLURRINESS implements GradualValue {
        public int MIN_AGE() {
            return NO_GLASSES_BLURRINESS_MIN_AGE.value();
        }

        public void MIN_AGE(int value) {
            instance.NO_GLASSES_BLURRINESS.MIN_AGE = value;
            NO_GLASSES_BLURRINESS_MIN_AGE.synchronizeWithBackingField();
        }

        public int MAX_AGE() {
            return NO_GLASSES_BLURRINESS_MAX_AGE.value();
        }

        public void MAX_AGE(int value) {
            instance.NO_GLASSES_BLURRINESS.MAX_AGE = value;
            NO_GLASSES_BLURRINESS_MAX_AGE.synchronizeWithBackingField();
        }

        public float START_VALUE() {
            return NO_GLASSES_BLURRINESS_START_VALUE.value();
        }

        public void START_VALUE(float value) {
            instance.NO_GLASSES_BLURRINESS.START_VALUE = value;
            NO_GLASSES_BLURRINESS_START_VALUE.synchronizeWithBackingField();
        }

        public float END_VALUE() {
            return NO_GLASSES_BLURRINESS_END_VALUE.value();
        }

        public void END_VALUE(float value) {
            instance.NO_GLASSES_BLURRINESS.END_VALUE = value;
            NO_GLASSES_BLURRINESS_END_VALUE.synchronizeWithBackingField();
        }

    }
    public int BASE_MAXIMUM_AGE() {
        return BASE_MAXIMUM_AGE.value();
    }

    public void BASE_MAXIMUM_AGE(int value) {
        instance.BASE_MAXIMUM_AGE = value;
        BASE_MAXIMUM_AGE.synchronizeWithBackingField();
    }

    public final EXTRA_LIFE EXTRA_LIFE = new EXTRA_LIFE();
    public class EXTRA_LIFE implements ExtraLife {
        public int START_AT_AGE() {
            return EXTRA_LIFE_START_AT_AGE.value();
        }

        public void START_AT_AGE(int value) {
            instance.EXTRA_LIFE.START_AT_AGE = value;
            EXTRA_LIFE_START_AT_AGE.synchronizeWithBackingField();
        }

        public float START_HOURS_PER_EXTRA_LIFE() {
            return EXTRA_LIFE_START_HOURS_PER_EXTRA_LIFE.value();
        }

        public void START_HOURS_PER_EXTRA_LIFE(float value) {
            instance.EXTRA_LIFE.START_HOURS_PER_EXTRA_LIFE = value;
            EXTRA_LIFE_START_HOURS_PER_EXTRA_LIFE.synchronizeWithBackingField();
        }

        public io.blodhgarm.personality.server.config.PersonalityConfigModel.Curve CURVE() {
            return EXTRA_LIFE_CURVE.value();
        }

        public void CURVE(io.blodhgarm.personality.server.config.PersonalityConfigModel.Curve value) {
            instance.EXTRA_LIFE.CURVE = value;
            EXTRA_LIFE_CURVE.synchronizeWithBackingField();
        }

    }
    public int MAX_EXTRA_YEARS_OF_LIFE() {
        return MAX_EXTRA_YEARS_OF_LIFE.value();
    }

    public void MAX_EXTRA_YEARS_OF_LIFE(int value) {
        instance.MAX_EXTRA_YEARS_OF_LIFE = value;
        MAX_EXTRA_YEARS_OF_LIFE.synchronizeWithBackingField();
    }

    public io.blodhgarm.personality.server.config.PersonalityConfigModel.ThemeMode THEME_MODE() {
        return THEME_MODE.value();
    }

    public void THEME_MODE(io.blodhgarm.personality.server.config.PersonalityConfigModel.ThemeMode value) {
        instance.THEME_MODE = value;
        THEME_MODE.synchronizeWithBackingField();
    }
    

    public interface GradualValue {
        int MIN_AGE();
        void MIN_AGE(int value);
        int MAX_AGE();
        void MAX_AGE(int value);
        float START_VALUE();
        void START_VALUE(float value);
        float END_VALUE();
        void END_VALUE(float value);
    }
    public interface ExtraLife {
        int START_AT_AGE();
        void START_AT_AGE(int value);
        float START_HOURS_PER_EXTRA_LIFE();
        void START_HOURS_PER_EXTRA_LIFE(float value);
        io.blodhgarm.personality.server.config.PersonalityConfigModel.Curve CURVE();
        void CURVE(io.blodhgarm.personality.server.config.PersonalityConfigModel.Curve value);
    }

}

