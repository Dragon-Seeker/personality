package io.blodhgarm.personality.api.reveal;

import net.minecraft.text.Text;

public enum InfoRevealLevel {
    NONE("none"), //No Information shown
    GENERAL("general"), //DESCRIPTION, ALIAS(Unknown if such is real)
    ASSOCIATE("associate"), //GENDER, AGE
    TRUSTED("trusted"), //BIOGRAPHY
    CONFIDANT("confidant"); //NAME

    private final String name;

    InfoRevealLevel(String name){
        this.name = name;
    }

    public Text getTranslation(){
        return Text.translatable("personality.info." + this.name);
    }

}
