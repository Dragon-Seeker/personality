package io.blodhgarm.personality.mixin;

import io.blodhgarm.personality.api.character.BaseCharacter;
import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.api.character.CharacterManager;
import io.blodhgarm.personality.api.core.KnownCharacterLookup;
import io.blodhgarm.personality.misc.config.ConfigHelper;
import io.blodhgarm.personality.misc.pond.CharacterToPlayerLink;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import static io.blodhgarm.personality.PersonalityMod.CONFIG;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements CharacterToPlayerLink {

    @ModifyArg(method = "addExhaustion", index = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;addExhaustion(F)V"))
    public float personality$addExtraExhaustionForYouth(float original) {
        if (((PlayerEntity)(Object) this) instanceof ServerPlayerEntity player) {

            Character character = CharacterManager.getManger(player).getCharacter(player);

            if (ConfigHelper.shouldApply(CONFIG.fasterExhaustion, character)) {
                original = original * ConfigHelper.apply(CONFIG.fasterExhaustion, character);
            }
        }

        return original;
    }

    @Override
    public BaseCharacter getCharacter(boolean prioritizeCL) {
        CharacterManager<PlayerEntity, Character> manager = CharacterManager.getManger((PlayerEntity) (Object) this);

        Character clientCharacter = manager.getClientCharacter();

        BaseCharacter character = null;

        if(clientCharacter != null){
            character = clientCharacter.getKnownCharacter((PlayerEntity) (Object) this);

            if(prioritizeCL) return character;
        }

        if(character == null) character = manager.getCharacter((PlayerEntity) (Object) this);

        return character;
    }

}
