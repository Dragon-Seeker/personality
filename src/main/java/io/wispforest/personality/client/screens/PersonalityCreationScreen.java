package io.wispforest.personality.client.screens;

import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.*;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.UIErrorToast;
import io.wispforest.personality.PersonalityMod;
import io.wispforest.personality.client.compat.BetterEditBoxWidget;
import io.wispforest.personality.client.compat.BetterTextFieldWidget;
import io.wispforest.personality.client.compat.CustomEntityComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class PersonalityCreationScreen extends BaseOwoScreen<FlowLayout> {

    List<PersonalityScreenAddon> screenAddons = new ArrayList<>();

    public PersonalityCreationScreen() {}

    public void addAddon(PersonalityScreenAddon addon){
        screenAddons.add(addon);
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

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        HorizontalFlowLayout mainFlowLayout = Containers.horizontalFlow(Sizing.content(), Sizing.content());

        Surface panel = PersonalityMod.isDarkMode() ? Surface.DARK_PANEL : Surface.PANEL;

        //Panel 1

        mainFlowLayout.child(Containers.verticalFlow(Sizing.content(), Sizing.fixed(182))
                .child(new CustomEntityComponent<>(Sizing.fixed(110), MinecraftClient.getInstance().player)
                        .scale(0.6F)
                        //.scaleToFit(true)
                        .allowMouseRotation(true)
                        .margins(Insets.of(40, 20, 0, 0))
                ).margins(Insets.right(20))
                .surface(panel));

        // END


        //Panel 2

        mainFlowLayout.child(Containers.verticalFlow(Sizing.fixed(182), Sizing.fixed(182))
                .child(Containers.verticalFlow(Sizing.content(), Sizing.content())
                        .child(Containers.horizontalFlow(Sizing.fill(100), Sizing.content())
                                .child(Components.label(Text.of("Name: "))
                                        //.margins(Insets.of(6, 5, 0, 0))
                                )
                                .child(BetterTextFieldWidget.textBox(Sizing.fill(75), "")
                                        .bqColor(Color.ofArgb(0xFF555555)))
                                .horizontalAlignment(HorizontalAlignment.CENTER)
                                .verticalAlignment(VerticalAlignment.CENTER)
                                .margins(Insets.bottom(6))
                        )
                        .child(Containers.verticalFlow(Sizing.content(), Sizing.content())
                                .child(Components.createWithSizing(() ->
                                        BetterEditBoxWidget.ofEmpty(Text.of(""), Text.of(""))
                                            .textWidth(164)
                                            .bqColor(Color.ofArgb(0xFF555555)),
                                    Sizing.fixed(164),
                                    Sizing.fixed(60)
                                    )
                                )
                                .verticalAlignment(VerticalAlignment.CENTER)
                                .horizontalAlignment(HorizontalAlignment.CENTER)
                        )
                )
                .padding(Insets.of(6))
                .surface(panel)
        );

        // END
        mainFlowLayout.positioning(Positioning.relative(50, 50));

//        mainFlowLayout.positioning(Positioning.relative(50, -200));
//
//        mainFlowLayout.positioning().animate(2000, Easing.CUBIC, Positioning.relative(50, 50)).forwards();

        //Origins Panel

        screenAddons.forEach(addon -> addon.build(mainFlowLayout, PersonalityMod.isDarkMode()));

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
