package io.blodhgarm.personality.misc.pond.owo;

import io.wispforest.owo.ui.core.Component;
import net.minecraft.client.gui.Drawable;
import org.jetbrains.annotations.Nullable;

public interface CustomFocusHighlighting<T extends Component>  {

    T addCustomFocusRendering(HighlightRenderEvent event);

    @Nullable HighlightRenderEvent getEvent();
}
