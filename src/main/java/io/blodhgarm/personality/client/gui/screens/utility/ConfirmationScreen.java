package io.blodhgarm.personality.client.gui.screens.utility;

import io.blodhgarm.personality.client.PersonalityClient;
import io.blodhgarm.personality.client.gui.ThemeHelper;
import io.blodhgarm.personality.client.gui.utils.owo.ExtraSurfaces;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfirmationScreen extends BaseOwoScreen<FlowLayout> {

    @Nullable private Screen originScreen = null;

    private Runnable closeAction = () -> {
        if(originScreen == null){
            this.close();
        } else {
            MinecraftClient.getInstance().setScreen(originScreen);
        }
    };

    private final Runnable acceptAction;

    private Text boxLabel = Text.of("Are you sure that you want to do this?");
    private Text bodyText = null;

    public ConfirmationScreen(Screen originScreen, Runnable acceptAction){
        this.originScreen = originScreen;
        this.acceptAction = acceptAction;
    }

    public ConfirmationScreen(Runnable deniedAction, Runnable acceptAction){
        this.closeAction = deniedAction;
        this.acceptAction = acceptAction;
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    public ConfirmationScreen setLabel(Text boxLabel){
        this.boxLabel = boxLabel;

        return this;
    }

    public ConfirmationScreen setBodyText(Text bodyText){
        this.bodyText = bodyText;

        return this;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        FlowLayout confirmationLayout = Containers.verticalFlow(Sizing.content(), Sizing.content())
                .configure(
                        layout -> layout.padding(Insets.of(6))
                                .surface(ThemeHelper.dynamicSurface())
                                .horizontalAlignment(HorizontalAlignment.CENTER)
                                .verticalAlignment(VerticalAlignment.CENTER)
                                .positioning(PersonalityClient.customRelative(200, 50))
                );

        confirmationLayout.child(
                Components.label(boxLabel)
                        .maxWidth(235)//135
                        .margins(Insets.bottom(3))
        ).child(
               Containers.verticalScroll(Sizing.content(), Sizing.fixed(90),
                       Containers.verticalFlow(Sizing.content(), Sizing.content())
                               .configure((FlowLayout layout) -> {
                                   if(bodyText != null) {
                                       layout.child(
                                               Components.label(bodyText)
//                                                .horizontalTextAlignment(HorizontalAlignment.CENTER)
                                                       .maxWidth(235)//135
                                                       .margins(Insets.bottom(3))
                                       );
                                   }
                               })
                               .id("confirmation_info_layout")
                               .margins(Insets.bottom(3))
               ).surface(ExtraSurfaces.INVERSE_PANEL)
                       .padding(Insets.of(6))
        ).child(
                Containers.horizontalFlow(Sizing.content(), Sizing.content())
                        .child(
                                Components.button(Text.of("No"), buttonComponent -> closeAction.run())
                                        .horizontalSizing(Sizing.fixed(45))
                        )
                        .child(
                                Components.button(Text.of("Yes"), buttonComponent -> {
                                            acceptAction.run();
                                            closeAction.run();
                                        })
                                        .horizontalSizing(Sizing.fixed(45))
                        )
                        .gap(10)
                        .horizontalAlignment(HorizontalAlignment.CENTER)
        );

        confirmationLayout
                .positioning().animate(500, Easing.LINEAR, PersonalityClient.customRelative(50, 50)).forwards();

        rootComponent.child(confirmationLayout);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
