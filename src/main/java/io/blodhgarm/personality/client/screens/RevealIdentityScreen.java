package io.blodhgarm.personality.client.screens;

import io.blodhgarm.personality.Networking;
import io.blodhgarm.personality.client.screens.components.FaceComponent;
import io.blodhgarm.personality.packets.RevealCharacterC2SPacket;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
                                        .child(entry("Short Range", 7).margins(Insets.top(4)))
                                        .child(entry("Long Range", 5))
                                        .child(Components.box(Sizing.fixed(72), Sizing.fixed(1))
                                                .color(new Color(0.776F,0.776F,0.776F))
                                                .margins(Insets.of(3,3,10,0)))
                                        .children(getPlayerComponents())
                        )
                        .padding(Insets.of(4,5,8,4))
                        .surface(Surface.DARK_PANEL)
                        .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                        .margins(Insets.of(0,3, 0, 2))
        );
    }

    private Component entry(String text, int range) {

        Component p = Components.button(Text.literal(text), 92,20, button -> {
            client.setScreen(null);
            Networking.sendC2S(new RevealCharacterC2SPacket.InRange(range));
        });

//        ParentComponent c = Containers.horizontalFlow(Sizing.fixed(100-8), Sizing.fixed(24))
//                .child( Components.label(Text.literal(text)).color(new Color(0.1F,0.1F,0.1F))
//                        .margins(Insets.left(19)))
//                .margins(Insets.bottom(1))
//                .alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
//                .surface(Surface.PANEL);
//
//        c.mouseUp().subscribe((mouseX, mouseY, button) -> {
//            if (button == 1) {
//                Networking.sendC2S(new RevealCharacterC2SPacket.InRange(range));
//                return true;
//            }
//            return false;
//        });

        return p;
    }

    private List<Component> getPlayerComponents() {
        List<Component> components = new ArrayList<>();

        List<AbstractClientPlayerEntity> playersSortedByDistance = client.world.getPlayers().stream()
                .filter(p -> client.player != p && p.distanceTo(client.player) < 15)
                .sorted((o1, o2) -> (int) (o1.distanceTo(client.player) - o2.distanceTo(client.player))).toList();

        for (PlayerEntity player : playersSortedByDistance) {


            Component p = Components.button(player.getDisplayName(), 92,20, button -> {
                client.setScreen(null);
                Networking.sendC2S(new RevealCharacterC2SPacket.ToPlayer(player.getUuidAsString()));
            });

//            ParentComponent c = Containers.horizontalFlow(Sizing.fixed(100-8), Sizing.fixed(24))
//                    .child( new FaceComponent(player.getUuidAsString()))
//                    .child( Components.label(player.getDisplayName()).color(new Color(0.1F,0.1F,0.1F)) )
//                    .margins(Insets.bottom(1))
//                    .alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
//                    .surface(Surface.PANEL);
//
//            c.mouseUp().subscribe((mouseX, mouseY, button) -> {
//                System.out.println("CLICKED");
//                if (button == 0) {
//                    System.out.println("CLICKED WITH 0");
//                    Networking.sendC2S(new RevealCharacterC2SPacket.ToPlayer(player.getUuidAsString()));
//                    return true;
//                }
//                return false;
//            });

            components.add(p);

        }

        return components;
    }

}
