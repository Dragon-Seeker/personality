package io.blodhgarm.personality.client.gui.utils.owo.layout;

import io.blodhgarm.personality.misc.pond.owo.LineManageable;
import io.wispforest.owo.ui.core.Component;

public interface LineEvent {

    boolean action(LineManageable<?> manager, String eventType);

}
