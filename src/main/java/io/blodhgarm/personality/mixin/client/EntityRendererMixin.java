package io.blodhgarm.personality.mixin.client;

import io.blodhgarm.personality.PersonalityMod;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityRenderer.class, priority = 1001)
public abstract class EntityRendererMixin<T extends Entity> {

    @ModifyVariable(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getHeight()F", shift = At.Shift.BY, by = 4))
    private float personality$changeLabelHeight(float value, T entity){
        return (PersonalityMod.CONFIG.showPlayerNamePlateAtChestLevel() && entity instanceof PlayerEntity) ? (value / 2) : value;
    }

    @Inject(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V"))
    private void personality$translateLabel(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci){
        if(PersonalityMod.CONFIG.showPlayerNamePlateAtChestLevel() && entity instanceof PlayerEntity) {
            matrices.scale(0.75f, 0.75f, 1.0f);
            matrices.translate(0, 0, -0.25f);
        }
    }
}
