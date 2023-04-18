package io.blodhgarm.personality.client.gui.components;

import com.mojang.datafixers.util.Function5;
import io.wispforest.owo.ui.component.ButtonComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class CustomButtonComponent extends ButtonComponent {

    private int yTextOffset = 0;

    private boolean floatPrecision = false;

    public CustomButtonComponent(Text message, Consumer<ButtonComponent> onPress) {
        super(message, onPress);
    }

    public CustomButtonComponent setYTextOffset(int yTextOffset){
        this.yTextOffset = yTextOffset;

        return this;
    }

    public CustomButtonComponent setFloatPrecision(boolean floatPrecision){
        this.floatPrecision = floatPrecision;

        return this;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderer.draw(matrices, this, delta);

        int color = this.active ? 0xffffff : 0xa0a0a0;

        var textRenderer = MinecraftClient.getInstance().textRenderer;

        var x = this.x + (this.width / 2f) - (textRenderer.getWidth(this.getMessage()) / 2f);
        var y = this.y + (this.height - 8) / 2f + yTextOffset;

        if (!floatPrecision) {
            x = Math.round(x);
            y = Math.round(y);
        }

        Function5<MatrixStack, Text, Float, Float, Integer, Integer> method = this.textShadow
                ? textRenderer::drawWithShadow
                : textRenderer::draw;

        method.apply(matrices, this.getMessage(), x, y, color);

        if (this.hovered) this.renderTooltip(matrices, mouseX, mouseY);
    }
}
