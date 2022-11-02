package io.blodhgarm.personality.compat.origins;

import com.mojang.logging.LogUtils;
import io.blodhgarm.personality.api.addon.AddonRegistry;
import io.blodhgarm.personality.api.addon.BaseAddon;
import io.blodhgarm.personality.mixin.OriginLayerAccessor;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class OriginsAddonRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static <T extends BaseAddon> void addonRegistry(AddonRegistry<T> registry) {
        registry.registerDelayedAddon(delayedRegistery -> {
            Predicate<T> addonValidation = t -> {
                if (t instanceof OriginAddon originAddon) {
                    try {
                        OriginLayer layer = OriginLayers.getLayer(originAddon.getOriginLayerId());

                        if (Objects.equals(originAddon.getOriginId().getPath(), "random")) {
                            List<Identifier> randomOrigins = layer.getOrigins().stream()
                                    .filter(o -> !((OriginLayerAccessor) layer).personality$OriginsExcludedFromRandom().contains(o))
                                    .filter(id -> ((OriginLayerAccessor) layer).personality$DoesRandomAllowUnchoosable() || OriginRegistry.get(id).isChoosable())
                                    .collect(Collectors.toList());

                            Identifier origin = Origin.EMPTY.getIdentifier();

                            if (layer.isRandomAllowed() && randomOrigins.size() > 0) {
                                origin = randomOrigins.get(new Random().nextInt(randomOrigins.size()));
                            }

                            originAddon.setOrigin(origin, layer.getIdentifier());

                            return true;
                        } else if (layer.getOrigins().contains(originAddon.getOriginId())) {
                            return true;
                        }
                    } catch (IllegalArgumentException e) {
                        LOGGER.error("[OriginAddon] It seems that there was a issue attempting to validate a Personality Addon leading to addon being thrown out");
                    }
                }

                return false;
            };

            OriginLayers.getLayers().forEach(originLayer -> {
                Origin origin = getChoosableSortedOrigins(originLayer).get(0);

                registry.registerAddon(originLayer.getIdentifier(),
                    (Class<T>) OriginAddon.class,
                    () -> (T) new OriginAddon(origin.getIdentifier(), originLayer.getIdentifier()),
                    addonValidation
                );
            });
        });
    }

    public static List<Origin> getChoosableSortedOrigins(OriginLayer currentLayer) {
        return getChoosableSortedOrigins(currentLayer, true);
    }

    public static List<Origin> getChoosableSortedOrigins(OriginLayer currentLayer, boolean alwaysIncludeForRandom){
        List<Origin> originSelection = new ArrayList<>(10);

        List<Identifier> originIdentifiers = currentLayer.getOrigins().stream()
                .filter(o -> alwaysIncludeForRandom || !((OriginLayerAccessor) currentLayer).personality$OriginsExcludedFromRandom().contains(o))
                .filter(id -> ((OriginLayerAccessor) currentLayer).personality$DoesRandomAllowUnchoosable() || OriginRegistry.get(id).isChoosable())
                .collect(Collectors.toList());

        originIdentifiers.forEach(originId -> {
            Origin origin = OriginRegistry.get(originId);

            if(origin.isChoosable()) originSelection.add(origin);
        });

        originSelection.sort((a, b) -> {
            int impDelta = a.getImpact().getImpactValue() - b.getImpact().getImpactValue();
            return impDelta == 0 ? a.getOrder() - b.getOrder() : impDelta;
        });

        return originSelection;
    }
}
