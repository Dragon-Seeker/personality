package io.blodhgarm.personality.client.gui.components;

import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.client.gui.utils.owo.VariantsNinePatchRender;
import io.blodhgarm.personality.misc.pond.owo.FocusCheckable;
import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Size;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class ButtonAddon<T extends BaseParentComponent> {

    @Nullable public VariantsNinePatchRender BUTTON_RENDER = null;

    protected boolean checkThemeMode = true;
    protected boolean squareMode = true;

    protected boolean hovered = false;
    protected boolean active = true;

    protected PressAction<T> onPress;

    protected final T linkedComponent;

    public ButtonAddon(T component) {
        component.mouseEnter().subscribe(() -> this.hovered = true);
        component.mouseLeave().subscribe(() -> this.hovered = false);

        if(component instanceof FocusCheckable focusCheckable) focusCheckable.focusCheck().subscribe(source -> active);

        this.linkedComponent = component;
    }

    public ButtonAddon<T> onPress(PressAction<T> onPress) {
        this.onPress = onPress;

        return this;
    }

    public ButtonAddon<T> setActive(boolean isActive){
        this.active = isActive;

        return this;
    }

    public ButtonAddon<T> checkThemeMode(boolean value){
        this.checkThemeMode = value;

        return this;
    }

    public ButtonAddon<T> changeButtonShape(boolean useSquareVersion){
        this.squareMode = useSquareVersion;

        return this;
    }

    public ButtonAddon<T> useCustomButtonSurface(Consumer<VariantsNinePatchRender> configure){
        this.BUTTON_RENDER = new VariantsNinePatchRender(PersonalityMod.id("textures/gui/button_surface.png"), Size.square(3), Size.square(48), false);

        configure.accept(BUTTON_RENDER);

        this.linkedComponent.surface(this.BUTTON_RENDER);

        return this;
    }

    public static int getVIndex(Boolean darkMode, Boolean squareShape){
        return ((squareShape) ? 1 : 0) + (((darkMode) ? 1 : 0) << 1);
    }

    public void beforeDraw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        if(this.BUTTON_RENDER != null) this.BUTTON_RENDER.setUIndex(this.active ? (this.hovered ? 1 : 0) : 2);
    }

    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        if (this.active) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                this.onPress.onPress(linkedComponent);
                return true;
            }
        }

        return false;
    }

    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        if (this.active) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                this.onPress.onPress(linkedComponent);
                return true;
            }
        }

        return false;
    }

    @Environment(EnvType.CLIENT)
    public interface PressAction<T extends Component> {
        void onPress(T button);
    }
}
