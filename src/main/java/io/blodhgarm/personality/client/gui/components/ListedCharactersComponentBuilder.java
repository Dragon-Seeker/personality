package io.blodhgarm.personality.client.gui.components;

import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.client.gui.CharacterScreenMode;
import io.blodhgarm.personality.mixin.client.accessor.ScrollContainerAccessor;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.fabricmc.loader.impl.util.StringUtil;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.FabricUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class ListedCharactersComponentBuilder {

    private final List<BiFunction<Character, Boolean, Component>> componentBuilders = new ArrayList<>();
    private final Supplier<List<Character>> characterSupplier;

    private CharacterScreenMode mode = CharacterScreenMode.VIEWING;

    public ListedCharactersComponentBuilder(Supplier<List<Character>> characterSupplier){
        this.characterSupplier = characterSupplier;
    }

    public ListedCharactersComponentBuilder changeMode(CharacterScreenMode mode){
        this.mode = mode;

        return this;
    }

    public ListedCharactersComponentBuilder addComponent(BiFunction<Character, Boolean, Component> componentBuildFunction){
        componentBuilders.add(componentBuildFunction);

        return this;
    }

    public void buildLayout(FlowLayout rootComponent, ScrollContainer<FlowLayout> parent){
        boolean isVertical = ((ScrollContainerAccessor) parent).personality$direction() == ScrollContainer.ScrollDirection.VERTICAL;

        FlowLayout mainLayout = isVertical
                ? Containers.verticalFlow(Sizing.content(), Sizing.content())
                : Containers.horizontalFlow(Sizing.content(), Sizing.content());

        List<Character> characters = this.characterSupplier.get();

        for(int i = 0; i < characters.size(); i++){
            Character character = characters.get(i);

            mainLayout.child(characterComponent(character, isVertical));

            if(i + 1 != characters.size()) {
                Sizing horizontal = isVertical ? Sizing.fill(100) : Sizing.fixed(1);
                Sizing vertical = !isVertical ? Sizing.fill(100) : Sizing.fixed(1);

                mainLayout.child(Components.box(horizontal, vertical)
                        .margins(Insets.vertical(3)));
            }
        }

        rootComponent.child(mainLayout);
    }

    public Component characterComponent(Character character, boolean isParentVertical){
        FlowLayout mainLayout = isParentVertical
                ? Containers.horizontalFlow(Sizing.content(), Sizing.content())
                : Containers.verticalFlow(Sizing.content(), Sizing.content());

        mainLayout.child(
                CustomEntityComponent.playerEntityComponent(Sizing.fixed(20), null)
                        .scale(0.4f)
                        .allowMouseRotation(true)
                        .tooltip(Text.of(character.getName()))
        );

        if(isParentVertical){
            mainLayout.child(Components.label(Text.of(StringUtil.capitalize(character.getName()))));
        }

        mainLayout.child(
                Components.button(Text.of(this.mode.isModifiableMode() ? "✎" : "☉"), (ButtonComponent component) -> {

                })
        );

        componentBuilders.forEach(componentBuilder -> {
            mainLayout.child(componentBuilder.apply(character, isParentVertical));
        });

        if(isParentVertical){
            mainLayout.verticalAlignment(VerticalAlignment.CENTER);
        } else {
            mainLayout.horizontalAlignment(HorizontalAlignment.CENTER);
        }

        return mainLayout;
    }
}
