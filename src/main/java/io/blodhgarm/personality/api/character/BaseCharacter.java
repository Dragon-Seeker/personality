package io.blodhgarm.personality.api.character;

import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.addon.BaseAddon;
import io.blodhgarm.personality.api.reveal.KnownCharacter;
import io.blodhgarm.personality.misc.config.PersonalityConfig;
import io.blodhgarm.personality.utils.Constants;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static java.lang.Math.*;
import static java.lang.Math.log;

/**
 * Base Interface for Characters used in both {@link Character} & {@link KnownCharacter}.
 */
public interface BaseCharacter {

    enum Stage {YOUTH, PRIME, OLD}

    Map<String, KnownCharacter> getKnownCharacters();

    Map<Identifier, BaseAddon> getAddons();

    /**
     * Method used to grab from the characters internal AddonMap via an Identifier
     * @param addonId Addons Identifier
     * @return Addon ID linked to the character Addon or null
     */
    @Nullable
    default BaseAddon getAddon(Identifier addonId){ return getAddons().get(addonId); }

    /**
     * Method that will return the Characters UUID as a String
     */
    String getUUID();

    /**
     * Method that will return the Creator of the Characters UUID as a String
     */
    String getPlayerUUID();

    /**
     * Method that will return the Characters Name as a String
     */
    String getName();

    /**
     * Method that will return the Characters Name formatted with its various Aliases
     */
    Text getFormattedName();

    String getAlias();

    /**
     * Method that will return the Characters Gender
     */
    String getGender();

    /**
     * Method that will return the Physical Description of what the Character looks like
     */
    String getDescription();

    /**
     * Method that will return the Biography of the Characters Life and Backstory
     */
    String getBiography();

    /**
     * Method used to get the information pertaining to how old the character is
     */
    int getAge();

    boolean isDead();

    int getPlaytime();

    void beforeSaving();

    default String getInfo() {
        StringBuilder baseInfoText = new StringBuilder();

        baseInfoText.append(getName() + "§r\n"
                + "\n§lUUID§r: " + getUUID()
                + "\n§lGender§r: " + getGender()
                + "\n§lDescription§r: " + getDescription()
                + "\n§lBio§r: " + getBiography()
                + "\n§lAge§r: " + getAge() + " / " + getMaxAge() + " (" + getStage() + ")"
                + "\n§lPlaytime§r: " + (getPlaytime() / Constants.HOUR_IN_MILLISECONDS));

        this.getAddons().values().forEach((baseAddon) -> baseInfoText.append(baseAddon.getInfo()));

        return baseInfoText.toString();
    }

    default Stage getStage() {
        int age = getAge();
        return age < 25 ? Stage.YOUTH : age < 60 ? Stage.PRIME : Stage.OLD;
    }

    default int getMaxAge() {
        return PersonalityMod.CONFIG.defaultMaximumAge() + Math.min(PersonalityMod.CONFIG.maximumExtraAge(), getExtraAge());
    }

    default int getExtraAge() {
        PersonalityConfig.ExtraLife config = PersonalityMod.CONFIG.extraAgeConfiguration;
        double hoursPlayed = (float) getPlaytime() / Constants.HOUR_IN_MILLISECONDS;
        int extraYears = 0;

        if (hoursPlayed > config.minimumHoursForExtraLife() && getAge() > config.minAge()) {
            for (int i = 1; ; i++) {
                double hoursNeeded = config.minimumHoursForExtraLife() + config.multiplier() * switch (config.calculationCurve()) {
                    case NONE -> 0;
                    case LINEAR -> i - 1;
                    case QUADRATIC -> pow(i, 2) - 1;
                    case SQRT -> sqrt(i) - 1;
                    case EXPONENTIAL, EXPONENTIAL_EXTREME -> pow(E, i - 1) - 1;
                    case LOGARITHMIC, LOGARITHMIC_EXTREME -> log(i);
                };

                if (hoursPlayed < hoursNeeded) break;

                hoursPlayed -= hoursNeeded;
            }
        }

        return extraYears;
    }
}
