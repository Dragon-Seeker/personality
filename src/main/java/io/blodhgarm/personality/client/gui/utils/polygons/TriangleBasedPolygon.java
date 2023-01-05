package io.blodhgarm.personality.client.gui.utils.polygons;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class TriangleBasedPolygon implements AbstractPolygon {

    private final List<Triangle> triangles;

    public TriangleBasedPolygon(List<Triangle> triangles){
        this.triangles = triangles;
    }

    @Override
    public boolean withinShape(float x, float y) {
        for(Triangle triangle : triangles){
            if(triangle.withinShape(x, y)) return true;
        }

        return false;
    }

    @Override
    public void drawPolygon(MatrixStack matrices, int color, boolean showOutline, boolean showBackground) {
        for(Triangle triangle : triangles){
            triangle.drawPolygon(matrices, color, showOutline, showBackground);
        }
    }

    @Override
    public void movePolygon(Vec3f vec, BiConsumer<Vec3f, Vec3f> action) {
        triangles.forEach(triangle -> triangle.movePolygon(vec, action));
    }

    @Override
    public List<Vec3f> getPoints() {
        List<Vec3f> points = new ArrayList<>();

        for(Triangle triangle : triangles){
            triangle.getPoints().forEach(vec2f -> {
                if(!points.contains(vec2f)) points.add(vec2f);
            });
        }

        return points;
    }

    @Override
    public String toString() {
        return "Triangles: " + this.triangles.toString();
    }


}
