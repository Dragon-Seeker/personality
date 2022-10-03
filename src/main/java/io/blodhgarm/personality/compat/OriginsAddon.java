package io.blodhgarm.personality.compat;

import io.blodhgarm.personality.api.AddonRegistry;
import io.blodhgarm.personality.api.PersonalityEntrypoint;
import io.blodhgarm.personality.api.addons.BaseAddon;
import io.blodhgarm.personality.client.compat.origins.OriginAddon;
import io.github.apace100.origins.origin.OriginLayers;
import net.minecraft.util.Identifier;

public class OriginsAddon implements PersonalityEntrypoint {

    @Override
    public <T extends BaseAddon<?>> void addonRegistry(AddonRegistry<T> registry) {
        registry.registerAddon("origins",
                (Class<T>) OriginAddon.class,
                () -> (T) new OriginAddon(new Identifier("origins","human"),
                                    new Identifier("origins","origin"))
        );
    }
}
