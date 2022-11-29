package io.blodhgarm.personality.mixin;

import io.wispforest.owo.ui.util.NinePatchRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = NinePatchRenderer.class, remap = false)
public interface NinePatchRendererAccessor {

    @Accessor("u")
    int getU();

    @Mutable
    @Accessor("u")
    void setU(int u);

    @Accessor("v")
    int getV();

    @Mutable
    @Accessor("v")
    void setV(int v);
}
