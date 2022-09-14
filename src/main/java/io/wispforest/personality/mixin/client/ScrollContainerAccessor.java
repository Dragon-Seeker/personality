package io.wispforest.personality.mixin.client;

import io.wispforest.owo.ui.container.ScrollContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ScrollContainer.class)
public interface ScrollContainerAccessor {

    @Accessor("currentScrollPosition")
    void personality$setCurrentScrollPosition(double currentScrollPosition);

    @Accessor("lastScrollPosition")
    void personality$setLastScrollPosition(int lastScrollPosition);
}
