package io.blodhgarm.personality.mixin;

import io.blodhgarm.personality.Character;
import io.blodhgarm.personality.server.ServerCharacters;
import io.blodhgarm.personality.PersonalityMod;
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

        Character character = ServerCharacters.getCharacter((ServerPlayerEntity)(Object)this);

        if (character != null && PersonalityMod.shouldGradualValue(CONFIG.FASTER_EXHAUSTION, character))
            return original * PersonalityMod.getGradualValue(CONFIG.FASTER_EXHAUSTION, character);
        else
            return original;
    }

}
