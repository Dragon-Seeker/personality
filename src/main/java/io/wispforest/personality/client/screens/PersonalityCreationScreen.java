package io.wispforest.personality.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginRegistry;
import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.container.*;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.Drawer;
import io.wispforest.owo.ui.util.UIErrorToast;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class PersonalityCreationScreen extends BaseOwoScreen<FlowLayout> {

    AdditionalCreationComponent component;

    public PersonalityCreationScreen(AdditionalCreationComponent additionalCreationComponent) {
        component = additionalCreationComponent;
    }

    @Override
    protected void init() {
        if (this.invalid) return;

        // Check whether this screen was already initialized

        try {
            this.uiAdapter = this.createAdapter();
            this.build(this.uiAdapter.rootComponent);

            this.uiAdapter.inflateAndMount();
            this.client.keyboard.setRepeatEvents(true);
        } catch (Exception error) {
            Owo.LOGGER.warn("Could not initialize owo screen", error);
            UIErrorToast.report(error);
            this.invalid = true;
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    public static void generateAdapterAndChildren(VerticalFlowLayout rootFlowLayout, int xOffset, int yOffset) {
        HorizontalFlowLayout mainFlowLayout = Containers.horizontalFlow(Sizing.content(), Sizing.content());

        //Panel 1

        mainFlowLayout.child(Containers.verticalFlow(Sizing.content(), Sizing.fixed(182))
                .child(
                        Components.entity(Sizing.fixed(100), MinecraftClient.getInstance().player)
                                .scaleToFit(true)
                                .allowMouseRotation(true)
                ).margins(Insets.right(20))
                .surface(Surface.DARK_PANEL));

        // END


        //Panel 2

        mainFlowLayout.child(Containers.verticalFlow(Sizing.fixed(180), Sizing.fixed(182))
                .surface(Surface.DARK_PANEL));

        // END

        mainFlowLayout.positioning(Positioning.relative(20, 50));

        rootFlowLayout.child(mainFlowLayout);
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        HorizontalFlowLayout mainFlowLayout = Containers.horizontalFlow(Sizing.content(), Sizing.content());

        //Panel 1

        mainFlowLayout.child(Containers.verticalFlow(Sizing.content(), Sizing.fixed(182))
                .child(
                        Components.entity(Sizing.fixed(100), MinecraftClient.getInstance().player)
                                .scaleToFit(true)
                                .allowMouseRotation(true)
                ).margins(Insets.right(20))
                .surface(Surface.DARK_PANEL));

        // END


        //Panel 2

        mainFlowLayout.child(Containers.verticalFlow(Sizing.fixed(180), Sizing.fixed(182))
                .surface(Surface.DARK_PANEL));

        // END
        mainFlowLayout.positioning(Positioning.relative(50, 50));

//        mainFlowLayout.positioning(Positioning.relative(50, -200));
//
//        mainFlowLayout.positioning().animate(2000, Easing.CUBIC, Positioning.relative(50, 50)).forwards();

        //Origins Panel

        component.build(mainFlowLayout);

        rootComponent.child(mainFlowLayout);

        rootComponent.keyPress().subscribe((keyCode, scanCode, modifiers) -> {
            if (keyCode == GLFW.GLFW_KEY_R && (modifiers & GLFW.GLFW_MOD_SHIFT) != 0) {
                this.clearAndInit();

                return true;
            }

            return false;
        });
    }


}
