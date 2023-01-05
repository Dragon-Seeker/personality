package io.blodhgarm.personality.client.gui.builders;

import io.blodhgarm.personality.api.BaseCharacter;
import io.blodhgarm.personality.client.gui.CharacterScreenMode;
import io.blodhgarm.personality.client.gui.components.owo.CustomEntityComponent;
import io.blodhgarm.personality.client.gui.components.owo.BetterGridLayout;
import io.blodhgarm.personality.client.gui.screens.CharacterScreen;
import io.blodhgarm.personality.misc.pond.owo.GridLayoutDuck;
import io.blodhgarm.personality.mixin.client.accessor.ScrollContainerAccessor;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ListedCharactersView {

    private final List<LabeledCharacterComponentWrapper> componentBuilders = new ArrayList<>();
    private final Supplier<List<BaseCharacter>> characterSupplier;

    private CharacterScreenMode mode = CharacterScreenMode.VIEWING;
    private Screen originScreen;

    public ListedCharactersView(Screen originScreen, Supplier<List<BaseCharacter>> characterSupplier){
        this.characterSupplier = characterSupplier;
        this.originScreen = originScreen;

        componentBuilders.add(new LabeledCharacterComponentWrapper(Text.empty(),
                (character, mode, isParentVertical) ->
                        Components.button(Text.of(mode.isModifiableMode() ? "✎" : "☉"), (ButtonComponent component) -> {
                            CharacterScreen screen = new CharacterScreen(mode, null, character);

                            screen.originScreen = originScreen;

                            MinecraftClient.getInstance().setScreen(screen);
                        }).sizing(Sizing.fixed(13))
                )
        );

        componentBuilders.add(new LabeledCharacterComponentWrapper(Text.of(""),
                (character, mode1, isParentVertical) ->
                        CustomEntityComponent.playerEntityComponent(Sizing.fixed(20), null)
                                .scale(0.4f)
                                .allowMouseRotation(true)
                                .tooltip(character.getFormattedName())
                )
        );

        componentBuilders.add(new LabeledCharacterComponentWrapper(Text.of("Name"),
                (character, mode1, isParentVertical) -> Components.label(character.getFormattedName())
                ).onlyAllowWhenVertical(true)
        );
    }

    public ListedCharactersView changeMode(CharacterScreenMode mode){
        this.mode = mode;

        return this;
    }

    public ListedCharactersView addComponent(Text text, LabeledCharacterComponentWrapper.CharacterBasedComponentBuilder builder){
        componentBuilders.add(new LabeledCharacterComponentWrapper(text, builder));

        return this;
    }

    public ListedCharactersView addComponent(LabeledCharacterComponentWrapper wrapper){
        componentBuilders.add(wrapper);

        return this;
    }

    public void buildLayout(FlowLayout rootComponent, ScrollContainer<FlowLayout> parent){
        boolean isVertical = ((ScrollContainerAccessor) parent).personality$direction() == ScrollContainer.ScrollDirection.VERTICAL;

        BetterGridLayout mainLayout = new BetterGridLayout(Sizing.content(), Sizing.content(), 1, 1)
                .setColumnDividingLine(1)
                .setRowDividingLine(1);

        List<BaseCharacter> characters = this.characterSupplier.get();

        List<LabeledCharacterComponentWrapper> wrappers = this.componentBuilders.stream()
                .filter(wrapper -> !wrapper.onlyShowWhenVertical || isVertical)
                .toList();

        boolean updatedSize = false;

        for(int i = 0; i < characters.size(); i++){
            BaseCharacter character = characters.get(i);

            List<Component> characterComponents = characterComponent(wrappers, character, isVertical);

            if(!updatedSize) updatedSize = ((GridLayoutDuck)mainLayout).resetSize(characters.size() + 1, characterComponents.size());

            for(int z = 0; z < characterComponents.size(); z++) {
                FlowLayout componentFlowlayout = Containers.verticalFlow(Sizing.content(), Sizing.content())
                        .child(characterComponents.get(z));

                if (isVertical) {
                    mainLayout.verticalAlignment(VerticalAlignment.CENTER);
                } else {
                    mainLayout.horizontalAlignment(HorizontalAlignment.CENTER);
                }

                mainLayout.child(componentFlowlayout,
                        isVertical ? i + 1 : z,
                        !isVertical ? i + 1 : z
                );
            }

//            if(i + 1 != characters.size()) {
//                Sizing horizontal = isVertical ? Sizing.fill(100) : Sizing.fixed(1);
//                Sizing vertical = !isVertical ? Sizing.fill(100) : Sizing.fixed(1);
//
//                mainLayout.child(Components.box(horizontal, vertical)
//                        .margins(Insets.vertical(3)));
//            }
        }

        for(int i = 0; i < wrappers.size(); i++){
            LabeledCharacterComponentWrapper wrapper = wrappers.get(i);

            mainLayout.child(wrapper.buildLabel(isVertical), 0, i);
        }

        rootComponent.child(mainLayout);
    }

    public List<Component> characterComponent(List<LabeledCharacterComponentWrapper> wrappers, BaseCharacter character, boolean isParentVertical){
        List<Component> mainComponentList = new ArrayList<>();

        wrappers.forEach(componentBuilder -> {
            mainComponentList.add(componentBuilder.build(character, this.mode, isParentVertical));
        });

//        if(isParentVertical){
//            mainLayout.verticalAlignment(VerticalAlignment.CENTER);
//        } else {
//            mainLayout.horizontalAlignment(HorizontalAlignment.CENTER);
//        }

        return mainComponentList;
    }
}
