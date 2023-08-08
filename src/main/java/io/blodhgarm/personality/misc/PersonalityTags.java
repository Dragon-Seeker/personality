package io.blodhgarm.personality.misc;

import io.blodhgarm.personality.PersonalityMod;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class PersonalityTags {

    public static class Items {
        public static final TagKey<Item> VISION_GLASSES = TagKey.of(RegistryKeys.ITEM, PersonalityMod.id("vision_glasses"));
        public static final TagKey<Item> WALKING_STICKS = TagKey.of(RegistryKeys.ITEM, PersonalityMod.id("walking_sticks"));

        public static final TagKey<Item> OBSCURES_IDENTITY = TagKey.of(RegistryKeys.ITEM, PersonalityMod.id("obscures_identity"));
    }

    public static class StatusEffects {
        public static final TagKey<StatusEffect> REVEAL_BLINDING_EFFECTS = TagKey.of(RegistryKeys.STATUS_EFFECT, PersonalityMod.id("reveal_blinding_effects"));
        public static final TagKey<StatusEffect> OBSCURING_EFFECTS = TagKey.of(RegistryKeys.STATUS_EFFECT, PersonalityMod.id("obscuring_effects"));
    }
}
