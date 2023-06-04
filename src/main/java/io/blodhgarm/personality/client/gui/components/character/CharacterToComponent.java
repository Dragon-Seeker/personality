package io.blodhgarm.personality.client.gui.components.character;

import io.blodhgarm.personality.api.character.BaseCharacter;
import io.blodhgarm.personality.client.gui.CharacterViewMode;
import io.blodhgarm.personality.client.gui.components.builders.ObjectToComponent;
import io.wispforest.owo.ui.core.Component;
import org.apache.commons.lang3.function.TriFunction;

import java.util.function.Supplier;

public class CharacterToComponent<T extends BaseCharacter> implements ObjectToComponent<T> {

    public final Supplier<CharacterViewMode> modeSup;
    public final TriFunction<T, CharacterViewMode, Boolean, Component> builder;

    public CharacterToComponent(Supplier<CharacterViewMode> modeSup, TriFunction<T, CharacterViewMode, Boolean, Component> builder){
        this.modeSup = modeSup;
        this.builder = builder;
    }

    public Component build(T entry, boolean isParentVertical) {
        return builder.apply(entry, modeSup.get(), isParentVertical);
    }
}
