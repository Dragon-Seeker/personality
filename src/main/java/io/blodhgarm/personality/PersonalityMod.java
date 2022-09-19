package io.blodhgarm.personality;

import com.jthemedetecor.OsThemeDetector;
import io.blodhgarm.personality.server.config.PersonalityConfig;
import io.blodhgarm.personality.server.config.PersonalityConfigModel;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class PersonalityMod {

    public static final PersonalityConfig CONFIG = PersonalityConfig.createAndLoad();
    public static final OsThemeDetector detector = OsThemeDetector.getDetector();

    public static final String MODID = "personality";

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    public static final TagKey<Item> VISION_GLASSES = TagKey.of(Registry.ITEM_KEY, id("vision_glasses"));
    public static final TagKey<Item> WALKING_STICKS = TagKey.of(Registry.ITEM_KEY, id("walking_sticks"));
    public static final TagKey<Item> OBSCURES_IDENTITY = TagKey.of(Registry.ITEM_KEY, id("obscures_identity"));

    public static boolean isDarkMode(){
        if(CONFIG.THEME_MODE() == PersonalityConfigModel.ThemeMode.SYSTEM){
            return detector.isDark();
        }

        return CONFIG.THEME_MODE() == PersonalityConfigModel.ThemeMode.DARK_MODE;
    }

    public static float getGradualValue(PersonalityConfig.GradualValue config, Character character) {
        float percentageInAgeRange = (character.getPreciseAge() - config.MIN_AGE()) / (config.MAX_AGE() - config.MIN_AGE());
        float valueRange = config.END_VALUE() - config.START_VALUE();

        return config.START_VALUE() + percentageInAgeRange*valueRange;
    }

    public static boolean shouldGradualValue(PersonalityConfig.GradualValue config, Character character) {
        int age = character.getAge();
        return age >= config.MIN_AGE() && age <= config.MAX_AGE();
    }

}
