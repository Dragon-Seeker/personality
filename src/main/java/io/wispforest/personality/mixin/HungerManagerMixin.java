package io.wispforest.personality.mixin;

import io.wispforest.personality.Personality;
import io.wispforest.personality.storage.Character;
import io.wispforest.personality.storage.CharacterManager;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(HungerManager.class)
public abstract class HungerManagerMixin {

    @Shadow public abstract void addExhaustion(float exhaustion);

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;addExhaustion(F)V"))
    public void personality$addExtraExhaustionForYouth(HungerManager instance, float exhaustion, PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity)) return;

        Character character = CharacterManager.getCharacter((ServerPlayerEntity)(Object)this);

        if (character != null && character.getStage() == Character.Stage.YOUTH)
            addExhaustion(exhaustion * Personality.YOUTH_EXHAUSTION_MULTIPLIER);
        else
            addExhaustion(exhaustion);

    }

}
