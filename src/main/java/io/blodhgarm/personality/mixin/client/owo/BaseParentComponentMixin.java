package io.blodhgarm.personality.mixin.client.owo;

import io.blodhgarm.personality.client.PersonalityClient;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.core.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Consumer;

@Mixin(BaseParentComponent.class)
public abstract class BaseParentComponentMixin extends BaseComponent implements ParentComponent {

    @Inject(method = "mountChild", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void personality$MountBasedOnCustomType(@Nullable Component child, Size space, Consumer<Component> layoutFunc, CallbackInfo ci, Positioning positioning, Insets componentMargins, Insets padding){
        if(positioning.type == PersonalityClient.RELATIVE_WITHOUT_CHILD){
            assert child != null;

            child.inflate(space);
            child.mount(
                    this,
                    this.x + padding.left() + componentMargins.left() + (Math.round((positioning.x / 100f) * (this.width() - padding.horizontal()))) - (child.fullSize().width() / 2),
                    this.y + padding.top() + componentMargins.top() + (Math.round((positioning.y / 100f) * (this.height() - padding.vertical()))) - (child.fullSize().height() / 2)
            );
        }
    }
}
