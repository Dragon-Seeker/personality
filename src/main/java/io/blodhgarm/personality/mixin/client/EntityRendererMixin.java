package io.blodhgarm.personality.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Shadow @Final protected EntityRenderDispatcher dispatcher;

    @Shadow public abstract TextRenderer getTextRenderer();

    /**
     * @author Minenash
     */
    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    protected void personality$renderLabelIfPresent(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo callback) {
        if (!(entity instanceof PlayerEntity))
            return;

        double d = this.dispatcher.getSquaredDistanceToCamera(entity);
        if (!(d > 4096.0)) {
            boolean bl = !entity.isSneaky();
            float f = entity.getHeight() / 2 + 0.5F;matrices.push();
            matrices.translate(0.0, f, 0.0);
            matrices.multiply(this.dispatcher.getRotation());
            matrices.scale(-0.0125F, -0.0125F, 0.0125F);
            matrices.translate(0, 0, -40);
            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            float g = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F);
            int j = (int)(g * 255.0F) << 24;
            TextRenderer textRenderer = this.getTextRenderer();
            float h = (float)(-textRenderer.getWidth(text) / 2);
            textRenderer.draw(text, h, 0, 553648127, false, matrix4f, vertexConsumers, bl, j, light);
            if (bl) {
                textRenderer.draw(text, h, 0, -1, false, matrix4f, vertexConsumers, false, 0, light);
            }

            matrices.pop();
        }

        callback.cancel();
    }

}
