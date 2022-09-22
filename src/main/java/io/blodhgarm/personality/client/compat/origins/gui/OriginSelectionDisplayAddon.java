package io.blodhgarm.personality.client.compat.origins.gui;

import io.blodhgarm.personality.client.compat.origins.gui.components.OriginHeaderComponent;
import io.blodhgarm.personality.client.compat.origins.gui.components.OriginImpactComponent;
import io.blodhgarm.personality.client.compat.origins.gui.components.OriginInfoContainer;
import io.blodhgarm.personality.client.screens.PersonalityCreationScreen;
import io.blodhgarm.personality.client.screens.PersonalityScreenAddon;
import io.blodhgarm.personality.client.screens.components.CustomSurfaces;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.origin.Impact;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.registry.ModItems;
import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class OriginSelectionDisplayAddon extends PersonalityScreenAddon {

    public static Identifier ORIGINS_GUI_TEXTURE = new Identifier("personality", "textures/gui/origins_gui.png");

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


    //----------------------------

    public BaseParentComponent rootBranchComponent = null;

    //----------------------------

    public OriginSelectionDisplayAddon(ArrayList<OriginLayer> layerList, int currentLayerIndex) {
        super("origin_addon_component");

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

    @Override
    public FlowLayout build(boolean isDarkMode){
        Surface panel = isDarkMode ? Surface.DARK_PANEL : Surface.PANEL;

        FlowLayout rootComponent = Containers.verticalFlow(Sizing.content(), Sizing.content());

        return (FlowLayout) rootComponent
                .child(Containers.verticalFlow(Sizing.fixed(176), Sizing.fixed(182))
                    .child(
                            Containers.verticalFlow(Sizing.fixed(162), Sizing.fixed(143))// y = 168
                                .child(
                                        new OriginHeaderComponent(Sizing.content(), Sizing.content(), getCurrentOrigin())
                                                .margins(Insets.of(4,0,4,0))
                                                .verticalAlignment(VerticalAlignment.CENTER)
                                )
                                .child(Containers.verticalScroll(Sizing.fixed(150), Sizing.fixed(121),// y = 142, x = 154
                                        new OriginInfoContainer(Sizing.fixed(137), Sizing.content(), getCurrentOrigin(), randomOriginText, isOriginRandom) //143
                                                .margins(Insets.left(6)
                                        ))
                                        .scrollbar(ScrollContainer.Scrollbar.vanilla())
                                        .scrollbarThiccness(8)
                                        .fixedScrollbarLength(27)
                                        .positioning(Positioning.absolute(6, 20))
                                        .padding(Insets.of(8, 4, 0,0))
                                        .id("origin_info"))
                                .surface(CustomSurfaces.INVERSE_PANEL)
                                .zIndex(-2)
                                    .margins(Insets.of(1,0,0,0))
                    )
                    .child(Containers.horizontalFlow(Sizing.content(), Sizing.content())
                            .child(Components.button(Text.of("<"), (ButtonComponent button) -> {
                                        currentOrigin = (currentOrigin - 1 + maxSelection) % maxSelection;
                                        Origin newOrigin = getCurrentOriginInternal();
                                        showOrigin(newOrigin, layerList.get(currentLayerIndex), newOrigin == randomOrigin);
                                        updateOriginData(rootComponent);
                                    }).sizing(Sizing.fixed(20))
                            )
                            .child(Components.button(Text.translatable(Origins.MODID + ".gui.select"), (ButtonComponent button) -> {
                                            this.closeAddon();
//                                        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
//                                        if(currentOrigin == originSelection.size()) {
//                                            buf.writeString(layerList.get(currentLayerIndex).getIdentifier().toString());
//                                            ClientPlayNetworking.send(ModPackets.CHOOSE_RANDOM_ORIGIN, buf);
//                                        } else {
//                                            buf.writeString(getCurrentOrigin().getIdentifier().toString());
//                                            buf.writeString(layerList.get(currentLayerIndex).getIdentifier().toString());
//                                            ClientPlayNetworking.send(ModPackets.CHOOSE_ORIGIN, buf);
//                                        }
                                        //openNextLayerScreen();
                                    }).sizing(Sizing.fixed(100), Sizing.fixed(20))
                                    .margins(Insets.horizontal(10)))
                            .child(Components.button(Text.of(">"), (ButtonComponent button) -> {
                                        currentOrigin = (currentOrigin + 1) % maxSelection;
                                        Origin newOrigin = getCurrentOriginInternal();
                                        showOrigin(newOrigin, layerList.get(currentLayerIndex), newOrigin == randomOrigin);
                                        updateOriginData(rootComponent);
                                    }).sizing(Sizing.fixed(20))
                            )
                            //.positioning(Positioning.absolute(8, 150))
                            .margins(Insets.top(5))
                    )
                    .horizontalAlignment(HorizontalAlignment.CENTER)
                    .padding(Insets.of(5)) //6
                    .surface(panel)
                )
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .margins(Insets.left(PersonalityCreationScreen.guiScale4OrAbove() ? 2 : 10));
    }

    @Override
    public void branchUpdate() {
        if(rootBranchComponent != null) {
            rootBranchComponent.childById(OriginHeaderComponent.class, "addon_component_origin_header").Origin(getCurrentOrigin());
        }
    }

    @Override
    public Component addBranchComponent(BaseParentComponent rootComponent) {
        this.rootBranchComponent = rootComponent;

        return new OriginHeaderComponent(Sizing.content(), Sizing.content(), getCurrentOrigin())
                .shortVersion(true)
                .verticalAlignment(VerticalAlignment.CENTER)
                .id("addon_component_origin_header");
    }

    @Override
    public void saveAddonData() {
        //TODO: IMPLEMENT THIS
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

        branchUpdate();
    }

    //----------------------------
}
