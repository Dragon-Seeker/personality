package io.blodhgarm.personality.client.gui.utils.polygons;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3f;

import java.util.List;
import java.util.function.BiConsumer;

public interface AbstractPolygon {

    boolean withinShape(float x, float y);

    default void drawPolygon(MatrixStack matrices, int color){
        this.drawPolygon(matrices, color, false, true);
    }

    void drawPolygon(MatrixStack matrices, int color, boolean showOutline, boolean showBackground);

    default void movePolygon(Vec3f vec, BiConsumer<Vec3f, Vec3f> action){
        this.getPoints().forEach(vec3f -> action.accept(vec3f, vec));
    }

    List<Vec3f> getPoints();
}
