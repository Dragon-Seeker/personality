package io.blodhgarm.personality.client.gui.components.vanilla;

import io.blodhgarm.personality.mixin.client.accessor.TextFieldWidgetAccessor;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class BetterTextFieldWidget extends TextFieldWidget {

    private Color backgroundColor = Color.BLACK;//Color.ofArgb(0xFF555555);
    private Color outlineColor = Color.ofArgb(0xFFa0a0a0);

    public BetterTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        this(textRenderer, x, y, width, height, null, text);
    }

    public BetterTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, @Nullable TextFieldWidget copyFrom, Text text) {
        super(textRenderer, x, y, width, height, copyFrom, text);
    }

    public int getInnerWidth() {
        return this.width - 8;
    }

    public static BetterTextFieldWidget textBox(Sizing horizontalSizing) {
        return Components.createWithSizing(
                () -> new BetterTextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, 0, 0, Text.empty()),
                horizontalSizing,
                Sizing.fixed(20)
        );
    }

    public static BetterTextFieldWidget textBox(Sizing horizontalSizing, String text) {
        final var textBox = textBox(horizontalSizing);
        textBox.setText(text);
        textBox.setCursorToStart();
        return textBox;
    }

    public BetterTextFieldWidget bqColor(Color color){
        this.backgroundColor = color;

        return this;
    }

    public BetterTextFieldWidget outlineColor(Color color){
        this.outlineColor = color;

        return this;
    }

    public BetterTextFieldWidget setEditAbility(boolean editable) {
        super.setEditable(editable);

        return this;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.isVisible()) {
            if(((TextFieldWidgetAccessor)this).personality$drawsBackground()) {
                int i = this.isFocused() ? -1 : outlineColor.argb();

                fill(matrices, this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, i);
                fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height, backgroundColor.argb());
            }

            super.renderButton(matrices, mouseX, mouseY, delta);
        }
    }
}
