package io.blodhgarm.personality.misc;

import io.blodhgarm.personality.PersonalityMod;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.Registry;

public class PersonalityTags {

    public static class Items {
        public static final TagKey<Item> VISION_GLASSES = TagKey.of(Registry.ITEM_KEY, PersonalityMod.id("vision_glasses"));
        public static final TagKey<Item> WALKING_STICKS = TagKey.of(Registry.ITEM_KEY, PersonalityMod.id("walking_sticks"));

        public static final TagKey<Item> OBSCURES_IDENTITY = TagKey.of(Registry.ITEM_KEY, PersonalityMod.id("obscures_identity"));
    }

    public static class StatusEffects {
        public static final TagKey<StatusEffect> REVEAL_BLINDING_EFFECTS = TagKey.of(Registry.STATUS_EFFECT.getKey(), PersonalityMod.id("reveal_blinding_effects"));
        public static final TagKey<StatusEffect> OBSCURING_EFFECTS = TagKey.of(Registry.STATUS_EFFECT.getKey(), PersonalityMod.id("obscuring_effects"));
    }
}
