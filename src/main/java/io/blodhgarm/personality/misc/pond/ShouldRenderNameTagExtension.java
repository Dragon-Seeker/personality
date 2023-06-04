package io.blodhgarm.personality.misc.pond;

import io.wispforest.owo.ui.component.EntityComponent;
import net.minecraft.entity.Entity;

import java.util.function.Consumer;

//Based on owo implementation
public interface ShouldRenderNameTagExtension<C> {

    C personality$setShouldNameTagRender(boolean value);

    boolean personality$shouldNameTagRender();

    static <T extends Entity> Consumer<EntityComponent<T>> disable(Consumer<EntityComponent<T>> extra){
        return component -> {
            ((ShouldRenderNameTagExtension) component).personality$setShouldNameTagRender(false);
            extra.accept(component);
        };
    }
}
