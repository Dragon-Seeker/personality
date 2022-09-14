package io.wispforest.personality.client.screens;

import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.origins.badge.Badge;
import io.github.apace100.origins.badge.BadgeManager;
import io.github.apace100.origins.origin.Origin;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class OriginInfoContainer extends VerticalFlowLayout {

    public Origin origin;

    private Text randomOriginText;
    boolean isOriginRandom;

    protected OriginInfoContainer(Sizing horizontalSizing, Sizing verticalSizing, Origin origin, Text randomOriginText, boolean isOriginRandom) {
        super(horizontalSizing, verticalSizing);
        this.origin = origin;

        this.randomOriginText = randomOriginText;
        this.isOriginRandom = isOriginRandom;

        build(this);
    }

    public OriginInfoContainer origin(Origin origin, Text randomOriginText, boolean isOriginRandom){
        this.origin = origin;

        this.randomOriginText = randomOriginText;
        this.isOriginRandom = isOriginRandom;

        this.onUpdate(this);

        return this;
    }

    public void build(FlowLayout rootComponent){
        //rootComponent.child(Components.box(Sizing.fill(100), Sizing.fixed(14)).color(Color.ofArgb(0)));

        rootComponent.child(Components.label(origin.getDescription())
                .color(Color.ofRgb(0xCCCCCC))
                .id("origin_info")
                .horizontalSizing(Sizing.fill(100))
                .margins(Insets.of(14, 12, 3, 0))
        );

        rootComponent.child(createSecondaryInfoComponent());
    }

    public void onUpdate(FlowLayout rootComponent){
        rootComponent.childById(LabelComponent.class, "origin_info").text(origin.getDescription());

        rootComponent.removeChild(rootComponent.childById(FlowLayout.class, "origin_secondary_info"));

        rootComponent.child(createSecondaryInfoComponent());
    }

    public FlowLayout createSecondaryInfoComponent(){
        FlowLayout mainComponent = Containers.verticalFlow(Sizing.content(), Sizing.content());

        mainComponent.id("origin_secondary_info");

        if(isOriginRandom) {
            mainComponent.child(Components.label(randomOriginText)
                    .horizontalSizing(Sizing.fill(100)));
        } else {
            for (PowerType<?> powerType : origin.getPowerTypes()) {
                if(powerType.isHidden()) {
                    continue;
                }

                List<Badge> badges = BadgeManager.getPowerBadges(powerType.getIdentifier());

                FlowLayout labelLayout = Containers.horizontalFlow(Sizing.content(), Sizing.content())
                    .child(Components.label(powerType.getName().formatted(Formatting.UNDERLINE)));

                for(Badge badge : badges){
                    labelLayout.child(new OriginBadgeComponent(powerType, badge)
                        .margins(Insets.left(4)));
                }

                mainComponent.child(labelLayout);
                mainComponent.child(Components.label(powerType.getDescription())
                    .color(Color.ofRgb(0xCCCCCC))
                    .horizontalSizing(Sizing.fill(100))
                    .margins(Insets.of(4, 12, 3, 0))
                );
            }
        }

        return mainComponent;
    }

//    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
//        renderedBadges.clear();
//
//        int x = this.x;
//        int y = this.y;
//
//        y += ((this.textRenderer.fontHeight + 3) * 2);
//
//        List<OrderedText> descLines = wrappedText;
//        for(OrderedText line : descLines) {
//
//            textRenderer.draw(matrices, line, x + 2, y - 6, 0xCCCCCC);
//
//            y += this.textRenderer.fontHeight + 3;
//        }
//
//        if(isOriginRandom) {
//            List<OrderedText> drawLines = textRenderer.wrapLines(randomOriginText, width);
//            for(OrderedText line : drawLines) {
//                y += this.textRenderer.fontHeight + 3;
//
//                textRenderer.draw(matrices, line, x + 2, y, 0xCCCCCC);
//
//            }
//            y += 14;
//        } else {
//            List<PowerType<?>> types = ((List<PowerType<?>>)powerTypes);
//            for(int i = 0; i < types.size(); i++) {
//                PowerType<?> p = types.get(i);
//                if(p.isHidden()) {
//                    continue;
//                }
//
//                OriginDescriptionComponent.PowerTextHolder holder = mainPowerTexts.get(p);
//
//                textRenderer.draw(matrices, holder.name, x, y, 0xFFFFFF);
//                int tw = textRenderer.getWidth(holder.name);
//                List<Badge> badges = BadgeManager.getPowerBadges(p.getIdentifier());
//                int xStart = x + tw + 4;
//                int bi = 0;
//                for(Badge badge : badges) {
//                    AdditionalCreationComponent.RenderedBadge renderedBadge = new AdditionalCreationComponent.RenderedBadge(p, badge,xStart + 10 * bi, y - 1);
//                    renderedBadges.add(renderedBadge);
//                    RenderSystem.setShaderTexture(0, badge.spriteId());
//Drawer.drawTexture(matrices, xStart + 10 * bi, y - 1, 0, 0, 9, 9, 9, 9);
//                    bi++;
//                }
//
//                for(OrderedText line : holder.desc) {
//                    y += this.textRenderer.fontHeight + 3;
//
//                    textRenderer.draw(matrices, line, x + 2, y, 0xCCCCCC);
//
//                }
//
//                if(i - 1 < types.size()){
//                    y += 14;
//                }
//            }
//        }
//
////        int x = this.x;
////        int y = this.y;
////
////        if (this.horizontalSizing.get().method == Sizing.Method.CONTENT) {
////            x += this.horizontalSizing.get().value;
////        }
////        if (this.verticalSizing.get().method == Sizing.Method.CONTENT) {
////            y += this.verticalSizing.get().value;
////        }
////
////        switch (this.verticalTextAlignment) {
////            case CENTER -> y += (this.height - ((this.wrappedText.size() * (this.textRenderer.fontHeight + 2)) - 2)) / 2;
////            case BOTTOM -> y += this.height - ((this.wrappedText.size() * (this.textRenderer.fontHeight + 2)) - 2);
////        }
////
////        for (int i = 0; i < this.wrappedText.size(); i++) {
////            var renderText = this.wrappedText.get(i);
////            int renderX = x;
////
////            switch (this.horizontalTextAlignment) {
////                case CENTER -> renderX += (this.width - this.textRenderer.getWidth(renderText)) / 2;
////                case RIGHT -> renderX += this.width - this.textRenderer.getWidth(renderText);
////            }
////
////            if (this.shadow) {
////                this.textRenderer.drawWithShadow(matrices, renderText, renderX, y + i * 11, this.color.get().argb());
////            } else {
////                this.textRenderer.draw(matrices, renderText, renderX, y + i * 11, this.color.get().argb());
////            }
////        }
//    }

    public record PowerTextHolder(OrderedText name, List<OrderedText> desc){

    }



}
