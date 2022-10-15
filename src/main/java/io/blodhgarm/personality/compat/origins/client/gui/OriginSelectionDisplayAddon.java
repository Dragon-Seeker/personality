package io.blodhgarm.personality.compat.origins.client.gui;

import io.blodhgarm.personality.api.addon.BaseAddon;
import io.blodhgarm.personality.compat.origins.client.OriginAddon;
import io.blodhgarm.personality.compat.origins.client.gui.components.OriginHeaderComponent;
import io.blodhgarm.personality.compat.origins.client.gui.components.OriginImpactComponent;
import io.blodhgarm.personality.compat.origins.client.gui.components.OriginInfoContainer;
import io.blodhgarm.personality.api.client.AddonObservable;
import io.blodhgarm.personality.client.screens.PersonalityCreationScreen;
import io.blodhgarm.personality.api.addon.client.PersonalityScreenAddon;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OriginSelectionDisplayAddon extends PersonalityScreenAddon {

    public static Identifier ORIGINS_GUI_TEXTURE = new Identifier("personality", "textures/gui/origins_gui.png");

    //----------------------------

    private final ArrayList<OriginLayer> layerList;

    private final Map<OriginLayer, OriginLayerHelper> originLayerHelpers = new HashMap<>();
    private final Map<OriginLayer, Origin> selectedOrigins = new HashMap<>();

    private Origin currentOrigin;

    protected int scrollPos = 0;
    public static float time = 0;

    //----------------------------


    //----------------------------

    private int currentLayerIndex;
    private int currentOriginIndex = 0;
    private List<Origin> originSelection;
    private int maxSelection;

    //----------------------------


    //----------------------------

    public OriginSelectionDisplayAddon(ArrayList<OriginLayer> layerList, int currentLayerIndex) {
        super(new Identifier("origins", "origin_selection_addon"));

        this.layerList = layerList;
        this.currentLayerIndex = currentLayerIndex;

        setupForSelectedLayer();
    }

    private void setupForSelectedLayer(){
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

        Origin origin;

        if(!originLayerHelpers.containsKey(currentLayer)){
            originLayerHelpers.put(currentLayer, new OriginLayerHelper(currentLayer, originSelection.get(0)));
        }

        if(!selectedOrigins.containsKey(currentLayer)){
            currentOriginIndex = 0;

            origin = getCurrentOriginInternal();
        } else {
            // If the OriginLayer was already selected, then we should set the display to such

            origin = selectedOrigins.get(currentLayer);

            currentOriginIndex = originSelection.indexOf(origin);
        }

        resetWithOrigin(origin);
    }

    //----------------------------

    public void resetWithOrigin(Origin origin) {
        this.currentOrigin = origin;
        this.scrollPos = 0;
        this.time = 0;
    }

    //----------------------------


    //----------------------------

    private Origin getCurrentOriginInternal() {
        OriginLayer layer = layerList.get(currentLayerIndex);

        if(currentOriginIndex == originSelection.size()) {
            if(originLayerHelpers.get(layer).randomOrigin == null) {
                initRandomOrigin(layerList);
            }

            return originLayerHelpers.get(layer).randomOrigin;
        }

        return originSelection.get(currentOriginIndex);
    }

    private void initRandomOrigin(ArrayList<OriginLayer> layerList) {
        Origin randomOrigin = new Origin(Origins.identifier("random"), new ItemStack(ModItems.ORB_OF_ORIGIN), Impact.NONE, -1, Integer.MAX_VALUE);

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

        originLayerHelpers.get(layerList.get(currentLayerIndex)).setRandomInfo(randomOrigin, randomOriginText);
    }

    public boolean isOriginRandom(Origin origin){
        OriginLayer layer = layerList.get(currentLayerIndex);
        Origin randomOrigin = originLayerHelpers.get(layer).randomOrigin;

        return randomOrigin != null && origin == randomOrigin;
    }

    public Origin getCurrentOrigin() {
        return currentOrigin;
    }

    //----------------------------


    //----------------------------

    @Override
    public FlowLayout build(boolean isDarkMode){
        Surface panel = isDarkMode ? Surface.DARK_PANEL : Surface.PANEL;

        FlowLayout rootComponent = Containers.verticalFlow(Sizing.content(), Sizing.content());

        OriginLayer currentLayer = layerList.get(currentLayerIndex);

        Text randomOriginText = null;

        if(isOriginRandom(getCurrentOrigin())){
            randomOriginText = originLayerHelpers.get(currentLayer).randomOriginText;
        }

        return (FlowLayout) rootComponent
                .child(Containers.verticalFlow(Sizing.fixed(176), Sizing.fixed(182))
                    .child(
                            Containers.verticalFlow(Sizing.fixed(162), Sizing.fixed(143))// y = 168
                                .child(
                                        new OriginHeaderComponent(Sizing.fixed(150), Sizing.fixed(26), getCurrentOrigin(), layerList.get(currentLayerIndex))
                                                .margins(Insets.of(4,0,4,0))
                                                .verticalAlignment(VerticalAlignment.CENTER)
                                                .zIndex(1)
                                )
                                .child(Containers.verticalScroll(Sizing.fixed(150), Sizing.fixed(121),// y = 142, x = 154
                                        new OriginInfoContainer(Sizing.fixed(137), Sizing.content(), getCurrentOrigin(), randomOriginText, isOriginRandom(getCurrentOrigin())) //143
                                                .margins(Insets.left(6)
                                        ).zIndex(1))
                                        .scrollbar(ScrollContainer.Scrollbar.vanilla())
                                        .scrollbarThiccness(8)
                                        .fixedScrollbarLength(27)
                                        .positioning(Positioning.absolute(6, 20))
                                        .padding(Insets.of(8, 4, 0,0))
                                        .id("origin_info"))
                                .surface(CustomSurfaces.INVERSE_PANEL)
                                    .margins(Insets.of(1,0,0,0))
                    )
                    .child(Containers.horizontalFlow(Sizing.content(), Sizing.content())
                            .child(Components.button(Text.of("<"), (ButtonComponent button) -> {
                                        currentOriginIndex = (currentOriginIndex - 1 + maxSelection) % maxSelection;
                                        Origin newOrigin = getCurrentOriginInternal();
                                        resetWithOrigin(newOrigin);
                                        updateOriginData(rootComponent);
                                    }).sizing(Sizing.fixed(20))
                            )
                            .child(Components.button(Text.translatable(Origins.MODID + ".gui.select"), (ButtonComponent button) -> {
                                        selectedOrigins.put(layerList.get(currentLayerIndex), getCurrentOriginInternal());

                                        this.currentLayerIndex = this.currentLayerIndex + 1;

                                        if(this.currentLayerIndex >= this.layerList.size()){
                                            this.currentLayerIndex = 0;
                                        }

                                        setupForSelectedLayer();

                                        updateOriginData(rootComponent);

                                        if(selectedOrigins.keySet().containsAll(layerList)) {
                                            this.closeAddon();
                                        }
                                    }).sizing(Sizing.fixed(100), Sizing.fixed(20))
                                    .margins(Insets.horizontal(10)))
                            .child(Components.button(Text.of(">"), (ButtonComponent button) -> {
                                        currentOriginIndex = (currentOriginIndex + 1) % maxSelection;
                                        Origin newOrigin = getCurrentOriginInternal();
                                        resetWithOrigin(newOrigin);
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
                .margins(Insets.left(PersonalityCreationScreen.guiScale4OrAbove() ? 6 : 10));
    }

    @Override
    public void branchUpdate() {
        if(getRootComponent() != null) {
            getRootComponent().childById(OriginHeaderComponent.class, "addon_component_origin_header").Origin(getCurrentOrigin(), layerList.get(currentLayerIndex));
        }
    }

    @Override
    public Component buildBranchComponent(AddonObservable addonObservable, BaseParentComponent rootComponent) {
        return new OriginHeaderComponent(Sizing.fixed(122), Sizing.fixed(32), getCurrentOrigin(), layerList.get(currentLayerIndex))
                .shortVersion(true)
                .showLayerInfo(true)
                .showButtons(
                        buttonComponent -> {
                            this.currentLayerIndex = this.currentLayerIndex - 1;

                            if(this.currentLayerIndex < 0){
                                this.currentLayerIndex = this.layerList.size() - 1;
                            }

                            setupForSelectedLayer();

                            if(addonObservable.isAddonOpen(this)){
                                updateOriginData(rootComponent);
                            } else {
                                branchUpdate();
                            }
                        },
                        buttonComponent -> {
                            this.currentLayerIndex = this.currentLayerIndex + 1;

                            if(this.currentLayerIndex >= this.layerList.size()){
                                this.currentLayerIndex = 0;
                            }

                            setupForSelectedLayer();

                            if(addonObservable.isAddonOpen(this)){
                                updateOriginData(rootComponent);
                            } else {
                                branchUpdate();
                            }
                        }
                )
                .rebuildComponent()
                .id("addon_component_origin_header");
    }

    @Override
    public Map<Identifier, BaseAddon> getAddonData() {
        Map<Identifier, BaseAddon> addonData = new HashMap<>();

        if(!selectedOrigins.isEmpty()) {
            selectedOrigins.forEach((originLayer, origin1) ->
                addonData.put(originLayer.getIdentifier(), new OriginAddon(origin1.getIdentifier(), originLayer.getIdentifier()))
            );
        }

        layerList.forEach(originLayer -> {
            Identifier originLayerId = originLayer.getIdentifier();

            if(!addonData.containsKey(originLayerId)){
                addonData.put(originLayerId, new OriginAddon(originLayerHelpers.get(originLayer).defaultOrigin.getIdentifier(), originLayerId));
            }
        });

        return addonData;
    }

    @Override
    public boolean isDataEmpty(BaseParentComponent rootComponent) {
        return false;
    }

    public void updateOriginData(BaseParentComponent rootComponent){
        ScrollContainer<?> container = rootComponent.childById(ScrollContainer.class, "origin_info");

        OriginInfoContainer child = ((OriginInfoContainer)container.child());

        //container.onChildMutated(child);
        container.scrollTo(child);

        if(isOriginRandom(getCurrentOrigin())){
            child.randomOrigin(getCurrentOrigin(), originLayerHelpers.get(layerList.get(currentLayerIndex)).randomOriginText);
        } else {
            child.origin(getCurrentOrigin());
        }


        rootComponent.childById(LabelComponent.class, "origin_name").text(getCurrentOrigin().getName());
        rootComponent.childById(OriginImpactComponent.class, "origin_impact").setImpact(getCurrentOrigin().getImpact());
        rootComponent.childById(ItemComponent.class, "origin_icon").stack(getCurrentOrigin().getDisplayItem());

        branchUpdate();
    }

    private record RandomOriginHelper(Origin randomOrigin, MutableText randomOriginText){}

    private static class OriginLayerHelper {
        public Origin randomOrigin = null;
        public MutableText randomOriginText = null;

        public final Origin defaultOrigin;

        public final OriginLayer layer;

        public OriginLayerHelper(OriginLayer layer, Origin defaultOrigin){
            this.layer = layer;
            this.defaultOrigin = defaultOrigin;
        }

        public void setRandomInfo(Origin randomOrigin, MutableText randomOriginText){
            this.randomOrigin = randomOrigin;
            this.randomOriginText = randomOriginText;
        }
    }

    //----------------------------
}
