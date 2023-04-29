package io.blodhgarm.personality.client.gui.components;

import com.eliotlash.mclib.math.functions.limit.Min;
import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.client.gui.ThemeHelper;
import io.blodhgarm.personality.client.gui.components.owo.CustomTextureComponent;
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
import net.minecraft.text.Text;
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

        // 48 32 | 36 24 | 29 19

        int width = 29;
        int height = 19;

//        float wScaleFactor = (MinecraftClient.getInstance().getWindow().getScaledWidth() / 512f) - 0.2f;
//        float hScaleFactor = (MinecraftClient.getInstance().getWindow().getScaledWidth() / 512f) - 0.2f;
//
//        width = Math.round(Math.min(width * wScaleFactor, width));
//        height = Math.round(Math.min(height * hScaleFactor, height));

        this.child(new CustomTextureComponent(TEXTURE1_LOCATION, 0,0, width, height, 1317, 878))
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
                .padding(Insets.of(2))
                .surface(ThemeHelper.dynamicSurface())
                .tooltip(Text.of("Designed for OUTLANDER: N-SMP"))
                .sizing(Sizing.content(), Sizing.content()); //65 43

    }
}
