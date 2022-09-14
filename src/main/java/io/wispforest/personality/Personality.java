package io.wispforest.personality;

import io.wispforest.personality.storage.CharacterManager;
import net.fabricmc.api.ModInitializer;

public class Personality implements ModInitializer {

	@Override
	public void onInitialize() {
		Commands.register();
		CharacterManager.loadCharacterReference();
		PersonalityNetworking.registerNetworking();
	}

}
