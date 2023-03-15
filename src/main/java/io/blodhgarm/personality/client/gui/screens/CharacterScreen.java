package io.blodhgarm.personality.client.gui.screens;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.RecordBuilder;
import io.blodhgarm.personality.Networking;
import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.addon.BaseAddon;
import io.blodhgarm.personality.api.character.BaseCharacter;
import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.api.addon.client.PersonalityScreenAddon;
import io.blodhgarm.personality.api.addon.client.AddonObservable;
import io.blodhgarm.personality.api.addon.client.PersonalityScreenAddonRegistry;
import io.blodhgarm.personality.client.gui.ThemeHelper;
import io.blodhgarm.personality.client.gui.CharacterScreenMode;
import io.blodhgarm.personality.client.gui.GenderSelection;
import io.blodhgarm.personality.client.gui.components.ButtonAddon;
import io.blodhgarm.personality.client.gui.components.owo.CustomEntityComponent;
import io.blodhgarm.personality.client.gui.utils.CustomSurfaces;
import io.blodhgarm.personality.client.gui.components.vanilla.EditBoxComponent;
import io.blodhgarm.personality.client.gui.components.vanilla.ColorableTextBoxComponent;
import io.blodhgarm.personality.misc.pond.owo.ButtonAddonDuck;
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
import io.wispforest.owo.ui.util.Drawer;
import io.wispforest.owo.ui.util.ScissorStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CursorMovement;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

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

    public boolean adminMode = false;

    //------------------------

    public boolean buildAsScreen = true;

    @Nullable
    public Screen originScreen = null;

    //------------------------

    public CharacterScreen(CharacterScreenMode currentMode, @Nullable PlayerEntity player, @Nullable BaseCharacter character, boolean adminMode) {
        this(currentMode, player, character);

        this.adminMode = adminMode;
    }

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

