package io.blodhgarm.personality.api.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class OnWorldSaveEvent {

    public static final Event<Save> ON_SAVE = EventFactory.createArrayBacked(Save.class, callbacks -> (suppressLogs, flush, force) -> {
        for (Save callback : callbacks) callback.onSave(suppressLogs, flush, force);
    });

    public interface Save { void onSave(boolean suppressLogs, boolean flush, boolean force); }
}
