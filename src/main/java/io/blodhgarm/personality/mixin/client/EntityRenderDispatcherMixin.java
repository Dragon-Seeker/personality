package io.blodhgarm.personality.mixin.client;

import io.blodhgarm.personality.misc.pond.ShouldRenderNameTagExtension;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;

//Based on owo implementation
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin implements ShouldRenderNameTagExtension<EntityRenderDispatcher> {

    private boolean personality$shouldShowNameTag = true;

    @Override
    public EntityRenderDispatcher personality$setShouldNameTagRender(boolean value) {
        this.personality$shouldShowNameTag = value;

        return (EntityRenderDispatcher)(Object) this;
    }

    @Override
    public boolean personality$shouldNameTagRender() {
        return personality$shouldShowNameTag;
    }
}
