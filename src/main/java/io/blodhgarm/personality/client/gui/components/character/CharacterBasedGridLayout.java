package io.blodhgarm.personality.client.gui.components.character;

import com.mojang.authlib.GameProfile;
import io.blodhgarm.personality.api.character.BaseCharacter;
import io.blodhgarm.personality.client.ClientCharacters;
import io.blodhgarm.personality.client.gui.CharacterViewMode;
import io.blodhgarm.personality.client.gui.components.builders.LabelComponentBuilder;
import io.blodhgarm.personality.client.gui.components.builders.LabeledObjectToComponent;
import io.blodhgarm.personality.client.gui.components.CustomButtonComponent;
import io.blodhgarm.personality.client.gui.components.grid.LabeledGridLayout;
import io.blodhgarm.personality.client.gui.screens.CharacterViewScreen;
import io.blodhgarm.personality.client.gui.utils.profiles.DelayableGameProfile;
import io.blodhgarm.personality.client.gui.utils.UIOps;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.apache.commons.lang3.function.TriFunction;

import java.util.*;

public class CharacterBasedGridLayout<T extends BaseCharacter> extends LabeledGridLayout<T> {

    private final List<ButtonComponent> buttons = new ArrayList<>();

    private final Screen originScreen;

    private CharacterViewMode mode = CharacterViewMode.VIEWING;

    private boolean openAsAdmin = false;

    public CharacterBasedGridLayout(Sizing horizontalSizing, Sizing verticalSizing, Screen originScreen) {
        super(horizontalSizing, verticalSizing);

        this.originScreen = originScreen;

        this.addBuilder(
                Text.empty(),
                (character, mode, isParentVertical) -> {
                    ButtonComponent buttonComponent = new CustomButtonComponent(
                            Text.of(mode.isModifiableMode() ? "✎" : "☰"),
                            (ButtonComponent component) -> {
                                GameProfile playerProfile = UIOps.getProfile(ClientCharacters.INSTANCE.getPlayerUUID(character.getUUID()));

                                CharacterViewScreen screen = new CharacterViewScreen(this.mode, playerProfile, character)
                                        .adminMode(openAsAdmin)
                                        .setOriginScreen(this.originScreen);

                                MinecraftClient.getInstance().setScreen(screen);
                            }
                    ).configure(component -> {
                        component.sizing(Sizing.fixed(10)) //13
                                .positioning(Positioning.absolute(16, 1))
                                .zIndex(10);
                    });

                    buttons.add(buttonComponent);

                    DelayableGameProfile profile = UIOps.getDelayedProfile(ClientCharacters.INSTANCE.getPlayerUUID(character.getUUID()));

                    return Containers.verticalFlow(Sizing.fixed(28), Sizing.fixed(24))
                            .child(buttonComponent)
                            .child(
                                    UIOps.playerEntityComponent(Sizing.fixed(20), profile)
                                            .scale(0.4f)
                                            .allowMouseRotation(true)
                                            //.tooltip(character.getFormattedName())
                                            .margins(Insets.of(4, 0, 4, 0))
                            );
                }
        ).addBuilder(
                Text.of("Name"),
                (character, mode1, isParentVertical) -> Containers.verticalFlow(Sizing.fixed(96), Sizing.content())
                        .child(Components.label(character.getFormattedName())
                                .maxWidth(100)
                                .margins(Insets.of(2))
                        )
        );
    }

    public CharacterBasedGridLayout<T> openAsAdmin(boolean value){
        this.openAsAdmin = value;

        return this;
    }

    public CharacterBasedGridLayout<T> addBuilder(Text text, TriFunction<T, CharacterViewMode, Boolean, Component> builder){
        this.addBuilder(-1, isParentVertical -> Components.label(text), builder);

        return this;
    }

    public CharacterBasedGridLayout<T> addBuilder(LabelComponentBuilder label, TriFunction<T, CharacterViewMode, Boolean, Component> builder){
        this.addBuilder(-1, label, builder);

        return this;
    }

    public CharacterBasedGridLayout<T> addBuilder(int index, LabelComponentBuilder label, TriFunction<T, CharacterViewMode, Boolean, Component> builder){
        this.addBuilder(index, new LabeledObjectToComponent<>(label, new CharacterToComponent(() -> this.mode, builder)));

        return this;
    }

    public CharacterBasedGridLayout<T> changeMode(CharacterViewMode mode){
        this.mode = mode;

        for (ButtonComponent b : buttons) b.setMessage(Text.of(mode.isModifiableMode() ? "✎" : "☰"));

        return this;
    }

    public CharacterViewMode getMode(){
        return this.mode;
    }

    public List<T> getCharactersWithinLayout(){
        return this.entryToComponents.keyList()
                .stream()
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public LabeledGridLayout<T> clearEntries() {
        this.buttons.clear();

        return super.clearEntries();
    }
}
