package io.blodhgarm.personality.client.screens;

import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.client.screens.components.CustomSurfaces;
import io.blodhgarm.personality.client.screens.components.vanilla.BetterEditBoxWidget;
import io.blodhgarm.personality.client.screens.components.vanilla.BetterTextFieldWidget;
import io.blodhgarm.personality.client.compat.CustomEntityComponent;
import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.HorizontalFlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.UIErrorToast;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class PersonalityCreationScreen extends BaseOwoScreen<FlowLayout> {

    List<PersonalityScreenAddon> screenAddons = new ArrayList<>();

    private PersonalityScreenAddon currentScreenAddon = null;

    public GenderSelection currentSelection = GenderSelection.MALE;

    public PersonalityCreationScreen() {}

    public void addAddon(PersonalityScreenAddon addon){
        screenAddons.add(addon.linkAddon(this));
    }

//    @Override
//    protected void init() {
//        if (this.invalid) return;
//
//        // Check whether this screen was already initialized
//
//        try {
//            this.uiAdapter = this.createAdapter();
//            this.build(this.uiAdapter.rootComponent);
//
//            this.uiAdapter.inflateAndMount();
//            this.client.keyboard.setRepeatEvents(true);
//        } catch (Exception error) {
//            Owo.LOGGER.warn("Could not initialize owo screen", error);
//            UIErrorToast.report(error);
//            this.invalid = true;
//        }
//    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
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
        HorizontalFlowLayout mainFlowLayout = (HorizontalFlowLayout) Containers.horizontalFlow(Sizing.content(), Sizing.content()).id("main_flow_layout");

        Surface panel = PersonalityMod.isDarkMode() ? Surface.DARK_PANEL : Surface.PANEL;

        //Panel 1

        FlowLayout mainOptionsSection = Containers.horizontalFlow(Sizing.content(), Sizing.content())
                .child(
                        Containers.verticalFlow(Sizing.content(), Sizing.content())
                                .child(
                                        Containers.verticalFlow(Sizing.content(), Sizing.fixed(149))
                                                .child(
                                                        Containers.horizontalFlow(Sizing.content(), Sizing.fixed(26 + 12))
                                                                .child(
                                                                        screenAddons.get(0).addBranchComponent(rootComponent)
                                                                                .margins(Insets.right(2))
                                                                )
                                                                .child(
                                                                        Components.button(Text.of("✎"), (ButtonComponent component) -> {
                                                                                    this.pushScreenAddon(screenAddons.get(0));
                                                                                })
                                                                                .sizing(Sizing.fixed(12))
                                                                                .positioning(Positioning.absolute(guiScale4OrAbove() ? 86 : 106, 24))
                                                                )
                                                                .allowOverflow(true)
                                                                //.horizontalAlignment(HorizontalAlignment.CENTER)
                                                                //.verticalAlignment(VerticalAlignment.CENTER)
                                                                .margins(Insets.of(4,0,4,4))
                                                )
                                                .child(
                                                        (new CustomEntityComponent<>(Sizing.fixed(100), MinecraftClient.getInstance().player))
                                                                .scale(0.50F)
                                                                //.scaleToFit(true)
                                                                .allowMouseRotation(true)
                                                                .margins(Insets.of(0, 6, 5, 5))
                                                )
                                                .surface(CustomSurfaces.INVERSE_PANEL)
                                                .horizontalAlignment(HorizontalAlignment.CENTER)
                                                .margins(Insets.right(4))
                                )
                )
                .child(
                        Containers.verticalScroll(Sizing.content(), Sizing.fixed(149),
                                Containers.verticalFlow(Sizing.content(), Sizing.content())
                                        .child(
                                                Containers.horizontalFlow(Sizing.content(), Sizing.content())
                                                        .child(
                                                                Components.label(Text.of("Name: "))
                                                                //.margins(Insets.of(6, 5, 0, 0))
                                                        )
                                                        .child(
                                                                BetterTextFieldWidget.textBox(Sizing.fixed(112), "") //132
                                                                        .bqColor(Color.ofArgb(0xFF555555))
                                                        )
                                                        .horizontalAlignment(HorizontalAlignment.LEFT)
                                                        .verticalAlignment(VerticalAlignment.CENTER)
                                                        .margins(Insets.bottom(8))
                                        )
                                        .child(
                                                Containers.horizontalFlow(Sizing.content(), Sizing.content())
                                                        .child(
                                                                Components.label(Text.of("Age: "))
                                                                        .margins(Insets.right(6))
                                                        )
                                                        .child(
                                                                Components.discreteSlider(Sizing.fixed(114), 17, 60) //134
                                                                        .snap(true)
                                                                        .id("age_slider")
                                                        )
                                                        .verticalAlignment(VerticalAlignment.CENTER)
                                                        .margins(Insets.bottom(8))
                                        )
                                        .child(
                                                Containers.horizontalFlow(Sizing.content(), Sizing.content())
                                                        .child(
                                                                Components.label(Text.of("Height: "))
                                                        )
                                                        .child(
                                                                Components.discreteSlider(Sizing.fixed(108), -0.5f, 0.5f) //128
                                                                        .decimalPlaces(1)
                                                                        .snap(true)
                                                                        .message(s -> {
                                                                            if(!s.startsWith("-") && !s.equals("0.0")){
                                                                                s = "+" + s;
                                                                            }

                                                                            return Text.literal(s);
                                                                        }).id("height_slider")
                                                        )
                                                        .verticalAlignment(VerticalAlignment.CENTER)
                                                        .margins(Insets.bottom(8))
                                        )
                                        .child(
                                                createGenderComponent()
                                        )
                                        .child(
                                                Containers.verticalFlow(Sizing.content(), Sizing.content())
                                                        .child(
                                                                Components.label(Text.of("Description: "))
                                                                        .margins(Insets.of(0, 4, 0, 0))
                                                        )
                                                        .child(
                                                                BetterEditBoxWidget.editBox(Sizing.fixed(136), Sizing.fixed(60), Text.of(""), Text.of(""))
                                                                        .textWidth(130)
                                                                        .bqColor(Color.ofArgb(0xFF555555))
                                                        )
                                                        .margins(Insets.bottom(8))
                                        )
                                        .child(
                                                Containers.verticalFlow(Sizing.content(), Sizing.content())
                                                        .child(
                                                                Components.label(Text.of("Bio: "))
                                                                        .margins(Insets.of(0, 4, 0, 0))
                                                        )
                                                        .child(
                                                                BetterEditBoxWidget.editBox(Sizing.fixed(136), Sizing.fixed(60), Text.of(""), Text.of(""))
                                                                        .textWidth(130)
                                                                        .bqColor(Color.ofArgb(0xFF555555))
                                                        )
                                        )
                                )
                                .surface(Surface.DARK_PANEL)
                                .padding(Insets.of(6))
                );


        mainFlowLayout.child(
                Containers.verticalFlow(Sizing.content(), Sizing.content())
                        .child(
                                Containers.verticalFlow(Sizing.content(), Sizing.fixed(182)) //Sizing.fixed(326)
                                        .child(
                                                Components.label(Text.of("Create Your Character"))
                                                        .margins(Insets.of(1, 0, 0,0))
                                        )
                                        .child(
                                                Components.box(Sizing.fixed(252), Sizing.fixed(1))
                                                        .color(Color.ofArgb(0xFFa0a0a0))
                                                        .margins(Insets.of(3, 4, 0, 0))
                                        )
                                        .child(
                                                mainOptionsSection
                                        )
                                        .horizontalAlignment(HorizontalAlignment.CENTER)
                                        .padding(Insets.of(6))
                                        .surface(panel)
                        )
                        .child(
                                Components.button(Text.of("Done"), (ButtonComponent button) -> {

                                })
                                .horizontalSizing(Sizing.fixed(100))
                                .margins(Insets.top(6))
                        )
                        .horizontalAlignment(HorizontalAlignment.CENTER)
        );

        // END

        // Root Component Manipulation

        mainFlowLayout.positioning(Positioning.relative(50, -350));

        mainFlowLayout.positioning().animate(2000, Easing.CUBIC, Positioning.relative(50, 50)).forwards();

        rootComponent.child(mainFlowLayout);

        rootComponent.keyPress().subscribe((keyCode, scanCode, modifiers) -> {
            if (keyCode == GLFW.GLFW_KEY_R && (modifiers & GLFW.GLFW_MOD_SHIFT) != 0) {
                this.clearAndInit();

                return true;
            }

            return false;
        });
    }

    public void pushScreenAddon(PersonalityScreenAddon screenAddon){
        FlowLayout flowLayout = this.uiAdapter.rootComponent.childById(FlowLayout.class, "main_flow_layout");

        assert flowLayout != null;

        FlowLayout addonMainFlow = flowLayout.childById(FlowLayout.class, "current_addon_screen");

        if(addonMainFlow != null){
            flowLayout.removeChild(addonMainFlow);

            if(addonMainFlow.childById(Component.class, screenAddon.addonId()) != null) return;
        }

        if(screenAddon == null) return;

        addonMainFlow = (FlowLayout) Containers.verticalFlow(Sizing.content(), Sizing.content())
                .child(screenAddon.createMainFlowlayout(PersonalityMod.isDarkMode()))
                .id("current_addon_screen");

//        addonMainFlow.positioning(Positioning.relative(300, 50));
//
//        addonMainFlow.positioning().animate(2000, Easing.CUBIC, Positioning.layout()).forwards();

        flowLayout.child(
                addonMainFlow
        );
    }

    public Component createGenderComponent(){
        FlowLayout horizontalComponent = Containers.horizontalFlow(Sizing.content(), Sizing.content());

        horizontalComponent
                .child(
                        Components.label(Text.of("Gender: "))
                )
                .child(
                        Components.button(currentSelection.translation, (ButtonComponent button) -> {
                            int nextIndex = currentSelection.ordinal() + 1;

                            currentSelection = GenderSelection.values()[nextIndex >= GenderSelection.values().length ? 0 : nextIndex];

                            button.setMessage(currentSelection.translation);

                            if(!currentSelection.openTextField()) {
                                BetterTextFieldWidget child = horizontalComponent.childById(BetterTextFieldWidget.class, "gender_text_field");

                                if(child != null) horizontalComponent.removeChild(child);

                            } else {
                                horizontalComponent.child(
                                        BetterTextFieldWidget.textBox(Sizing.fixed(58), "")
                                                .setEditAbility(currentSelection.openTextField())
                                                .id("gender_text_field")
                                );
                            }

                            button.horizontalSizing(Sizing.fixed(MinecraftClient.getInstance().textRenderer.getWidth(currentSelection.translation.asOrderedText()) + 10));
                        })
                                .horizontalSizing(Sizing.fixed(MinecraftClient.getInstance().textRenderer.getWidth(currentSelection.translation.asOrderedText()) + 10)) //fixed(65)
                                .margins(Insets.of(1, 1, 0,4))
                ).verticalAlignment(VerticalAlignment.CENTER);

        if(currentSelection.openTextField()){
            horizontalComponent.child(
                    BetterTextFieldWidget.textBox(Sizing.fixed(51), "")
                            .setEditAbility(currentSelection.openTextField())
                            .id("gender_text_field")
            );
        }

        return horizontalComponent.margins(Insets.bottom(8));
    }

    private boolean finishCharacterCreation(BaseParentComponent rootComponent){
        //TODO: IMPLEMENT THIS

        return true;
    }

    public static boolean guiScale4OrAbove(){
        return MinecraftClient.getInstance().options.getGuiScale().getValue() >= 4 || MinecraftClient.getInstance().options.getGuiScale().getValue() == 0;
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
