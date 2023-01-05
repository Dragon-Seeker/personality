package io.blodhgarm.personality.client.gui.builders;

import io.blodhgarm.personality.api.BaseCharacter;
import io.blodhgarm.personality.client.gui.CharacterScreenMode;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.core.Component;
import net.minecraft.text.Text;

public class LabeledCharacterComponentWrapper {

    private final Text text;
    private final CharacterBasedComponentBuilder builder;

    public boolean onlyShowWhenVertical = false;

    public LabeledCharacterComponentWrapper(Text text, CharacterBasedComponentBuilder builder){
        this.text = text;
        this.builder = builder;
    }

    public LabeledCharacterComponentWrapper onlyAllowWhenVertical(boolean value){
        onlyShowWhenVertical = value;

        return this;
    }

    public LabelComponent buildLabel(boolean isParentVertical){
        return Components.label(text);
    }

    public Component build(BaseCharacter character, CharacterScreenMode mode, boolean isParentVertical){
        return builder.build(character, mode, isParentVertical);
    }

    public interface CharacterBasedComponentBuilder {
        Component build(BaseCharacter character, CharacterScreenMode mode, boolean isParentVertical);
    }
}
