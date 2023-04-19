package io.blodhgarm.personality.misc.pond.owo;

import io.wispforest.owo.ui.core.Animatable;
import io.wispforest.owo.ui.core.Animation;

import java.util.function.Consumer;

public interface AnimationExtension<A extends Animatable<A>> {

    Animation<A> setOnCompletionEvent(Consumer<Animation<A>> event);

    A getCurrentValue();

    A getStartingValue();

    A getEndingValue();

}
