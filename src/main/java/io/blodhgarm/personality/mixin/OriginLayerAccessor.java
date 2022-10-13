package io.blodhgarm.personality.mixin;

import io.github.apace100.origins.origin.OriginLayer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(OriginLayer.class)
public interface OriginLayerAccessor {

    @Accessor("originsExcludedFromRandom")
    List<Identifier> personality$OriginsExcludedFromRandom();

    @Accessor("doesRandomAllowUnchoosable")
    boolean personality$DoesRandomAllowUnchoosable();
}
