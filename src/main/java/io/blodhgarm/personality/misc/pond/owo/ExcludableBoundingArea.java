package io.blodhgarm.personality.misc.pond.owo;

import io.blodhgarm.personality.client.gui.utils.polygons.AbstractPolygon;
import io.wispforest.owo.ui.core.Component;
import net.minecraft.util.math.Vec2f;

import java.util.List;

public interface ExcludableBoundingArea<T extends Component> {

    <P extends AbstractPolygon> T addExclusionZone(P... polygon);

    <P extends AbstractPolygon> T addExclusionZone(List<P> polygons);

    List<AbstractPolygon> getExclusionZones();

    default boolean isWithinExclusionZone(float x, float y){
        for(AbstractPolygon polygon : getExclusionZones()){
            if(polygon.withinShape(x, y)) return true;
        }

        return false;
    }
}
