package io.blodhgarm.personality.compat.origins;

import io.blodhgarm.personality.api.addon.BaseAddon;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.networking.ModPacketsC2S;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.registry.ModComponents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class OriginAddon extends BaseAddon {

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

    public Identifier getOriginId(){
        return origin_id;
    }

    public Identifier getOriginLayerId(){
        return layer_id;
    }

    /**
     * This method is similar to origins own method within {@link ModPacketsC2S#chooseOrigin}
     */
    @Override
    public void applyAddon(PlayerEntity player) {
        OriginComponent component = ModComponents.ORIGIN.get(player);

        Origin origin = OriginRegistry.get(origin_id);
        OriginLayer layer = OriginLayers.getLayer(layer_id);

        if (origin.isChoosable() && layer.contains(origin)) {
            boolean hadOriginBefore = component.hadOriginBefore();
            boolean hadAllOrigins = component.hasAllOrigins();

            component.setOrigin(layer, origin);
            component.checkAutoChoosingLayers(player, false);
            component.sync();

            //TODO: Origins have certain spawns or places to be meaning we may need to save the characters position or something idk.
            if (component.hasAllOrigins() && !hadAllOrigins) OriginComponent.onChosen(player, true/*hadOriginBefore*/);

            Origins.LOGGER.info("Player " + player.getDisplayName().getContent() + " chose Origin: " + origin_id + ", for layer: " + layer_id);
        } else {
            Origins.LOGGER.info("Player " + player.getDisplayName().getContent() + " tried to choose unchoosable Origin for layer " + layer_id + ": " + origin_id + ".");
            component.setOrigin(layer, Origin.EMPTY);
        }
    }

    @Override
    public AddonEnvironment getAddonEnvironment() {
        return AddonEnvironment.SERVER;
    }

    @Override
    public String getInfo() {
        return "\n§l" + (Text.translatable(OriginLayers.getLayer(layer_id).getTranslationKey()).getString()) +
                "§r: " + OriginRegistry.get(origin_id).getName().getString();
    }

    @Override
    public boolean isEqualToPlayer(PlayerEntity player) {
        OriginComponent component = ModComponents.ORIGIN.get(player);
        OriginLayer layer = OriginLayers.getLayer(this.getOriginLayerId());

        return component.getOrigins().containsKey(layer)
                && component.getOrigin(layer).getIdentifier().equals(this.getOriginId());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof OriginAddon addon){
            return addon.getOriginId().equals(this.getOriginId())
                    && addon.getOriginLayerId().equals(this.getOriginLayerId());
        }

        return false;
    }
}
