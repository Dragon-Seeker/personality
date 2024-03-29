package io.blodhgarm.personality.compat.origins.client.gui.components;

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
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class OriginInfoContainer extends VerticalFlowLayout {

    private Origin origin;
    private Text randomOriginText;
    private boolean isOriginRandom;

    public OriginInfoContainer(Sizing horizontalSizing, Sizing verticalSizing, Origin origin, Text randomOriginText, boolean isOriginRandom) {
        super(horizontalSizing, verticalSizing);

        this.origin = origin; this.randomOriginText = randomOriginText; this.isOriginRandom = isOriginRandom;

        build(this);
    }

    public void origin(Origin origin){
        this.origin = origin; this.randomOriginText = null; this.isOriginRandom = false;

        this.onUpdate(this);
    }

    public void randomOrigin(Origin origin, Text randomOriginText){
        this.origin = origin; this.randomOriginText = randomOriginText; this.isOriginRandom = true;

        this.onUpdate(this);
    }

    public void build(FlowLayout rootComponent){
        rootComponent.child(Components.label(origin.getDescription())
                .color(Color.ofRgb(0xCCCCCC))
                .id("origin_info")
                .horizontalSizing(Sizing.fill(100))
                .margins(Insets.of(14, 12, 3, 0))
        );

        rootComponent.child(createSecondaryInfoComponent(origin, randomOriginText, isOriginRandom));
    }

    public void onUpdate(FlowLayout rootComponent){
        rootComponent.childById(LabelComponent.class, "origin_info").text(origin.getDescription());

        rootComponent.removeChild(rootComponent.childById(FlowLayout.class, "origin_secondary_info"));

        rootComponent.child(createSecondaryInfoComponent(origin, randomOriginText, isOriginRandom));
    }

    public FlowLayout createSecondaryInfoComponent(Origin origin, Text randomOriginText, boolean isOriginRandom){
        FlowLayout mainComponent = Containers.verticalFlow(Sizing.content(), Sizing.content());

        mainComponent.id("origin_secondary_info");

        if(isOriginRandom) {
            mainComponent.child(Components.label(randomOriginText)
                    .horizontalSizing(Sizing.fill(100)));
        } else {
            for (PowerType<?> powerType : origin.getPowerTypes()) {
                if(powerType.isHidden()) continue;

                List<Badge> badges = BadgeManager.getPowerBadges(powerType.getIdentifier());

                FlowLayout labelLayout = Containers.horizontalFlow(Sizing.content(), Sizing.content())
                    .child(Components.label(powerType.getName().formatted(Formatting.UNDERLINE)));

                for(Badge badge : badges){
                    labelLayout.child(new OriginBadgeComponent(powerType, badge).margins(Insets.left(4)));
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
}
