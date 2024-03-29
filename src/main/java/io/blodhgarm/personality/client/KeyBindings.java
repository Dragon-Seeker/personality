package io.blodhgarm.personality.client;

import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.api.character.CharacterManager;
import io.blodhgarm.personality.client.glisco.DescriptionRenderer;
import io.blodhgarm.personality.client.gui.screens.CharacterInfoScreen;
import io.blodhgarm.personality.client.gui.screens.PersonalitySubScreen;
import io.blodhgarm.personality.client.gui.screens.RevealIdentityScreen;
import io.blodhgarm.personality.server.PrivilegeManager;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    private static final MinecraftClient client = MinecraftClient.getInstance();

    public static KeyBinding REVEAL_KEYBIND;
    public static KeyBinding OPEN_SUB_SCREEN_KEYBIND;

    public static KeyBinding TOGGLE_DESCRIPTION_VIEW;
    public static KeyBinding TOGGLE_AUTOMATIC_SCROLLING;

    public static KeyBinding TOGGLE_AUTO_DISCOVERY;

    private static KeyBinding register(String key, int defaultKey) {
        KeyBinding binding = new KeyBinding("personality.keybind." + key, InputUtil.Type.KEYSYM, defaultKey, "category." + PersonalityMod.MODID);
        KeyBindingHelper.registerKeyBinding(binding);
        return binding;
    }

    public static void processKeybindings(ClientWorld world) {
        while (REVEAL_KEYBIND.wasPressed()) {
            if(client.player != null || FabricLoader.getInstance().isDevelopmentEnvironment()) {
                Character character = CharacterManager.getManger(world).getCharacter(client.player);

                if(character != null) {
                    client.setScreen(new RevealIdentityScreen());
                } else {
                    client.player.sendMessage(Text.of("Character Reveal Menu requires a characters before being able to be used."));
                }
            }
        }

        while (OPEN_SUB_SCREEN_KEYBIND.wasPressed()) {
            if (PrivilegeManager.PrivilegeLevel.MODERATOR.test(MinecraftClient.getInstance().player) || FabricLoader.getInstance().isDevelopmentEnvironment()) {
                client.setScreen(new PersonalitySubScreen());
            } else {
                Character character = CharacterManager.getManger(world).getCharacter(client.player);

                if (character != null || FabricLoader.getInstance().isDevelopmentEnvironment()) {
                    client.setScreen(new CharacterInfoScreen());
                } else {
                    client.player.sendMessage(Text.of("Character Info Menu requires a characters before being able to be used."));
                }
            }
        }

        if(PersonalityMod.CONFIG.descriptionConfig.descriptionView()) {
            var renderer = DescriptionRenderer.INSTANCE;

            if (PersonalityMod.CONFIG.descriptionConfig.descriptionKeybindingControl().toggle()) {
                while (TOGGLE_DESCRIPTION_VIEW.wasPressed()) {
                    renderer.disableRenderer = !renderer.disableRenderer;

                    if (!renderer.disableRenderer) {
                        client.getMessageHandler().onGameMessage(Text.of("Enabled Description View"), true);
                    } else {
                        client.getMessageHandler().onGameMessage(Text.of("Disable Description View"), true);
                    }
                }
            } else {
                var enableDescriptionView = TOGGLE_DESCRIPTION_VIEW.isPressed();

                if (renderer.disableRenderer != enableDescriptionView) renderer.disableRenderer = enableDescriptionView;
            }

            while (PersonalityMod.CONFIG.descriptionConfig.automaticScrolling() && TOGGLE_AUTOMATIC_SCROLLING.wasPressed()){
                renderer.disableAutomaticScrolling = !renderer.disableAutomaticScrolling;

                if(!DescriptionRenderer.INSTANCE.disableAutomaticScrolling){
                    DescriptionRenderer.INSTANCE.lineSpeedHandler.reset(0.0f);

                    client.getMessageHandler().onGameMessage(Text.of("Enabled Auto Scrolling"), true);
                } else {
                    client.getMessageHandler().onGameMessage(Text.of("Disabled Auto Scrolling"), true);
                }
            }

            while (TOGGLE_AUTO_DISCOVERY.wasPressed() && PersonalityMod.CONFIG.autoDiscovery()){
                ClientCharacterTick.INSTANCE.disableDiscovery = !ClientCharacterTick.INSTANCE.disableDiscovery;

                client.getMessageHandler().onGameMessage(
                        Text.of((!ClientCharacterTick.INSTANCE.disableDiscovery) ? "Enabled Auto Discovery" : "Disabled Auto Discovery"),
                        true
                );
            }
        }
    }

    public static void init(){
        REVEAL_KEYBIND = register("reveal", GLFW.GLFW_KEY_J);
        OPEN_SUB_SCREEN_KEYBIND = register("open_subscreen", GLFW.GLFW_KEY_K);

        TOGGLE_AUTOMATIC_SCROLLING = register("toggle_automatic_scrolling", GLFW.GLFW_KEY_Y);
        TOGGLE_DESCRIPTION_VIEW = register("toggle_description_view", GLFW.GLFW_KEY_U);
        TOGGLE_AUTO_DISCOVERY = register("toggle_auto_discovery", GLFW.GLFW_KEY_I);
    }
}
