package io.blodhgarm.personality.client.gui.screens;

import com.mojang.logging.LogUtils;
import io.blodhgarm.personality.api.reveal.InfoRevealLevel;
import io.blodhgarm.personality.client.gui.builders.SimpleRadialLayoutBuilder;
import io.blodhgarm.personality.impl.RevelCharacterInfo;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RevealIdentityScreen extends BaseOwoScreen<FlowLayout> {

    private static final Logger LOGGER = LogUtils.getLogger();

    private SimpleRadialLayoutBuilder revealLevel;
    private SimpleRadialLayoutBuilder revealRange;

    @Nullable
    private InfoRevealLevel selectedRevealLevel = null;

    @Nullable
    private RevelCharacterInfo.RevealRange selectedRevealRange = null;

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {

//        for(int i = 0; i < 5; i++){
//            components.add(Components.box(Sizing.fixed(20), Sizing.fixed(20))
//                    .color(Color.GREEN)
//            );
//        }

//        components.add(Containers.verticalFlow(Sizing.fixed(0), Sizing.fixed(0)));

        revealLevel = new SimpleRadialLayoutBuilder().adjustRadi(0, 15, 140, 80)
                .addComponents(
                        Arrays.stream(InfoRevealLevel.values())
                                .map(level -> {
                                    return Containers.verticalFlow(Sizing.fixed(50), Sizing.fixed(50))
                                            .child(
                                                    Components.label(level.getTranslation())
                                            )
                                            .verticalAlignment(VerticalAlignment.CENTER)
                                            .horizontalAlignment(HorizontalAlignment.CENTER)
                                            .id(level.toString());
                                }).toList()
                )
                .setComponentId("REVEAL_LEVEL")
                .onSelection(component -> {
                    if (component instanceof BaseParentComponent baseParentComponent) {
                        String id = baseParentComponent.children().get(0).id();

                        if (id != null) {
                            try {
                                this.selectedRevealLevel = InfoRevealLevel.valueOf(id);

                                Component mainFlowLayout = this.uiAdapter.rootComponent.childById(Component.class, "REVEAL_LEVEL");

                                this.uiAdapter.rootComponent.removeChild(mainFlowLayout);

                                this.uiAdapter.rootComponent.child(revealRange.getComponent(this.uiAdapter.rootComponent));

                                return true;
                            } catch (IllegalArgumentException e) {
                                LOGGER.warn(e.getMessage());
                            }
                        }
                    }

                    return false;
                });

        List<Component> components = Arrays.stream(RevelCharacterInfo.RevealRange.values())
                .map(range -> {
                    return Containers.verticalFlow(Sizing.fixed(50), Sizing.fixed(50))
                            .child(
                                    Components.label(range.getTranslation())
                            )
                            .verticalAlignment(VerticalAlignment.CENTER)
                            .horizontalAlignment(HorizontalAlignment.CENTER)
                            .id(range.toString());
                })
                .collect(Collectors.toList());

        components.add(Containers.verticalFlow(Sizing.fixed(0), Sizing.fixed(0)));

        revealRange = new SimpleRadialLayoutBuilder().adjustRadi(0, 15, 140, 80)
                .addComponents(components)
                .setComponentId("REVEAL_RANGE")
                .onSelection(component -> {
                    if (component instanceof BaseParentComponent baseParentComponent) {
                        String id = baseParentComponent.children().get(0).id();

                        if (id != null) {
                            try {
                                this.selectedRevealRange = RevelCharacterInfo.RevealRange.valueOf(id);

                                Component mainFlowLayout = this.uiAdapter.rootComponent.childById(Component.class, "REVEAl_RANGE");

                                this.uiAdapter.rootComponent.removeChild(mainFlowLayout);

                                return true;
                            } catch (IllegalArgumentException e) {
                                LOGGER.warn(e.getMessage());
                            }
                        }
                    }

                    return false;
                });

        root.child(revealLevel.getComponent(root));

        root.mouseUp().subscribe((mouseX, mouseY, button) -> {
            Component component = root.childAt(Math.round((float) mouseX), Math.round((float) mouseY));

            if(component != null
                    && (!Objects.equals(component.id(), revealLevel.mainComponentID) && !Objects.equals(component.id(), revealRange.mainComponentID))) return false;

            if((button | GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_MOUSE_BUTTON_LEFT){
                this.close();

                return true;
            }

            return false;
        });

        uiAdapter.enableInspector = false;
        uiAdapter.globalInspector = false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
