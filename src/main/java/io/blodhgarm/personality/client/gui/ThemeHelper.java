package io.blodhgarm.personality.client.gui;

import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.misc.config.PersonalityConfigModel;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Formatting;

public class ThemeHelper {

    public static boolean isDarkMode(){
        if(PersonalityMod.CONFIG.themeMode() == PersonalityConfigModel.ThemeMode.SYSTEM){
            return PersonalityMod.detector.isDark();
        }

        return PersonalityMod.CONFIG.themeMode() == PersonalityConfigModel.ThemeMode.DARK_MODE;
    }

    public static Surface dynamicSurface(){
        return isDarkMode() ? Surface.DARK_PANEL : Surface.PANEL;
    }

    public static Color dynamicColor(){
        return isDarkMode() ? Color.WHITE : Color.BLACK;
    }

    public static Formatting dynamicTextColor(){
        return isDarkMode() ? Formatting.WHITE : Formatting.BLACK;
    }

    public static boolean guiScale4OrAbove(){
        return MinecraftClient.getInstance().options.getGuiScale().getValue() >= 4 || MinecraftClient.getInstance().options.getGuiScale().getValue() == 0;
    }
}
