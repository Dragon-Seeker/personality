package io.wispforest.personality.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.container.*;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.event.WindowResizeCallback;
import io.wispforest.owo.ui.util.Drawer;
import io.wispforest.owo.ui.util.UIErrorToast;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class PersonalityCreationScreen extends BaseOwoScreen<FlowLayout>  {

    public static Identifier ORIGINS_GUI_TEXTURE = new Identifier("personality", "textures/gui/origins_gui.png");
    public static Identifier INVERSE_PANEL_TEXTURE = new Identifier("personality", "textures/gui/inverse_panel.png");

    public static Surface INVERSE_PANEL = (matrices, component) -> {
        int x = component.x();
        int y = component.y();
        int width = component.width();
        int height = component.height();

        RenderSystem.setShaderTexture(0, INVERSE_PANEL_TEXTURE);

        drawTexture(matrices, x, y, 0, 0, 5, 5, 16, 16);
        drawTexture(matrices, x + width - 5, y, 10, 0, 5, 5, 16, 16);
        drawTexture(matrices, x, y + height - 5, 0, 10, 5, 5, 16, 16);
        drawTexture(matrices, x + width - 5, y + height - 5, 10, 10, 5, 5, 16, 16);

        if (width > 10 && height > 10) {
            drawTexture(matrices, x + 5, y + 5, width - 10, height - 10, 5, 5, 5, 5, 16, 16);
        }

        if (width > 10) {
            drawTexture(matrices, x + 5, y, width - 10, 5, 5, 0, 5, 5, 16, 16);
            drawTexture(matrices, x + 5, y + height - 5, width - 10, 5, 5, 10, 5, 5, 16, 16);
        }

        if (height > 10) {
            drawTexture(matrices, x, y + 5, 5, height - 10, 0, 5, 5, 5, 16, 16);
            drawTexture(matrices, x + width - 5, y + 5, 5, height - 10, 10, 5, 5, 5, 16, 16);
        }
    };

    public PersonalityCreationScreen(){}

    @Override
    protected void init() {
        if (this.invalid) return;

        // Check whether this screen was already initialized

        try {
            this.uiAdapter = this.createAdapter();
            this.build(this.uiAdapter.rootComponent);

            this.uiAdapter.inflateAndMount();
            this.client.keyboard.setRepeatEvents(true);
        } catch (Exception error) {
            Owo.LOGGER.warn("Could not initialize owo screen", error);
            UIErrorToast.report(error);
            this.invalid = true;
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    public static void generateAdapterAndChildren(VerticalFlowLayout rootFlowLayout, int xOffset, int yOffset) {
        HorizontalFlowLayout mainFlowLayout = Containers.horizontalFlow(Sizing.content(), Sizing.content());

        //Panel 1

        mainFlowLayout.child(Containers.verticalFlow(Sizing.content(), Sizing.fixed(182))
            .child(
                Components.entity(Sizing.fixed(100), MinecraftClient.getInstance().player)
                    .scaleToFit(true)
                    .allowMouseRotation(true)
            ).margins(Insets.right(20))
            .surface(Surface.DARK_PANEL));

        // END


        //Panel 2

        mainFlowLayout.child(Containers.verticalFlow(Sizing.fixed(180), Sizing.fixed(182))
                .surface(Surface.DARK_PANEL));

        // END

        mainFlowLayout.positioning(Positioning.relative(20, 50));

        rootFlowLayout.child(mainFlowLayout);
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        HorizontalFlowLayout mainFlowLayout = Containers.horizontalFlow(Sizing.content(), Sizing.content());

        //Panel 1

        mainFlowLayout.child(Containers.verticalFlow(Sizing.content(), Sizing.fixed(182))
                .child(
                        Components.entity(Sizing.fixed(100), MinecraftClient.getInstance().player)
                                .scaleToFit(true)
                                .allowMouseRotation(true)
                ).margins(Insets.right(20))
                .surface(Surface.DARK_PANEL));

        // END


        //Panel 2

        mainFlowLayout.child(Containers.verticalFlow(Sizing.fixed(180), Sizing.fixed(182))
                .surface(Surface.DARK_PANEL));

        // END

        mainFlowLayout.positioning(Positioning.relative(50, -200));

        mainFlowLayout.positioning().animate(2000, Easing.CUBIC, Positioning.relative(50, 50)).forwards();

        //Origins Panel

        mainFlowLayout.child(Containers.verticalFlow(Sizing.fixed(176), Sizing.fixed(182))
            .child(Containers.verticalFlow(Sizing.fixed(162), Sizing.fixed(168))
                .child(new CustomScrollContainer<>(ScrollContainer.ScrollDirection.VERTICAL, Sizing.fixed(149), Sizing.fixed(142),
                    Components.label(Text.of(loremIpsum)).maxWidth(149))
                    .positioning(Positioning.absolute(6, 20)))
                .surface(INVERSE_PANEL)
                    .positioning(Positioning.absolute(7, 7)))
            .child(Components.texture(ORIGINS_GUI_TEXTURE, 29,0, 7, 16,48, 48)
                .sizing(Sizing.fixed(123), Sizing.fixed(16))
                .positioning(Positioning.absolute(34,14)))
            .child(Components.texture(ORIGINS_GUI_TEXTURE, 36,0, 3, 16,48, 48)
                .positioning(Positioning.absolute(34 + 123,14)))
            .child(Components.texture(ORIGINS_GUI_TEXTURE, 0,0, 26, 26, 48, 48)
                .positioning(Positioning.absolute(9,9)))
            .surface(Surface.PANEL)
            .margins(Insets.left(20)));

        rootComponent.child(mainFlowLayout);

        rootComponent.keyPress().subscribe((keyCode, scanCode, modifiers) -> {
            if(keyCode == GLFW.GLFW_KEY_R && (modifiers & GLFW.GLFW_MOD_SHIFT) != 0){
                this.clearAndInit();

                return true;
            }

            return false;
        });
    }

    public static class CustomTextureComponent extends TextureComponent {

//        private int width, height;

        protected CustomTextureComponent(Identifier texture, int width, int height, int u, int v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
            super(texture, u, v, regionWidth, regionHeight, textureWidth, textureHeight);

            this.width = width;
            this.height = height;
        }

        @Override
        public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
            RenderSystem.setShaderTexture(0, this.texture);
            RenderSystem.enableDepthTest();

            matrices.push();
            matrices.translate(x, y, 0);
            matrices.scale(this.width / (float) this.regionWidth, this.height / (float) this.regionHeight, 0);

            var visibleArea = this.visibleArea.get();

            int bottomEdge = Math.min(visibleArea.y() + visibleArea.height(), regionHeight);
            int rightEdge = Math.min(visibleArea.x() + visibleArea.width(), regionWidth);

            Drawer.drawTexture(matrices,
                    visibleArea.x(),
                    visibleArea.y(),
                    width,
                    height,
                    this.u + visibleArea.x(),
                    this.v + visibleArea.y(),
                    rightEdge - visibleArea.x(),
                    bottomEdge - visibleArea.y(),
                    this.textureWidth, this.textureHeight
            );

            matrices.pop();
        }
    }


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
