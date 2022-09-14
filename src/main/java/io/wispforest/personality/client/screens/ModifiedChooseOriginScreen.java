package io.wispforest.personality.client.screens;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.networking.ModPackets;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.screen.ChooseOriginScreen;
import io.netty.buffer.Unpooled;
import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.util.UIErrorToast;
import io.wispforest.personality.mixin.client.origins.ChooseOriginScreenAccessor;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

import java.util.ArrayList;

public class ModifiedChooseOriginScreen extends ChooseOriginScreen {

    public OwoUIAdapter<VerticalFlowLayout> uiAdapter;

    public boolean invalid = false;

    public ModifiedChooseOriginScreen(ArrayList<OriginLayer> layerList, int currentLayerIndex, boolean showDirtBackground) {
        super(layerList, currentLayerIndex, false);
    }

    @Override
    protected void init() {
        if(this.invalid) return;

        super.init();

        // Check whether this screen was already initialized
//        if (this.uiAdapter != null) {
//            // If it was, only resize the adapter instead of recreating it - this preserves UI state
//            this.uiAdapter.moveAndResize(0, 0, this.width, this.height);
//            // Re-add it as a child to circumvent vanilla clearing them
//            this.addDrawableChild(this.uiAdapter);
//        } else {
            try {
                this.uiAdapter = OwoUIAdapter.create(this, Containers::verticalFlow);
                this.buildOwoAdapter(this.uiAdapter.rootComponent);

                this.uiAdapter.inflateAndMount();
                this.client.keyboard.setRepeatEvents(true);
            } catch (Exception error) {
                Owo.LOGGER.warn("Could not initialize owo screen", error);
                UIErrorToast.report(error);
                this.invalid = true;
            }
        //}

        ChooseOriginScreenAccessor accessor = ((ChooseOriginScreenAccessor)this);
        if(accessor.personality$MaxSelection() > 1) {
            addDrawableChild(new ButtonWidget(guiLeft + 10,guiTop + windowHeight + 5, 20, 20, Text.of("<"), b -> {
                accessor.personality$setCurrentOrigin((accessor.personality$CurrentOrigin() - 1 + accessor.personality$MaxSelection()) % accessor.personality$MaxSelection());
                Origin newOrigin = accessor.personality$GetCurrentOriginInternal();
                showOrigin(newOrigin, accessor.personality$LayerList().get(accessor.personality$CurrentLayerIndex()), newOrigin == accessor.personality$RandomOrigin());
            }));
            addDrawableChild(new ButtonWidget(guiLeft + windowWidth - 30, guiTop + windowHeight + 5, 20, 20, Text.of(">"), b -> {
                accessor.personality$setCurrentOrigin((accessor.personality$CurrentOrigin() + 1) % accessor.personality$MaxSelection());
                Origin newOrigin = accessor.personality$GetCurrentOriginInternal();
                showOrigin(newOrigin, accessor.personality$LayerList().get(accessor.personality$CurrentLayerIndex()), newOrigin == accessor.personality$RandomOrigin());
            }));
        }
        addDrawableChild(new ButtonWidget(guiLeft + windowWidth / 2 - 50, guiTop + windowHeight + 5, 100, 20, Text.translatable(Origins.MODID + ".gui.select"), b -> {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            if(accessor.personality$CurrentOrigin() == accessor.personality$OriginSelection().size()) {
                buf.writeString(accessor.personality$LayerList().get(accessor.personality$CurrentLayerIndex()).getIdentifier().toString());
                ClientPlayNetworking.send(ModPackets.CHOOSE_RANDOM_ORIGIN, buf);
            } else {
                buf.writeString(getCurrentOrigin().getIdentifier().toString());
                buf.writeString(accessor.personality$LayerList().get(accessor.personality$CurrentLayerIndex()).getIdentifier().toString());
                ClientPlayNetworking.send(ModPackets.CHOOSE_ORIGIN, buf);
            }
        }));
    }

    public void buildOwoAdapter(VerticalFlowLayout flowLayout){
        PersonalityCreationScreen.generateAdapterAndChildren(flowLayout, 0, guiTop);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if(!this.invalid) {
            super.render(matrices, mouseX, mouseY, delta);
        } else {
            this.close();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.uiAdapter.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY) || this.uiAdapter.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void removed() {
        if (this.uiAdapter != null) this.uiAdapter.dispose();
        this.client.keyboard.setRepeatEvents(false);
    }
}
