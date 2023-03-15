package io.blodhgarm.personality.client.gui.components.owo.character;

import io.blodhgarm.personality.api.character.BaseCharacter;
import io.blodhgarm.personality.client.gui.CharacterScreenMode;
import io.blodhgarm.personality.client.gui.builders.LabelComponentBuilder;
import io.blodhgarm.personality.client.gui.builders.LabeledObjectToComponent;
import io.blodhgarm.personality.client.gui.components.owo.CustomButtonComponent;
import io.blodhgarm.personality.client.gui.components.owo.CustomEntityComponent;
import io.blodhgarm.personality.client.gui.components.owo.LabeledGridLayout;
import io.blodhgarm.personality.client.gui.screens.CharacterScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.apache.commons.lang3.function.TriFunction;

import java.util.function.BiFunction;

public class CharacterGridLayout extends LabeledGridLayout<BaseCharacter> {

    protected CharacterScreenMode mode = CharacterScreenMode.VIEWING;

    protected BiFunction<CharacterScreenMode, BaseCharacter, CharacterScreen> screenBuilder = (characterScreenMode, baseCharacter) -> {
        return new CharacterScreen(characterScreenMode, null, baseCharacter);
    };

    protected Screen originScreen;

    public CharacterGridLayout(Sizing horizontalSizing, Sizing verticalSizing, Screen originScreen, BiFunction<CharacterScreenMode, BaseCharacter, CharacterScreen> screenBuilder) {
        this(horizontalSizing, verticalSizing, originScreen);

        this.screenBuilder = screenBuilder;
    }

    public CharacterGridLayout(Sizing horizontalSizing, Sizing verticalSizing, Screen originScreen) {
        super(horizontalSizing, verticalSizing);

        this.originScreen = originScreen;

        addBuilder(
                Text.empty(),
                (character, mode, isParentVertical) -> {
                    FlowLayout mainLayout = Containers.verticalFlow(Sizing.fixed(28), Sizing.fixed(24));

                    mainLayout.child(new CustomButtonComponent(Text.of(mode.isModifiableMode() ? "✎" : "☰"), (ButtonComponent component) -> {
                                CharacterScreen screen = screenBuilder.apply(this.mode, character);

                                screen.originScreen = this.originScreen;

                                MinecraftClient.getInstance().setScreen(screen);
                            }).sizing(Sizing.fixed(10)) //13
                                    .positioning(Positioning.absolute(16, 1))
                                    .zIndex(10)
                    );

                    mainLayout.child(CustomEntityComponent.playerEntityComponent(Sizing.fixed(20), null)
                            .scale(0.4f)
                            .allowMouseRotation(true)
                            //                                        .tooltip(character.getFormattedName())
                            .margins(Insets.of(4, 0, 4, 0))
                    );

                    return mainLayout;
                }
        );

        addBuilder(
                Text.of("Name"),
                (character, mode1, isParentVertical) -> Containers.verticalFlow(Sizing.fixed(96), Sizing.content())
                        .child(Components.label(character.getFormattedName())
                                .maxWidth(100)
                                .margins(Insets.of(2))
                        )
        );
    }

    public CharacterGridLayout addBuilder(Text text, TriFunction<BaseCharacter, CharacterScreenMode, Boolean, Component> builder){
        this.addBuilder(-1, isParentVertical -> Components.label(text), builder);

        return this;
    }

    public CharacterGridLayout addBuilder(LabelComponentBuilder label, TriFunction<BaseCharacter, CharacterScreenMode, Boolean, Component> builder){
        this.addBuilder(-1, label, builder);

        return this;
    }

    public CharacterGridLayout addBuilder(int index, LabelComponentBuilder label, TriFunction<BaseCharacter, CharacterScreenMode, Boolean, Component> builder){
        this.addBuilder(index, new LabeledObjectToComponent<>(label, new CharacterToComponent(() -> this.mode, builder)));

        return this;
    }

    public CharacterGridLayout changeMode(CharacterScreenMode mode){
        this.mode = mode;

        return this;
    }
}
