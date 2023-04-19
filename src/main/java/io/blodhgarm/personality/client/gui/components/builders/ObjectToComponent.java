package io.blodhgarm.personality.client.gui.components.builders;

import io.wispforest.owo.ui.core.Component;

public interface ObjectToComponent<T> {
    Component build(T entry, boolean isParentVertical);
}
