package io.blodhgarm.personality.client.gui.builders;

import io.wispforest.owo.ui.core.Component;

public interface ObjectToComponent<T> {
    Component build(T character, boolean isParentVertical);
}
