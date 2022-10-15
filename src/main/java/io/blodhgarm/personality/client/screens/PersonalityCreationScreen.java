package io.blodhgarm.personality.client.screens;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.Networking;
import io.blodhgarm.personality.api.addon.client.PersonalityScreenAddon;
import io.blodhgarm.personality.api.client.AddonObservable;
import io.blodhgarm.personality.api.client.PersonalityScreenAddonRegistry;
import io.blodhgarm.personality.client.PersonalityClient;
import io.blodhgarm.personality.client.screens.components.CustomSurfaces;
import io.blodhgarm.personality.client.screens.components.vanilla.BetterEditBoxWidget;
import io.blodhgarm.personality.client.screens.components.vanilla.BetterTextFieldWidget;
import io.blodhgarm.personality.client.screens.components.CustomEntityComponent;
import io.blodhgarm.personality.packets.SyncC2SPackets;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.DiscreteSliderComponent;
import io.wispforest.owo.ui.container.*;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class PersonalityCreationScreen extends BaseOwoScreen<FlowLayout> implements AddonObservable {

    private static final MutableText requiredText = Text.literal("*").formatted(Formatting.BOLD, Formatting.RED);

    private final Map<Identifier, PersonalityScreenAddon> screenAddons = new HashMap<>();

    public GenderSelection currentSelection = GenderSelection.MALE;

    public PersonalityCreationScreen(ClientPlayerEntity player) {
        PersonalityScreenAddonRegistry.ALL_SCREEN_ADDONS
                .forEach((identifier, addonFactory) -> screenAddons.put(identifier, addonFactory.buildAddon().linkAddon(this)));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if((modifiers & GLFW.GLFW_MOD_CONTROL) == 2 && (modifiers & GLFW.GLFW_MOD_ALT) == 4 && keyCode == GLFW.GLFW_KEY_R){
            this.uiAdapter = null;
            this.clearAndInit();

            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

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

        Surface panel = PersonalityClient.isDarkMode() ? Surface.DARK_PANEL : Surface.PANEL;

        //---------------------------- Character Panel ----------------------------

        //--- Player Entity Display Panel ---

        FlowLayout playerDisplayComponent = Containers.verticalFlow(Sizing.content(), Sizing.fixed(149));

        Identifier originScreenAddon = new Identifier("origins", "origin_selection_addon");
        boolean originAddonExists = screenAddons.containsKey(originScreenAddon);

        if(originAddonExists) {
            playerDisplayComponent.child(
                    Containers.horizontalFlow(Sizing.content(), Sizing.fixed(26 + 12))
                            .child(
                                    screenAddons.get(originScreenAddon).addBranchComponent(this, rootComponent)
                                            .margins(Insets.right(2))
                            )
                            .child(
                                    Components.button(Text.of("✎"), (ButtonComponent component) -> {
                                                this.pushScreenAddon(screenAddons.get(originScreenAddon));
                                            })
                                            .sizing(Sizing.fixed(12))
                                            .positioning(Positioning.absolute(guiScale4OrAbove() ? 86 : 106, 30))
                            )
                            .allowOverflow(true)
                            //.horizontalAlignment(HorizontalAlignment.CENTER)
                            //.verticalAlignment(VerticalAlignment.CENTER)
                            .margins(Insets.of(4, 0, 4, 4))
            );
        }

        playerDisplayComponent.child(
                (new CustomEntityComponent<>(Sizing.fixed(originAddonExists ? 85 : 100), MinecraftClient.getInstance().player))
                        .scale(originAddonExists ? 0.55F : 0.65F)
                        //.scaleToFit(true)
                        .allowMouseRotation(true)
                        .margins(Insets.of(10, 6, 5, 5))
        );

        //--------------------------------


        //----- Character Properties -----

        FlowLayout characterPropertiesContainer = Containers.verticalFlow(Sizing.content(), Sizing.content())
                .child(
                        Containers.horizontalFlow(Sizing.content(), Sizing.content())
                                .child(
                                        Components.label(Text.empty()
                                                .append(requiredText.copy())
                                                .append(Text.literal("Name: ")))
                                        //.margins(Insets.of(6, 5, 0, 0))
                                )
                                .child(
                                        BetterTextFieldWidget.textBox(Sizing.fixed(112), "") //132
                                                .bqColor(Color.ofArgb(0xFF555555))
                                                .id("character_name")
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
                                                .setFromDiscreteValue(0.0)
                                                .message(s -> {
                                                    if(!s.startsWith("-") && !s.equals("0.0")){
                                                        s = "+" + s;
                                                    }
                                                    return Text.literal(s);
                                                })
                                                .id("height_slider")
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
                                                .id("description_text_box")
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
                                                .id("biography_text_box")
                                )
                );

        screenAddons.entrySet()
                .stream()
                .filter(entry -> entry.getKey() == originScreenAddon)
                .forEach(entry -> characterPropertiesContainer
                        .child(entry.getValue().addBranchComponent(this, characterPropertiesContainer))
                );

        //--------------------------------


        //--------- Panel Layout ---------

        FlowLayout mainOptionsSection = Containers.horizontalFlow(Sizing.content(), Sizing.content())
                .child(
                        Containers.verticalFlow(Sizing.content(), Sizing.content())
                                .child(
                                        playerDisplayComponent
                                                .surface(CustomSurfaces.INVERSE_PANEL)
                                                .horizontalAlignment(HorizontalAlignment.CENTER)
                                                .margins(Insets.right(4))
                                )
                )
                .child(
                        Containers.verticalScroll(Sizing.content(), Sizing.fixed(149), characterPropertiesContainer)
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
                                    if(this.finishCharacterCreation(rootComponent)){
                                        this.close();
                                    } else {
                                        //TODO: Handle this
                                    }
                                })
                                .horizontalSizing(Sizing.fixed(100))
                                .margins(Insets.top(6))
                        )
                        .horizontalAlignment(HorizontalAlignment.CENTER)
        );

        //---------------------------------- END ----------------------------------


        //---------------------- Root Component Manipulation ----------------------

        mainFlowLayout.positioning(Positioning.relative(50, -100));

        mainFlowLayout.positioning().animate(2000, Easing.CUBIC, Positioning.relative(50, 50)).forwards();

        rootComponent.child(mainFlowLayout);

        rootComponent.surface(Surface.VANILLA_TRANSLUCENT);

        //---------------------------------- END ----------------------------------
    }

    public Component createGenderComponent(){
        FlowLayout horizontalComponent = Containers.horizontalFlow(Sizing.content(), Sizing.content());

        horizontalComponent
                .child(Components.label(Text.of("Gender: ")))
                .child(Components.button(currentSelection.translation, (ButtonComponent button) -> {
                            currentSelection = currentSelection.getNextSelection();

                            button.setMessage(currentSelection.translation);

                            if(currentSelection.openTextField()) {
                                horizontalComponent.child(createGenderTextField());
                            } else {
                                BetterTextFieldWidget child = horizontalComponent.childById(BetterTextFieldWidget.class, "gender_text_field");

                                if(child != null) horizontalComponent.removeChild(child);
                            }

                            button.horizontalSizing(Sizing.fixed(currentSelection.textSizing() + 10));
                        })
                        .horizontalSizing(Sizing.fixed(currentSelection.textSizing() + 10)) //fixed(65)
                        .margins(Insets.of(1, 1, 0,4))
                ).verticalAlignment(VerticalAlignment.CENTER);

        if(currentSelection.openTextField()) horizontalComponent.child(createGenderTextField());

        return horizontalComponent.margins(Insets.bottom(8));
    }

    public Component createGenderTextField(){
        return BetterTextFieldWidget.textBox(Sizing.fixed(58), "")
                .setEditAbility(currentSelection.openTextField())
                .tooltip(Text.of("Required"))
                .id("gender_text_field");
    }

    public void pushScreenAddon(PersonalityScreenAddon screenAddon){
        FlowLayout flowLayout = this.uiAdapter.rootComponent.childById(FlowLayout.class, "main_flow_layout");
        FlowLayout addonMainFlow = flowLayout.childById(FlowLayout.class, "current_addon_screen");

        if(addonMainFlow != null){
            flowLayout.removeChild(addonMainFlow);

            if(addonMainFlow.childById(Component.class, screenAddon.addonId().toString()) != null) return;
        }

        if(screenAddon == null) return;

        addonMainFlow = (FlowLayout) Containers.verticalFlow(Sizing.content(), Sizing.content())
                .child(screenAddon.createMainFlowlayout(PersonalityClient.isDarkMode()))
                .id("current_addon_screen");

        flowLayout.child(addonMainFlow);
    }

    public boolean isAddonOpen(PersonalityScreenAddon screenAddon){
        FlowLayout addonMainFlow = (this.uiAdapter.rootComponent
                .childById(FlowLayout.class, "main_flow_layout"))
                .childById(FlowLayout.class, "current_addon_screen");

        return addonMainFlow != null && addonMainFlow.childById(Component.class, screenAddon.addonId().toString()) != null;
    }

    private boolean finishCharacterCreation(BaseParentComponent rootComponent){
        String name = rootComponent.childById(TextFieldWidget.class, "character_name").getText();

        if(name.isEmpty()) return false;

        String gender = currentSelection != GenderSelection.OTHER
                ? currentSelection.translation.getString()
                : rootComponent.childById(TextFieldWidget.class, "gender_text_field").getText();

        String description = rootComponent.childById(BetterEditBoxWidget.class, "description_text_box").convertTextBox();

        String biography = rootComponent.childById(BetterEditBoxWidget.class, "biography_text_box").convertTextBox();

        float heightOffset = (float) rootComponent.childById(DiscreteSliderComponent.class, "height_slider").discreteValue();

        int age = (int) rootComponent.childById(DiscreteSliderComponent.class, "age_slider").discreteValue();

        int activityOffset = MinecraftClient.getInstance().player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME));

        Character character = new Character(name, gender, description, biography, heightOffset, age, activityOffset);

        //Addon Data

        Map<Identifier, String> addonData = new HashMap<>();

        for(Map.Entry<Identifier, PersonalityScreenAddon> entry : screenAddons.entrySet()){
            PersonalityScreenAddon addon = entry.getValue();

            if(addon.isDataEmpty(rootComponent) && addon.requiresUserInput()) return false;

            addon.getAddonData().forEach((addonId, baseAddon) -> addonData.put(addonId, PersonalityMod.GSON.toJson(baseAddon)));
        }

        Networking.sendC2S(new SyncC2SPackets.NewCharacter(PersonalityMod.GSON.toJson(character), addonData, true));

        return true;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
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

        public GenderSelection getNextSelection(){
            int nextIndex = this.ordinal() + 1;

            return GenderSelection.values()[nextIndex >= GenderSelection.values().length ? 0 : nextIndex];
        }

        public int textSizing(){
            return MinecraftClient.getInstance().textRenderer.getWidth(this.translation.asOrderedText());
        }
    }
}
