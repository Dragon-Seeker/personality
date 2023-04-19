package io.blodhgarm.personality.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import io.blodhgarm.personality.mixin.client.accessor.EditBoxAccessor;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Size;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.inject.ComponentStub;
import io.blodhgarm.personality.mixin.client.accessor.EditBoxWidgetAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.EditBox;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.input.CursorMovement;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

/**
 * This is manly a copy of EditBoxWidget with certain changes to how it functions for Personality
 */
public class EditBoxComponent extends EditBoxWidget implements ComponentStub {

    private Color backgroundColor = Color.BLACK;//Color.ofArgb(0xFF555555);
    private Color outlineColor = Color.ofArgb(0xFFa0a0a0);

    private boolean canEdit = true;

    public enum ScrollBarSide { LEFT, RIGHT }

    private ScrollBarSide position = ScrollBarSide.LEFT;

    public EditBoxComponent(Text placeholder, Text message) {
        super(MinecraftClient.getInstance().textRenderer, 0,0, Integer.MAX_VALUE,0, placeholder, message);

        this.setText("");
    }

    public static EditBoxComponent editBox(Sizing horizontalSizing, Sizing verticalSizing, Text placeholder, Text msg, String info){
        return Components.createWithSizing(() -> new EditBoxComponent(placeholder, msg), horizontalSizing, verticalSizing)
                .setInfo(info);
    }

    public EditBoxComponent setInfo(String info) {
        super.setText(info);

        return this;
    }

    public EditBoxComponent textWidth(int width){
        EditBoxAccessor accessor = ((EditBoxAccessor)((EditBoxWidgetAccessor) this).personality$getEditBox());

        accessor.personality$setWidth(width);
        accessor.personality$callOnChange();

        return this;
    }

    public EditBoxComponent bqColor(Color color){
        this.backgroundColor = color;

        return this;
    }

    public EditBoxComponent outlineColor(Color color){
        this.outlineColor = color;

        return this;
    }

    public EditBoxComponent scrollBarPosition(ScrollBarSide side){
        this.position = side;

        return this;
    }

    public EditBoxComponent canEdit(boolean value){
        this.canEdit = value;

        return this;
    }

    public EditBoxComponent setCursorPosition(CursorMovement movement, int amount){
        this.editBox.moveCursor(movement, amount);

        return this;
    }