//        playerDisplayComponent.gap(5);

        playerDisplayComponent.child(
                (CustomEntityComponent.playerEntityComponent(Sizing.fixed(originAddonExists ? 85 : 100), player))
                        .scale(originAddonExists ? 0.45F : 0.55F)
                        //.scaleToFit(true)
                        .allowMouseRotation(true)
                        .margins(Insets.of(originAddonExists ? 10 : 30, 6, 5, 5))
        );

        //--------------------------------


        //----- Character Properties -----

        //-- Name Property --
        FlowLayout namePropertyLayout = Containers.horizontalFlow(Sizing.content(), Sizing.content());

        MutableText nameLabel = Text.empty();

        nameLabel.append(isModifiable ? requiredText.copy() : Text.empty())
                .append(Text.literal("Name: "))
                .append(!isModifiable ? currentCharacter.getFormattedName() : Text.empty());

        namePropertyLayout.child(
                Components.label(nameLabel)
                        .maxWidth(144)
                        .color(ThemeHelper.dynamicColor())
        );

        if (isModifiable) {
            namePropertyLayout
                    .child(
                            ColorableTextBoxComponent.textBox(Sizing.fixed(112), importCharacterData ? currentCharacter.getName() : "") //132
                                    .bqColor(Color.ofArgb(0xFF555555))
                                    .configure(component -> {
                                        if(component instanceof ColorableTextBoxComponent widget){
                                            widget.setMaxLength(40);
                                        }
                                    })
                                    .id("character_name")
                    );
        }


        FlowLayout uuidInfoLayout = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        FlowLayout playerUuidInfoLayout = Containers.horizontalFlow(Sizing.content(), Sizing.content());

        if(!isModifiable && adminMode) {
            ((ButtonAddonDuck<FlowLayout>) uuidInfoLayout).setButtonAddon(flowLayout -> {
                return new ButtonAddon<>(flowLayout)
                        .useCustomButtonSurface((addon, matrices, component) -> {
                            ScissorStack.drawUnclipped(() -> {
                                if(addon.isHovered()){
                                    Drawer.drawRectOutline(matrices, component.x() - 2, component.y() - 2, component.width() + 4, component.height() + 4, Color.WHITE.argb());
                                }
                            });
                        })
                        .onPress(button -> {
                            MinecraftClient.getInstance().keyboard.setClipboard(currentCharacter.getUUID());
                        });
            }).child(
                    Components.label(Text.empty().append(Text.literal("UUID: " + currentCharacter.getUUID())))
                            .color(ThemeHelper.dynamicColor())
            );

            ((ButtonAddonDuck<FlowLayout>) playerUuidInfoLayout).setButtonAddon(flowLayout -> {
                return new ButtonAddon<>(flowLayout)
                        .useCustomButtonSurface((addon, matrices, component) -> {
                            ScissorStack.drawUnclipped(() -> {
                                if (addon.isHovered()) {
                                    Drawer.drawRectOutline(matrices, component.x() - 2, component.y() - 2, component.width() + 4, component.height() + 4, Color.WHITE.argb());
                                }
                            });
                        })
                        .onPress(button -> {
                            MinecraftClient.getInstance().keyboard.setClipboard(currentCharacter.getPlayerUUID());
                        });
            }).child(
                    Components.label(Text.empty().append(Text.literal("Player UUID: " + currentCharacter.getPlayerUUID())))
                            .color(ThemeHelper.dynamicColor())
            );
        }

        //-------------------

        //-- Age Property --
        FlowLayout agePropertyLayout = Containers.horizontalFlow(Sizing.content(), Sizing.content());

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

        //------------------

        FlowLayout characterPropertiesContainer = Containers.verticalFlow(Sizing.content(), Sizing.content())
                .child(namePropertyLayout
                        .horizontalAlignment(HorizontalAlignment.LEFT)
                        .verticalAlignment(VerticalAlignment.CENTER)
                        .margins(Insets.bottom(8))
                )
                .configure((FlowLayout component) -> {
                    if(!isModifiable && adminMode){
                        component
                            .child(
                                    Containers.horizontalScroll(Sizing.fixed(144), Sizing.fixed(13), uuidInfoLayout)
                                            .horizontalAlignment(HorizontalAlignment.LEFT)
                                            .verticalAlignment(VerticalAlignment.TOP)
                                            .margins(Insets.bottom(4))
                            )
                            .child(
                                    Containers.horizontalScroll(Sizing.fixed(144), Sizing.fixed(13), playerUuidInfoLayout)
                                            .horizontalAlignment(HorizontalAlignment.LEFT)
                                            .verticalAlignment(VerticalAlignment.TOP)
                                            .margins(Insets.bottom(4))
                            );
                    }
                })

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
                                        EditBoxComponent.editBox(Sizing.fixed(136), Sizing.fixed(60), Text.of(""), Text.of(""), importCharacterData ? currentCharacter.getDescription() : "")
                                                .setCursorPosition(CursorMovement.ABSOLUTE, 0)
                                                .textWidth(130)
                                                .bqColor(Color.ofArgb(0xFF555555))
                                                .canEdit(isModifiable)
                                                .configure(component -> {
                                                    if(component instanceof EditBoxComponent betterEditBoxWidget){
                                                        betterEditBoxWidget.setMaxLength(5000);
                                                    }
                                                })
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
                                        EditBoxComponent.editBox(Sizing.fixed(136), Sizing.fixed(60), Text.of(""), Text.of(""), importCharacterData ? currentCharacter.getBiography() : "")
                                                .setCursorPosition(CursorMovement.ABSOLUTE, 0)
                                                .textWidth(130)
                                                .bqColor(Color.ofArgb(0xFF555555))
                                                .canEdit(isModifiable)
                                                .configure(component -> {
                                                    if(component instanceof EditBoxComponent betterEditBoxWidget){
                                                        betterEditBoxWidget.setMaxLength(5000);
                                                    }
                                                })
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
                                            //TODO: Handle this better with custom info on what is missing

                                            MinecraftClient.getInstance().getToastManager()
                                                    .add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION,
                                                            Text.of("Missing Information for Character"),
                                                            Text.of("Your character is missing required information to be created!"))
                                                    );
                                        }
                                    })
                                    .horizontalSizing(Sizing.fixed(100))
                                    .margins(Insets.top(6))
                    )
                    .horizontalAlignment(HorizontalAlignment.CENTER);
        } else {
            if(this.buildAsScreen) {
                characterPanel.child(
                        Components.button(Text.literal("❌").formatted(ThemeHelper.dynamicTextColor()), (ButtonComponent component) -> this.close())
                                .textShadow(ThemeHelper.isDarkMode())
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
            mainFlowLayout.positioning(Positioning.relative(50, -175));

            mainFlowLayout.positioning().animate(1000, Easing.CUBIC, Positioning.relative(50, 50)).forwards();

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
                                    ColorableTextBoxComponent child = horizontalComponent.childById(ColorableTextBoxComponent.class, "gender_text_field");

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
        return ColorableTextBoxComponent.textBox(Sizing.fixed(58), importCharacterData ? currentCharacter.getGender() : "")
                .setEditAbility(currentSelection.openTextField())
                .tooltip(Text.of("Required"))
                .configure(component -> {
                    if(component instanceof ColorableTextBoxComponent widget){
                        widget.setMaxLength(100);
                    }
                })
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

        String gender;

        if(currentSelection != GenderSelection.OTHER){
            gender = currentSelection.translation().getString();
        } else {
            gender = rootComponent.childById(TextFieldWidget.class, "gender_text_field").getText();

            if(gender.isBlank()) return false;
        }

        ClientPlayerEntity entity = MinecraftClient.getInstance().player;

        String description = rootComponent.childById(EditBoxComponent.class, "description_text_box").convertTextBox();

        String biography = rootComponent.childById(EditBoxComponent.class, "biography_text_box").convertTextBox();

        int age = (int) rootComponent.childById(DiscreteSliderComponent.class, "age_slider").discreteValue();

        int activityOffset = entity.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME));

        //-----------------------------Addon Data-----------------------------

        Map<Identifier, BaseAddon> addonData = new HashMap<>();

        for(Map.Entry<Identifier, PersonalityScreenAddon> entry : screenAddons.entrySet()){
            PersonalityScreenAddon addon = entry.getValue();

            if(addon.isDataEmpty(rootComponent) && addon.requiresUserInput()) return false;

            addonData.putAll(addon.getAddonData());
        }

        Map<Identifier, String> addonDataJson = Util.make(new HashMap<>(), map -> addonData.forEach((id, addon) -> map.put(id, PersonalityMod.GSON.toJson(addon))));

        Character character = new Character(this.currentCharacter != null ? this.currentCharacter.getUUID() : UUID.randomUUID().toString(), entity.getUuidAsString(), name, gender, description, biography, age, activityOffset);

        if(this.currentMode == CharacterScreenMode.EDITING){
            if(this.currentCharacter == null) return false;

            List<String> changedValues = new ArrayList<>();

            Map<String, Supplier<Boolean>> mapInteraction = Map.of(
                    "gender", () -> this.currentCharacter.getGender().equals(gender),
                    "name", () -> this.currentCharacter.getName().equals(name),
                    "description", () -> this.currentCharacter.getDescription().equals(description),
                    "biography", () -> this.currentCharacter.getBiography().equals(biography),
                    "age", () -> this.currentCharacter.getAge() == age
            );

            boolean baseCharacterDataChanged = false;

            for (Map.Entry<String, Supplier<Boolean>> entry : mapInteraction.entrySet()) {
                if(!entry.getValue().get()){
                    baseCharacterDataChanged = true;
                    changedValues.add(entry.getKey());
                }
            }

            boolean addonDataChanged = false;

            for(Map.Entry<Identifier, BaseAddon> entry : addonData.entrySet()){
                BaseAddon addon = this.currentCharacter.getAddon(entry.getKey());

                if(!entry.getValue().equals(addon)) {
                    addonDataChanged = true;

                    changedValues.add(entry.getKey().toString());
                }
            }

            //Nothing to change so return
            if(!(baseCharacterDataChanged || addonDataChanged)) return true;

            if(baseCharacterDataChanged && addonDataChanged){
                Networking.sendC2S(new SyncC2SPackets.ModifyEntireCharacter(PersonalityMod.GSON.toJson(character), character.getUUID(), addonDataJson, changedValues));
            } else if(baseCharacterDataChanged){
                Networking.sendC2S(new SyncC2SPackets.ModifyBaseCharacterData(PersonalityMod.GSON.toJson(character), changedValues));
            } else if(addonDataChanged){
                Networking.sendC2S(new SyncC2SPackets.ModifyAddonData(character.getUUID(), addonDataJson, changedValues));
            }

        } else {
            Networking.sendC2S(new SyncC2SPackets.NewCharacter(PersonalityMod.GSON.toJson(character), addonDataJson, true));
        }

        return true;
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
        return currentMode.importFromCharacter();
    }
}
