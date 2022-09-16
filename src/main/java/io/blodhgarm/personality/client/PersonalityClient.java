package io.blodhgarm.personality.client;

import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import net.fabricmc.api.ClientModInitializer;

public class PersonalityClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
		ShaderEffectRenderCallback.EVENT.register(new OldPersonShaderEffect());
    }

}
