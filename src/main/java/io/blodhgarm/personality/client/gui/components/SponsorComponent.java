package io.blodhgarm.personality.client.gui.components;

import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.client.gui.ThemeHelper;
import io.blodhgarm.personality.client.gui.utils.owo.layout.ButtonAddon;
import io.blodhgarm.personality.misc.pond.owo.ButtonAddonDuck;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.PositionedRectangle;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;

public class SponsorComponent extends VerticalFlowLayout {

    public Identifier TEXTURE1_LOCATION = PersonalityMod.id("textures/gui/1.png");
    public Identifier TEXTURE2_LOCATION = PersonalityMod.id("textures/gui/2.png");

    public SponsorComponent(Sizing horizontalSizing, Sizing verticalSizing) {
        super(horizontalSizing, verticalSizing);

        this.build();
    }

    public void build(){
        if(PersonalityMod.CONFIG.disableSponsorComponent()) return;

        this.child(
                    Components.texture(TEXTURE1_LOCATION, 0,0, 1317/*.85*/, 878/*.9*/, 1317, 878)
                            .visibleArea(PositionedRectangle.of(0,0, 65/*.85*/, 43/*.9*/))
                )
                .configure((FlowLayout component) -> {
                    ((ButtonAddonDuck<FlowLayout>) component)
                            .setButtonAddon(layout -> {
                                return new ButtonAddon<>(layout)
                                        .onPress(button -> {
                                            Screen currentScreen = MinecraftClient.getInstance().currentScreen;

                                            MinecraftClient.getInstance().setScreen(
                                                    new ConfirmLinkScreen(
                                                            b -> MinecraftClient.getInstance().setScreen(currentScreen),
                                                            "https://disboard.org/server/1011294378624553051",
                                                            true
                                                    )
                                            );
                                        });
                            });
                })
                .padding(Insets.of(3))
                .surface(ThemeHelper.dynamicSurface())
                .sizing(Sizing.fixed(65), Sizing.fixed(43));

    }
}
