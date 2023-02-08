package io.blodhgarm.personality.client.gui.screens;

import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.api.character.CharacterManager;
import io.blodhgarm.personality.client.gui.ThemeHelper;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class PersonalitySubScreen extends BaseOwoScreen<FlowLayout> {

    public PersonalitySubScreen(){
        super(Text.of("sub_screen"));
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        FlowLayout mainFlowLayout = Containers.verticalFlow(Sizing.fixed(125), Sizing.content());

        mainFlowLayout.child(
                Components.label(Text.of("Personality Sub Menu"))
                        .color(ThemeHelper.dynamicColor())
                        .margins(Insets.of(2, 3, 0, 0))
        );
        mainFlowLayout.child(
                Components.button(Text.of("Character Info"), (ButtonComponent component) -> {
                            Character character = CharacterManager.getManger(client.world).getCharacter(client.player);

                            if(character != null || FabricLoader.getInstance().isDevelopmentEnvironment()){
                                client.setScreen(new CharacterInfoScreen());
                            } else {
                                client.player.sendMessage(Text.of("Character Info Menu requires a characters before being able to be used."));
                            }
                }).horizontalSizing(Sizing.fill(100))
                        .margins(Insets.of(0, 4, 1, 1))
        );

        mainFlowLayout.child(
                Components.button(Text.of("Full Character List"), (ButtonComponent component) -> {
                        MinecraftClient.getInstance().setScreen(new AdminCharacterScreen());
                }).horizontalSizing(Sizing.fill(100))
                        .margins(Insets.of(0, 4, 1, 1))
        );

        mainFlowLayout
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .surface(ThemeHelper.dynamicSurface())
                .padding(Insets.of(4))
                .positioning(Positioning.relative(50, 50));

        rootComponent.child(mainFlowLayout);

    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
