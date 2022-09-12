package io.wispforest.personality;

import io.wispforest.personality.storage.Character;
import io.wispforest.personality.storage.CharacterManager;
import net.fabricmc.api.ModInitializer;

import java.util.UUID;


public class Personality implements ModInitializer {

	@Override
	public void onInitialize() {
		CharacterManager.loadCharacterReference();
		System.out.println("test");
		for (int i = 0; i < 10; i++)
			CharacterManager.playerToCharacters.put(UUID.randomUUID().toString(), UUID.randomUUID().toString());
		System.out.println("test2");
		CharacterManager.saveCharacterReference();
	}
}
