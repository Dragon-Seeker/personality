package io.blodhgarm.personality.client.glisco;

import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.client.HotbarMouseEvents;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Easing;
import io.wispforest.owo.ui.util.Drawer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

public class DescriptionRenderer implements InWorldTooltipRenderer.Renderer, HotbarMouseEvents.AllowMouseScroll {

    public static final DescriptionRenderer INSTANCE = new DescriptionRenderer();

    //----

    public boolean disableRenderer = true;

    public boolean disableAutomaticScrolling = false;

    //----

    private List<OrderedText> convertedData = List.of();

    private int maxTextWidth = 0;

    private int totalLineCount = 0;

    public float YOffset = 0.0f;

    public float maxTextHeight = 0;

    //----

    public float endingWait = -2f;

    public InterpolatedHandler<Float> lineSpeedHandler = new InterpolatedHandler<>(1.25f, 3.0f, (baseValue, delta) -> delta * baseValue);

    public UnitHandler<Float> textPositionHandler = new UnitHandler<>(1.0f, delta -> delta * totalLineCount)
            .setUnitConversion(delta -> {
                boolean disableSpeed = !PersonalityMod.CONFIG.descriptionConfig.automaticScrolling()
                        || disableAutomaticScrolling
                        || endingWait != -2f
                        || isTextSizeLessThanWindow();

                return ((delta * (disableSpeed ? 0f : lineSpeedHandler.get())) / totalLineCount);
            })
            .setResetCheckFunc(delta -> {
                if(delta > 1.0f && endingWait == -2f) endingWait = 10;

                return false;
            });

    //----

    public InterpolatedHandler<Color> bqHandler = new InterpolatedHandler<>(new Color(0.2f, 0.2f, 0.2f, 0.6f), 0.5f, (baseValue, delta) -> {
        return baseValue.interpolate(new Color(0.2f, 0.2f, 0.2f, 0.0f), 1.0f - delta);
    });

    public UnitHandler<Float> startFadeHandler = new UnitHandler<>(0.0f, (delta) -> {
        float progress; //= (currViewTime - 5 - Math.round(YOffset / 10f)) / 15;

        float diff = (YOffset / 10) - ((delta * (isTextSizeLessThanWindow() ? (maxTextHeight / 10) : totalLineCount)) - 12);

        float windowSize = 10.0f;

        //if(isTextSizeLessThanWindow()) return 1.0f;

        if(diff > windowSize) { progress = 0.0f; }
        else if(diff < 0.0f) { progress = 1.0f; }
        else { progress = 1.0f - (diff / windowSize); }

        return progress;
    }).setUnitConversion(delta -> (delta * 16.5f));

    //----

    public List<UpdateHandler<?, ?>> handlers = List.of(lineSpeedHandler, textPositionHandler, bqHandler, startFadeHandler);

    //---

    public static void init(){
        HotbarMouseEvents.ALLOW_MOUSE_SCROLL.register(INSTANCE);
    }

    //---

    @Override
    public void render(List<InWorldTooltipProvider.Entry> entries, InWorldTooltipRenderer.HitResultInfo hitResult, MatrixStack matrices, boolean newProvider, float delta) {
        var textXOffset = 15;
        var maxTextWidth = 200; //200
        var textRender = MinecraftClient.getInstance().textRenderer;

        var maxBqHeight = ((hitResult.targetShape().maxY + .15) / 0.01) - 30;

        var tenthPlace = maxBqHeight % 10;

        maxBqHeight += (tenthPlace > 6 ? 10 : 0) - tenthPlace;

        maxTextHeight = (float) (maxBqHeight - 10);

        if(newProvider){
            convertedData = entries.stream()
                    .map(e -> Util.make(
                            new ArrayList<>(textRender.wrapLines(e.label(), maxTextWidth)),
                            textList -> { if(textList.isEmpty()) textList.add(OrderedText.EMPTY); }
                    ))
                    .flatMap(Collection::stream)
                    .peek(o -> this.maxTextWidth = Math.max(this.maxTextWidth, textRender.getWidth(o)))
                    .toList();

            totalLineCount = convertedData.size();

            if(!isTextSizeLessThanWindow()) {
                totalLineCount = totalLineCount - Math.round(((float) Math.floor(maxTextHeight + 10) / 10));
            }

            handlers.forEach(UpdateHandler::reset);

            lineSpeedHandler.waitValue = 2.5f;

            startFadeHandler.updateCutoffValue(isTextSizeLessThanWindow() ? (maxTextHeight / 10) : totalLineCount);

            disableAutomaticScrolling = false;
        }

        if(disableRenderer) return;

        if(endingWait == -1.0f) {
            startFadeHandler.reset();

            lineSpeedHandler.reset();

            textPositionHandler.reset();

            endingWait = -2f;
        } else if(endingWait > -1.0f){
            endingWait = Math.max(endingWait - (delta/20), -1f);
        }

        handlers.forEach(h -> h.addWithTime((delta / 20)));

        matrices.push();

//        matrices.translate(0,-10, 0);
//
//        matrices.translate(0,-12,0);

        matrices.translate(0,8,0);

        //---- Background

        int padding = 8;

        matrices.push();

        matrices.translate(textXOffset, -4.5, -5); // -0.5

        Drawer.fill(matrices, -padding, -padding, Math.round(padding * 1.5f) + maxTextWidth, (padding * 2) + Math.round((float) maxBqHeight) + 12, bqHandler.get().argb());

        matrices.pop();

        textRender.draw(matrices, Text.of("Description"), textXOffset, 0, (Math.max(4, (int) (0xFF * startFadeHandler.get())) << 24) | 0xFFFFFF);

        matrices.translate(0, 12, 0);


        //----

        this.YOffset = (textPositionHandler.get() * -10);

        for (OrderedText orderedText : convertedData) {
            AtomicBoolean isEmpty = new AtomicBoolean(true);

            orderedText.accept((i, s, c) -> isEmpty.getAndSet(false));

            if (YOffset + 10 >= 0 && !isEmpty.get()) {
                float progress = calculateProgress(YOffset, maxTextHeight);

                matrices.push();
                matrices.translate(0, 0, Easing.CUBIC.apply(1 - progress) * -4);

                textRender.draw(matrices, orderedText, textXOffset, YOffset, (Math.max(4, (int) (0xFF * progress)) << 24) | 0xFFFFFF);

                matrices.pop();
            }

            YOffset += 10;

            if(YOffset > maxTextHeight + 10f) break;
        }

        matrices.pop();
    }

