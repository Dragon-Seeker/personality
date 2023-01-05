package io.blodhgarm.personality.misc.pond.owo;

import io.blodhgarm.personality.client.gui.components.LineComponent;
import io.blodhgarm.personality.misc.owo.LineEvent;
import io.wispforest.owo.ui.core.Component;

import java.util.List;

public interface LineManageable<T extends Component> {

    T addLine(LineComponent line);

    T addLines(List<LineComponent> lines);

    T addLineEvent(LineEvent event);

    List<LineComponent> getLines();

    List<LineEvent> getEvents();
}
