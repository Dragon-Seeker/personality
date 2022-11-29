package io.blodhgarm.personality.client;

import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.client.gui.screens.PersonalitySubScreen;
import io.blodhgarm.personality.client.gui.screens.RevealIdentityScreen;
import io.github.apace100.origins.Origins;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
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
        while (REVEAL_KEYBIND.wasPressed())
            client.setScreen(new RevealIdentityScreen());

        while (OPEN_SUB_SCREEN_KEYBIND.wasPressed())
            client.setScreen(new PersonalitySubScreen());
    }

    public static void init(){
        REVEAL_KEYBIND = register("reveal", GLFW.GLFW_KEY_LEFT_ALT & GLFW.GLFW_KEY_P);
        OPEN_SUB_SCREEN_KEYBIND = register("open_subscreen", GLFW.GLFW_KEY_LEFT_ALT & GLFW.GLFW_KEY_L);
    }
}
