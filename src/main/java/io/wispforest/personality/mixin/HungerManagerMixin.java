package io.wispforest.personality.mixin;

import io.wispforest.personality.PersonalityMod;
import io.wispforest.personality.server.ServerCharacters;
import io.wispforest.personality.Character;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HungerManager.class)
public abstract class HungerManagerMixin {

    @Shadow public abstract void addExhaustion(float exhaustion);
    @Shadow private int foodLevel;
    @Shadow private int foodTickTimer;

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;addExhaustion(F)V"))
    public void personality$addExtraExhaustionForYouth(HungerManager instance, float exhaustion, PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity))
            return;

        Character character = ServerCharacters.getCharacter((ServerPlayerEntity)player);

        if (character != null && character.getStage() == Character.Stage.YOUTH)
            addExhaustion(exhaustion * PersonalityMod.CONFIG.YOUTH_EXHAUSTION_MULTIPLIER());
        else
            addExhaustion(exhaustion);

    }

    @ModifyConstant(method = "update", constant = @Constant(intValue = 80, ordinal = 0))
    public int personality$healFasterForYouth(int original) {
        return (int) (original / PersonalityMod.CONFIG.YOUTH_HEAL_RATE_MULTIPLIER());
    }

    @ModifyConstant(method = "update", constant = @Constant(intValue = 18))
    public int personality$modifyMinimumHungerForHealForYouth(int original) {
        return PersonalityMod.CONFIG.YOUTH_HEAL_HUNGER_MINIMUM();
    }

}
