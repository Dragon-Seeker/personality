package io.blodhgarm.personality.misc;

import io.blodhgarm.personality.PersonalityMod;
import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;

public class PersonalitySoundEvents implements AutoRegistryContainer<SoundEvent> {

    public static final SoundEvent ITEM_GLASSES_EQUIP = SoundEvent.of(PersonalityMod.id("glasses_equipped"));

    @Override
    public Registry<SoundEvent> getRegistry() {
        return Registries.SOUND_EVENT;
    }

    @Override
    public Class<SoundEvent> getTargetFieldType() {
        return SoundEvent.class;
    }
}
