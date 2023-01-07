package io.blodhgarm.personality.misc.pond.owo;

import io.wispforest.owo.ui.core.Animatable;
import io.wispforest.owo.ui.core.Animation;

import java.util.function.Consumer;

public interface AnimationExtension<A extends Animatable<A>, T extends Animation<A>> {

    T setOnCompletionEvent(Consumer<Animation<A>> event);

}
