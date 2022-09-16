package io.blodhgarm.personality.client.compat.origins.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.badge.Badge;
import io.github.apace100.origins.origin.Impact;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.registry.ModItems;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.Drawer;
import io.blodhgarm.personality.client.screens.PersonalityScreenAddon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
    private OriginLayer layer;
    private boolean isOriginRandom;
    private Text randomOriginText;

    protected static final int windowWidth = 176;
    protected static final int windowHeight = 182;
    protected int scrollPos = 0;
    private int currentMaxScroll = 0;
    public static float time = 0;

    protected int guiTop, guiLeft;

    private final LinkedList<RenderedBadge> renderedBadges = new LinkedList<>();

    //----------------------------


    //----------------------------

    private final ArrayList<OriginLayer> layerList;
    private int currentLayerIndex = 0;
    private int currentOrigin = 0;
    private final List<Origin> originSelection;
    private int maxSelection = 0;

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
        this.layer = layer;
        this.isOriginRandom = isRandom;
        this.scrollPos = 0;
        this.time = 0;
    }

    public void setRandomOriginText(Text text) {
        this.randomOriginText = text;
    }

    public static class RenderedBadge {
        public final PowerType<?> powerType;
        public final Badge badge;


        public RenderedBadge(PowerType<?> powerType, Badge badge) {
            this.powerType = powerType;
            this.badge = badge;
        }

        public boolean hasTooltip() {
            return badge.hasTooltip();
        }

        public List<TooltipComponent> getTooltipComponents(TextRenderer textRenderer, int widthLimit) {
            return badge.getTooltipComponents(powerType, widthLimit, OriginSelectionComponent.time, textRenderer);
        }

    }

    //----------------------------


    //----------------------------

    private Origin getCurrentOriginInternal() {
        if(currentOrigin == originSelection.size()) {
            if(randomOrigin == null) {
                initRandomOrigin();
            }
            return randomOrigin;
        }
        return originSelection.get(currentOrigin);
    }

    private void initRandomOrigin() {
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
                    //                    PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                    //                    if(currentOrigin == originSelection.size()) {
                    //                        buf.writeString(layerList.get(currentLayerIndex).getIdentifier().toString());
                    //                        ClientPlayNetworking.send(ModPackets.CHOOSE_RANDOM_ORIGIN, buf);
                    //                    } else {
                    //                        buf.writeString(getCurrentOrigin().getIdentifier().toString());
                    //                        buf.writeString(layerList.get(currentLayerIndex).getIdentifier().toString());
                    //                        ClientPlayNetworking.send(ModPackets.CHOOSE_ORIGIN, buf);
                    //                    }
                    //                    openNextLayerScreen();
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

    public static String loremIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ultrices gravida dictum fusce ut placerat. Id leo in vitae turpis massa. Rutrum quisque non tellus orci ac auctor augue. Fringilla est ullamcorper eget nulla. Libero enim sed faucibus turpis in eu. Sed elementum tempus egestas sed sed risus pretium quam vulputate. Dictum varius duis at consectetur lorem donec massa sapien. Faucibus interdum posuere lorem ipsum dolor sit amet consectetur. Amet dictum sit amet justo donec enim diam.\n" +
            "\n" +
            "Risus pretium quam vulputate dignissim suspendisse. Blandit aliquam etiam erat velit scelerisque. Quis ipsum suspendisse ultrices gravida dictum. Molestie nunc non blandit massa. Sagittis vitae et leo duis ut diam quam nulla. Vestibulum rhoncus est pellentesque elit ullamcorper dignissim cras tincidunt lobortis. Egestas integer eget aliquet nibh praesent tristique. Dolor morbi non arcu risus quis varius. Interdum consectetur libero id faucibus nisl. Viverra aliquet eget sit amet. Metus dictum at tempor commodo ullamcorper a lacus vestibulum. Quis eleifend quam adipiscing vitae. Nunc sed id semper risus in hendrerit gravida rutrum. Vitae semper quis lectus nulla at. Egestas quis ipsum suspendisse ultrices gravida dictum fusce ut. Neque viverra justo nec ultrices. Congue quisque egestas diam in arcu. Eu sem integer vitae justo eget magna fermentum iaculis eu. Sed lectus vestibulum mattis ullamcorper velit sed ullamcorper morbi tincidunt.\n" +
            "\n" +
            "Amet mattis vulputate enim nulla aliquet porttitor. Libero enim sed faucibus turpis. Elementum nibh tellus molestie nunc non blandit massa enim. Tortor at risus viverra adipiscing at in tellus. Mauris rhoncus aenean vel elit scelerisque mauris pellentesque pulvinar pellentesque. Purus non enim praesent elementum facilisis leo. Laoreet sit amet cursus sit amet dictum sit amet justo. Mauris sit amet massa vitae tortor condimentum lacinia quis vel. Turpis nunc eget lorem dolor sed viverra ipsum. Morbi tincidunt augue interdum velit euismod. Egestas sed sed risus pretium quam. Nam aliquam sem et tortor consequat id. Enim nec dui nunc mattis enim ut tellus elementum sagittis. Tellus at urna condimentum mattis pellentesque id nibh tortor. Arcu felis bibendum ut tristique et egestas. Id diam maecenas ultricies mi eget mauris pharetra et. Id neque aliquam vestibulum morbi. Urna duis convallis convallis tellus. At volutpat diam ut venenatis tellus in metus vulputate eu. Ac orci phasellus egestas tellus rutrum tellus pellentesque.\n" +
            "\n" +
            "Molestie nunc non blandit massa. Etiam tempor orci eu lobortis elementum nibh tellus molestie. Quis vel eros donec ac odio tempor. Enim nulla aliquet porttitor lacus luctus accumsan. Eleifend donec pretium vulputate sapien nec sagittis aliquam. Morbi non arcu risus quis. Sit amet consectetur adipiscing elit duis tristique sollicitudin. Volutpat commodo sed egestas egestas fringilla. Lacinia at quis risus sed vulputate odio ut enim blandit. Et tortor consequat id porta nibh venenatis cras sed. Arcu dictum varius duis at consectetur. Adipiscing bibendum est ultricies integer quis auctor. Turpis massa sed elementum tempus egestas. Libero id faucibus nisl tincidunt eget. Etiam dignissim diam quis enim lobortis scelerisque fermentum dui faucibus. Tristique senectus et netus et malesuada fames ac. Laoreet suspendisse interdum consectetur libero id faucibus. Id venenatis a condimentum vitae sapien pellentesque.\n" +
            "\n" +
            "Magnis dis parturient montes nascetur ridiculus mus mauris. Volutpat consequat mauris nunc congue nisi vitae. Ac odio tempor orci dapibus ultrices in iaculis. Lacus sed viverra tellus in hac. Venenatis tellus in metus vulputate. Feugiat scelerisque varius morbi enim nunc faucibus a. Tortor vitae purus faucibus ornare suspendisse sed nisi lacus sed. Facilisi etiam dignissim diam quis enim lobortis. A erat nam at lectus urna duis convallis. Eget est lorem ipsum dolor sit amet. Magna fermentum iaculis eu non diam phasellus vestibulum lorem. Mi tempus imperdiet nulla malesuada pellentesque elit. Gravida in fermentum et sollicitudin ac orci phasellus egestas tellus. Et odio pellentesque diam volutpat commodo sed egestas. Faucibus scelerisque eleifend donec pretium vulputate sapien nec sagittis. Amet commodo nulla facilisi nullam vehicula ipsum a arcu. Elementum facilisis leo vel fringilla. Rutrum tellus pellentesque eu tincidunt tortor aliquam nulla facilisi. Gravida cum sociis natoque penatibus et magnis. Pretium fusce id velit ut tortor pretium viverra.\n" +
            "\n" +
            "Mauris rhoncus aenean vel elit scelerisque mauris pellentesque. Eu turpis egestas pretium aenean pharetra. A lacus vestibulum sed arcu non odio. Hendrerit gravida rutrum quisque non tellus orci ac auctor. Duis ut diam quam nulla porttitor massa. Id leo in vitae turpis. Arcu non sodales neque sodales ut etiam sit amet. Tellus in metus vulputate eu scelerisque felis imperdiet proin fermentum. In egestas erat imperdiet sed euismod nisi porta lorem mollis. In nulla posuere sollicitudin aliquam ultrices. Amet facilisis magna etiam tempor orci. Arcu dui vivamus arcu felis bibendum ut tristique et. Risus in hendrerit gravida rutrum quisque non tellus orci. Tempor id eu nisl nunc mi. Eget dolor morbi non arcu.\n" +
            "\n" +
            "Tristique sollicitudin nibh sit amet commodo nulla facilisi nullam vehicula. Non arcu risus quis varius quam. Auctor eu augue ut lectus arcu bibendum at varius. At tempor commodo ullamcorper a lacus vestibulum. Nunc sed blandit libero volutpat. Facilisis sed odio morbi quis commodo odio aenean. Id venenatis a condimentum vitae sapien pellentesque habitant morbi tristique. Viverra justo nec ultrices dui sapien. Dolor purus non enim praesent elementum facilisis leo. Adipiscing tristique risus nec feugiat. Amet nisl suscipit adipiscing bibendum est. Sociis natoque penatibus et magnis dis.\n" +
            "\n" +
            "Sed sed risus pretium quam vulputate. Mauris pellentesque pulvinar pellentesque habitant morbi tristique senectus et netus. Tristique senectus et netus et malesuada. Eget aliquet nibh praesent tristique magna sit amet. Quis auctor elit sed vulputate. Odio aenean sed adipiscing diam donec adipiscing tristique risus nec. Feugiat pretium nibh ipsum consequat nisl vel pretium. Egestas erat imperdiet sed euismod nisi porta lorem mollis aliquam. A arcu cursus vitae congue mauris. Diam in arcu cursus euismod quis viverra nibh cras pulvinar.\n" +
            "\n" +
            "Tincidunt vitae semper quis lectus nulla at. Proin fermentum leo vel orci porta non pulvinar neque laoreet. Pretium fusce id velit ut tortor pretium. Porttitor lacus luctus accumsan tortor posuere ac. Morbi non arcu risus quis varius. Euismod elementum nisi quis eleifend quam adipiscing. Et netus et malesuada fames ac turpis egestas sed. Dui id ornare arcu odio ut. Sit amet massa vitae tortor condimentum lacinia. Amet volutpat consequat mauris nunc congue nisi vitae suscipit tellus.";



}
