package io.wispforest.personality;

import net.minecraft.util.Identifier;

public class PersonalityMod {

    public static final String MODID = "personality";

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

}
