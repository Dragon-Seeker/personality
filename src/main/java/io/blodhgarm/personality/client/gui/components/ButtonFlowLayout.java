package io.blodhgarm.personality.client.gui.components;

import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.client.gui.VariantsNinePatchRender;
import io.wispforest.owo.ui.container.HorizontalFlowLayout;
import io.wispforest.owo.ui.core.Size;
import io.wispforest.owo.ui.core.Sizing;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import org.lwjgl.glfw.GLFW;

public class ButtonFlowLayout extends HorizontalFlowLayout {

    public final VariantsNinePatchRender BUTTON_RENDER = new VariantsNinePatchRender(PersonalityMod.id("textures/gui/button_surface.png"), Size.square(3), Size.square(48), false);

    protected boolean hovered = false;
    public boolean active = true;
    public boolean visible = true;

    protected PressAction onPress;

    public ButtonFlowLayout(Sizing horizontalSizing, Sizing verticalSizing) {
        super(horizontalSizing, verticalSizing);

        this.mouseEnter().subscribe(() -> this.hovered = true);
        this.mouseLeave().subscribe(() -> this.hovered = false);

        this.surface(BUTTON_RENDER);
    }

    public ButtonFlowLayout onPress(PressAction onPress1) {
        onPress = onPress1;

        return this;
    }

    public ButtonFlowLayout setUIndex(int uIndex){
        this.BUTTON_RENDER.setUIndex(uIndex);

        return this;
    }

    public ButtonFlowLayout setVIndex(int vIndex){
        this.BUTTON_RENDER.setVIndex(vIndex);

        return this;
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        this.BUTTON_RENDER.setUIndex(this.active ? (this.hovered ? 1 : 0) : 2);

        super.draw(matrices, mouseX, mouseY, partialTicks, delta);
    }

    @Override
    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        if (this.active && this.visible) {
            if (keyCode != GLFW.GLFW_KEY_ENTER && keyCode != GLFW.GLFW_KEY_SPACE && keyCode != GLFW.GLFW_KEY_KP_ENTER) {
                return false;
            } else {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                this.onPress.onPress(this);
                return true;
            }
        } else {
            return super.onKeyPress(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        if (this.active && this.visible) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                this.onPress.onPress(this);
                return true;
            }
        }

        return false;
    }

    @Environment(EnvType.CLIENT)
    public interface PressAction {
        void onPress(ButtonFlowLayout button);
    }
}
