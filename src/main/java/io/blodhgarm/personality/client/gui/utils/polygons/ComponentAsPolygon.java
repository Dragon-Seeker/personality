package io.blodhgarm.personality.client.gui.utils.polygons;

import io.wispforest.owo.ui.core.PositionedRectangle;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Vector3f;

import java.util.List;

public class ComponentAsPolygon implements AbstractPolygon {

    public final PositionedRectangle wrappedComponent;

    public ComponentAsPolygon(PositionedRectangle component){
        this.wrappedComponent = component;
    }

    @Override
    public boolean withinShape(float x, float y) {
        return wrappedComponent.isInBoundingBox(x, y);
    }

    @Override
    public void drawPolygon(MatrixStack matrices, int color, boolean showOutline, boolean showBackground) {}

    @Override
    public List<Vector3f> getPoints() {
        return List.of();
    }
}
