package io.blodhgarm.personality.mixin;

import io.blodhgarm.personality.api.BaseCharacter;
import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.api.CharacterManager;
import io.blodhgarm.personality.api.core.KnownCharacterLookup;
import io.blodhgarm.personality.misc.config.ConfigHelper;
import io.blodhgarm.personality.misc.pond.CharacterToPlayerLink;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static io.blodhgarm.personality.PersonalityMod.CONFIG;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements CharacterToPlayerLink<PlayerEntity> {

    @Nullable public BaseCharacter playersGivenCharacter = null;

    @Shadow public abstract Text getName();

    @ModifyArg(method = "addExhaustion", index = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;addExhaustion(F)V"))
    public float personality$addExtraExhaustionForYouth(float original) {
        if (((PlayerEntity)(Object)this) instanceof ServerPlayerEntity serverPlayerEntity) {

            Character character = CharacterManager.getManger(serverPlayerEntity).getCharacter(serverPlayerEntity);

            if (ConfigHelper.shouldApply(CONFIG.FASTER_EXHAUSTION, character)) {
                original = original * ConfigHelper.apply(CONFIG.FASTER_EXHAUSTION, character);
            }
        }

        return original;
    }

    @ModifyVariable(method = "getDisplayName", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Team;decorateName(Lnet/minecraft/scoreboard/AbstractTeam;Lnet/minecraft/text/Text;)Lnet/minecraft/text/MutableText;", shift = At.Shift.BY, by = 2))
    private MutableText personality$attemptToAddCharacterName(MutableText mutableText){
//        Character character = CharacterManager.getManger((PlayerEntity) (Object) this).getCharacter((PlayerEntity) (Object) this);

        MutableText characterInfo = Text.empty();

        CharacterManager<PlayerEntity> manager = CharacterManager.getManger((PlayerEntity) (Object) this);

        if(manager instanceof KnownCharacterLookup lookup){
            BaseCharacter character = lookup.getKnownCharacter((PlayerEntity) (Object) this);

            if(character != null){
                characterInfo
                        .append(" | ")
                        .append(Text.literal(character.getName()));
            } else {
                if(CharacterManager.getClientCharacter() != null
                        && manager.getCharacter((PlayerEntity) (Object) this) != null) {
                    characterInfo
                            .append(" | ")
                            .append(Text.literal("Obscured"));
                }
            }
        }

        return mutableText
                .append(characterInfo);
    }

    @Override
    public BaseCharacter getCharacter() {
        return this.playersGivenCharacter;
    }

    @Override
    public void setCharacter(BaseCharacter character) {
        this.playersGivenCharacter = character;
    }
}
