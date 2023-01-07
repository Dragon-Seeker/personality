package io.blodhgarm.personality.mixin.client.owo;

import io.blodhgarm.personality.misc.pond.owo.GridLayoutDuck;
import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.core.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GridLayout.class)
@Pseudo
public abstract class GridLayoutMixin implements GridLayoutDuck {

    @Shadow @Mutable @Final protected int columns;
    @Shadow @Mutable @Final protected int rows;

    @Shadow @Mutable @Final protected Component[] children;

    @Unique private boolean hasComponentBeenAdded = false;

    @Override
    public boolean resetSize(int rowSize, int columnSize) {
        if(hasComponentBeenAdded) return false;

        this.columns = columnSize;
        this.rows = rowSize;

        this.children = new Component[rowSize * columnSize];

        return true;
    }

    @Inject(method = "child", at = @At("HEAD"))
    private void disableResettingSize(Component child, int row, int column, CallbackInfoReturnable<GridLayout> cir){ if(!hasComponentBeenAdded) hasComponentBeenAdded = true; }
}
