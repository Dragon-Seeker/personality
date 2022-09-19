package io.blodhgarm.personality.client.compat;

import io.blodhgarm.personality.mixin.client.accessor.EditBoxAccessor;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.inject.ComponentStub;
import io.blodhgarm.personality.mixin.client.accessor.EditBoxWidgetAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class BetterEditBoxWidget extends EditBoxWidget implements ComponentStub {

    private Color backgroundColor = Color.BLACK;//Color.ofArgb(0xFF555555);
    private Color outlineColor = Color.ofArgb(0xFFa0a0a0);

    public BetterEditBoxWidget(Text placeholder, Text message) {
        super(MinecraftClient.getInstance().textRenderer, 0,0, Integer.MAX_VALUE,0, placeholder, message);

        this.setText("");
    }

    public static BetterEditBoxWidget ofEmpty(Text placeholder, Text message){
        BetterEditBoxWidget widget = new BetterEditBoxWidget(placeholder, message);;

        widget.setText("");

        return widget;
    }

    public BetterEditBoxWidget textWidth(int width){
        EditBoxAccessor accessor = ((EditBoxAccessor)((EditBoxWidgetAccessor) this).personality$getEditBox());

        accessor.personality$setWidth(width);
        accessor.personality$callOnChange();

        return this;
    }

    public BetterEditBoxWidget bqColor(Color color){
        this.backgroundColor = color;

        return this;
    }

    public BetterEditBoxWidget outlineColor(Color color){
        this.outlineColor = color;

        return this;
    }

    @Override
    public int width() {
        return super.width() + 8;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.visible) {
            this.drawBox(matrices);
            enableScissor(this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1);
            matrices.push();
            matrices.translate(0.0, -this.getScrollY(), 0.0);
            this.renderContents(matrices, mouseX, mouseY, delta);
            matrices.pop();
            disableScissor();
            this.renderOverlay(matrices);
        }
    }

    private void drawBox(MatrixStack matrices) {
        int i = this.isFocused() ? -1 : outlineColor.argb();

        fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height, i);
        fill(matrices, this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1, backgroundColor.argb());
    }

    @Override
    public void drawFocusHighlight(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        // noop, since TextFieldWidget already does this
    }

    protected CursorStyle owo$preferredCursorStyle() {
        return CursorStyle.TEXT;
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return true;
    }
}
