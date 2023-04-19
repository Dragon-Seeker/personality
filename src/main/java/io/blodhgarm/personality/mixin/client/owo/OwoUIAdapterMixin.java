package io.blodhgarm.personality.mixin.client.owo;

import io.blodhgarm.personality.misc.pond.owo.UnimportantToggleHelper;
import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.util.Drawer;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = OwoUIAdapter.class)
public abstract class OwoUIAdapterMixin {

    @Inject(method = "keyPressed", at = @At(value = "HEAD"))
    private void test(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir){
        if(Owo.DEBUG && (modifiers & GLFW.GLFW_MOD_SHIFT) != 0 && keyCode == GLFW.GLFW_KEY_END){
            ((UnimportantToggleHelper)Drawer.debug()).toggleUnimportantComponents();
            
            System.out.println(((UnimportantToggleHelper)Drawer.debug()).filterUnimportantComponents());
        }
    }
}
