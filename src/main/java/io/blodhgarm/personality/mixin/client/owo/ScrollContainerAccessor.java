package io.blodhgarm.personality.mixin.client.owo;

import io.wispforest.owo.ui.container.ScrollContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ScrollContainer.class, remap = false)
public interface ScrollContainerAccessor {

    @Accessor("direction")
    ScrollContainer.ScrollDirection personality$direction();
}
