package io.blodhgarm.personality.mixin;

import io.blodhgarm.personality.server.ServerCharacterTick;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.registry.Registerable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DamageTypes.class)
public interface DamageTypesMixin {

    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void personality$addNewDamageTypes(Registerable<DamageType> damageTypeRegisterable, CallbackInfo ci){
        ServerCharacterTick.personality$bootstrap(damageTypeRegisterable);
    }
}