    public float calculateProgress(float YOffset, double maxTextHeight){
        float startProgress = this.startFadeHandler.get();

        float scrolledProgress = 1.0f;

        if(YOffset - maxTextHeight > 0){
            scrolledProgress = (10 - (YOffset - Math.round(maxTextHeight))) / 10f;
        } else if(YOffset < 5 ) { //&& YOffset >= 0
            scrolledProgress = (((disableAutomaticScrolling) ? 10 : 3) + YOffset) / 10f;
        }

        return MathHelper.clamp(Math.min(scrolledProgress, startProgress), 0, 1);
    }

    public boolean isTextSizeLessThanWindow(){
        return (totalLineCount * 10) < maxTextHeight;
    }

    @Override
    public boolean allowMouseScroll(ClientPlayerEntity player, double horizontalAmount, double verticalAmount) {
        boolean bl = disableRenderer
                || !InWorldTooltipRenderer.INSTANCE.attemptingRendering()
                || !InWorldTooltipRenderer.INSTANCE.lastProvider.getTooltipId().equals(PersonalityMod.id("description"))
                || isTextSizeLessThanWindow();

        if(bl) return true;

        float scrolledTextPosition = (float) ((Math.round(this.textPositionHandler.get()) - (verticalAmount % totalLineCount)) / totalLineCount);

        this.textPositionHandler.currentValue = MathHelper.clamp(scrolledTextPosition, 0.0f, 1.0f);

        if(!this.disableAutomaticScrolling && PersonalityMod.CONFIG.descriptionConfig.automaticScrolling()){
            player.sendMessage(Text.of("Disabled Auto Scrolling"), true);

            this.disableAutomaticScrolling = true;
        }

        return false;
    }

    public boolean currentlyActive(){
        var provider = InWorldTooltipRenderer.INSTANCE.lastProvider;

        return provider != null && InWorldTooltipRenderer.INSTANCE.REGISTERED_RENDERS.get(provider.getTooltipId()) instanceof DescriptionRenderer;
    }

    public static abstract class UpdateHandler<T, H extends UpdateHandler<T, H>> implements Supplier<T> {
        public float currentValue = 0;

        public float waitValue = 0;
        public float cutoffValue;

        public Function<Float, Boolean> shouldRest = aFloat -> false;

        public UpdateHandler(float cutoffValue){
            this.cutoffValue = cutoffValue;
        }

        public H setResetCheckFunc(Function<Float, Boolean> shouldRest){
            this.shouldRest = shouldRest;

            return (H) this;
        }

        public H updateCutoffValue(float cutoffValue){
            this.cutoffValue = cutoffValue;

            return (H) this;
        }

        public H reset(){
            return reset(this.waitValue);
        }

        public H reset(float waitValue){
            this.currentValue = 0;
            this.waitValue = waitValue;

            return (H) this;
        }

        public H addWithTime(float time) {
            this.currentValue += time;

            if(this.shouldRest.apply(currentValue)) this.currentValue = 0;

            return (H) this;
        }

        public T get() {
            float finalMaxValue = this.waitValue + cutoffValue;

            float delta = (this.currentValue <= finalMaxValue)
                    ? Math.max(this.currentValue - this.waitValue, 0.0f) * (1.0f / (cutoffValue))
                    : 1.0f;

            return applyDelta(delta);
        }

        protected abstract T applyDelta(float delta);

    }

    public static class InterpolatedHandler<T> extends UpdateHandler<T, InterpolatedHandler<T>> {
        public ComputeValue<T> interpretFunc;
        public T defaultValue;

        public InterpolatedHandler(T baseValue, float cutoffValue, ComputeValue<T> interpretFunc){
            super(cutoffValue);

            this.interpretFunc = interpretFunc;

            this.defaultValue = baseValue;
        }

        @Override
        public T applyDelta(float delta) {
            return interpretFunc.compute(defaultValue, delta);
        }

        public interface ComputeValue<T> {
            T compute(T baseValue, float delta);
        }
    }

    public static class UnitHandler<T> extends UpdateHandler<T, UnitHandler<T>> {
        public Function<Float, Float> unitConversion = time -> time;
        public Function<Float, T> computeFunc;

        public UnitHandler(float cutoffValue, Function<Float, T> computeFunc){
            super(cutoffValue);

            this.computeFunc = computeFunc;
        }

        public UnitHandler<T> setUnitConversion(Function<Float, Float> unitConversion){
            this.unitConversion = unitConversion;

            return this;
        }

        public UnitHandler<T> addWithTime(float time){
            return super.addWithTime(unitConversion.apply(time));
        }

        @Override
        public T applyDelta(float delta) {
            return computeFunc.apply(delta);
        }
    }
}
