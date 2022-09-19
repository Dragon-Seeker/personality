package io.blodhgarm.personality.client.screens;

import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.client.compat.BetterEditBoxWidget;
import io.blodhgarm.personality.client.compat.BetterTextFieldWidget;
import io.blodhgarm.personality.client.compat.CustomEntityComponent;
import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.HorizontalFlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.UIErrorToast;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class PersonalityCreationScreen extends BaseOwoScreen<FlowLayout> {

    List<PersonalityScreenAddon> screenAddons = new ArrayList<>();

    public GenderSelection currentSelection = GenderSelection.MALE;

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

        mainFlowLayout.child(
                Containers.verticalFlow(Sizing.content(), Sizing.fixed(182))
                    .child(
                            (new CustomEntityComponent<>(Sizing.fixed(110), MinecraftClient.getInstance().player))
                                .scale(0.6F)
                                //.scaleToFit(true)
                                .allowMouseRotation(true)
                                .margins(Insets.of(40, 20, 0, 0))
                    )
                    .margins(Insets.right(20))
                    .surface(panel)
        );

        // END


        //Panel 2

        mainFlowLayout.child(
                Containers.verticalFlow(Sizing.fixed(188), Sizing.fixed(182))
                    .child(
                            Containers.verticalFlow(Sizing.content(), Sizing.content())
                                .child(
                                        Components.label(Text.of("Create Your Character"))
                                            .margins(Insets.of(2, 0, 0,0))
                                )
                                .child(
                                        Components.box(Sizing.fixed(152), Sizing.fixed(1))
                                                .color(Color.ofArgb(0xFFa0a0a0))
                                                .margins(Insets.of(4, 6, 0, 0))
                                )
                                .child(
                                        Containers.verticalScroll(Sizing.content(), Sizing.fixed(142),
                                                Containers.verticalFlow(Sizing.content(), Sizing.content())
                                                        .child(
                                                                Containers.horizontalFlow(Sizing.content(), Sizing.content())
                                                                        .child(
                                                                                Components.label(Text.of("Name: "))
                                                                                //.margins(Insets.of(6, 5, 0, 0))
                                                                        )
                                                                        .child(
                                                                                BetterTextFieldWidget.textBox(Sizing.fixed(132), "")
                                                                                        .bqColor(Color.ofArgb(0xFF555555))
                                                                        )
                                                                        .horizontalAlignment(HorizontalAlignment.LEFT)
                                                                        .verticalAlignment(VerticalAlignment.CENTER)
                                                                        .margins(Insets.bottom(6))
                                                        )
                                                        .child(
                                                                createGenderComponent()
                                                        )
                                                        .child(Containers.verticalFlow(Sizing.content(), Sizing.content())
                                                                .child(
                                                                        Components.label(Text.of("Bio: "))
                                                                                .margins(Insets.of(0, 4, 0, 0))
                                                                )
                                                                .child(
                                                                        Components.createWithSizing(() -> BetterEditBoxWidget.ofEmpty(Text.of(""), Text.of(""))
                                                                                        .textWidth(164)
                                                                                        .bqColor(Color.ofArgb(0xFF555555)),
                                                                                Sizing.fixed(164),
                                                                                Sizing.fixed(60)
                                                                        )
                                                                )
                                                        )
                                                        .margins(Insets.left(4))
                                        )
                                )
//                                .child(
//                                        Containers.verticalFlow(Sizing.content(), Sizing.content())
//                                            .child(
//                                                    Containers.horizontalFlow(Sizing.content(), Sizing.content())
//                                                        .child(
//                                                                Components.label(Text.of("Name: "))
//                                                                //.margins(Insets.of(6, 5, 0, 0))
//                                                        )
//                                                        .child(
//                                                                BetterTextFieldWidget.textBox(Sizing.fixed(132), "")
//                                                                    .bqColor(Color.ofArgb(0xFF555555))
//                                                        )
//                                                        .horizontalAlignment(HorizontalAlignment.LEFT)
//                                                        .verticalAlignment(VerticalAlignment.CENTER)
//                                                        .margins(Insets.bottom(6))
//                                            )
//                                            .child(
//                                                    createGenderComponent()
//                                            )
//                                            .child(Containers.verticalFlow(Sizing.content(), Sizing.content())
//                                                    .child(
//                                                            Components.label(Text.of("Bio: "))
//                                                                .margins(Insets.of(0, 4, 0, 0))
//                                                    )
//                                                    .child(
//                                                            Components.createWithSizing(() -> BetterEditBoxWidget.ofEmpty(Text.of(""), Text.of(""))
//                                                                    .textWidth(164)
//                                                                    .bqColor(Color.ofArgb(0xFF555555)),
//                                                                Sizing.fixed(164),
//                                                                Sizing.fixed(60)
//                                                            )
//                                                    )
//                                            )
//                                            .margins(Insets.left(4))
//                                )
                                .horizontalAlignment(HorizontalAlignment.CENTER)
                    )
                    .padding(Insets.of(6))
                    .surface(panel)
        );

        // END


        //Addon Panels

        screenAddons.forEach(addon -> addon.build(mainFlowLayout, PersonalityMod.isDarkMode()));

        // END


        // Root Component Manipulation

        mainFlowLayout.positioning(Positioning.relative(50, 50));

//        mainFlowLayout.positioning(Positioning.relative(50, -200));
//
//        mainFlowLayout.positioning().animate(2000, Easing.CUBIC, Positioning.relative(50, 50)).forwards();

        rootComponent.child(mainFlowLayout);

        rootComponent.keyPress().subscribe((keyCode, scanCode, modifiers) -> {
            if (keyCode == GLFW.GLFW_KEY_R && (modifiers & GLFW.GLFW_MOD_SHIFT) != 0) {
                this.clearAndInit();

                return true;
            }

            return false;
        });
    }

    public Component createGenderComponent(){
        FlowLayout horizontalComponent = Containers.horizontalFlow(Sizing.content(), Sizing.content());

        horizontalComponent
                .child(
                        Components.label(Text.of("Gender:"))
                                .margins(Insets.right(4))
                )
                .child(
                        Components.button(currentSelection.translation, button -> {
                            int nextIndex = currentSelection.ordinal() + 1;

                            currentSelection = GenderSelection.values()[nextIndex >= GenderSelection.values().length ? 0 : nextIndex];

                            button.setMessage(currentSelection.translation);

                            if(!currentSelection.openTextField()) {
                                BetterTextFieldWidget child = horizontalComponent.childById(BetterTextFieldWidget.class, "gender_text_field");

                                if(child != null) horizontalComponent.removeChild(child);

                            } else {
                                horizontalComponent.child(
                                        BetterTextFieldWidget.textBox(Sizing.fixed(51), "")
                                                .setEditAbility(currentSelection.openTextField())
                                                .id("gender_text_field")
                                );
                            }
                        }).horizontalSizing(Sizing.fixed(65))
                                .margins(Insets.of(1, 1, 0,4))
                ).verticalAlignment(VerticalAlignment.CENTER);

        if(currentSelection.openTextField()){
            horizontalComponent.child(
                    BetterTextFieldWidget.textBox(Sizing.fixed(51), "")
                            .setEditAbility(currentSelection.openTextField())
                            .id("gender_text_field")
            );
        }

        return horizontalComponent.margins(Insets.bottom(6));
    }


    public enum GenderSelection {
        MALE(Text.translatable("personality.gender.male")),
        FEMALE(Text.translatable("personality.gender.female")),
        NON_BINARY(Text.translatable("personality.gender.non_binary")),
        OTHER(Text.translatable("personality.gender.other"));

        public final Text translation;

        GenderSelection(Text text){
            this.translation = text;
        }

        public boolean openTextField(){
            return this == GenderSelection.OTHER;
        }
    }
}
