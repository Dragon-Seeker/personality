package io.blodhgarm.personality.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;

public class CustomEntityComponent<E extends Entity> extends EntityComponent<E> {

    public CustomEntityComponent(Sizing entitySizing, E entity) {
        super(entitySizing, entity);
    }

    public CustomEntityComponent(Sizing entitySizing, EntityType<E> type, @Nullable NbtCompound nbt) {
        super(entitySizing, type, nbt);
    }

    public static <P extends PlayerEntity, E extends Entity> CustomEntityComponent<E> playerEntityComponent(Sizing entitySizing, @Nullable P entity){
        return new CustomEntityComponent<>(entitySizing, (E) (entity != null ? entity : new CustomTexturedRenderablePlayerEntity()));
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        matrices.push();

        matrices.translate(x + this.width / 2f, y + this.height / 2f, 100);
        matrices.scale(75 * this.scale * this.width / 64f, -75 * this.scale * this.height / 64f, 75 * this.scale);

        matrices.translate(0, entity.getHeight() / -2f, 0);

        if (this.lookAtCursor) {
            float xRotation = (float) Math.toDegrees(Math.atan((mouseY - this.y - this.height / 2f) / 40f));
            float yRotation = (float) Math.toDegrees(Math.atan((mouseX - this.x - this.width / 2f) / 40f));

            if (this.entity instanceof LivingEntity living) {
                living.prevHeadYaw = -yRotation;
            }

            this.entity.prevYaw = -yRotation;
            this.entity.prevPitch = xRotation * .65f;

            // We make sure the xRotation never becomes 0, as the lighting otherwise becomes very unhappy
            if (xRotation == 0) xRotation = .1f;
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(xRotation * .15f));
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(yRotation * .15f));
        } else {
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(0));
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-45 + this.mouseRotation));
        }

        RenderSystem.setShaderLights(new Vec3f(0, 1, .45F), new Vec3f(0, -1, .45F));
        this.dispatcher.setRenderShadows(false);
        this.dispatcher.render(this.entity, 0, 0, 0, 0, 0, matrices, this.entityBuffers, LightmapTextureManager.MAX_LIGHT_COORDINATE);
        this.dispatcher.setRenderShadows(true);
        this.entityBuffers.draw();
        DiffuseLighting.enableGuiDepthLighting();

        matrices.pop();
    }

    protected static class CustomTexturedRenderablePlayerEntity extends RenderablePlayerEntity {

        //Credit to ObeyTheFist on SkinIndex [Link: https://www.minecraftskins.com/skin/9965036/requests/]
        public static final Identifier MISSING_SKIN_TEXTURE_ID = new Identifier("personality", "textures/skins/question_mark.png");

        public CustomTexturedRenderablePlayerEntity() {
            super(MinecraftClient.getInstance().player.getGameProfile());
        }

        @Override
        public Identifier getSkinTexture() {
            return new Identifier("personality", "textures/skins/question_mark_2.png");
        }

        @Override
        public String getModel() {
            return "default";
        }
    }

}
