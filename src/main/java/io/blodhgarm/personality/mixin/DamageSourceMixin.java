package io.blodhgarm.personality.mixin;

import io.blodhgarm.personality.misc.pond.DamageSourceExtended;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageSource.class)
public class DamageSourceMixin implements DamageSourceExtended{

    private boolean personality$disableDeathMessage = false;

    @Inject(method = "getDeathMessage", at = @At("HEAD"), cancellable = true)
    private void personality$disableDeathMessage(LivingEntity entity, CallbackInfoReturnable<Text> cir){
        if(personality$disableDeathMessage) cir.setReturnValue(Text.empty());
    }

    @Override
    public void disableDeathMessage(boolean value) {
        this.personality$disableDeathMessage = value;
    }
}
