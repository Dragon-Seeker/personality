package io.blodhgarm.personality.client.gui.screens;

import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.client.gui.ThemeHelper;
import io.blodhgarm.personality.client.gui.utils.owo.layout.ButtonAddon;
import io.blodhgarm.personality.client.gui.utils.owo.VariantButtonSurface;
import io.blodhgarm.personality.client.gui.utils.owo.VariantsNinePatchRender;
import io.blodhgarm.personality.misc.pond.owo.AnimationExtension;
import io.blodhgarm.personality.misc.pond.owo.ButtonAddonDuck;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.collections4.map.LinkedMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class TabbedScreen extends BaseOwoScreen<FlowLayout> {

    public LinkedMap<Identifier, TabComponentBuilder<BaseParentComponent>> registeredBuilders = new LinkedMap<>();

    public List<ComponentBuilder<FlowLayout>> globalComponentBuilders = new ArrayList<>();

    @Nullable public Identifier activeTabId = null;
    @Nullable protected Component activeTab = null;

    public TabbedScreen(Text title){
        super(title);

        this.registerTabs(registeredBuilders);
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        this.uiAdapter.rootComponent.allowOverflow(true);

        //----------------------------------------

        int targetValue = 480 - 34;

        FlowLayout tabLayout = Containers.horizontalFlow(Sizing.fixed(targetValue - 32), Sizing.content());

        int sizing = MathHelper.floor((targetValue - 34) / ((float) registeredBuilders.size())); //100 / registeredBuilders.size();

        int builderIndex = 0;

        for (Map.Entry<Identifier, TabComponentBuilder<BaseParentComponent>> entry : registeredBuilders.entrySet()) {
            tabLayout.child(
                    entry.getValue().build(this, sizing, Sizing::fixed)
                            .margins(Insets.right((builderIndex + 1) == registeredBuilders.size() ? 0 : 2))
            );

            builderIndex++;
        }

        //----------------------------------------

        FlowLayout tabBar = Containers.horizontalFlow(Sizing.content(), Sizing.content())
                .child(
                        tabLayout
                )
                .child(
                        Containers.horizontalFlow(Sizing.content(), Sizing.content())
                                .child(
                                        Components.button(Text.literal("❌").formatted(ThemeHelper.dynamicTextColor()), (ButtonComponent component) -> this.close())
                                                .textShadow(ThemeHelper.isDarkMode())
                                                .renderer(ButtonComponent.Renderer.flat(0, 0, 0))
                                                .sizing(Sizing.fixed(16))
                                ).surface(new VariantsNinePatchRender(PersonalityMod.id("textures/gui/tab_surface.png"), Size.square(3), Size.square(64), false)
                                        .setUIndex(0)
                                        .setVIndex(4))
                );

        rootComponent //mainlayout
                .child(
                        tabBar.surface(ThemeHelper.dynamicSurface())
                                .padding(Insets.of(5)) //5
                                .positioning(Positioning.relative(50, 9))
                                .margins(Insets.bottom(3))
                )
                .horizontalAlignment(HorizontalAlignment.CENTER);

        for (ComponentBuilder<FlowLayout> b : this.globalComponentBuilders) {
            rootComponent.child(b.build(rootComponent));
        }

        this.openTab(registeredBuilders.get(registeredBuilders.firstKey()));
    }

    public void addGlobalBuilder(ComponentBuilder<FlowLayout> builder){
        this.globalComponentBuilders.add(builder);
    }

    public abstract void registerTabs(Map<Identifier, TabComponentBuilder<BaseParentComponent>> registeredBuilders);

    public boolean isTabOpen(Identifier id){
        return activeTabId != null && activeTabId.equals(id);
    }

    public <R extends BaseParentComponent> void openTab(TabComponentBuilder<R> tabComponentBuilder){
        if(isTabOpen(tabComponentBuilder.id)) return;

        this.activeTabId = tabComponentBuilder.id;

        var oldActiveTab = this.activeTab;

        if(oldActiveTab != null) {
            Positioning oldPosition = ((AnimationExtension<Positioning>) oldActiveTab.positioning().animation())
                    .getCurrentValue();

            oldActiveTab.positioning(oldPosition);

            oldActiveTab.positioning().animate(1000, Easing.LINEAR, Positioning.relative(-250, oldPosition.y)).forwards()//250
                    .finished().subscribe((direction, looping) -> this.uiAdapter.rootComponent.removeChild(oldActiveTab));
        }

        this.activeTab = tabComponentBuilder.pageBuilder.build(this.uiAdapter.rootComponent);

        this.uiAdapter.rootComponent.child(this.activeTab);

        this.uiAdapter.inflateAndMount();
    }

    public static class TabComponentBuilder<R extends BaseParentComponent> {
        public final Identifier id;
        public final Text title;

        public final ComponentBuilder<R> pageBuilder;

        private int textureIconWidth = 16, textureIconHeight = 16;

        @Nullable private Identifier iconId = null;
        @Nullable private ItemStack stackIcon = null;

        public TabComponentBuilder(Identifier id, Text title, ComponentBuilder<R> pageBuilder){
            this.id = id;
            this.title = title;

            this.pageBuilder = pageBuilder;
        }

        public TabComponentBuilder<R> iconIdentifier(Identifier iconId, int textureIconWidth, int textureIconHeight){
            this.textureIconWidth = textureIconWidth;
            this.textureIconHeight = textureIconHeight;

            return iconIdentifier(iconId);
        }

        public TabComponentBuilder<R> iconIdentifier(Identifier iconId){
            this.iconId = iconId;
            this.stackIcon = null;

            return this;
        }

        public TabComponentBuilder<R> iconStack(ItemStack stack){
            this.stackIcon = stack;
            this.iconId = null;

            return this;
        }

        public FlowLayout build(TabbedScreen screen, int sizing, Function<Integer, Sizing> widthFunc){
            FlowLayout mainLayout = Containers.horizontalFlow(widthFunc.apply(sizing), Sizing.fixed(16))
                    .configure(flowLayout -> {
                        ((ButtonAddonDuck<FlowLayout>) flowLayout)
                                .setButtonAddon(layout -> {
                                    return new ButtonAddon<>(layout)
                                            .useCustomButtonSurface(VariantButtonSurface.surfaceLike(Size.square(3), Size.square(48), false, ThemeHelper.isDarkMode(), false))
                                            .onPress(button -> {
                                                screen.isTabOpen(id);

                                                screen.openTab(this);
                                            });
                                })
                                .padding(Insets.of(3))
                                .verticalAlignment(VerticalAlignment.CENTER)
                                .tooltip(this.title);
                    });

            if(iconId != null){
                mainLayout.child(
                        Components.texture(iconId, 0, 0, textureIconWidth, textureIconHeight)
                                .sizing(Sizing.fixed(8))
                );
            } else if(this.stackIcon != null){
                mainLayout.child(
                        Components.item(stackIcon)
                                .sizing(Sizing.fixed(8))
                );
            }

            mainLayout.child(Components.label(title).color(ThemeHelper.dynamicColor()));

            return mainLayout;
        }
    }

    public interface ComponentBuilder<R extends BaseParentComponent> {
        Component build(FlowLayout layout);
    }
}
