package io.blodhgarm.personality.mixin;

import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.server.ServerCharacters;
import io.blodhgarm.personality.misc.config.ConfigHelper;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.blodhgarm.personality.PersonalityMod.CONFIG;

@Mixin(HungerManager.class)
public abstract class HungerManagerMixin {

    @Unique private Character character;

    @Inject(method = "update", at = @At("HEAD"))
    public void personality$getPlayerForAgeModifiers(PlayerEntity player, CallbackInfo info) {
        this.character = ServerCharacters.INSTANCE.getCharacter((ServerPlayerEntity)player);
    }

    @ModifyConstant(method = "update", constant = @Constant(intValue = 80, ordinal = 0))
    public int personality$healFasterForYouth(int original) {
        return ConfigHelper.shouldApply(CONFIG.fasterHealing, character)
                ? Math.round(original / ConfigHelper.apply(CONFIG.fasterHealing, character))
                : original;
    }

    @ModifyConstant(method = "update", constant = @Constant(intValue = 18))
    public int personality$modifyMinimumHungerForHealForYouth(int original) {
        return ConfigHelper.shouldApply(CONFIG.minimumHungerToHeal, character)
                ? Math.round(ConfigHelper.apply(CONFIG.minimumHungerToHeal, character))
                : original;
    }

    @ModifyVariable(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;addExhaustion(F)V", ordinal = 0))
    private float personality$addExtraExhaustionForYouth1(float exhaustion){ return adjustExhaustionValue(exhaustion); }

    @ModifyConstant(method = "update", constant = @Constant(floatValue = 6.0f, ordinal = 2))
    private float personality$addExtraExhaustionForYouth2(float exhaustion){ return adjustExhaustionValue(exhaustion); }

    private float adjustExhaustionValue(float exhaustion){
        return ConfigHelper.shouldApply(CONFIG.fasterExhaustion, character)
                ? exhaustion * ConfigHelper.apply(CONFIG.fasterExhaustion, character)
                : exhaustion;
    }
}
