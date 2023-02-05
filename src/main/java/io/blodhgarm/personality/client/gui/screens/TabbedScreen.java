package io.blodhgarm.personality.client.gui.screens;

import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.client.gui.ThemeHelper;
import io.blodhgarm.personality.client.gui.components.ButtonAddon;
import io.blodhgarm.personality.client.gui.utils.owo.VariantsNinePatchRender;
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

import java.util.Map;
import java.util.function.Function;

public abstract class TabbedScreen extends BaseOwoScreen<FlowLayout> {

    public LinkedMap<Identifier, TabComponentBuilder<BaseParentComponent>> registeredBuilders = new LinkedMap<>();

    @Nullable
    public Identifier activeTabId = null;

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
        FlowLayout mainLayout = Containers.verticalFlow(Sizing.fixed(480), Sizing.content());

        FlowLayout tabLayout = Containers.horizontalFlow(Sizing.fixed(480 - 32), Sizing.content());

        int sizing = MathHelper.floor((480 - 34) / ((float) registeredBuilders.size())); //100 / registeredBuilders.size();

        int builderIndex = 0;

        for (Map.Entry<Identifier, TabComponentBuilder<BaseParentComponent>> entry : registeredBuilders.entrySet()) {
            boolean isLast = (builderIndex + 1) == registeredBuilders.size();

            tabLayout.child(
                    entry.getValue().build(this, tabLayout, sizing, Sizing::fixed)
                            .margins(Insets.right(isLast ? 0 : 2))
            );

            builderIndex++;
        }

        FlowLayout tabBar = Containers.horizontalFlow(Sizing.content(), Sizing.content());

        tabBar.child(tabLayout);

        tabBar.child(
                Containers.horizontalFlow(Sizing.content(), Sizing.content())
                        .child(
                                Components.button(Text.literal("❌").formatted(ThemeHelper.dynamicTextColor()), (ButtonComponent component) -> this.close())
                                        .textShadow(ThemeHelper.isDarkMode())
                                        .renderer(ButtonComponent.Renderer.flat(0, 0, 0))
                                        .sizing(Sizing.fixed(16))
                        ).surface(TabComponentBuilder.tabSurfaceRender)
        );

        tabBar
                .surface(ThemeHelper.dynamicSurface())
                .padding(Insets.of(5)); //5

        mainLayout.child(
                tabBar.margins(Insets.bottom(3))
        );

//        mainLayout.child(
//                Components.box(Sizing.fill(100), Sizing.fixed(1))
//                        .margins(Insets.of(0, 3, 3, 3))
//        );

        FlowLayout tabView = (FlowLayout) Containers.verticalFlow(Sizing.content(), Sizing.content())
//                .surface(ThemeHelper.dynamicSurface())
                .id("tab_view");

//        GridLayout buttonLayout = Containers.grid(Sizing.content(), Sizing.content(), 4, 4);
//
//        for(int i = 0; i < 16; i++){
//            int x = i % 4;
//            int y = i / 4;
//
//            buttonLayout.child(Components.button(Text.of(String.valueOf(i)), (ButtonComponent button) -> {
//                TabComponentBuilder.tabSurfaceRender.setUIndex(x);
//                TabComponentBuilder.tabSurfaceRender.setVIndex(y);
//            }).margins(Insets.of(3)),
//                    y, x);
//        }
//
//        tabView.child(buttonLayout);

        mainLayout.child(
                tabView
        );

        mainLayout.positioning(Positioning.relative(50, 50))
                .horizontalAlignment(HorizontalAlignment.CENTER);
//                .padding(Insets.of(6));
//                .surface(ThemeHelper.dynamicSurface());

        rootComponent.child(mainLayout);

        this.openTab(registeredBuilders.get(registeredBuilders.firstKey()));
    }

    public abstract void registerTabs(Map<Identifier, TabComponentBuilder<BaseParentComponent>> registeredBuilders);

    public boolean isTabOpen(Identifier id){
        return activeTabId != null && activeTabId.equals(id);
    }

    public <R extends BaseParentComponent> void openTab(TabComponentBuilder<R> tabComponentBuilder){
        if(!isTabOpen(tabComponentBuilder.id)){
            this.activeTabId = tabComponentBuilder.id;

            FlowLayout pageLayout = this.uiAdapter.rootComponent.childById(FlowLayout.class, "tab_view");

            pageLayout.clearChildren();

            tabComponentBuilder.pageBuilder.build(pageLayout);

            this.uiAdapter.inflateAndMount();
        }
    }

    public static class TabComponentBuilder<R extends BaseParentComponent> {

        protected static final VariantsNinePatchRender tabSurfaceRender = new VariantsNinePatchRender(PersonalityMod.id("textures/gui/tab_surface.png"), Size.square(3), Size.square(64), false)
                .setUIndex(0)
                .setVIndex(4);

        public final Identifier id;
        public final Text title;

        public final ComponentBuilder<R> pageBuilder;

        @Nullable private Identifier iconId = null;
        private int textureIconWidth = 16, textureIconHeight = 16;

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

        public FlowLayout build(TabbedScreen screen, FlowLayout rootComponent, int sizing, Function<Integer, Sizing> widthFunc){
            FlowLayout mainLayout = Containers.horizontalFlow(widthFunc.apply(sizing), Sizing.fixed(16));

            ((ButtonAddonDuck<FlowLayout>) mainLayout)
                    .setButtonAddon(layout -> {
                        return new ButtonAddon<>(layout)
                                .useCustomButtonSurface(render -> render.setVIndex(ButtonAddon.getVIndex(ThemeHelper.isDarkMode(), false)))
                                .onPress(button -> {
                                    screen.isTabOpen(id);

                                    screen.openTab(this);
                                });
                    })
                    .padding(Insets.of(3))
                    .verticalAlignment(VerticalAlignment.CENTER)
                    .tooltip(this.title);

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
        void build(FlowLayout layout);
    }
}
