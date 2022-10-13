package io.blodhgarm.personality.client.compat.origins.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import io.blodhgarm.personality.client.compat.origins.gui.OriginSelectionDisplayAddon;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.origin.Impact;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.Drawer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.List;

public class OriginImpactComponent extends BaseComponent {

    protected Impact impact;

    protected final int regionWidth, regionHeight;
    protected final int textureWidth, textureHeight;

    protected OriginImpactComponent(Impact impact) {
        this.regionWidth = 8;
        this.regionHeight = 8;
        this.textureWidth = 48;
        this.textureHeight = 48;

        setImpact(impact);

        this.width = 28;
        this.height = 8;
    }

    @Override
    protected void applyHorizontalContentSizing(Sizing sizing) {}

    @Override
    protected void applyVerticalContentSizing(Sizing sizing) {}

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        RenderSystem.setShaderTexture(0, OriginSelectionDisplayAddon.ORIGINS_GUI_TEXTURE);
        RenderSystem.enableDepthTest();

        matrices.push();
        //matrices.translate(x, y, 0);
//        matrices.scale(this.width / (float) this.regionWidth, this.height / (float) this.regionHeight, 0);

        for(int i = 0; i < 3; i++){
            int v = (impact.getImpactValue() - i > 0) ? (impact.getImpactValue() * 8) : 0;

            Drawer.drawTexture(matrices,
                    x + (i * 8) + (i * 2),
                    y,
                    regionWidth,
                    regionHeight,
                    36,
                    v,
                    regionWidth,
                    regionHeight,
                    this.textureWidth, this.textureHeight
            );
        }

        matrices.pop();
    }

    public void setImpact(Impact impact){
        this.impact = impact;

        this.tooltip(List.of(TooltipComponent.of(Text.translatable(Origins.MODID + ".gui.impact.impact").append(": ").append(impact.getTextComponent()).asOrderedText())));
    }
}
