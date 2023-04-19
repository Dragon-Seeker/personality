package io.blodhgarm.personality.utils;

import com.mojang.authlib.GameProfile;
import net.minecraft.util.Formatting;

public class Constants {

    public static final int WEEK_IN_MILLISECONDS = 604_800_000;
    public static final int HOUR_IN_MILLISECONDS =   3_600_000;

    public static final GameProfile ERROR_PROFILE = new GameProfile(null, "ERROR");

    public static Formatting CHARACTER_FORMATTING;
}
