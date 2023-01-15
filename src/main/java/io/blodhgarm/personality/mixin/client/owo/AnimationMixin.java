package io.blodhgarm.personality.mixin.client.owo;

import io.blodhgarm.personality.misc.pond.owo.AnimationExtension;
import io.wispforest.owo.ui.core.Animatable;
import io.wispforest.owo.ui.core.Animation;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Mixin(value = Animation.class, remap = false)
public abstract class AnimationMixin<A extends Animatable<A>> implements AnimationExtension<A, Animation<A>> {

    @Unique
    @Nullable
    private Consumer<Animation<A>> onCompletion = null;

    @Inject(method = "update", at = @At(value = "JUMP", opcode = Opcodes.IFNE, shift = At.Shift.BY, by = 2))
    private void afterAnimationCompletion(float delta, CallbackInfo ci){
        if(this.onCompletion != null) this.onCompletion.accept((Animation<A>) (Object) this);
    }

    @Override
    public Animation<A> setOnCompletionEvent(Consumer<Animation<A>> event) {
        this.onCompletion = event;

        return (Animation<A>) (Object) this;
    }
}
