package io.blodhgarm.personality.misc.pond.owo;

import io.blodhgarm.personality.client.gui.utils.polygons.AbstractPolygon;
import io.wispforest.owo.ui.core.Component;

import java.util.List;

public interface InclusiveBoundingArea<T extends Component> {

    <P extends AbstractPolygon> T addInclusionZone(P... polygon);

    <P extends AbstractPolygon> T addInclusionZone(List<P> polygons);

    List<AbstractPolygon> getInclusionZones();

    default boolean isWithinInclusionZone(float x, float y){
        for(AbstractPolygon polygon : getInclusionZones()){
            if(polygon.withinShape(x, y)) return true;
        }

        return false;
    }
}
