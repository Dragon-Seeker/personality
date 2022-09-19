package io.blodhgarm.personality.server.config;

import io.blodhgarm.personality.Character;
import io.blodhgarm.personality.server.config.PersonalityConfig.GradualValue;

import static io.blodhgarm.personality.PersonalityMod.CONFIG;
import static java.lang.Math.*;

public class ConfigHelper {

//    public static float apply(GradualValue config, Character character) {
//        float percentageInAgeRange = (character.getPreciseAge() - config.MIN_AGE()) / (maxAge(config) - config.MIN_AGE());
//        float valueRange = config.END_VALUE() - config.START_VALUE();
//
//        return config.START_VALUE() + percentageInAgeRange*valueRange;
//    }

    public static float apply(GradualValue config, Character character) {
        double a = calculateCurveModifier(config);
        double x = character.getPreciseAge() - config.MIN_AGE();
        float m = config.START_VALUE();

        return (float) switch (config.CURVE()) {
            case NONE -> 0;
            case LINEAR -> a*x;
            case QUADRATIC -> a*pow(x,2);
            case SQRT -> a*sqrt(x);
            case EXPONENTIAL -> pow(E, a*x - 1);
            case LOGARITHMIC -> a*log(x) + 1;
            case EXPONENTIAL_EXTREME -> a*pow(E,x);
            case LOGARITHMIC_EXTREME -> log(a*x+1);
        } + m;

    }

    public static boolean shouldApply(GradualValue config, Character character) {
        if (character == null)
            return false;
        int age = character.getAge();
        return age >= config.MIN_AGE() && age <= maxAge(config);
    }

    private static int maxAge(GradualValue config) {
        if (config.MAX_AGE() == Integer.MAX_VALUE)
            return CONFIG.BASE_MAXIMUM_AGE() + CONFIG.MAX_EXTRA_YEARS_OF_LIFE();
        return config.MAX_AGE();

    }

    public static double calculateCurveModifier(GradualValue c) {
        double ageRange = c.MAX_AGE() - c.MIN_AGE();
        double valueRange = c.END_VALUE() - c.START_VALUE();

        return switch (c.CURVE()) {
            case NONE -> 0;
            case LINEAR -> valueRange / ageRange;
            case QUADRATIC -> valueRange / pow(ageRange, 2);
            case SQRT -> valueRange / sqrt(ageRange);
            case EXPONENTIAL -> log( valueRange + 1 ) / ageRange;
            case LOGARITHMIC -> valueRange / log(ageRange + 1);
            case EXPONENTIAL_EXTREME -> valueRange / pow(E, ageRange);
            case LOGARITHMIC_EXTREME -> (pow(E, valueRange) - 1) / ageRange;
        };

    }

}
