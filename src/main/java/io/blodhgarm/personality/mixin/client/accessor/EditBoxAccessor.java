package io.blodhgarm.personality.mixin.client.accessor;

import net.minecraft.client.gui.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EditBox.class)
public interface EditBoxAccessor {

    @Invoker("onChange")
    void personality$callOnChange();

    @Mutable
    @Accessor("width")
    void personality$setWidth(int width);
}
