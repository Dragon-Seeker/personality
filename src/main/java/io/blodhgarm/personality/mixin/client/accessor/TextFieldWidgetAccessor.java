package io.blodhgarm.personality.mixin.client.accessor;

import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TextFieldWidget.class)
public interface TextFieldWidgetAccessor {
    @Invoker("drawsBackground") boolean personality$drawsBackground();
}
