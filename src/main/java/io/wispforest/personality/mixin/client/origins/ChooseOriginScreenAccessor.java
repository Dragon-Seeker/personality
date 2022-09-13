package io.wispforest.personality.mixin.client.origins;

import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.screen.ChooseOriginScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.ArrayList;
import java.util.List;

@Mixin(ChooseOriginScreen.class)
public interface ChooseOriginScreenAccessor {

    @Accessor("layerList")
    ArrayList<OriginLayer> personality$LayerList();

    @Accessor("currentLayerIndex")
    int personality$CurrentLayerIndex();

    @Accessor("currentOrigin")
    int personality$CurrentOrigin();

    @Accessor("currentOrigin")
    void personality$setCurrentOrigin(int currentOrigin);

    @Accessor("originSelection")
    List<Origin> personality$OriginSelection();

    @Accessor("maxSelection")
    int personality$MaxSelection();

    @Accessor("randomOrigin")
    Origin personality$RandomOrigin();

    //---------------------------------------------------

    @Invoker("getCurrentOriginInternal")
    Origin personality$GetCurrentOriginInternal();
}
