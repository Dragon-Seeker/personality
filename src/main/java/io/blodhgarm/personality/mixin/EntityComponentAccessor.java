package io.blodhgarm.personality.mixin;

import io.wispforest.owo.ui.component.EntityComponent;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityComponent.class)
public interface EntityComponentAccessor<E extends Entity> {
    @Mutable @Accessor void setEntity(E entity);
}
