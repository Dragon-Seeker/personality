package io.wispforest.personality;

import io.wispforest.personality.server.config.PersonalityConfig;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class PersonalityMod {

    public static final PersonalityConfig CONFIG = PersonalityConfig.createAndLoad();

    public static final String MODID = "personality";

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }


    public static final TagKey<Item> VISION_GLASSES = TagKey.of(Registry.ITEM_KEY, id("vision_glasses"));
    public static final TagKey<Item> WALKING_STICKS = TagKey.of(Registry.ITEM_KEY, id("walking_sticks"));

}
