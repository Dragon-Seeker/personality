package io.wispforest.personality;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.registry.ModComponents;
import io.wispforest.owo.network.ClientAccess;
import io.wispforest.owo.network.OwoNetChannel;
import io.wispforest.personality.client.screens.AdditionalCreationComponent;
import io.wispforest.personality.client.screens.PersonalityCreationScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class PersonalityNetworking {

    public static final OwoNetChannel CHANNEL = OwoNetChannel.create(new Identifier("personality", "main"));

    public static void registerNetworking(){
        PersonalityNetworking.CHANNEL.registerClientbound(OpenPersonalityCreationScreen.class, OpenPersonalityCreationScreen::openScreen);
    }

    public static record OpenPersonalityCreationScreen(){

        @Environment(EnvType.CLIENT)
        public static void openScreen(OpenPersonalityCreationScreen message, ClientAccess access){
            ArrayList<OriginLayer> layers = new ArrayList<>();
            OriginComponent component = ModComponents.ORIGIN.get(access.player());
            OriginLayers.getLayers().forEach(layer -> {
                if(layer.isEnabled() && !component.hasOrigin(layer)) {
                    layers.add(layer);
                }
            });
            Collections.sort(layers);

            if(layers.isEmpty()){
                PlayerEntity player = MinecraftClient.getInstance().player;
                HashMap<OriginLayer, Origin> origins = ModComponents.ORIGIN.get(player).getOrigins();
                ArrayList<Pair<OriginLayer, Origin>> originLayers = new ArrayList<>(origins.size());

                origins.forEach((layer, origin) -> {
                    ItemStack displayItem = origin.getDisplayItem();
                    if(displayItem.getItem() == Items.PLAYER_HEAD) {
                        if(!displayItem.hasNbt() || !displayItem.getNbt().contains("SkullOwner")) {
                            displayItem.getOrCreateNbt().putString("SkullOwner", player.getDisplayName().getString());
                        }
                    }
                    if((origin != Origin.EMPTY || layer.getOriginOptionCount(player) > 0) && !layer.isHidden()) {
                        originLayers.add(new Pair<>(layer, origin));
                    }
                });
                originLayers.sort(Comparator.comparing(Pair::getLeft));
                if(originLayers.size() > 0) {
                    Pair<OriginLayer, Origin> current = originLayers.get(0);

                    layers.add(current.getLeft());
                } else {
                    layers.add(null);
                }
            }

            MinecraftClient.getInstance().setScreen(new PersonalityCreationScreen(new AdditionalCreationComponent(layers, 0)));
        }
    }

}
