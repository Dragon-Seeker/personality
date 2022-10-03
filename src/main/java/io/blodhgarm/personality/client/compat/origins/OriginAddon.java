package io.blodhgarm.personality.client.compat.origins;

import io.blodhgarm.personality.api.addons.BaseAddon;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class OriginAddon extends BaseAddon<Origin> {

    private Identifier origin_id;
    private Identifier layer_id;

    public OriginAddon(Identifier origin_id, Identifier layer_id){
        this.origin_id = origin_id;
        this.layer_id = layer_id;
    }

    public void setOrigin(Identifier origin_id, Identifier layer_id){
        this.origin_id = origin_id;
        this.layer_id = layer_id;
    }

    @Override
    public void applyAddon(PlayerEntity player) {
        Origin origin = OriginRegistry.get(origin_id);
        OriginLayer originLayer = OriginLayers.getLayer(layer_id);

        ModComponents.ORIGIN.get(player).setOrigin(originLayer, origin);
    }

    @Override
    public boolean applyOnClient() {
        return false;
    }
}
