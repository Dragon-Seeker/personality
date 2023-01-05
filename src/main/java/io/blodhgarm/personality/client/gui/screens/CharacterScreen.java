package io.blodhgarm.personality.client.gui.screens;

import com.mojang.logging.LogUtils;
import io.blodhgarm.personality.Networking;
import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.BaseCharacter;
import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.api.addon.client.PersonalityScreenAddon;
import io.blodhgarm.personality.api.client.AddonObservable;
import io.blodhgarm.personality.api.client.PersonalityScreenAddonRegistry;
import io.blodhgarm.personality.client.ThemeHelper;
import io.blodhgarm.personality.client.gui.CharacterScreenMode;
import io.blodhgarm.personality.client.gui.components.owo.CustomEntityComponent;
import io.blodhgarm.personality.client.gui.utils.CustomSurfaces;
import io.blodhgarm.personality.client.gui.components.vanilla.BetterEditBoxWidget;
import io.blodhgarm.personality.client.gui.components.vanilla.BetterTextFieldWidget;
import io.blodhgarm.personality.packets.SyncC2SPackets;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.DiscreteSliderComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.HorizontalFlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CursorMovement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.*;

public class CharacterScreen extends BaseOwoScreen<FlowLayout> implements AddonObservable {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final MutableText requiredText = Text.literal("*").formatted(Formatting.BOLD, Formatting.RED);

    //------------------------

    private final Map<Identifier, PersonalityScreenAddon> screenAddons = new HashMap<>();

    public FlowLayout rootComponent;

    public GenderSelection currentSelection = GenderSelection.MALE;

    //------------------------

    public final CharacterScreenMode currentMode;

    @Nullable public final BaseCharacter currentCharacter;
    @Nullable public final PlayerEntity player;

    //------------------------

    public boolean buildAsScreen = true;

    @Nullable
    public Screen originScreen = null;

    //------------------------

