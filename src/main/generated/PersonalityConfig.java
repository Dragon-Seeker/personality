package io.blodhgarm.personality.misc.config;

import io.wispforest.owo.config.ConfigWrapper;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.util.Observable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class PersonalityConfig extends ConfigWrapper<io.blodhgarm.personality.misc.config.PersonalityConfigModel> {

    private final Option<java.lang.Integer> defaultMaximumAge = this.optionForKey(new Option.Key("defaultMaximumAge"));
    private final Option<java.lang.Integer> fasterExhaustion_minAge = this.optionForKey(new Option.Key("fasterExhaustion.minAge"));
    private final Option<java.lang.Integer> fasterExhaustion_maxAge = this.optionForKey(new Option.Key("fasterExhaustion.maxAge"));
    private final Option<java.lang.Float> fasterExhaustion_startingValue = this.optionForKey(new Option.Key("fasterExhaustion.startingValue"));
    private final Option<java.lang.Float> fasterExhaustion_endingValue = this.optionForKey(new Option.Key("fasterExhaustion.endingValue"));
    private final Option<io.blodhgarm.personality.misc.config.PersonalityConfigModel.Curve> fasterExhaustion_calculationCurve = this.optionForKey(new Option.Key("fasterExhaustion.calculationCurve"));
    private final Option<java.lang.Integer> fasterHealing_minAge = this.optionForKey(new Option.Key("fasterHealing.minAge"));
    private final Option<java.lang.Integer> fasterHealing_maxAge = this.optionForKey(new Option.Key("fasterHealing.maxAge"));
    private final Option<java.lang.Float> fasterHealing_startingValue = this.optionForKey(new Option.Key("fasterHealing.startingValue"));
    private final Option<java.lang.Float> fasterHealing_endingValue = this.optionForKey(new Option.Key("fasterHealing.endingValue"));
    private final Option<io.blodhgarm.personality.misc.config.PersonalityConfigModel.Curve> fasterHealing_calculationCurve = this.optionForKey(new Option.Key("fasterHealing.calculationCurve"));
    private final Option<java.lang.Integer> minimumHungerToHeal_minAge = this.optionForKey(new Option.Key("minimumHungerToHeal.minAge"));
    private final Option<java.lang.Integer> minimumHungerToHeal_maxAge = this.optionForKey(new Option.Key("minimumHungerToHeal.maxAge"));
    private final Option<java.lang.Float> minimumHungerToHeal_startingValue = this.optionForKey(new Option.Key("minimumHungerToHeal.startingValue"));
    private final Option<java.lang.Float> minimumHungerToHeal_endingValue = this.optionForKey(new Option.Key("minimumHungerToHeal.endingValue"));
    private final Option<io.blodhgarm.personality.misc.config.PersonalityConfigModel.Curve> minimumHungerToHeal_calculationCurve = this.optionForKey(new Option.Key("minimumHungerToHeal.calculationCurve"));
    private final Option<java.lang.Integer> agingSlowness_minAge = this.optionForKey(new Option.Key("agingSlowness.minAge"));
    private final Option<java.lang.Integer> agingSlowness_maxAge = this.optionForKey(new Option.Key("agingSlowness.maxAge"));
    private final Option<java.lang.Float> agingSlowness_startingValue = this.optionForKey(new Option.Key("agingSlowness.startingValue"));
    private final Option<java.lang.Float> agingSlowness_endingValue = this.optionForKey(new Option.Key("agingSlowness.endingValue"));
    private final Option<io.blodhgarm.personality.misc.config.PersonalityConfigModel.Curve> agingSlowness_calculationCurve = this.optionForKey(new Option.Key("agingSlowness.calculationCurve"));
    private final Option<java.lang.Integer> agingBlurriness_minAge = this.optionForKey(new Option.Key("agingBlurriness.minAge"));
    private final Option<java.lang.Integer> agingBlurriness_maxAge = this.optionForKey(new Option.Key("agingBlurriness.maxAge"));
    private final Option<java.lang.Float> agingBlurriness_startingValue = this.optionForKey(new Option.Key("agingBlurriness.startingValue"));
    private final Option<java.lang.Float> agingBlurriness_endingValue = this.optionForKey(new Option.Key("agingBlurriness.endingValue"));
    private final Option<io.blodhgarm.personality.misc.config.PersonalityConfigModel.Curve> agingBlurriness_calculationCurve = this.optionForKey(new Option.Key("agingBlurriness.calculationCurve"));
    private final Option<java.lang.Integer> extraAgeConfiguration_minAge = this.optionForKey(new Option.Key("extraAgeConfiguration.minAge"));
    private final Option<java.lang.Integer> extraAgeConfiguration_maxAge = this.optionForKey(new Option.Key("extraAgeConfiguration.maxAge"));
    private final Option<java.lang.Float> extraAgeConfiguration_minimumHoursForExtraLife = this.optionForKey(new Option.Key("extraAgeConfiguration.minimumHoursForExtraLife"));
    private final Option<java.lang.Float> extraAgeConfiguration_multiplier = this.optionForKey(new Option.Key("extraAgeConfiguration.multiplier"));
    private final Option<io.blodhgarm.personality.misc.config.PersonalityConfigModel.Curve> extraAgeConfiguration_calculationCurve = this.optionForKey(new Option.Key("extraAgeConfiguration.calculationCurve"));
    private final Option<java.lang.Integer> maximumExtraAge = this.optionForKey(new Option.Key("maximumExtraAge"));
    private final Option<java.lang.Integer> characterDeathWindow = this.optionForKey(new Option.Key("characterDeathWindow"));
    private final Option<java.lang.Boolean> killPlayerOnCharacterDeath = this.optionForKey(new Option.Key("killPlayerOnCharacterDeath"));
    private final Option<io.blodhgarm.personality.api.reveal.InfoLevel> minimumBaseInfo = this.optionForKey(new Option.Key("minimumBaseInfo"));
    private final Option<io.blodhgarm.personality.api.reveal.InfoLevel> minimumRevealInfo = this.optionForKey(new Option.Key("minimumRevealInfo"));
    private final Option<io.blodhgarm.personality.api.reveal.InfoLevel> minimumDiscoveryInfo = this.optionForKey(new Option.Key("minimumDiscoveryInfo"));
    private final Option<java.util.List<java.lang.String>> moderationList = this.optionForKey(new Option.Key("moderationList"));
    private final Option<java.util.List<java.lang.String>> administrationList = this.optionForKey(new Option.Key("administrationList"));
    private final Option<java.lang.Boolean> adjustWidthAndHeightOnly = this.optionForKey(new Option.Key("adjustWidthAndHeightOnly"));
    private final Option<io.blodhgarm.personality.misc.config.PersonalityConfigModel.ThemeMode> themeMode = this.optionForKey(new Option.Key("themeMode"));
    private final Option<java.lang.Boolean> showPlayerNameInChat = this.optionForKey(new Option.Key("showPlayerNameInChat"));
    private final Option<java.lang.Boolean> showPlayerNameInNamePlate = this.optionForKey(new Option.Key("showPlayerNameInNamePlate"));
    private final Option<java.lang.Boolean> showPlayerNamePlateAtChestLevel = this.optionForKey(new Option.Key("showPlayerNamePlateAtChestLevel"));
    private final Option<java.lang.Boolean> autoDiscovery = this.optionForKey(new Option.Key("autoDiscovery"));
    private final Option<java.lang.Boolean> descriptionConfig_descriptionView = this.optionForKey(new Option.Key("descriptionConfig.descriptionView"));
    private final Option<io.blodhgarm.personality.misc.config.PersonalityConfigModel.ControlType> descriptionConfig_descriptionKeybindingControl = this.optionForKey(new Option.Key("descriptionConfig.descriptionKeybindingControl"));
    private final Option<java.lang.Boolean> descriptionConfig_automaticScrolling = this.optionForKey(new Option.Key("descriptionConfig.automaticScrolling"));

    private PersonalityConfig() {
        super(io.blodhgarm.personality.misc.config.PersonalityConfigModel.class);
    }

    public static PersonalityConfig createAndLoad() {
        var wrapper = new PersonalityConfig();
        wrapper.load();
        return wrapper;
    }

    public int defaultMaximumAge() {
        return defaultMaximumAge.value();
    }

    public void defaultMaximumAge(int value) {
        defaultMaximumAge.set(value);
    }

    public final FasterExhaustion fasterExhaustion = new FasterExhaustion();
    public class FasterExhaustion implements GradualValue {
        public int minAge() {
            return fasterExhaustion_minAge.value();
        }

        public void minAge(int value) {
            fasterExhaustion_minAge.set(value);
        }

        public int maxAge() {
            return fasterExhaustion_maxAge.value();
        }

        public void maxAge(int value) {
            fasterExhaustion_maxAge.set(value);
        }

        public float startingValue() {
            return fasterExhaustion_startingValue.value();
        }

        public void startingValue(float value) {
            fasterExhaustion_startingValue.set(value);
        }

        public float endingValue() {
            return fasterExhaustion_endingValue.value();
        }

        public void endingValue(float value) {
            fasterExhaustion_endingValue.set(value);
        }

        public io.blodhgarm.personality.misc.config.PersonalityConfigModel.Curve calculationCurve() {
            return fasterExhaustion_calculationCurve.value();
        }

        public void calculationCurve(io.blodhgarm.personality.misc.config.PersonalityConfigModel.Curve value) {
            fasterExhaustion_calculationCurve.set(value);
        }

    }
    public final FasterHealing fasterHealing = new FasterHealing();
    public class FasterHealing implements GradualValue {
        public int minAge() {
            return fasterHealing_minAge.value();
        }

        public void minAge(int value) {
            fasterHealing_minAge.set(value);
        }

        public int maxAge() {
            return fasterHealing_maxAge.value();
        }

        public void maxAge(int value) {
            fasterHealing_maxAge.set(value);
        }

        public float startingValue() {
            return fasterHealing_startingValue.value();
        }

        public void startingValue(float value) {
            fasterHealing_startingValue.set(value);
        }

        public float endingValue() {
            return fasterHealing_endingValue.value();
        }

        public void endingValue(float value) {
            fasterHealing_endingValue.set(value);
        }

        public io.blodhgarm.personality.misc.config.PersonalityConfigModel.Curve calculationCurve() {
            return fasterHealing_calculationCurve.value();
        }

        public void calculationCurve(io.blodhgarm.personality.misc.config.PersonalityConfigModel.Curve value) {
            fasterHealing_calculationCurve.set(value);
        }

    }
    public final MinimumHungerToHeal minimumHungerToHeal = new MinimumHungerToHeal();
    public class MinimumHungerToHeal implements GradualValue {
        public int minAge() {
            return minimumHungerToHeal_minAge.value();
        }

        public void minAge(int value) {
            minimumHungerToHeal_minAge.set(value);
        }

        public int maxAge() {
            return minimumHungerToHeal_maxAge.value();
        }

        public void maxAge(int value) {
            minimumHungerToHeal_maxAge.set(value);
        }

        public float startingValue() {
            return minimumHungerToHeal_startingValue.value();
        }

        public void startingValue(float value) {
            minimumHungerToHeal_startingValue.set(value);
        }

        public float endingValue() {
            return minimumHungerToHeal_endingValue.value();
        }

        public void endingValue(float value) {
            minimumHungerToHeal_endingValue.set(value);
        }

        public io.blodhgarm.personality.misc.config.PersonalityConfigModel.Curve calculationCurve() {
            return minimumHungerToHeal_calculationCurve.value();
        }

        public void calculationCurve(io.blodhgarm.personality.misc.config.PersonalityConfigModel.Curve value) {
            minimumHungerToHeal_calculationCurve.set(value);
        }

    }
    public final AgingSlowness agingSlowness = new AgingSlowness();
    public class AgingSlowness implements GradualValue {
        public int minAge() {
            return agingSlowness_minAge.value();
        }

        public void minAge(int value) {
            agingSlowness_minAge.set(value);
        }

        public int maxAge() {
            return agingSlowness_maxAge.value();
        }

        public void maxAge(int value) {
            agingSlowness_maxAge.set(value);
        }

        public float startingValue() {
            return agingSlowness_startingValue.value();
        }

        public void startingValue(float value) {
            agingSlowness_startingValue.set(value);
        }

        public float endingValue() {
            return agingSlowness_endingValue.value();
        }

        public void endingValue(float value) {
            agingSlowness_endingValue.set(value);
        }

        public io.blodhgarm.personality.misc.config.PersonalityConfigModel.Curve calculationCurve() {
            return agingSlowness_calculationCurve.value();
        }

        public void calculationCurve(io.blodhgarm.personality.misc.config.PersonalityConfigModel.Curve value) {
            agingSlowness_calculationCurve.set(value);
        }

    }
    public final AgingBlurriness agingBlurriness = new AgingBlurriness();
    public class AgingBlurriness implements GradualValue {
        public int minAge() {
            return agingBlurriness_minAge.value();
        }

        public void minAge(int value) {
            agingBlurriness_minAge.set(value);
        }

        public int maxAge() {
            return agingBlurriness_maxAge.value();
        }

        public void maxAge(int value) {
            agingBlurriness_maxAge.set(value);
        }

        public float startingValue() {
            return agingBlurriness_startingValue.value();
        }

        public void startingValue(float value) {
            agingBlurriness_startingValue.set(value);
        }

        public float endingValue() {
            return agingBlurriness_endingValue.value();
        }

        public void endingValue(float value) {
            agingBlurriness_endingValue.set(value);
        }

        public io.blodhgarm.personality.misc.config.PersonalityConfigModel.Curve calculationCurve() {
            return agingBlurriness_calculationCurve.value();
        }

        public void calculationCurve(io.blodhgarm.personality.misc.config.PersonalityConfigModel.Curve value) {
            agingBlurriness_calculationCurve.set(value);
        }

    }
    public final ExtraAgeConfiguration extraAgeConfiguration = new ExtraAgeConfiguration();
    public class ExtraAgeConfiguration implements ExtraLife {
        public int minAge() {
            return extraAgeConfiguration_minAge.value();
        }

        public void minAge(int value) {
            extraAgeConfiguration_minAge.set(value);
        }

        public int maxAge() {
            return extraAgeConfiguration_maxAge.value();
        }

        public void maxAge(int value) {
            extraAgeConfiguration_maxAge.set(value);
        }

        public float minimumHoursForExtraLife() {
            return extraAgeConfiguration_minimumHoursForExtraLife.value();
        }

        public void minimumHoursForExtraLife(float value) {
            extraAgeConfiguration_minimumHoursForExtraLife.set(value);
        }

        public float multiplier() {
            return extraAgeConfiguration_multiplier.value();
        }

        public void multiplier(float value) {
            extraAgeConfiguration_multiplier.set(value);
        }

        public io.blodhgarm.personality.misc.config.PersonalityConfigModel.Curve calculationCurve() {
            return extraAgeConfiguration_calculationCurve.value();
        }

        public void calculationCurve(io.blodhgarm.personality.misc.config.PersonalityConfigModel.Curve value) {
            extraAgeConfiguration_calculationCurve.set(value);
        }

    }
    public int maximumExtraAge() {
        return maximumExtraAge.value();
    }

    public void maximumExtraAge(int value) {
        maximumExtraAge.set(value);
    }

    public int characterDeathWindow() {
        return characterDeathWindow.value();
    }

    public void characterDeathWindow(int value) {
        characterDeathWindow.set(value);
    }

    public boolean killPlayerOnCharacterDeath() {
        return killPlayerOnCharacterDeath.value();
    }

    public void killPlayerOnCharacterDeath(boolean value) {
        killPlayerOnCharacterDeath.set(value);
    }

    public io.blodhgarm.personality.api.reveal.InfoLevel minimumBaseInfo() {
        return minimumBaseInfo.value();
    }

    public void minimumBaseInfo(io.blodhgarm.personality.api.reveal.InfoLevel value) {
        minimumBaseInfo.set(value);
    }

    public io.blodhgarm.personality.api.reveal.InfoLevel minimumRevealInfo() {
        return minimumRevealInfo.value();
    }

    public void minimumRevealInfo(io.blodhgarm.personality.api.reveal.InfoLevel value) {
        minimumRevealInfo.set(value);
    }

    public io.blodhgarm.personality.api.reveal.InfoLevel minimumDiscoveryInfo() {
        return minimumDiscoveryInfo.value();
    }

    public void minimumDiscoveryInfo(io.blodhgarm.personality.api.reveal.InfoLevel value) {
        minimumDiscoveryInfo.set(value);
    }

    public java.util.List<java.lang.String> moderationList() {
        return moderationList.value();
    }

    public void moderationList(java.util.List<java.lang.String> value) {
        moderationList.set(value);
    }

    public java.util.List<java.lang.String> administrationList() {
        return administrationList.value();
    }

    public void administrationList(java.util.List<java.lang.String> value) {
        administrationList.set(value);
    }

    public boolean adjustWidthAndHeightOnly() {
        return adjustWidthAndHeightOnly.value();
    }

    public void adjustWidthAndHeightOnly(boolean value) {
        adjustWidthAndHeightOnly.set(value);
    }

    public void subscribeToAdjustWidthAndHeightOnly(Consumer<java.lang.Boolean> subscriber) {
        adjustWidthAndHeightOnly.observe(subscriber);
    }

    public io.blodhgarm.personality.misc.config.PersonalityConfigModel.ThemeMode themeMode() {
        return themeMode.value();
    }

    public void themeMode(io.blodhgarm.personality.misc.config.PersonalityConfigModel.ThemeMode value) {
        themeMode.set(value);
    }

    public boolean showPlayerNameInChat() {
        return showPlayerNameInChat.value();
    }

    public void showPlayerNameInChat(boolean value) {
        showPlayerNameInChat.set(value);
    }

    public boolean showPlayerNameInNamePlate() {
        return showPlayerNameInNamePlate.value();
    }

    public void showPlayerNameInNamePlate(boolean value) {
        showPlayerNameInNamePlate.set(value);
    }

    public boolean showPlayerNamePlateAtChestLevel() {
        return showPlayerNamePlateAtChestLevel.value();
    }

    public void showPlayerNamePlateAtChestLevel(boolean value) {
        showPlayerNamePlateAtChestLevel.set(value);
    }

    public boolean autoDiscovery() {
        return autoDiscovery.value();
    }

    public void autoDiscovery(boolean value) {
        autoDiscovery.set(value);
    }

    public final DescriptionConfig descriptionConfig = new DescriptionConfig();
    public class DescriptionConfig implements DescriptionViewConfig {
        public boolean descriptionView() {
            return descriptionConfig_descriptionView.value();
        }

        public void descriptionView(boolean value) {
            descriptionConfig_descriptionView.set(value);
        }

        public io.blodhgarm.personality.misc.config.PersonalityConfigModel.ControlType descriptionKeybindingControl() {
            return descriptionConfig_descriptionKeybindingControl.value();
        }

        public void descriptionKeybindingControl(io.blodhgarm.personality.misc.config.PersonalityConfigModel.ControlType value) {
            descriptionConfig_descriptionKeybindingControl.set(value);
        }

        public boolean automaticScrolling() {
            return descriptionConfig_automaticScrolling.value();
        }

        public void automaticScrolling(boolean value) {
            descriptionConfig_automaticScrolling.set(value);
        }

    }

    public interface DescriptionViewConfig {
        boolean descriptionView();
        void descriptionView(boolean value);
        io.blodhgarm.personality.misc.config.PersonalityConfigModel.ControlType descriptionKeybindingControl();
        void descriptionKeybindingControl(io.blodhgarm.personality.misc.config.PersonalityConfigModel.ControlType value);
        boolean automaticScrolling();
        void automaticScrolling(boolean value);
    }
    public interface GradualValue {
        int minAge();
        void minAge(int value);
        int maxAge();
        void maxAge(int value);
        float startingValue();
        void startingValue(float value);
        float endingValue();
        void endingValue(float value);
        io.blodhgarm.personality.misc.config.PersonalityConfigModel.Curve calculationCurve();
        void calculationCurve(io.blodhgarm.personality.misc.config.PersonalityConfigModel.Curve value);
    }
    public interface ExtraLife {
        int minAge();
        void minAge(int value);
        int maxAge();
        void maxAge(int value);
        float minimumHoursForExtraLife();
        void minimumHoursForExtraLife(float value);
        float multiplier();
        void multiplier(float value);
        io.blodhgarm.personality.misc.config.PersonalityConfigModel.Curve calculationCurve();
        void calculationCurve(io.blodhgarm.personality.misc.config.PersonalityConfigModel.Curve value);
    }

}

