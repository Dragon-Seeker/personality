package io.blodhgarm.personality.client.gui.screens;

import io.blodhgarm.personality.client.ThemeHelper;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class PersonalitySubScreen extends BaseOwoScreen<FlowLayout> {

    public PersonalitySubScreen(){
        super(Text.of("sub_screen"));
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        FlowLayout mainFlowLayout = Containers.verticalFlow(Sizing.content(), Sizing.content());

        mainFlowLayout.child(
                Components.label(Text.of("Personality Sub Menu"))
                        .color(ThemeHelper.dynamicColor())
                        .margins(Insets.bottom(4))
        );

        mainFlowLayout.child(
                Components.button(Text.of("Character Info"), (ButtonComponent component) -> {
                        MinecraftClient.getInstance().setScreen(new CharacterInfoScreen());
                })
                .margins(Insets.bottom(4))
        );

        mainFlowLayout.child(
                Components.button(Text.of("Full Character List"), (ButtonComponent component) -> {

                })
                .margins(Insets.bottom(4))
        );

        mainFlowLayout
                .verticalAlignment(VerticalAlignment.CENTER)
                .surface(ThemeHelper.dynamicSurface())
                .padding(Insets.of(4))
                .positioning(Positioning.relative(50, 50));

        rootComponent.child(mainFlowLayout);

    }
}
