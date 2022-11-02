package io.blodhgarm.personality.impl;

import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.misc.PersonalityTags;
import io.blodhgarm.personality.misc.config.ConfigHelper;
import io.blodhgarm.personality.misc.config.PersonalityConfig;
import io.github.apace100.calio.mixin.DamageSourceAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.UUID;

public class CharacterTick implements ServerTickEvents.EndWorldTick {

    public static final DamageSource DEATH_BY_OLD_AGE = DamageSourceAccessor.createDamageSource("oldAge");
    private static final UUID NO_STICK_SLOWNESS = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278F");

    @Override
    public void onEndTick(ServerWorld world) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            Character c = ServerCharacters.INSTANCE.getCharacter(player);
            if (c == null)
                return;

            // Kill Too Old Characters
            if (c.getAge() >= c.getMaxAge()) {
                ServerCharacters.INSTANCE.killCharacter(c);
                player.damage(DEATH_BY_OLD_AGE, Float.MAX_VALUE);
                continue;
            }

            // Apply Slowness to Old Characters without a stick
            PersonalityConfig.GradualValue config = PersonalityMod.CONFIG.NO_STICK_SLOWNESS;
            EntityAttributeInstance instance = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            if (ConfigHelper.shouldApply(config, c) && !player.getOffHandStack().isIn(PersonalityTags.WALKING_STICKS) && !player.getMainHandStack().isIn(PersonalityTags.WALKING_STICKS)) {
                if (instance.getModifier(NO_STICK_SLOWNESS) == null)
                    instance.addTemporaryModifier(new EntityAttributeModifier(NO_STICK_SLOWNESS, "Old Person with No Stick", ConfigHelper.apply(config, c), EntityAttributeModifier.Operation.MULTIPLY_BASE));
            }
            else {
                instance.tryRemoveModifier(NO_STICK_SLOWNESS);
            }
        }
    }
}
