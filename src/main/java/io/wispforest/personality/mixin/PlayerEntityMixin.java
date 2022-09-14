package io.wispforest.personality.mixin;

import io.wispforest.personality.Personality;
import io.wispforest.personality.storage.Character;
import io.wispforest.personality.storage.CharacterManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Optional;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @ModifyArg(method = "addExhaustion", index = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;addExhaustion(F)V"))
    public float personality$addExtraExhaustionForYouth(float original) {
        if (!((PlayerEntity)(Object)this instanceof ServerPlayerEntity)) return original;

        Character character = CharacterManager.getCharacter((ServerPlayerEntity)(Object)this);

        if (character != null && character.getStage() == Character.Stage.YOUTH)
            return original * Personality.YOUTH_EXHAUSTION_MULTIPLIER;
        else
            return original;
    }

}
