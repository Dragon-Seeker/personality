package io.blodhgarm.personality.client.gui.builders;

import io.blodhgarm.personality.api.character.BaseCharacter;
import io.blodhgarm.personality.client.gui.CharacterScreenMode;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.core.Component;
import net.minecraft.text.Text;

public class PerCharacterComponentHelper {

    private final LabelBuilder labelBuilder;
    private final PerCharacterBuilder perCharacterBuilder;

    public boolean onlyShowWhenVertical = false;

    //---------------------------------

    public PerCharacterComponentHelper(LabelBuilder labelBuilder, PerCharacterBuilder perCharacterBuilder){
        this.labelBuilder = labelBuilder;
        this.perCharacterBuilder = perCharacterBuilder;
    }

    public static PerCharacterComponentHelper of(Text text, PerCharacterBuilder perCharacterBuilder){
        return new PerCharacterComponentHelper(isParentVertical -> Components.label(text), perCharacterBuilder);
    }

    //---------------------------------

    public PerCharacterComponentHelper onlyAllowWhenVertical(boolean value){
        onlyShowWhenVertical = value;

        return this;
    }

    //---------------------------------

    public Component buildLabel(boolean isParentVertical){
        return labelBuilder.build(isParentVertical);
    }

    public Component buildPerCharacterComponent(BaseCharacter character, CharacterScreenMode mode, boolean isParentVertical){
        return perCharacterBuilder.build(character, mode, isParentVertical);
    }

    //---------------------------------

    public interface LabelBuilder { Component build(boolean isParentVertical); }
    public interface PerCharacterBuilder { Component build(BaseCharacter character, CharacterScreenMode mode, boolean isParentVertical); }
}
