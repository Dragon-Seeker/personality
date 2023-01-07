package io.blodhgarm.personality.misc.pond.owo;

import net.minecraft.client.util.math.MatrixStack;

public interface HighlightRenderEvent {

    boolean drawHighlight(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta);
}
