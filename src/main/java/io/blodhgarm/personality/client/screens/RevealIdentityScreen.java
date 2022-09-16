package io.blodhgarm.personality.client.screens;

import io.blodhgarm.personality.client.screens.components.FaceComponent;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class RevealIdentityScreen extends BaseOwoScreen<FlowLayout> {

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        root.alignment(HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM);

        root.child(
                Containers.verticalScroll(Sizing.fixed(108), Sizing.fill(60),
                                Containers.verticalFlow(Sizing.fixed(108), Sizing.content())
                                        .child(entry("Short Range").margins(Insets.top(4)))
                                        .child(entry("Long Range"))
                                        .child(Components.box(Sizing.fixed(72), Sizing.fixed(1))
                                                .color(new Color(0.776F,0.776F,0.776F))
                                                .margins(Insets.of(3,3,10,0)))
                                        .child(playerEntry())
                                        .child(playerEntry())
                                        .child(playerEntry())
                                        .child(playerEntry())
                                        .child(playerEntry())
                                        .child(playerEntry())
                        )
                        .padding(Insets.of(4,5,8,4))
                        .surface(Surface.DARK_PANEL)
                        .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                        .margins(Insets.of(0,3, 0, 2))
        );
    }

    private Component entry(String text) {
        return Containers.horizontalFlow(Sizing.fixed(100-8), Sizing.fixed(24))
                .child( Components.label(Text.literal(text)).color(new Color(0.1F,0.1F,0.1F))
                        .margins(Insets.left(19)))
                .margins(Insets.bottom(1))
                .alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
                .surface(Surface.PANEL);
    }

    private Component playerEntry() {
        return Containers.horizontalFlow(Sizing.fixed(100-8), Sizing.fixed(24))
//                .child( character() )
                .child( new FaceComponent(client.player.getUuidAsString()))
                .child( Components.label(client.player.getDisplayName()).color(new Color(0.1F,0.1F,0.1F)) )
                .margins(Insets.bottom(1))
                .alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
                .surface(Surface.PANEL);
    }

    private Component character() {
        EntityComponent<PlayerEntity> c = Components.entity(Sizing.fixed(7), EntityComponent.createRenderablePlayer(client.player.getGameProfile()));
//        c.scaleToFit(true);
        c.margins(Insets.of(0,3,6,6));
        return c;
    }
}
