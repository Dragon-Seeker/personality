package io.blodhgarm.personality.client.compat.origins.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.badge.Badge;
import io.github.apace100.origins.networking.ModPackets;
import io.github.apace100.origins.origin.Impact;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.registry.ModItems;
import io.netty.buffer.Unpooled;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.Drawer;
import io.blodhgarm.personality.client.screens.PersonalityScreenAddon;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class OriginSelectionComponent implements PersonalityScreenAddon {

    public static Identifier ORIGINS_GUI_TEXTURE = new Identifier("personality", "textures/gui/origins_gui.png");
    public static Identifier INVERSE_PANEL_TEXTURE = new Identifier("personality", "textures/gui/inverse_panel.png");

    //----------------------------

    private Origin origin;
    private boolean isOriginRandom;
    private Text randomOriginText;

    protected int scrollPos = 0;
    public static float time = 0;

    //----------------------------


    //----------------------------

    private final ArrayList<OriginLayer> layerList;
    private final int currentLayerIndex;
    private int currentOrigin = 0;
    private final List<Origin> originSelection;
    private int maxSelection;

    private Origin randomOrigin;

    //----------------------------

    public OriginSelectionComponent(ArrayList<OriginLayer> layerList, int currentLayerIndex) {
        this.layerList = layerList;
        this.currentLayerIndex = currentLayerIndex;
        this.originSelection = new ArrayList<>(10);

        PlayerEntity player = MinecraftClient.getInstance().player;

        OriginLayer currentLayer = layerList.get(currentLayerIndex);

        List<Identifier> originIdentifiers = currentLayer.getOrigins(player);

        originIdentifiers.forEach(originId -> {
            Origin origin = OriginRegistry.get(originId);
            if(origin.isChoosable()) {
                ItemStack displayItem = origin.getDisplayItem();
                if(displayItem.getItem() == Items.PLAYER_HEAD) {
                    if(!displayItem.hasNbt() || !displayItem.getNbt().contains("SkullOwner")) {
                        displayItem.getOrCreateNbt().putString("SkullOwner", player.getDisplayName().getString());
                    }
                }
                this.originSelection.add(origin);
            }
        });

        originSelection.sort((a, b) -> {
            int impDelta = a.getImpact().getImpactValue() - b.getImpact().getImpactValue();
            return impDelta == 0 ? a.getOrder() - b.getOrder() : impDelta;
        });

        maxSelection = originSelection.size();

        if(currentLayer.isRandomAllowed() && currentLayer.getRandomOrigins(player).size() > 0) {
            maxSelection += 1;
        }

        if(maxSelection == 0) {
            //openNextLayerScreen();
        }

        Origin newOrigin = getCurrentOriginInternal();
        showOrigin(newOrigin, layerList.get(currentLayerIndex), newOrigin == randomOrigin);
    }

    //----------------------------

    public void showOrigin(Origin origin, OriginLayer layer, boolean isRandom) {
        this.origin = origin;
        this.isOriginRandom = isRandom;
        this.scrollPos = 0;
        this.time = 0;
    }

    public void setRandomOriginText(Text text) {
        this.randomOriginText = text;
    }

    //----------------------------


    //----------------------------

    private Origin getCurrentOriginInternal() {
        if(currentOrigin == originSelection.size()) {
            if(randomOrigin == null) {
                initRandomOrigin(layerList);
            }
            return randomOrigin;
        }

        return originSelection.get(currentOrigin);
    }

    private void initRandomOrigin(ArrayList<OriginLayer> layerList) {
        this.randomOrigin = new Origin(Origins.identifier("random"), new ItemStack(ModItems.ORB_OF_ORIGIN), Impact.NONE, -1, Integer.MAX_VALUE);

        MutableText randomOriginText = (MutableText)Text.of("");

        List<Identifier> randoms = layerList.get(currentLayerIndex).getRandomOrigins(MinecraftClient.getInstance().player);

        randoms.sort((ia, ib) -> {
            Origin a = OriginRegistry.get(ia);
            Origin b = OriginRegistry.get(ib);
            int impDelta = a.getImpact().getImpactValue() - b.getImpact().getImpactValue();
            return impDelta == 0 ? a.getOrder() - b.getOrder() : impDelta;
        });

        for(Identifier id : randoms) {
            randomOriginText.append(OriginRegistry.get(id).getName());
            randomOriginText.append(Text.of("\n"));
        }

        setRandomOriginText(randomOriginText);
    }

    public Origin getCurrentOrigin() {
        return origin;
    }

    //----------------------------


    //----------------------------

    public void build(FlowLayout rootComponent, boolean isDarkMode){
        Surface panel = isDarkMode ? Surface.DARK_PANEL : Surface.PANEL;

        rootComponent.child(Containers.verticalFlow(Sizing.content(), Sizing.content())
            .child(Containers.verticalFlow(Sizing.fixed(176), Sizing.fixed(182))
                .child(
                    Containers.verticalFlow(Sizing.fixed(162), Sizing.fixed(168))
                        .child(Containers.verticalScroll(Sizing.fixed(149), Sizing.fixed(142),
                                new OriginInfoContainer(Sizing.fixed(137), Sizing.content(), getCurrentOrigin(), randomOriginText, isOriginRandom) //143
                                        .margins(Insets.left(6)
                                ))
                                .scrollbarThiccness(6)
                                .positioning(Positioning.absolute(6, 20))
                                .id("origin_info"))
                        .surface(INVERSE_PANEL)
                        .positioning(Positioning.absolute(7, 7))
                        .zIndex(-1)
                )
                //Bar Length
                .child(Components.texture(ORIGINS_GUI_TEXTURE, 29,0, 7, 16,48, 48)
                    .sizing(Sizing.fixed(122), Sizing.fixed(16))
                    .positioning(Positioning.absolute(35,15)))
                //Bar end
                .child(Components.texture(ORIGINS_GUI_TEXTURE, 36,0, 3, 16,48, 48)
                    .positioning(Positioning.absolute(35 + 122,15)))
                //Origin Label
                .child(Components.label(getCurrentOrigin().getName())
                    .shadow(true)
                    .positioning(Positioning.absolute(39, 19))
                    .id("origin_name"))
                .child(new OriginImpactComponent(getCurrentOrigin().getImpact())
                    .positioning(Positioning.absolute(128, 19))
                    .id("origin_impact"))
                //Square Symbol
                .child(Components.texture(ORIGINS_GUI_TEXTURE, 0,0, 26, 26, 48, 48)
                    .positioning(Positioning.absolute(10,10)))
                .child(Components.item(getCurrentOrigin().getDisplayItem())
                    .positioning(Positioning.absolute(15, 15))
                    .id("origin_icon"))
                .surface(panel))
            .child(Containers.horizontalFlow(Sizing.content(), Sizing.content())
                .child(Components.button(Text.of("<"), 20, 20, button -> {
                        currentOrigin = (currentOrigin - 1 + maxSelection) % maxSelection;
                        Origin newOrigin = getCurrentOriginInternal();
                        showOrigin(newOrigin, layerList.get(currentLayerIndex), newOrigin == randomOrigin);
                        updateOriginData(rootComponent);
                    }))
                .child(Components.button(Text.translatable(Origins.MODID + ".gui.select"), 100, 20, button -> {
                        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                        if(currentOrigin == originSelection.size()) {
                            buf.writeString(layerList.get(currentLayerIndex).getIdentifier().toString());
                            ClientPlayNetworking.send(ModPackets.CHOOSE_RANDOM_ORIGIN, buf);
                        } else {
                            buf.writeString(getCurrentOrigin().getIdentifier().toString());
                            buf.writeString(layerList.get(currentLayerIndex).getIdentifier().toString());
                            ClientPlayNetworking.send(ModPackets.CHOOSE_ORIGIN, buf);
                        }
                        //openNextLayerScreen();
                    })
                    .margins(Insets.horizontal(10)))
                .child(Components.button(Text.of(">"), 20, 20, button -> {
                        currentOrigin = (currentOrigin + 1) % maxSelection;
                        Origin newOrigin = getCurrentOriginInternal();
                        showOrigin(newOrigin, layerList.get(currentLayerIndex), newOrigin == randomOrigin);
                        updateOriginData(rootComponent);
                    }))
                .margins(Insets.top(5))
            ).horizontalAlignment(HorizontalAlignment.CENTER)
            .margins(Insets.left(20))
        );
    }

    public void updateOriginData(FlowLayout rootComponent){
        ScrollContainer<?> container = rootComponent.childById(ScrollContainer.class, "origin_info");

        OriginInfoContainer child = ((OriginInfoContainer)container.child());

        //container.onChildMutated(child);
        container.scrollTo(child);

        child.origin(getCurrentOrigin(), randomOriginText, isOriginRandom);

        rootComponent.childById(LabelComponent.class, "origin_name").text(getCurrentOrigin().getName());
        rootComponent.childById(OriginImpactComponent.class, "origin_impact").setImpact(getCurrentOrigin().getImpact());
        rootComponent.childById(ItemComponent.class, "origin_icon").stack(getCurrentOrigin().getDisplayItem());
    }

    //----------------------------

    public static Surface INVERSE_PANEL = (matrices, component) -> {
        int x = component.x();
        int y = component.y();
        int width = component.width();
        int height = component.height();

        RenderSystem.setShaderTexture(0, INVERSE_PANEL_TEXTURE);

        Drawer.drawTexture(matrices, x, y, 0, 0, 5, 5, 16, 16);
        Drawer.drawTexture(matrices, x + width - 5, y, 10, 0, 5, 5, 16, 16);
        Drawer.drawTexture(matrices, x, y + height - 5, 0, 10, 5, 5, 16, 16);
        Drawer.drawTexture(matrices, x + width - 5, y + height - 5, 10, 10, 5, 5, 16, 16);

        if (width > 10 && height > 10) {
            Drawer.drawTexture(matrices, x + 5, y + 5, width - 10, height - 10, 5, 5, 5, 5, 16, 16);
        }

        if (width > 10) {
            Drawer.drawTexture(matrices, x + 5, y, width - 10, 5, 5, 0, 5, 5, 16, 16);
            Drawer.drawTexture(matrices, x + 5, y + height - 5, width - 10, 5, 5, 10, 5, 5, 16, 16);
        }

        if (height > 10) {
            Drawer.drawTexture(matrices, x, y + 5, 5, height - 10, 0, 5, 5, 5, 16, 16);
            Drawer.drawTexture(matrices, x + width - 5, y + 5, 5, height - 10, 10, 5, 5, 5, 16, 16);
        }
    };

    public static class RandomOrigin {

    }
}
