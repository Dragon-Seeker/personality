package io.blodhgarm.personality.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import io.blodhgarm.personality.client.ClientCharacterTick;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.Animatable;
import io.wispforest.owo.ui.core.AnimatableProperty;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.Drawer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DiscoveryProgressComponent extends BaseComponent {

    public AnimatableProperty<AnimatableObject<Float>> alphaProperty = AnimatableProperty.of(new AnimatableObject<>(0.85f, (current, next, delta) -> MathHelper.lerp(delta, current, next)));

    public AnimatableProperty<Color> primaryColor = AnimatableProperty.of(new Color(0.2f, 0.5f, 0.5f));

    @Override
    public void update(float delta, int mouseX, int mouseY) {
        super.update(delta, mouseX, mouseY);

        alphaProperty.update(delta);
        primaryColor.update(delta);
    }

    public AnimatableProperty<AnimatableObject<Float>> alphaProperty(){
        return this.alphaProperty;
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        RenderSystem.enableBlend();

        RenderSystem.defaultBlendFunc();

        float alpha = alphaProperty.get().get();

        Color currentPrimary = primaryColor.get();

        Color outlineColor = currentPrimary.interpolate(new Color(0.05f,0.05f,0.05f), 0.6f);

        Drawer.drawRectOutline(matrices, x, y, width, height, withAlpha(outlineColor, alpha)); //+ 0.15f

        matrices.push();

        matrices.translate(1, 1,0);

        int fillWidth = width - 2;

        float fillAmount = MathHelper.clamp(ClientCharacterTick.INSTANCE.timeLookedAt / 50f, 0, 1);

        float value = 0.7f;

        int left = withAlpha(new Color(value, value, value).interpolate(outlineColor, 0.5f), alpha);
        int right = withAlpha(currentPrimary, alpha); //new Color(1.0f,1.0f,1.0f).interpolate(primaryColor,0.9f)

        Drawer.drawGradientRect(matrices, x, y, Math.round(fillAmount * fillWidth), height - 2, left, right, right, left);

        matrices.pop();

        RenderSystem.disableBlend();
    }

    public static int withAlpha(Color color, float alpha){
        return color.rgb() | (int) (alpha * 255) << 24;
    }

    @Override
    protected int determineHorizontalContentSize(Sizing sizing) {
        return sizing.value;
    }

    @Override
    protected int determineVerticalContentSize(Sizing sizing) {
        return sizing.value;
    }

    public static class AnimatableObject<T> implements Animatable<AnimatableObject<T>>, Supplier<T> {

        public T value;

        private final Interpolation<T> interpolateFunc;

        public AnimatableObject(T value, Interpolation<T> interpolateFunc){
            this.value = value;

            this.interpolateFunc = interpolateFunc;
        }

        public static AnimatableObject<Float> ofFloat(Float num){
            return new AnimatableObject<>(num, (current, next, delta) -> MathHelper.lerp(delta, current, next));
        }

        @Override
        public AnimatableObject<T> interpolate(AnimatableObject<T> next, float delta) {
            T newValue = interpolateFunc.interpolate(this.value, next.value, delta);

            return new AnimatableObject<>(newValue, interpolateFunc);
        }

        @Override
        public T get() {
            return value;
        }

        public AnimatableObject<T> set(T value){
            this.value = value;

            return this;
        }

        public interface Interpolation<T> {
            T interpolate(T current, T next, float delta);
        }
    }
}
