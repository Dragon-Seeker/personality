package io.blodhgarm.personality.misc.pond.owo;

import io.blodhgarm.personality.client.gui.components.LineComponent;
import io.blodhgarm.personality.client.gui.utils.owo.LineEvent;
import io.wispforest.owo.ui.core.Component;

import java.util.List;

public interface LineManageable<T extends Component> extends Component {

    default T addLine(LineComponent line){
        this.getLines().add(line);

        return (T) this;
    }

    default T addLines(List<LineComponent> lines){
        this.getLines().addAll(lines);

        return (T) this;
    }

    default T addLineEvent(LineEvent event){
        this.getLineEvents().add(event);

        if(!hasSetupInteractionEvents()){
            this.focusGained().subscribe(source -> {
                this.getLineEvents().forEach(event1 -> event1.action(this, "focus_gained"));
            });

            this.focusLost().subscribe(() -> {
                this.getLineEvents().forEach(event1 -> event1.action(this, "focus_lost"));
            });

            this.mouseEnter().subscribe(() -> {
                this.getLineEvents().forEach(event1 -> event1.action(this, "mouse_enter"));
            });

            this.mouseLeave().subscribe(() -> {
                this.getLineEvents().forEach(event1 -> event1.action(this, "mouse_leave"));
            });

            toggleSetupInteractionEvents();
        }

        return (T) this;
    }

    List<Component> getLines();

    List<LineEvent> getLineEvents();

    boolean hasSetupInteractionEvents();

    void toggleSetupInteractionEvents();
}
