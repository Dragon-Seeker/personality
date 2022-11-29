package io.blodhgarm.personality.client;

import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.misc.config.PersonalityConfigModel;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.client.MinecraftClient;

public class ThemeHelper {

    public static boolean isDarkMode(){
        if(PersonalityMod.CONFIG.THEME_MODE() == PersonalityConfigModel.ThemeMode.SYSTEM){
            return PersonalityMod.detector.isDark();
        }

        return PersonalityMod.CONFIG.THEME_MODE() == PersonalityConfigModel.ThemeMode.DARK_MODE;
    }

    public static Surface dynamicSurface(){
        return isDarkMode() ? Surface.DARK_PANEL : Surface.PANEL;
    }

    public static Color dynamicColor(){
        return isDarkMode() ? Color.WHITE : Color.BLACK;
    }

    public static boolean guiScale4OrAbove(){
        return MinecraftClient.getInstance().options.getGuiScale().getValue() >= 4 || MinecraftClient.getInstance().options.getGuiScale().getValue() == 0;
    }
}
