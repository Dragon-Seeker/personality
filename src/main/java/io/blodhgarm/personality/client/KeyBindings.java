package io.blodhgarm.personality.client;

import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.api.character.CharacterManager;
import io.blodhgarm.personality.client.gui.screens.CharacterInfoScreen;
import io.blodhgarm.personality.client.gui.screens.PersonalitySubScreen;
import io.blodhgarm.personality.client.gui.screens.RevealIdentityScreen;
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

        while (OPEN_SUB_SCREEN_KEYBIND.wasPressed())
            client.setScreen(CharacterManager.hasModerationPermissions(MinecraftClient.getInstance().player)
                    ? new PersonalitySubScreen()
                    : new CharacterInfoScreen()
            );
    }

    public static void init(){
        REVEAL_KEYBIND = register("reveal", GLFW.GLFW_KEY_LEFT_ALT & GLFW.GLFW_KEY_P);
        OPEN_SUB_SCREEN_KEYBIND = register("open_subscreen", GLFW.GLFW_KEY_LEFT_ALT & GLFW.GLFW_KEY_L);
    }
}
