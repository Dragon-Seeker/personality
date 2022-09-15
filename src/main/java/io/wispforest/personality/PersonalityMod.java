package io.wispforest.personality;

import io.wispforest.personality.server.config.PersonalityConfig;
import net.minecraft.util.Identifier;

public class PersonalityMod {

    public static final PersonalityConfig CONFIG = PersonalityConfig.createAndLoad();

    public static final String MODID = "personality";

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }


}
