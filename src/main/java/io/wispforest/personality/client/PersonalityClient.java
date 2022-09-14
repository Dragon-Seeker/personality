package io.wispforest.personality.client;

import net.fabricmc.api.ClientModInitializer;

public class PersonalityClient implements ClientModInitializer {

    //	private final ManagedShaderEffect blur = ShaderEffectManager.getInstance().manage(new Identifier("blur", "shaders/post/fade_in_blur.json"),
//			shader -> shader.setUniformValue("Radius", Config.OLD_PERSON_BLUR_WITHOUT_GLASSES_RADIUS));

    @Override
    public void onInitializeClient() {
//		ShaderEffectRenderCallback.EVENT.register((deltaTick) -> {
//			eafea++;
//			blur.findUniform1f("Progress").set(eafea/8000F);
//			if (eafea >= 8000)
//				eafea = 0;
//
//			blur.render(deltaTick);
//		});
    }

}
