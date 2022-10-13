package io.blodhgarm.personality.client.compat.origins.gui.components;

import io.blodhgarm.personality.client.screens.PersonalityCreationScreen;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.HorizontalFlowLayout;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public class OriginHeaderComponent extends VerticalFlowLayout {

    public static Identifier ORIGINS_GUI_TEXTURE = new Identifier("personality", "textures/gui/origins_gui.png");

    private boolean shortVersion = false;
    private boolean showLayerInfo = false;

    private Consumer<ButtonComponent> leftButtonAction = null;
    private Consumer<ButtonComponent> rightButtonAction = null;

    private Origin origin;
    private OriginLayer layer;

    public OriginHeaderComponent(Sizing horizontalSizing, Sizing verticalSizing, Origin origin, OriginLayer layer) {
        super(horizontalSizing, verticalSizing);

        this.origin = origin;
        this.layer = layer;

        this.build(this);
    }

    public OriginHeaderComponent shortVersion(boolean shortVersion){
        this.shortVersion = shortVersion;

        return this;
    }

    public OriginHeaderComponent showLayerInfo(boolean showLayerInfo){
        this.showLayerInfo = showLayerInfo;

        return this;
    }

    public OriginHeaderComponent showButtons(Consumer<ButtonComponent> leftButtonAction, Consumer<ButtonComponent> rightButtonAction){
        this.leftButtonAction = leftButtonAction;
        this.rightButtonAction = rightButtonAction;

        return this;
    }

    public void Origin(Origin origin, OriginLayer layer){
        this.origin = origin;
        this.layer = layer;

        onUpdate();
    }

    public OriginHeaderComponent rebuildComponent(){
        this.clearChildren();

        this.build(this);

        return this;
    }

    private void build(FlowLayout rootComponent){
        int v = 0; //(showLayerInfo ? 16 : 0);
        int regionHeight = 16; //showLayerInfo ? 24 : 16;

        int height = 16; //showLayerInfo ? 24 : 16;

        int barSizing = !shortVersion ? 122 : (PersonalityCreationScreen.guiScale4OrAbove() ? 72 : 92);

        rootComponent.child(
                Containers.horizontalFlow(Sizing.fixed(25 + barSizing + 3), Sizing.fixed(26))
                        //Square Symbol
                        .child(
                                Components.texture(ORIGINS_GUI_TEXTURE, 0,0, 26, 26, 48, 48)
                                        //.margins(Insets.of(0, -6,0,0))
                                        .zIndex(2)
                                //.positioning(Positioning.absolute(10,10))
                        )
                        //Bar Length
                        .child(
                                Components.texture(ORIGINS_GUI_TEXTURE, 26, v, 7, regionHeight,48, 48)
                                        .sizing(Sizing.fixed(barSizing), Sizing.fixed(height))// 92
                                        .margins(Insets.left(-2))
                                        .zIndex(0)
                                //.positioning(Positioning.absolute(15,15))
                        )
                        //Bar end
                        .child(
                                Components.texture(ORIGINS_GUI_TEXTURE, 33, v, 3, regionHeight,48, 48)
                                //.positioning(Positioning.absolute(15 + 122,15))
                        )
                        //.allowOverflow(true)
                        .verticalAlignment(VerticalAlignment.CENTER)
                        .positioning(showLayerInfo ? Positioning.absolute(0,7) : Positioning.layout())
                        .id("bq_components")
        );

        rootComponent.allowOverflow(true);

        if(!shortVersion) {
            rootComponent
                    //Origin Impact
                    .child(
                            new OriginImpactComponent(origin.getImpact())
                                    .positioning(Positioning.absolute(117, 9))
                                    .id("origin_impact")
                    );
        }

        if(showLayerInfo){
            FlowLayout layerInfo = Containers.horizontalFlow(Sizing.content(), Sizing.content());

            boolean bl = leftButtonAction != null && rightButtonAction != null;

            if(bl) {
                layerInfo.child(
                        Components.button(Text.of("<"), leftButtonAction)
                                .sizing(Sizing.fixed(10))
                        //.positioning(Positioning.absolute(70, 2))
                );
            }

            layerInfo
                    .child(
                            Containers.horizontalFlow(Sizing.fixed(bl ? 67 : 97), Sizing.content())
                                    .child(
                                            Components.label(Text.translatable(layer.getTranslationKey()))
                                                    .shadow(true)
                                                    .margins(Insets.top(1))
                                                    //.positioning(Positioning.absolute(29, 1))
                                                    .id("origin_layer")
                                    )
                                    .horizontalAlignment(HorizontalAlignment.CENTER)
                    );

            if(bl) {
                layerInfo
                        .child(
                                Components.button(Text.of(">"), rightButtonAction)
                                        .sizing(Sizing.fixed(10))
                                //.positioning(Positioning.absolute(82, 2))
                        );
            }


            rootComponent.child(
                    layerInfo
                        .verticalAlignment(VerticalAlignment.TOP)
                        .positioning(Positioning.absolute(28, 1))
            );

//            rootComponent
//                    .child(
//                        Components.label(Text.translatable(layer.getTranslationKey()))
//                                .shadow(true)
//                                .positioning(Positioning.absolute(29, 1))
//                                .id("origin_layer")
//                    );

//            rootComponent
//                    .child(
//                            Components.box(Sizing.fixed(shortVersion ? 89 : 92), Sizing.fixed(1))
//                                    .color(Color.ofArgb(0xFF634809))
//                                    .positioning(Positioning.absolute(27, 12))
//                    );

//            rootComponent
//                    .child(
//                            Components.button(Text.of("<"), (ButtonComponent buttonComponent) -> {})
//                                    .sizing(Sizing.fixed(10))
//                                    .positioning(Positioning.absolute(70, 2))
//                    ).child(
//                            Components.button(Text.of(">"), (ButtonComponent buttonComponent) -> {})
//                                    .positioning(Positioning.absolute(82, 2))
//                                    .sizing(Sizing.fixed(10))
//                    );
        }

        int nameY = showLayerInfo ? 16 : 9; //showLayerInfo ? 14 : 9;

        rootComponent
                //Origin Label
                .child(
                        Components.label(origin.getName())
                                .shadow(true)
                                .positioning(Positioning.absolute(29, nameY))
                                .id("origin_name")
                )
                //Origin Display Icon
                .child(
                        Components.item(origin.getDisplayItem())
                                .positioning(Positioning.absolute(5, showLayerInfo ? 12 : 5))
                                .id("origin_icon")
                );
    }

    public void onUpdate(){
        if(!shortVersion) {
            this.childById(OriginImpactComponent.class, "origin_impact").setImpact(this.origin.getImpact());
        }

        if(showLayerInfo) {
            this.childById(LabelComponent.class, "origin_layer").text(Text.translatable(this.layer.getTranslationKey()));
        }

        this.childById(LabelComponent.class, "origin_name").text(this.origin.getName());
        this.childById(ItemComponent.class, "origin_icon").stack(this.origin.getDisplayItem());
    }
}
