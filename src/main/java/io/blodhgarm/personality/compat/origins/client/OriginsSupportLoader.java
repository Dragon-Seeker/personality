package io.blodhgarm.personality.compat.origins.client;

import io.blodhgarm.personality.api.addon.client.PersonalityScreenAddon;
import io.blodhgarm.personality.compat.origins.client.gui.OriginSelectionDisplayAddon;
import io.blodhgarm.personality.client.screens.PersonalityCreationScreen;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class OriginsSupportLoader {

    public static PersonalityScreenAddon addToPersonalityScreen(ClientPlayerEntity player){
        ArrayList<OriginLayer> layers = new ArrayList<>();

        OriginLayers.getLayers().forEach(layer -> {
            if(layer.isEnabled()) {
                layers.add(layer);
            }
        });

        Collections.sort(layers);

        //Idk if this is really needed

//        if(layers.isEmpty()){
//            HashMap<OriginLayer, Origin> origins = ModComponents.ORIGIN.get(player).getOrigins();
//            ArrayList<Pair<OriginLayer, Origin>> originLayers = new ArrayList<>(origins.size());
//
//            origins.forEach((layer, origin) -> {
//                ItemStack displayItem = origin.getDisplayItem();
//                if(displayItem.getItem() == Items.PLAYER_HEAD) {
//                    if(!displayItem.hasNbt() || !displayItem.getNbt().contains("SkullOwner")) {
//                        displayItem.getOrCreateNbt().putString("SkullOwner", player.getDisplayName().getString());
//                    }
//                }
//                if((origin != Origin.EMPTY || layer.getOriginOptionCount(player) > 0) && !layer.isHidden()) {
//                    originLayers.add(new Pair<>(layer, origin));
//                }
//            });
//            originLayers.sort(Comparator.comparing(Pair::getLeft));
//            if(originLayers.size() > 0) {
//                Pair<OriginLayer, Origin> current = originLayers.get(0);
//
//                layers.add(current.getLeft());
//            } else {
//                layers.add(null);
//            }
//        }

        return new OriginSelectionDisplayAddon(layers, 0);
    }
}
