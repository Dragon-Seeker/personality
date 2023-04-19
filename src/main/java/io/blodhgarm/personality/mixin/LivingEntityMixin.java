package io.blodhgarm.personality.mixin;

import io.blodhgarm.personality.item.WoodenCane;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow public abstract ItemStack getMainHandStack();

    @Inject(method = "disablesShield", at = @At("HEAD"), cancellable = true)
    private void personality$disableShieldIfIsWalkingStick(CallbackInfoReturnable<Boolean> cir){
        if(this.getMainHandStack().getItem() instanceof WoodenCane) cir.setReturnValue(true);
    }
}
