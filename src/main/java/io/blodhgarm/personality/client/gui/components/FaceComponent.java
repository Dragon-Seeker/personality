package io.blodhgarm.personality.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.AnimatableProperty;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class FaceComponent extends BaseComponent {

    private static final MinecraftClient client = MinecraftClient.getInstance();

    private final Identifier skin;

    public FaceComponent(String playerUUID) {
        skin = client.player.getSkinTexture();
        horizontalSizing = AnimatableProperty.of(Sizing.fixed(24));
        verticalSizing = AnimatableProperty.of(Sizing.fixed(24));
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {


        RenderSystem.setShaderTexture(0, skin);
        DrawableHelper.drawTexture(matrices, x+4, y+4, 16, 16, 8.0F, 8.0F, 8, 8, 64, 64);
        RenderSystem.enableBlend();
        DrawableHelper.drawTexture(matrices, x+4, y+4, 16, 16, 40.0F, 8.0F, 8, 8, 64, 64);
        RenderSystem.disableBlend();

    }

    @Override
    protected void applyHorizontalContentSizing(Sizing sizing) {

    }

    @Override
    protected void applyVerticalContentSizing(Sizing sizing) {

    }




}
