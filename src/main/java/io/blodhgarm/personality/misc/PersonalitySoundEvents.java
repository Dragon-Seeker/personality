package io.blodhgarm.personality.misc;

import io.blodhgarm.personality.PersonalityMod;
import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.registry.Registry;

public class PersonalitySoundEvents implements AutoRegistryContainer<SoundEvent> {

    public static final SoundEvent ITEM_GLASSES_EQUIP = new SoundEvent(PersonalityMod.id("glasses_equipped"));

    @Override
    public Registry<SoundEvent> getRegistry() {
        return Registry.SOUND_EVENT;
    }

    @Override
    public Class<SoundEvent> getTargetFieldType() {
        return SoundEvent.class;
    }
}
