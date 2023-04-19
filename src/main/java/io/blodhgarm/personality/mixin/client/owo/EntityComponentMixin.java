package io.blodhgarm.personality.mixin.client.owo;

import io.blodhgarm.personality.misc.pond.ShouldRenderNameTagExtension;
import io.wispforest.owo.ui.component.EntityComponent;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

//Based on owo implementation
@Mixin(EntityComponent.class)
public class EntityComponentMixin implements ShouldRenderNameTagExtension {

    boolean shouldRenderNameTag = true;

    @Shadow @Final protected EntityRenderDispatcher dispatcher;

    @Override
    public void personality$setShouldNameTagRender(boolean value) {
        shouldRenderNameTag = false;
    }

    @Override
    public boolean personality$shouldNameTagRender() {
        return shouldRenderNameTag;
    }

    @Inject(method = "draw",
            at = {
                    @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;render(Lnet/minecraft/entity/Entity;DDDFFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", id = "before"),
                    @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;render(Lnet/minecraft/entity/Entity;DDDFFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", shift = At.Shift.AFTER, id = "after")
            }
    )
    private void personality$setShouldNameTagRenderOnDispatcher(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta, CallbackInfo ci){
        ((ShouldRenderNameTagExtension)this.dispatcher).personality$setShouldNameTagRender(
                Objects.equals(ci.getId(), "before") ? shouldRenderNameTag : true
        );
    }
}
