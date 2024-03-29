package io.blodhgarm.personality.api.reveal;

import net.minecraft.text.Text;

/**
 * Enum that currently holds the given Info Levels which are used
 * within {@link KnownCharacter} to store what info is available to
 * a given character
 */
public enum InfoLevel {
    UNDISCOVERED("undiscovered"),
    NONE("none"), //No Information shown
    GENERAL("general"), //DESCRIPTION, ALIAS(Unknown if such is real)
    ASSOCIATE("associate"), //GENDER, AGE
    TRUSTED("trusted"), //NAME
    CONFIDANT("confidant"); //BIOGRAPHY

    public static final InfoLevel[] VALID_VALUES = new InfoLevel[]{
            NONE,
            GENERAL,
            ASSOCIATE,
            TRUSTED,
            CONFIDANT
    };

    private final String name;

    InfoLevel(String name){
        this.name = name;
    }

    public Text getTranslation(){
        return Text.translatable("personality.info." + this.name);
    }

    public boolean shouldUpdateLevel(InfoLevel level){
        return shouldUpdateLevel(level, false);
    }

    public boolean shouldUpdateLevel(InfoLevel level, boolean allowDowngrading){
        return allowDowngrading || this.ordinal() < level.ordinal();
    }

}
