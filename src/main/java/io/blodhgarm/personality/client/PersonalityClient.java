package io.blodhgarm.personality.client;

import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class PersonalityClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
		ShaderEffectRenderCallback.EVENT.register(new BlurryVisionShaderEffect());
        ClientTickEvents.END_WORLD_TICK.register(KeyBindings::processKeybindings);
    }

}
