package io.blodhgarm.personality.client.gui.components.builders;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.core.Component;
import net.minecraft.text.Text;

public class LabeledObjectToComponent<T> implements ObjectToComponent<T>, LabelComponentBuilder {

    private final LabelComponentBuilder labelBuilder;
    private final ObjectToComponent<T> perCharacterBuilder;

    //---------------------------------

    public LabeledObjectToComponent(LabelComponentBuilder labelBuilder, ObjectToComponent<T> perCharacterBuilder){
        this.labelBuilder = labelBuilder;
        this.perCharacterBuilder = perCharacterBuilder;
    }

    public static <T> LabeledObjectToComponent<T> of(Text text, ObjectToComponent<T> perCharacterBuilder){
        return new LabeledObjectToComponent<>(isParentVertical -> Components.label(text), perCharacterBuilder);
    }

    //---------------------------------

    @Override
    public Component build(T entry, boolean isParentVertical) {
        return perCharacterBuilder.build(entry, isParentVertical);
    }

    @Override
    public Component buildLabel(boolean isParentVertical) {
        return labelBuilder.buildLabel(isParentVertical);
    }

    //---------------------------------

}
