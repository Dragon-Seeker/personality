package io.blodhgarm.personality.mixin;

import io.blodhgarm.personality.Character;
import io.blodhgarm.personality.server.ServerCharacters;
import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.server.config.ConfigHelper;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.blodhgarm.personality.PersonalityMod.CONFIG;

@Mixin(HungerManager.class)
public abstract class HungerManagerMixin {

    @Shadow public abstract void addExhaustion(float exhaustion);
    @Unique private Character character;

    @Inject(method = "update", at = @At("HEAD"))
    public void personality$getPlayerForAgeModifiers(PlayerEntity player, CallbackInfo info) {
        this.character = ServerCharacters.getCharacter((ServerPlayerEntity)player);
    }

    @ModifyConstant(method = "update", constant = @Constant(intValue = 80, ordinal = 0))
    public int personality$healFasterForYouth(int original) {
        if (ConfigHelper.shouldApply(CONFIG.FASTER_HEAL, character))
            return (int) (original / ConfigHelper.apply(CONFIG.FASTER_HEAL, character));
        return original;
    }

    @ModifyConstant(method = "update", constant = @Constant(intValue = 18))
    public int personality$modifyMinimumHungerForHealForYouth(int original) {
        if (ConfigHelper.shouldApply(CONFIG.LOWER_HUNGER_MINIMUM, character))
            return (int) ConfigHelper.apply(CONFIG.LOWER_HUNGER_MINIMUM, character);
        return original;
    }


    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;addExhaustion(F)V"))
    public void personality$addExtraExhaustionForYouth(HungerManager instance, float exhaustion, PlayerEntity player) {
        if (ConfigHelper.shouldApply(CONFIG.FASTER_EXHAUSTION, character))
            addExhaustion(exhaustion * ConfigHelper.apply(CONFIG.FASTER_EXHAUSTION, character));
        else
            addExhaustion(exhaustion);

    }

}
