package io.blodhgarm.personality.client.gui.screens;

import io.blodhgarm.personality.Networking;
import io.blodhgarm.personality.client.gui.ThemeHelper;
import io.blodhgarm.personality.client.gui.utils.owo.ExtraSurfaces;
import io.blodhgarm.personality.packets.CharacterDeathPackets;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CharacterDeathScreen extends BaseOwoScreen<FlowLayout> {

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.child(
                Containers.verticalFlow(Sizing.content(), Sizing.content())
                        .configure((FlowLayout layout) -> {
                            layout.surface(ThemeHelper.dynamicSurface())
                                    .horizontalAlignment(HorizontalAlignment.CENTER)
                                    .padding(Insets.of(4))
                                    .positioning(Positioning.relative(50, 50));
                        })
                        .child(
                                Components.label(Text.of("It seems that your characters has finally come to its end!"))
                                        .horizontalTextAlignment(HorizontalAlignment.CENTER)
                                        .maxWidth(300)
                                        .margins(Insets.of(2))
                        )
                        .child(
                                Containers.verticalFlow(Sizing.content(), Sizing.content())
                                        .child(
                                                Components.label(Text.literal("What did your character die to exactly?"))
                                                        .maxWidth(305)
                                                        .margins(Insets.of(4, 0,4,4))
                                        )
                                        .child(
                                                Components.label(Text.literal("e.g. \"has died from sneezing too hard\"").formatted(Formatting.GRAY))
                                                        .margins(Insets.of(2, 0,4,4))
                                        )
                                        .child(
                                                Components.textBox(Sizing.fixed(290))
                                                        .configure((TextBoxComponent c) -> c.setMaxLength(120))
                                                        .margins(Insets.of(3, 0,3,3))
                                                        .tooltip(List.of(Text.of("Was it due to a medical condition or was "), Text.of("it due to a huge brawl you got into?")))
                                                        .id("death_message")
                                        )
                                        .child(
                                                Containers.horizontalFlow(Sizing.content(), Sizing.content())
                                                        .child(Components.button(Text.of("Keep Living!"), component -> {
                                                            Networking.sendC2S(new CharacterDeathPackets.DeathScreenOpenResponse(false));

                                                            this.close();
                                                        }))
                                                        .child(Components.button(Text.of("Welcome Death"), component -> {
                                                            String message = rootComponent.childById(TextBoxComponent.class, "death_message").getText();

                                                            Networking.sendC2S(new CharacterDeathPackets.CustomDeathMessage(message));

                                                            this.close();
                                                        })).gap(30)
                                                        .padding(Insets.of(4))
                                        )
                                        .surface(ExtraSurfaces.INVERSE_PANEL)
                                        .horizontalAlignment(HorizontalAlignment.CENTER)
                                        .padding(Insets.of(2))
                                        .margins(Insets.of(2))
                        )

        );
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
