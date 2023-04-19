package io.blodhgarm.personality.mixin.client.owo;

import io.blodhgarm.personality.misc.pond.owo.AnimationExtension;
import io.wispforest.owo.ui.core.Animatable;
import io.wispforest.owo.ui.core.Animation;
import io.wispforest.owo.ui.core.Easing;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Mixin(value = Animation.class, remap = false)
public abstract class AnimationMixin<A extends Animatable<A>> implements AnimationExtension<A> {

    @Shadow private float delta;

    @Shadow @Final private Easing easing;
    @Shadow @Final private A from;
    @Shadow @Final private A to;

    @Unique @Nullable private Consumer<Animation<A>> onCompletion = null;

    @Unique private boolean eventCompleted = false;

    @Inject(method = "update", at = @At(value = "JUMP", opcode = Opcodes.IFNE, shift = At.Shift.BY, by = 2))
    private void afterAnimationCompletion(float delta, CallbackInfo ci){
        if(this.onCompletion != null && !this.eventCompleted){
            this.onCompletion.accept((Animation<A>) (Object) this);

            this.eventCompleted = true;
        }
    }

    @Inject(method = "update", at = @At(value = "JUMP", opcode = Opcodes.IFEQ, shift = At.Shift.BY, by = 2))
    private void personality$resetEventCompleted1(float delta, CallbackInfo ci){
        this.eventCompleted = false;
    }

    @Override
    public Animation<A> setOnCompletionEvent(Consumer<Animation<A>> event) {
        this.onCompletion = event;

        return (Animation<A>) (Object) this;
    }

    @Inject(method = {"forwards", "backwards", "reverse"}, at = @At("HEAD"))
    private void personality$resetEventCompleted2(CallbackInfoReturnable<Animation<A>> cir){
        this.eventCompleted = false;
    }

    @Override
    public A getCurrentValue() {
        return this.from.interpolate(this.to, this.easing.apply(this.delta));
    }

    @Override
    public A getStartingValue() {
        return this.from;
    }

    @Override
    public A getEndingValue() {
        return this.to;
    }
}
