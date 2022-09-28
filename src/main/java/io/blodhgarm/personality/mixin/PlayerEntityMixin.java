package io.blodhgarm.personality.mixin;

import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.impl.ServerCharacters;
import io.blodhgarm.personality.misc.config.ConfigHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import static io.blodhgarm.personality.PersonalityMod.CONFIG;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @ModifyArg(method = "addExhaustion", index = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;addExhaustion(F)V"))
    public float personality$addExtraExhaustionForYouth(float original) {
        if (!((PlayerEntity)(Object)this instanceof ServerPlayerEntity))
            return original;

        Character character = ServerCharacters.INSTANCE.getCharacter((ServerPlayerEntity)(Object)this);

        if (ConfigHelper.shouldApply(CONFIG.FASTER_EXHAUSTION, character))
            return original * ConfigHelper.apply(CONFIG.FASTER_EXHAUSTION, character);
        else
            return original;
    }

}
