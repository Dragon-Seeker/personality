package io.blodhgarm.personality.misc.config;

import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.misc.config.PersonalityConfig.GradualValue;

import static io.blodhgarm.personality.PersonalityMod.CONFIG;
import static java.lang.Math.*;

public class ConfigHelper {

//    public static float apply(GradualValue config, Character character) {
//        float percentageInAgeRange = (character.getPreciseAge() - config.MIN_AGE()) / (maxAge(config) - config.MIN_AGE());
//        float valueRange = config.END_VALUE() - config.START_VALUE();
//
//        return config.START_VALUE() + percentageInAgeRange*valueRange;
//    }

    public static float apply(io.blodhgarm.personality.misc.config.PersonalityConfig.GradualValue config, Character character) {
        double a = calculateCurveModifier(config);
        double x = character.getPreciseAge() - config.minAge();
        float m = config.startingValue();

        return (float) switch (config.calculationCurve()) {
            case NONE -> 0;
            case LINEAR -> a*x;
            case QUADRATIC -> a*pow(x,2);
            case SQRT -> a*sqrt(x);
            case EXPONENTIAL -> pow(E, a*x - 1);
            case LOGARITHMIC -> a*log(x) + 1;
            case EXPONENTIAL_EXTREME -> (pow(E,x) - 1)/a;
            case LOGARITHMIC_EXTREME -> log(a*x+1);
        } + m;

    }

    public static boolean shouldApply(GradualValue config, Character character) {
        if (character == null) return false;

        int age = character.getAge();

        return age >= config.minAge() && age <= maxAge(config);
    }

    private static int maxAge(GradualValue config) {
        return (config.maxAge() == Integer.MAX_VALUE)
            ? CONFIG.defaultMaximumAge() + CONFIG.defaultMaximumAge()
            : config.maxAge();

    }

    public static double calculateCurveModifier(GradualValue c) {
        double ageRange = c.maxAge() - c.minAge();
        double valueRange = c.endingValue() - c.startingValue();

        return switch (c.calculationCurve()) {
            case NONE -> 0;
            case LINEAR -> valueRange / ageRange;
            case QUADRATIC -> valueRange / pow(ageRange, 2);
            case SQRT -> valueRange / sqrt(ageRange);
            case EXPONENTIAL -> log( valueRange + 1 ) / ageRange;
            case LOGARITHMIC -> valueRange / log(ageRange + 1);
            case EXPONENTIAL_EXTREME -> (pow(E, ageRange) - 1)/ valueRange;
            case LOGARITHMIC_EXTREME -> (pow(E, valueRange) - 1) / ageRange;
        };

    }

}
