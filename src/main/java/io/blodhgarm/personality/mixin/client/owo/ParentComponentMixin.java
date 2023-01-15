package io.blodhgarm.personality.mixin.client.owo;

import io.blodhgarm.personality.misc.pond.owo.UnimportantComponent;
import io.blodhgarm.personality.misc.pond.owo.UnimportantToggleHelper;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.util.Drawer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ParentComponent.class, remap = false)
public interface ParentComponentMixin extends Component {

    @Redirect(method = "childAt", at = @At(value = "INVOKE", target = "Lio/wispforest/owo/ui/core/Component;isInBoundingBox(DD)Z"))
    private boolean childAt(Component instance, double x, double y){
        return ((UnimportantToggleHelper) Drawer.debug()).filterUnimportantComponents() && instance instanceof UnimportantComponent
                ? false
                : instance.isInBoundingBox(x, y);
    }
}
