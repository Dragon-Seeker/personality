package io.blodhgarm.personality.client.gui.utils.polygons;

import com.mojang.blaze3d.systems.RenderSystem;
import io.blodhgarm.personality.client.gui.components.LineComponent;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec2f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;
import java.util.Objects;

public final class Triangle implements AbstractPolygon {

    private final Vector3f point1;
    private final Vector3f point2;
    private final Vector3f point3;

    public Triangle(Vector3f point1, Vector3f point2, Vector3f point3) {
        this.point1 = point1;
        this.point2 = point2;
        this.point3 = point3;
    }

    @Override
    public boolean withinShape(float x, float y) {
        return isWithinTriangle(x, y);
    }

    /**
     * Method used to check if a point belongs within the given Triangle
     * @return if the given point is found within the triangle
     */
    public boolean isWithinTriangle(float x, float y) {
        // Based on code Made by SebLague : https://github.com/SebLague/Gamedev-Maths/blob/master/PointInTriangle.cs

        //Used to prevent a divide by zero issue on w2
        boolean flipPoints = point3.y() - point1.y() == 0;

        Vector3f point1 = flipPoints ? this.point2 : this.point1;
        Vector3f point2 = flipPoints ? this.point1 : this.point2;

        float yDiffP_1 = y - point1.y();
        float xDiff1_P = point1.x() - x;

        float yDiff2_1 = point2.y() - point1.y();
        float xDiff2_1 = point2.x() - point1.x();

        float yDiff3_1 = point3.y() - point1.y();
        float xDiff3_1 = point3.x() - point1.x();

        float w1 = (xDiff1_P * yDiff3_1 + yDiffP_1 * xDiff3_1) / (yDiff2_1 * xDiff3_1 - xDiff2_1 * yDiff3_1);
        float w2 = (yDiffP_1 - w1 * yDiff2_1) / yDiff3_1;

        return w1 >= 0 && w2 >= 0 && (w1 + w2) <= 1;
    }

    public void drawPolygon(MatrixStack matrices, int color, boolean showOutline, boolean showBackground) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float x1 = this.point1.x(), y1 = this.point1.y(),
                x2 = this.point2.x(), y2 = this.point2.y(),
                x3 = this.point3.x(), y3 = this.point3.y();

        float f = (float) (color >> 24 & 0xFF) / 255.0F;
        float g = (float) (color >> 16 & 0xFF) / 255.0F;
        float h = (float) (color >> 8 & 0xFF) / 255.0F;
        float j = (float) (color & 0xFF) / 255.0F;

        if (showBackground) {
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            RenderSystem.enableBlend();
            //RenderSystem.disableTexture();
            RenderSystem.defaultBlendFunc();

            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();

            RenderSystem.setShader(GameRenderer::getPositionColorProgram);

            bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

            bufferBuilder.vertex(matrix, (float) x1, (float) y1, 0.0F).color(g, h, j, f).next();
            bufferBuilder.vertex(matrix, (float) x2, (float) y2, 0.0F).color(g, h, j, f).next();
            bufferBuilder.vertex(matrix, (float) x3, (float) y3, 0.0F).color(g, h, j, f).next();

            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

            RenderSystem.enableCull();
            RenderSystem.disableDepthTest();

            //RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        }

        if (showOutline) {
            float lineWidth = 1.0f;

            int newColor = new Color(g, h, j, 1.0f).argb();

            LineComponent.drawLine(matrices, new Vec2f(x1, y1), new Vec2f(x2, y2), lineWidth, newColor, newColor);
            LineComponent.drawLine(matrices, new Vec2f(x2, y2), new Vec2f(x3, y3), lineWidth, newColor, newColor);
            LineComponent.drawLine(matrices, new Vec2f(x3, y3), new Vec2f(x1, y1), lineWidth, newColor, newColor);
        }
    }

    @Override
    public List<Vector3f> getPoints() {
        return List.of(point1, point2, point3);
    }

    public Vector3f point1() {
        return point1;
    }

    public Vector3f point2() {
        return point2;
    }

    public Vector3f point3() {
        return point3;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Triangle) obj;
        return Objects.equals(this.point1, that.point1) &&
                Objects.equals(this.point2, that.point2) &&
                Objects.equals(this.point3, that.point3);
    }

    @Override
    public int hashCode() {
        return Objects.hash(point1, point2, point3);
    }

    @Override
    public String toString() {
        return "Triangle[" +
                "point1=" + "[X: " + point1.x() + ", Y: " + point1.y() + "]" + ", " +
                "point2=" + "[X: " + point2.x() + ", Y: " + point2.y() + "]" + ", " +
                "point3=" + "[X: " + point3.x() + ", Y: " + point3.y() + "]" + ']';
    }

}


