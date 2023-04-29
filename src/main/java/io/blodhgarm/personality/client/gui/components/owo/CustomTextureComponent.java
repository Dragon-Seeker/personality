package io.blodhgarm.personality.client.gui.components.owo;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.util.Drawer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class CustomTextureComponent extends TextureComponent {

    public CustomTextureComponent(Identifier texture, int u, int v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        super(texture, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        RenderSystem.setShaderTexture(0, this.texture);
        RenderSystem.enableDepthTest();

        if (this.blend) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
        }

        matrices.push();
        matrices.translate(x, y, 0);
        matrices.scale(this.width / (float) this.regionWidth, this.height / (float) this.regionHeight, 0);

        var visibleArea = this.visibleArea.get();

        int bottomEdge = Math.min(visibleArea.y() + visibleArea.height(), regionHeight);
        int rightEdge = Math.min(visibleArea.x() + visibleArea.width(), regionWidth);

        Drawer.drawTexture(matrices,
                visibleArea.x(),
                visibleArea.y(),
                rightEdge - visibleArea.x(),
                bottomEdge - visibleArea.y(),
                this.u + visibleArea.x(),
                this.v + visibleArea.y(),
                this.textureWidth, this.textureHeight,
                this.textureWidth, this.textureHeight
        );

        if (this.blend) RenderSystem.disableBlend();

        matrices.pop();
    }
}