    public CharacterScreen(CharacterScreenMode currentMode, @Nullable PlayerEntity player, @Nullable BaseCharacter character) {
        this.currentMode = currentMode;

        this.player = player;
        this.currentCharacter = character;

        PersonalityScreenAddonRegistry.ALL_SCREEN_ADDONS
                .forEach((identifier, addonFactory) -> screenAddons.put(identifier, addonFactory.buildAddon(this.currentMode, this.currentCharacter, this.player).linkAddon(this)));
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    protected void buildAsChild(FlowLayout rootComponent){
        this.buildAsScreen = false;

        this.build(rootComponent);

        this.buildAsScreen = true;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        boolean isModifiable = this.currentMode.isModifiableMode();
        boolean importCharacterData = this.currentMode.importFromCharacter();

        this.rootComponent = rootComponent;

        HorizontalFlowLayout mainFlowLayout = (HorizontalFlowLayout) Containers.horizontalFlow(Sizing.content(), Sizing.content()).id("main_flow_layout");

        //---------------------------- Character Panel ----------------------------

        //--- Player Entity Display Panel ---

        FlowLayout playerDisplayComponent = Containers.verticalFlow(Sizing.content(), Sizing.fixed(149));

        Identifier originScreenAddon = new Identifier("origins", "origin_selection_addon");
        boolean originAddonExists = screenAddons.containsKey(originScreenAddon);

        if(originAddonExists) {
            playerDisplayComponent.child(screenAddons.get(originScreenAddon).addBranchComponent(rootComponent));
        }

        playerDisplayComponent.child(
                (CustomEntityComponent.playerEntityComponent(Sizing.fixed(originAddonExists ? 85 : 100), player))
                        .scale(originAddonExists ? 0.55F : 0.65F)
                        //.scaleToFit(true)
                        .allowMouseRotation(true)
                        .margins(Insets.of(10, 6, 5, 5))
        );

        //--------------------------------


        //----- Character Properties -----

        //-- Name Property --
        FlowLayout namePropertyLayout = Containers.horizontalFlow(Sizing.content(), Sizing.content());

        {
            MutableText nameLabel = Text.empty();

            nameLabel.append(isModifiable ? requiredText.copy() : Text.empty())
                    .append(Text.literal("Name: "))
                    .append(!isModifiable ? currentCharacter.getFormattedName() : Text.empty());

            namePropertyLayout.child(Components.label(nameLabel));

            if (isModifiable) {
                namePropertyLayout
                        .child(
                                BetterTextFieldWidget.textBox(Sizing.fixed(112), importCharacterData ? currentCharacter.getName() : "") //132
                                        .bqColor(Color.ofArgb(0xFF555555))
                                        .id("character_name")
                        );
            }
        }
        //-------------------

        //-- Age Property --
        FlowLayout agePropertyLayout = Containers.horizontalFlow(Sizing.content(), Sizing.content());

        {
            MutableText ageLabel = Text.empty()
                    .append(Text.literal("Age: "));

            if(!isModifiable){
                int age = currentCharacter.getAge();

                ageLabel.append(Text.literal(age > 0 ? age + " Years" : "Unknown"));
            }

            agePropertyLayout.child(
                    Components.label(ageLabel)
                            .color(ThemeHelper.dynamicColor())
                            .margins(Insets.right(6))
            );

            if(isModifiable){
                agePropertyLayout
                        .child(
                                Components.discreteSlider(Sizing.fixed(114), 17, 60) //134
                                        .setFromDiscreteValue(importCharacterData ? currentCharacter.getAge() : 17)
                                        .snap(true)
                                        .id("age_slider")
                        );
            }
        }
        //------------------

        FlowLayout characterPropertiesContainer = Containers.verticalFlow(Sizing.content(), Sizing.content())
                .child(namePropertyLayout
                        .horizontalAlignment(HorizontalAlignment.LEFT)
                        .verticalAlignment(VerticalAlignment.CENTER)
                        .margins(Insets.bottom(8))
                )
                .child(agePropertyLayout
                        .verticalAlignment(VerticalAlignment.CENTER)
                        .margins(Insets.bottom(8))
                );


        Identifier pehkuiScreenAddon = new Identifier("pehkui", "scale_selection_addon");

        if(screenAddons.containsKey(pehkuiScreenAddon)) {
            characterPropertiesContainer
                    .child(
                            screenAddons.get(pehkuiScreenAddon).addBranchComponent(rootComponent)
                    );
        }

        characterPropertiesContainer
                .child(
                        createGenderComponent(isModifiable, importCharacterData)
                )
                .child(
                        Containers.verticalFlow(Sizing.content(), Sizing.content())
                                .child(
                                        Components.label(Text.of("Description: "))
                                                .color(ThemeHelper.dynamicColor())
                                                .margins(Insets.of(0, 4, 0, 0))
                                )
                                .child(
                                        BetterEditBoxWidget.editBox(Sizing.fixed(136), Sizing.fixed(60), Text.of(""), Text.of(""), importCharacterData ? currentCharacter.getDescription() : "")
                                                .setCursorPosition(CursorMovement.ABSOLUTE, 0)
                                                .textWidth(130)
                                                .bqColor(Color.ofArgb(0xFF555555))
                                                .canEdit(isModifiable)
                                                .id("description_text_box")
                                )
                                .margins(Insets.bottom(8))
                )
                .child(
                        Containers.verticalFlow(Sizing.content(), Sizing.content())
                                .child(
                                        Components.label(Text.of("Bio: "))
                                                .color(ThemeHelper.dynamicColor())
                                                .margins(Insets.of(0, 4, 0, 0))
                                )
                                .child(
                                        BetterEditBoxWidget.editBox(Sizing.fixed(136), Sizing.fixed(60), Text.of(""), Text.of(""), importCharacterData ? currentCharacter.getBiography() : "")
                                                .setCursorPosition(CursorMovement.ABSOLUTE, 0)
                                                .textWidth(130)
                                                .bqColor(Color.ofArgb(0xFF555555))
                                                .canEdit(isModifiable)
                                                .id("biography_text_box")
                                )
                );

        screenAddons.entrySet()
                .stream()
                .filter(entry -> entry.getKey() == originScreenAddon)
                .forEach(entry -> characterPropertiesContainer
                        .child(entry.getValue().addBranchComponent(characterPropertiesContainer))
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
                                .surface(ThemeHelper.dynamicSurface())
                                .padding(Insets.of(6))
                );

        FlowLayout characterPanel = (FlowLayout) Containers.verticalFlow(Sizing.content(), Sizing.fixed(182)) //Sizing.fixed(326)
                .child(
                        Components.label(Text.of("Create Your Character"))
                                .color(ThemeHelper.dynamicColor())
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
                .surface(ThemeHelper.dynamicSurface());

        FlowLayout buttonLayout = null;

        if(this.currentMode.isModifiableMode()) {
            buttonLayout = (FlowLayout) Containers.verticalFlow(Sizing.content(), Sizing.content())
                    .child(
                            characterPanel
                    )
                    .child(
                            Components.button(Text.of(this.currentMode.isModifiableMode() ? "Done" : "Close"), (ButtonComponent button) -> {
                                        if (!this.currentMode.isModifiableMode()) {
                                            this.close();

                                            return;
                                        }

                                        if (this.finishCharacterCreation(rootComponent)) {
                                            this.close();
                                        } else {
                                            //TODO: Handle this
                                        }
                                    })
                                    .horizontalSizing(Sizing.fixed(100))
                                    .margins(Insets.top(6))
                    )
                    .horizontalAlignment(HorizontalAlignment.CENTER);
        } else {
            if(this.buildAsScreen) {
                characterPanel.child(
                        Components.button(Text.of("❌"), (ButtonComponent component) -> this.close())
                                .renderer(ButtonComponent.Renderer.flat(0, 0, 0))
                                .sizing(Sizing.fixed(12))
                                .positioning(Positioning.relative(100, 0))
                );
            }
        }

        mainFlowLayout.child(
                buttonLayout != null ? buttonLayout : characterPanel
        );

        //---------------------------------- END ----------------------------------


        //---------------------- Root Component Manipulation ----------------------

        rootComponent.child(mainFlowLayout);

        if(buildAsScreen) {
            mainFlowLayout.positioning(Positioning.relative(50, -100));

            mainFlowLayout.positioning().animate(2000, Easing.CUBIC, Positioning.relative(50, 50)).forwards();

            rootComponent.surface(Surface.VANILLA_TRANSLUCENT);
        }

        //---------------------------------- END ----------------------------------
    }

    @Override
    public void close() {
        if (this.originScreen != null) {
            this.client.setScreen(this.originScreen);
        } else {
            super.close();
        }
    }

    private Component createGenderComponent(boolean modifiable, boolean importCharacterData){
        FlowLayout horizontalComponent = Containers.horizontalFlow(Sizing.content(), Sizing.content());

        MutableText text = Text.literal("Gender: ");

        if (!modifiable) {
            text.append(Text.of(currentCharacter.getGender().replace("_", " ")));
        }

        horizontalComponent
                .child(Components.label(text)
                        .color(ThemeHelper.dynamicColor()));

        if(modifiable) {
            if (importCharacterData) currentSelection = GenderSelection.attemptToGetGender(currentCharacter.getGender());

            horizontalComponent
                .child(Components.button(currentSelection.translation(), (ButtonComponent button) -> {
                                currentSelection = currentSelection.getNextSelection();

                                button.setMessage(currentSelection.translation());

                                if (currentSelection.openTextField()) {
                                    horizontalComponent.child(createGenderTextField(importCharacterData));
                                } else {
                                    BetterTextFieldWidget child = horizontalComponent.childById(BetterTextFieldWidget.class, "gender_text_field");

                                    if (child != null) horizontalComponent.removeChild(child);
                                }

                                button.horizontalSizing(Sizing.fixed(currentSelection.textSizing() + 10));
                            })
                            .horizontalSizing(Sizing.fixed(currentSelection.textSizing() + 10)) //fixed(65)
                            .margins(Insets.of(1, 1, 0, 4))
            ).verticalAlignment(VerticalAlignment.CENTER);

            if (currentSelection.openTextField()) horizontalComponent.child(createGenderTextField(importCharacterData));
        }

        return horizontalComponent.margins(Insets.bottom(8));
    }

    private Component createGenderTextField(boolean importCharacterData){
        return BetterTextFieldWidget.textBox(Sizing.fixed(58), importCharacterData ? currentCharacter.getGender() : "")
                .setEditAbility(currentSelection.openTextField())
                .tooltip(Text.of("Required"))
                .id("gender_text_field");
    }

    @Override
    public void pushScreenAddon(PersonalityScreenAddon screenAddon){
        FlowLayout flowLayout = this.rootComponent.childById(FlowLayout.class, "main_flow_layout");
        FlowLayout addonMainFlow = flowLayout.childById(FlowLayout.class, "current_addon_screen");

        if(addonMainFlow != null){
            flowLayout.removeChild(addonMainFlow);

            if(addonMainFlow.childById(Component.class, screenAddon.addonId().toString()) != null) return;
        }

        if(screenAddon == null) return;

        addonMainFlow = (FlowLayout) Containers.verticalFlow(Sizing.content(), Sizing.content())
                .child(screenAddon.createMainFlowlayout(ThemeHelper.isDarkMode()))
                .id("current_addon_screen");

        flowLayout.child(addonMainFlow);
    }

    @Override
    public boolean isAddonOpen(PersonalityScreenAddon screenAddon){
        FlowLayout addonMainFlow = (this.rootComponent
                .childById(FlowLayout.class, "main_flow_layout"))
                .childById(FlowLayout.class, "current_addon_screen");

        return addonMainFlow != null && addonMainFlow.childById(Component.class, screenAddon.addonId().toString()) != null;
    }

    private boolean finishCharacterCreation(BaseParentComponent rootComponent){
        String name = rootComponent.childById(TextFieldWidget.class, "character_name").getText();

        if(name.isEmpty()) return false;

        String gender = currentSelection != GenderSelection.OTHER
                ? currentSelection.translation().getString()
                : rootComponent.childById(TextFieldWidget.class, "gender_text_field").getText();

        String description = rootComponent.childById(BetterEditBoxWidget.class, "description_text_box").convertTextBox();

        String biography = rootComponent.childById(BetterEditBoxWidget.class, "biography_text_box").convertTextBox();

        int age = (int) rootComponent.childById(DiscreteSliderComponent.class, "age_slider").discreteValue();

        int activityOffset = MinecraftClient.getInstance().player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME));

        Character character = new Character(name, gender, description, biography, age, activityOffset);

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

    public enum GenderSelection {
        MALE("male"),
        FEMALE("female"),
        NON_BINARY("non-binary"),
        OTHER("other");

        public final String name;

        GenderSelection(String name){
            this.name = name;
        }

        public static GenderSelection attemptToGetGender(String gender){
            for(GenderSelection selection : GenderSelection.values()){
                if(Objects.equals(selection.name, gender.toLowerCase(Locale.ROOT))){
                    return selection;
                }
            }

            return OTHER;
        }

        public boolean openTextField(){
            return this == GenderSelection.OTHER;
        }

        public GenderSelection getNextSelection(){
            int nextIndex = this.ordinal() + 1;

            return GenderSelection.values()[nextIndex >= GenderSelection.values().length ? 0 : nextIndex];
        }

        public Text translation(){
            return Text.translatable("personality.gender." + name.replace(" ", "_").toLowerCase(Locale.ROOT));
        }

        public String translatedString(){
            return translation().getString();
        }


        public int textSizing(){
            return MinecraftClient.getInstance().textRenderer.getWidth(this.translation().asOrderedText());
        }
    }

    //-------------------------------------------------------

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
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
