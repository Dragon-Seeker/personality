package io.blodhgarm.personality.client.gui.utils.owo;

import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.client.gui.ThemeHelper;
import io.blodhgarm.personality.client.gui.components.ButtonAddon;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Size;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class VariantButtonSurface extends VariantsNinePatchRender implements ButtonSurface {

    public VariantButtonSurface(Identifier texture, Size patchSize, Size textureSize, boolean repeat) {
        super(texture, patchSize, textureSize, repeat);
    }

    public static VariantButtonSurface surfaceLike(Size patchSize, Size textureSize, boolean repeat, boolean darkMode, boolean squareVariant){
        return (VariantButtonSurface) new VariantButtonSurface(PersonalityMod.id("textures/gui/button_surface.png"), patchSize, textureSize, repeat)
                .setVIndex(ButtonAddon.getVIndex(ThemeHelper.isDarkMode(), false));
    }

    @Override
    public void draw(ButtonAddon<?> buttonAddon, MatrixStack matrices, ParentComponent component) {
        this.setUIndex(buttonAddon.isActive() ? (buttonAddon.isHovered() ? 1 : 0) : 2);

        this.draw(matrices, component);
    }
}
