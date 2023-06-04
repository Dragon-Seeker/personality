package io.blodhgarm.personality.misc.pond;

import io.wispforest.owo.ui.component.EntityComponent;
import net.minecraft.entity.Entity;

public interface EntityComponentExtension<E extends Entity> {

    EntityComponent<E> removeXAngle(boolean value);
}
