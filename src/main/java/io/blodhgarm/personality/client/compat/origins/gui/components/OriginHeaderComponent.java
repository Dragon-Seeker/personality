package io.blodhgarm.personality.client.compat.origins.gui.components;

import io.blodhgarm.personality.client.screens.PersonalityCreationScreen;
import io.github.apace100.origins.origin.Origin;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.HorizontalFlowLayout;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.util.Identifier;

public class OriginHeaderComponent extends HorizontalFlowLayout {

    public static Identifier ORIGINS_GUI_TEXTURE = new Identifier("personality", "textures/gui/origins_gui.png");

    private boolean shortVersion = false;

    private Origin origin;

    public OriginHeaderComponent(Sizing horizontalSizing, Sizing verticalSizing, Origin origin) {
        super(horizontalSizing, verticalSizing);

        this.origin = origin;

        this.build(this);
    }

    public OriginHeaderComponent shortVersion(boolean shortVersion){
        this.shortVersion = shortVersion;

        this.clearChildren();

        this.build(this);

        return this;
    }

    public void Origin(Origin origin){
        this.origin = origin;

        onUpdate(this);
    }

    public void build(FlowLayout rootComponent){
        rootComponent
                //Square Symbol
                .child(
                        Components.texture(ORIGINS_GUI_TEXTURE, 0,0, 26, 26, 48, 48)
                                //.margins(Insets.of(0, -6,0,0))
                                .zIndex(2)
                        //.positioning(Positioning.absolute(10,10))
                )
                //Bar Length
                .child(
                        Components.texture(ORIGINS_GUI_TEXTURE, 29,0, 7, 16,48, 48)
                                .sizing(Sizing.fixed(!shortVersion ? 122 : (PersonalityCreationScreen.guiScale4OrAbove() ? 72 : 92)), Sizing.fixed(16))// 92
                                .margins(Insets.left(-2))
                                .zIndex(0)
                        //.positioning(Positioning.absolute(15,15))
                )
                //Bar end
                .child(
                        Components.texture(ORIGINS_GUI_TEXTURE, 36,0, 3, 16,48, 48)
                        //.positioning(Positioning.absolute(15 + 122,15))
                );

        if(!shortVersion) {
            rootComponent
                    //Origin Impact
                    .child(
                            new OriginImpactComponent(origin.getImpact())
                                    .positioning(Positioning.absolute(117, 9))
                                    .id("origin_impact")
                    );
        }

        rootComponent
                //Origin Label
                .child(
                        Components.label(origin.getName())
                                .shadow(true)
                                .positioning(Positioning.absolute(29, 9))
                                .id("origin_name")
                )
                //Origin Display Icon
                .child(
                        Components.item(origin.getDisplayItem())
                                .positioning(Positioning.absolute(5, 5))
                                .id("origin_icon")
                );
    }

    public void onUpdate(FlowLayout rootComponent){
        if(!shortVersion) {
            rootComponent.childById(OriginImpactComponent.class, "origin_impact").setImpact(this.origin.getImpact());
        }

        rootComponent.childById(LabelComponent.class, "origin_name").text(this.origin.getName());
        rootComponent.childById(ItemComponent.class, "origin_icon").stack(this.origin.getDisplayItem());
    }
}
