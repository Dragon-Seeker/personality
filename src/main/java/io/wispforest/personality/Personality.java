package io.wispforest.personality;

import io.wispforest.personality.storage.CharacterManager;
import net.fabricmc.api.ModInitializer;

public class Personality implements ModInitializer {

	public static final float YOUTH_EXHAUSTION_MULTIPLIER = 1.2F;

	@Override
	public void onInitialize() {
		Commands.register();
		CharacterManager.loadCharacterReference();
	}

}
