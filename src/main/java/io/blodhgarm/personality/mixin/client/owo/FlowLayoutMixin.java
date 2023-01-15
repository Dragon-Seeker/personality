package io.blodhgarm.personality.mixin.client.owo;

import io.blodhgarm.personality.client.gui.components.LineComponent;
import io.blodhgarm.personality.client.gui.utils.polygons.AbstractPolygon;
import io.blodhgarm.personality.misc.owo.LineEvent;
import io.blodhgarm.personality.misc.pond.owo.*;
import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = FlowLayout.class, remap = false)
public abstract class FlowLayoutMixin extends BaseParentComponent implements ExcludableBoundingArea<FlowLayout>, RefinedBoundingArea<FlowLayout>, LineManageable<FlowLayout>, FocusCheckable, CustomFocusHighlighting<FlowLayout> {

    private final List<AbstractPolygon> exclusionZones = new ArrayList<>();

    @Nullable private AbstractPolygon refinedBoundingArea = null;

    private final List<LineComponent> lines = new ArrayList<>();

    private final List<LineEvent> lineEvents = new ArrayList<>();

    private boolean setupInteractionEvents = false;

    private final EventStream<FocusCheck> allowFocusEvents = FocusCheck.newStream();

    @Nullable private HighlightRenderEvent highlightRenderEvent = null;

    protected FlowLayoutMixin(Sizing horizontalSizing, Sizing verticalSizing) {
        super(horizontalSizing, verticalSizing);
    }

    //----------------------------

    @Override
    public List<AbstractPolygon> getExclusionZones() {
        return exclusionZones;
    }

    @SafeVarargs
    @Override
    public final <P extends AbstractPolygon> FlowLayout addExclusionZone(P... polygons) {
        this.exclusionZones.addAll(List.of(polygons));

        return (FlowLayout) (Object) this;
    }

    @Override
    public final <P extends AbstractPolygon> FlowLayout addExclusionZone(List<P> polygons) {
        this.exclusionZones.addAll(polygons);

        return (FlowLayout) (Object) this;
    }

    //----------------------------

    @Override
    public <P extends AbstractPolygon> FlowLayout setRefinedBound(P polygon) {
        this.refinedBoundingArea = polygon;

        return (FlowLayout) (Object) this;
    }

    @Nullable
    @Override
    public AbstractPolygon getRefinedBound() {
        return refinedBoundingArea;
    }

    //----------------------------

    @Override
    public FlowLayout addLine(LineComponent line) {
        this.lines.add(line);

        return (FlowLayout) (Object) this;
    }

    @Override
    public FlowLayout addLines(List<LineComponent> lines) {
        this.lines.addAll(lines);

        return (FlowLayout) (Object) this;
    }

    @Override
    public FlowLayout addLineEvent(LineEvent event) {
        this.lineEvents.add(event);

        if(!setupInteractionEvents){
            this.focusGained().subscribe(source -> {
                this.lineEvents.forEach(event1 -> event1.action(this, "focus_gained"));
            });

            this.focusLost().subscribe(() -> {
                this.lineEvents.forEach(event1 -> event1.action(this, "focus_lost"));
            });

            this.mouseEnter().subscribe(() -> {
                this.lineEvents.forEach(event1 -> event1.action(this, "mouse_enter"));
            });

            this.mouseLeave().subscribe(() -> {
                this.lineEvents.forEach(event1 -> event1.action(this, "mouse_leave"));
            });

            setupInteractionEvents = true;
        }

        return (FlowLayout) (Object) this;
    }

    @Override
    public List<LineComponent> getLines() {
        return lines;
    }

    @Override
    public List<LineEvent> getLineEvents() {
        return lineEvents;
    }

    //----------------------------

    @Override
    public boolean canFocus(FocusSource source) {
        return allowFocusEvents.sink().allowFocusSource(source);
    }

    @Override
    public EventSource<FocusCheck> focusCheck() {
        return allowFocusEvents.source();
    }

    //----------------------------

    @Override
    public FlowLayout addCustomFocusRendering(HighlightRenderEvent event) {
        this.highlightRenderEvent = event;

        return (FlowLayout) (Object) this;
    }

    @Override
    @Nullable
    public HighlightRenderEvent getEvent() {
        return this.highlightRenderEvent;
    }
}
