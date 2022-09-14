package io.wispforest.personality.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.origins.badge.Badge;
import io.github.apace100.origins.badge.BadgeManager;
import io.github.apace100.origins.origin.Origin;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.Drawer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Language;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OriginDescriptionComponent extends LabelComponent {

    private Text randomOriginText;
    boolean isOriginRandom;

    private Iterable<PowerType<?>> powerTypes;

    private Map<PowerType<?>, PowerTextHolder> mainPowerTexts = new HashMap<>();

    private final LinkedList<AdditionalCreationComponent.RenderedBadge> renderedBadges = new LinkedList<>();

    protected OriginDescriptionComponent(Origin origin, Text randomOriginText, boolean isOriginRandom) {
        super(origin.getDescription());

        origin(origin, randomOriginText, isOriginRandom);
    }

    public OriginDescriptionComponent origin(Origin origin, Text randomOriginText, boolean isOriginRandom){
        this.text(origin.getDescription());

        this.randomOriginText = randomOriginText;
        this.isOriginRandom = isOriginRandom;

        this.powerTypes = origin.getPowerTypes();

        this.createTextMap();

        return this;
    }

    private void createTextMap(){
        if(!mainPowerTexts.isEmpty()) mainPowerTexts.clear();

        for(PowerType<?> p : this.powerTypes) {
            if(p.isHidden()) {
                continue;
            }

            int width = this.horizontalSizing.get().method != Sizing.Method.CONTENT ? this.width : this.maxWidth;

            OrderedText name = Language.getInstance().reorder(textRenderer.trimToWidth(p.getName().formatted(Formatting.UNDERLINE), width));
            List<OrderedText> drawLines = textRenderer.wrapLines(p.getDescription(), width);

            mainPowerTexts.put(p, new PowerTextHolder(name, drawLines));

//            if(y >= startY - 24 && y <= endY + 12) {
//                textRenderer.draw(matrices, name, x, y, 0xFFFFFF);
//                int tw = textRenderer.getWidth(name);
//                List<Badge> badges = BadgeManager.getPowerBadges(p.getIdentifier());
//                int xStart = x + tw + 4;
//                int bi = 0;
//                for(Badge badge : badges) {
//                    RenderedBadge renderedBadge = new RenderedBadge(p, badge,xStart + 10 * bi, y - 1);
//                    renderedBadges.add(renderedBadge);
//                    RenderSystem.setShaderTexture(0, badge.spriteId());
//                    drawTexture(matrices, xStart + 10 * bi, y - 1, 0, 0, 9, 9, 9, 9);
//                    bi++;
//                }
//            }
//            for(OrderedText line : drawLines) {
//                y += 12;
//                if(y >= startY - 24 && y <= endY + 12) {
//                    textRenderer.draw(matrices, line, x + 2, y, 0xCCCCCC);
//                }
//            }
//
//            y += 14;

        }
    }

    public OriginDescriptionComponent maxWidth(int maxWidth) {
        super.maxWidth(maxWidth);

        this.createTextMap();

        return this;
    }

    @Override
    protected void applyHorizontalContentSizing(Sizing sizing) {
        int widestText = 0;
        for (var line : this.wrappedText) {
            int width = this.textRenderer.getWidth(line);
            if (width > widestText) widestText = width;
        }

        if (widestText > this.maxWidth) {
            //Cheating to run wrapLines
            this.maxWidth(this.maxWidth);

            this.applyHorizontalContentSizing(sizing);
        } else {
            this.width = widestText + sizing.value * 2;
        }
    }

    @Override
    protected void applyVerticalContentSizing(Sizing sizing) {
        //Cheating to run wrapLines
        this.maxWidth(this.maxWidth);

        int powerTextHeight = 0;

        for(Map.Entry<PowerType<?>, PowerTextHolder> entry : mainPowerTexts.entrySet()){
            powerTextHeight += ((entry.getValue().desc().size() + 1) * (this.textRenderer.fontHeight + 3));
        }

        powerTextHeight += (mainPowerTexts.size() - 1) * 14;

        this.height = ((this.textRenderer.fontHeight + 3) * 2) + (this.wrappedText.size() * (this.textRenderer.fontHeight + 3)) + powerTextHeight - 2 + sizing.value * 2;
    }

    @Override
    public void inflate(Size space) {
        super.inflate(space);
        this.createTextMap();
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        renderedBadges.clear();

        int x = this.x;
        int y = this.y;

        y += ((this.textRenderer.fontHeight + 3) * 2);

        List<OrderedText> descLines = wrappedText;
        for(OrderedText line : descLines) {

            textRenderer.draw(matrices, line, x + 2, y - 6, 0xCCCCCC);

            y += this.textRenderer.fontHeight + 3;
        }

        if(isOriginRandom) {
            List<OrderedText> drawLines = textRenderer.wrapLines(randomOriginText, width);
            for(OrderedText line : drawLines) {
                y += this.textRenderer.fontHeight + 3;

                textRenderer.draw(matrices, line, x + 2, y, 0xCCCCCC);

            }
            y += 14;
        } else {
            List<PowerType<?>> types = ((List<PowerType<?>>)powerTypes);
            for(int i = 0; i < types.size(); i++) {
                PowerType<?> p = types.get(i);
                if(p.isHidden()) {
                    continue;
                }

                PowerTextHolder holder = mainPowerTexts.get(p);

                textRenderer.draw(matrices, holder.name, x, y, 0xFFFFFF);
                int tw = textRenderer.getWidth(holder.name);
                List<Badge> badges = BadgeManager.getPowerBadges(p.getIdentifier());
                int xStart = x + tw + 4;
                int bi = 0;
                for(Badge badge : badges) {
                    AdditionalCreationComponent.RenderedBadge renderedBadge = new AdditionalCreationComponent.RenderedBadge(p, badge,xStart + 10 * bi, y - 1);
                    renderedBadges.add(renderedBadge);
                    RenderSystem.setShaderTexture(0, badge.spriteId());
                    Drawer.drawTexture(matrices, xStart + 10 * bi, y - 1, 0, 0, 9, 9, 9, 9);
                    bi++;
                }

                for(OrderedText line : holder.desc) {
                    y += this.textRenderer.fontHeight + 3;

                    textRenderer.draw(matrices, line, x + 2, y, 0xCCCCCC);

                }

                if(i - 1 < types.size()){
                    y += 14;
                }
            }
        }

//        int x = this.x;
//        int y = this.y;
//
//        if (this.horizontalSizing.get().method == Sizing.Method.CONTENT) {
//            x += this.horizontalSizing.get().value;
//        }
//        if (this.verticalSizing.get().method == Sizing.Method.CONTENT) {
//            y += this.verticalSizing.get().value;
//        }
//
//        switch (this.verticalTextAlignment) {
//            case CENTER -> y += (this.height - ((this.wrappedText.size() * (this.textRenderer.fontHeight + 2)) - 2)) / 2;
//            case BOTTOM -> y += this.height - ((this.wrappedText.size() * (this.textRenderer.fontHeight + 2)) - 2);
//        }
//
//        for (int i = 0; i < this.wrappedText.size(); i++) {
//            var renderText = this.wrappedText.get(i);
//            int renderX = x;
//
//            switch (this.horizontalTextAlignment) {
//                case CENTER -> renderX += (this.width - this.textRenderer.getWidth(renderText)) / 2;
//                case RIGHT -> renderX += this.width - this.textRenderer.getWidth(renderText);
//            }
//
//            if (this.shadow) {
//                this.textRenderer.drawWithShadow(matrices, renderText, renderX, y + i * 11, this.color.get().argb());
//            } else {
//                this.textRenderer.draw(matrices, renderText, renderX, y + i * 11, this.color.get().argb());
//            }
//        }
    }

    @Override
    public void drawTooltip(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        if(renderBadgeTooltip(matrices, mouseX, mouseY)) return;

        super.drawTooltip(matrices, mouseX, mouseY, partialTicks, delta);
    }

    private boolean renderBadgeTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        for(AdditionalCreationComponent.RenderedBadge rb : renderedBadges) {
            if(mouseX >= rb.x &&
                    mouseX < rb.x + 9 &&
                    mouseY >= rb.y &&
                    mouseY < rb.y + 9 &&
                    rb.hasTooltip()) {
                int widthLimit = width;
                Drawer.drawTooltip(matrices, mouseX, mouseY, rb.getTooltipComponents(textRenderer, widthLimit));

                return true;
            }
        }

        return false;
    }

    public record PowerTextHolder(OrderedText name, List<OrderedText> desc){

    }
}
