package io.blodhgarm.personality.misc.owo;

import io.blodhgarm.personality.misc.pond.owo.LineManageable;
import io.wispforest.owo.ui.core.Component;

public interface LineEvent {

    boolean action(LineManageable<?> manager, String eventType);

}
