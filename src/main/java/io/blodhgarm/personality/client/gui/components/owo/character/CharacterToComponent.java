package io.blodhgarm.personality.client.gui.components.owo.character;

import io.blodhgarm.personality.api.character.BaseCharacter;
import io.blodhgarm.personality.client.gui.CharacterScreenMode;
import io.blodhgarm.personality.client.gui.builders.LabelComponentBuilder;
import io.blodhgarm.personality.client.gui.builders.LabeledObjectToComponent;
import io.blodhgarm.personality.client.gui.builders.ObjectToComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.core.Component;
import net.minecraft.text.Text;
import org.apache.commons.lang3.function.TriFunction;

import java.util.function.Supplier;

public class CharacterToComponent implements ObjectToComponent<BaseCharacter> {

    public final Supplier<CharacterScreenMode> modeSup;
    public final TriFunction<BaseCharacter, CharacterScreenMode, Boolean, Component> builder;

    public CharacterToComponent(Supplier<CharacterScreenMode> modeSup, TriFunction<BaseCharacter, CharacterScreenMode, Boolean, Component> builder){
        this.modeSup = modeSup;
        this.builder = builder;
    }

    public Component build(BaseCharacter character, boolean isParentVertical) {
        return builder.apply(character, modeSup.get(), isParentVertical);
    }
}