    @Override
    public void inflate(Size space) {
        super.inflate(space);

        this.editBox.width = width() - this.getPaddingDoubled();
        this.editBox.rewrap();
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return !canEdit || super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return !canEdit || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public int width() {
        return super.width() + 8;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.visible) {
            return false;
        } else {
            boolean bl = this.isWithinBounds(mouseX, mouseY);
            boolean bl2 = this.overflows()
                    && mouseX >= (double)(this.x + ((this.position == ScrollBarSide.RIGHT) ? this.width : 0))
                    && mouseX <= (double)(this.x + ((this.position == ScrollBarSide.RIGHT) ? this.width : 0) + 8)
                    && mouseY >= (double)this.y
                    && mouseY < (double)(this.y + this.height);
            this.setFocused(bl || bl2);
            if (bl2 && button == 0) {
                this.scrollbarDragged = true;
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    protected void renderContents(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        int x = this.x + ((this.position == ScrollBarSide.RIGHT) ? 0 : 8);

        String string = this.editBox.getText();
        if (string.isEmpty() && !this.isFocused()) {
            textRenderer.drawTrimmed(this.placeholder, x + this.getPadding(), this.y + this.getPadding(), this.width - this.getPaddingDoubled(), -857677600);
        } else {
            int i = this.editBox.getCursor();
            boolean bl = this.isFocused() && this.tick / 6 % 2 == 0;
            boolean bl2 = i < string.length();
            int j = 0;
            int k = 0;
            int l = this.y + this.getPadding();

            for(EditBox.Substring substring : this.editBox.getLines()) {
                boolean bl3 = this.isVisible(l, l + 9);
                if (bl && bl2 && i >= substring.beginIndex() && i <= substring.endIndex()) {
                    if (bl3) {
                        j = textRenderer.drawWithShadow(matrices, string.substring(substring.beginIndex(), i), (float)(x + this.getPadding()), (float)l, -2039584) - 1;
                        DrawableHelper.fill(matrices, j, l - 1, j + 1, l + 1 + 9, -3092272);
                        textRenderer.drawWithShadow(matrices, string.substring(i, substring.endIndex()), (float)j, (float)l, -2039584);
                    }
                } else {
                    if (bl3) {
                        j = textRenderer
                                .drawWithShadow(matrices, string.substring(substring.beginIndex(), substring.endIndex()), (float)(x + this.getPadding()), (float)l, -2039584)
                                - 1;
                    }

                    k = l;
                }

                l += 9;
            }

            if (bl && !bl2 && this.isVisible(k, k + 9)) {
                textRenderer.drawWithShadow(matrices, "_", (float)j, (float)k, -3092272);
            }

            if (this.editBox.hasSelection()) {
                EditBox.Substring substring2 = this.editBox.getSelection();
                int m = x + this.getPadding();
                l = this.y + this.getPadding();

                for(EditBox.Substring substring3 : this.editBox.getLines()) {
                    if (substring2.beginIndex() > substring3.endIndex()) {
                        l += 9;
                    } else {
                        if (substring3.beginIndex() > substring2.endIndex()) {
                            break;
                        }

                        if (this.isVisible(l, l + 9)) {
                            int n = textRenderer.getWidth(string.substring(substring3.beginIndex(), Math.max(substring2.beginIndex(), substring3.beginIndex())));
                            int o;
                            if (substring2.endIndex() > substring3.endIndex()) {
                                o = this.width - this.getPadding();
                            } else {
                                o = textRenderer.getWidth(string.substring(substring3.beginIndex(), substring2.endIndex()));
                            }

                            this.drawSelection(matrices, m + n, l, m + o, l + 9);
                        }

                        l += 9;
                    }
                }
            }

        }
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.visible) {
            this.drawBox(matrices);

            int x = this.x + ((this.position == ScrollBarSide.RIGHT) ? 0 : 8);
            enableScissor(x + 1, this.y + 1, x + this.width - 1, this.y + this.height - 1);

            matrices.push();
            matrices.translate(0.0, -this.getScrollY(), 0.0);
            this.renderContents(matrices, mouseX, mouseY, delta);
            matrices.pop();
            disableScissor();
            this.renderOverlay(matrices);
        }
    }

    @Override
    protected void drawScrollbar() {
        int i = this.getScrollbarThumbHeight();
        int j = this.x + ((this.position == ScrollBarSide.RIGHT) ? this.width : 0);
        int k = j + 8;
        int l = Math.max(this.y, (int)this.getScrollY() * (this.height - i) / this.getMaxScrollY() + this.y);
        int m = l + i;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex((double)j, (double)m, 0.0).color(128, 128, 128, 255).next();
        bufferBuilder.vertex((double)k, (double)m, 0.0).color(128, 128, 128, 255).next();
        bufferBuilder.vertex((double)k, (double)l, 0.0).color(128, 128, 128, 255).next();
        bufferBuilder.vertex((double)j, (double)l, 0.0).color(128, 128, 128, 255).next();
        bufferBuilder.vertex((double)j, (double)(m - 1), 0.0).color(192, 192, 192, 255).next();
        bufferBuilder.vertex((double)(k - 1), (double)(m - 1), 0.0).color(192, 192, 192, 255).next();
        bufferBuilder.vertex((double)(k - 1), (double)l, 0.0).color(192, 192, 192, 255).next();
        bufferBuilder.vertex((double)j, (double)l, 0.0).color(192, 192, 192, 255).next();
        tessellator.draw();
    }

    @Override
    protected void drawBox(MatrixStack matrices) {
        int i = this.isFocused() ? -1 : outlineColor.argb();

        int x = this.x + ((this.position == ScrollBarSide.RIGHT) ? 0 : 8);

        fill(matrices, x, this.y, x + this.width, this.y + this.height, i);
        fill(matrices, x + 1, this.y + 1, x + this.width - 1, this.y + this.height - 1, backgroundColor.argb());
    }

    @Override
    public void drawFocusHighlight(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        // noop, since TextFieldWidget already does this
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return true;
    }
}
