package io.blodhgarm.personality.client.gui.screens;

import com.mojang.logging.LogUtils;
import io.blodhgarm.personality.Networking;
import io.blodhgarm.personality.api.reveal.InfoRevealLevel;
import io.blodhgarm.personality.client.PersonalityClient;
import io.blodhgarm.personality.client.gui.ThemeHelper;
import io.blodhgarm.personality.client.gui.components.builders.SimpleRadialLayoutBuilder;
import io.blodhgarm.personality.client.gui.utils.owo.ExtraSurfaces;
import io.blodhgarm.personality.api.reveal.RevelInfoManager;
import io.blodhgarm.personality.misc.pond.owo.AnimationExtension;
import io.blodhgarm.personality.packets.RevealCharacterC2SPacket;
import io.blodhgarm.personality.utils.DebugCharacters;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
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
    private FlowLayout confirmationLayout;

    @Nullable
    private InfoRevealLevel selectedRevealLevel = null;

    @Nullable
    private RevelInfoManager.RevealRange selectedRevealRange = null;

    private final List<String> closableComponentIds = List.of("REVEAL_LEVEL", "REVEAL_RANGE", "ROOT");

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        root.id("ROOT");

        confirmationLayout = Containers.verticalFlow(Sizing.content(), Sizing.content())
                .configure(
                        layout -> layout.padding(Insets.of(6))
                                .surface(ThemeHelper.dynamicSurface())
                                .horizontalAlignment(HorizontalAlignment.CENTER)
                                .verticalAlignment(VerticalAlignment.CENTER)
                                .positioning(PersonalityClient.customRelative(200, 50))
                );

        confirmationLayout.child(
                Components.label(Text.of("Are you sure that you want to send this info out?"))
                        .maxWidth(135)
                        .margins(Insets.bottom(3))
        ).child(
                Containers.verticalFlow(Sizing.content(), Sizing.content())
                        .surface(ExtraSurfaces.INVERSE_PANEL)
                        .padding(Insets.of(6))
                        .id("confirmation_info_layout")
                        .margins(Insets.bottom(3))
        ).child(
                Containers.horizontalFlow(Sizing.content(), Sizing.content())
                        .child(
                                Components.button(Text.of("No"), buttonComponent -> this.close())
                                        .horizontalSizing(Sizing.fixed(45))
                        )
                        .child(
                                Components.button(Text.of("Yes"), buttonComponent -> confirmSelection())
                                        .horizontalSizing(Sizing.fixed(45))
                        )
                        .gap(10)
                        .horizontalAlignment(HorizontalAlignment.CENTER)
        );

        //-----------------------------------------------------------------------------

        revealLevel = new SimpleRadialLayoutBuilder().adjustRadi(0, 15, 140, 80)
                .addComponents(
                        Arrays.stream(InfoRevealLevel.VALID_VALUES)
                                .map(level -> {
                                    return Containers.verticalFlow(Sizing.fixed(50), Sizing.fixed(50))
                                            .child(Components.label(level.getTranslation()))
                                            .verticalAlignment(VerticalAlignment.CENTER)
                                            .horizontalAlignment(HorizontalAlignment.CENTER)
                                            .id(level.toString());
                                }).toList()
                )
                .changeRadialStartOffset(-(Math.PI / 2))
                .setComponentId("REVEAL_LEVEL")
                .onSelection(component -> {
                    if (component instanceof BaseParentComponent baseParentComponent) {
                        String id = baseParentComponent.children().get(0).id();

                        if (id != null) {
                            try {
                                this.selectedRevealLevel = InfoRevealLevel.valueOf(id);

                                ((AnimationExtension<Positioning>) this.revealLevel.getComponent()
                                        .positioning().animate(500, Easing.LINEAR, PersonalityClient.customRelative(-200, 50)).forwards())
                                        .setOnCompletionEvent(animation -> this.uiAdapter.rootComponent.removeChild(this.revealLevel.getComponent()));

                                this.uiAdapter.rootComponent.child(this.revealRange.getComponent());

                                this.revealRange.getComponent()
                                        .positioning().animate(500, Easing.LINEAR, PersonalityClient.customRelative(50, 50)).forwards();

                                return true;
                            } catch (IllegalArgumentException e) {
                                LOGGER.warn(e.getMessage());
                            }
                        }
                    }

                    return false;
                });

        //-----------------------------------------------------------------------------

        List<Component> components = new ArrayList<>();

        components.add(Containers.verticalFlow(Sizing.fixed(0), Sizing.fixed(0)));

        components.addAll(
                Arrays.stream(RevelInfoManager.RevealRange.values())
                        .map(range -> {
                            return Containers.verticalFlow(Sizing.fixed(50), Sizing.fixed(50))
                                    .child(
                                            Components.label(range.getTranslation())
                                    )
                                    .verticalAlignment(VerticalAlignment.CENTER)
                                    .horizontalAlignment(HorizontalAlignment.CENTER)
                                    .id(range.toString());
                        })
                        .collect(Collectors.toList())
        );

        revealRange = new SimpleRadialLayoutBuilder().adjustRadi(0, 15, 140, 80)
                .addComponents(components)
                .setComponentId("REVEAL_RANGE")
                .changeRadialStartOffset(-(Math.PI / 2))
                .onSelection(component -> {
                    if (component instanceof BaseParentComponent baseParentComponent) {
                        String id = baseParentComponent.children().get(0).id();

                        if (id != null) {
                            try {
                                this.selectedRevealRange = RevelInfoManager.RevealRange.valueOf(id);

                                ((AnimationExtension<Positioning>) this.revealRange.getComponent()
                                        .positioning().animate(500, Easing.LINEAR, PersonalityClient.customRelative(-200, 50)).forwards())
                                        .setOnCompletionEvent(animation -> this.uiAdapter.rootComponent.removeChild(this.revealRange.getComponent()));

                                this.uiAdapter.rootComponent.child(confirmationLayout);

                                FlowLayout infoLayout = confirmationLayout.childById(FlowLayout.class, "confirmation_info_layout");

                                if(infoLayout != null){
                                    infoLayout
                                            .child(
                                                    Components.label(
                                                            Text.literal("Info Level: ")
                                                                    .append(this.selectedRevealLevel.getTranslation())
                                                    ).margins(Insets.bottom(3))
                                            ).child(
                                                    Components.label(
                                                            Text.literal("Reveal Range: ")
                                                                    .append(this.selectedRevealRange.getTranslation())
                                                    )
                                            );
                                }

                                confirmationLayout
                                        .positioning().animate(500, Easing.LINEAR, PersonalityClient.customRelative(50, 50)).forwards();

                                return true;
                            } catch (IllegalArgumentException e) {
                                LOGGER.warn(e.getMessage());
                            }
                        }
                    }

                    return false;
                });

        //-----------------------------------------------------------------------------

        revealRange.getComponent(root).positioning(PersonalityClient.customRelative(200, 50));

        root.child(revealLevel.getComponent(root).positioning(PersonalityClient.customRelative(200, 50)));

        revealLevel.getComponent().positioning()
                        .animate(750, Easing.QUADRATIC, PersonalityClient.customRelative(50, 50)).forwards();

        //-----------------------------------------------------------------------------

        root.mouseDown().subscribe((mouseX, mouseY, button) -> {
            Component component = root.childAt(Math.round((float) mouseX), Math.round((float) mouseY));

            if(component != null && component.id() != null && closableComponentIds.contains(component.id())){
                if((button | GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_MOUSE_BUTTON_LEFT){
                    this.close();

                    return true;
                }
            }

            return false;
        });

        uiAdapter.enableInspector = false;
        uiAdapter.globalInspector = false;
    }

    public void confirmSelection(){
        String playerUUID = "";

        if(selectedRevealRange == RevelInfoManager.RevealRange.DIRECTED) {
            HitResult result = MinecraftClient.getInstance().player
                    .raycast(60, MinecraftClient.getInstance().getTickDelta(), false);

            if (result instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof PlayerEntity playerEntity) {
                playerUUID = playerEntity.getUuid().toString();
            } else {
                LOGGER.warn("[RevealIdentityScreen] The Directed Reveal Range was used but that the Client wasn't looking at a player");

                playerUUID = DebugCharacters.ERROR.getUUID();
            }
        }

        if(!Objects.equals(playerUUID, DebugCharacters.ERROR.getUUID())) {
            Networking.sendC2S(new RevealCharacterC2SPacket(this.selectedRevealLevel, this.selectedRevealRange, playerUUID));
        }

        this.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
