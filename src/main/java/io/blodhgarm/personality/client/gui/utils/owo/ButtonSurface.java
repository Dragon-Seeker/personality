package io.blodhgarm.personality.client.gui.utils.owo;

import io.blodhgarm.personality.client.gui.components.ButtonAddon;
import io.wispforest.owo.ui.core.ParentComponent;
import net.minecraft.client.util.math.MatrixStack;

public interface ButtonSurface {
    void draw(ButtonAddon<?> buttonAddon, MatrixStack matrices, ParentComponent component);
}
