package io.blodhgarm.personality.client;

import io.blodhgarm.personality.client.screens.RevealIdentityScreen;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    private static final MinecraftClient client = MinecraftClient.getInstance();

    public static final KeyBinding REVEAL_KEYBIND = register("reveal", GLFW.GLFW_KEY_LEFT_ALT & GLFW.GLFW_KEY_P);

    private static KeyBinding register(String key, int defaultKey) {
        KeyBinding binding = new KeyBinding("personality.keybind." + key, InputUtil.Type.KEYSYM, defaultKey, KeyBinding.MISC_CATEGORY);
        KeyBindingHelper.registerKeyBinding(binding);
        return binding;
    }

    public static void processKeybindings(ClientWorld world) {
        while (REVEAL_KEYBIND.wasPressed())
            client.setScreen(new RevealIdentityScreen());
    }

}
