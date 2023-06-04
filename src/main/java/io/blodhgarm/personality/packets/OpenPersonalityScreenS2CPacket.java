package io.blodhgarm.personality.packets;

import com.mojang.logging.LogUtils;
import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.api.character.CharacterManager;
import io.blodhgarm.personality.client.ClientCharacters;
import io.blodhgarm.personality.client.gui.CharacterViewMode;
import io.blodhgarm.personality.client.gui.screens.AdminCharacterScreen;
import io.blodhgarm.personality.client.gui.screens.CharacterViewScreen;
import io.wispforest.owo.network.ClientAccess;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.slf4j.Logger;

public record OpenPersonalityScreenS2CPacket(CharacterViewMode mode, String characterUUID, boolean fromAdminScreen) {

    public OpenPersonalityScreenS2CPacket(CharacterViewMode mode, String characterUUID){
        this(mode, characterUUID, false);
    }

    private static final Logger LOGGER = LogUtils.getLogger();

    @Environment(EnvType.CLIENT)
    public static void openScreen(OpenPersonalityScreenS2CPacket message, ClientAccess access){
        Character character = null;

        if(message.mode.importFromCharacter()){
            ClientCharacters manager = ClientCharacters.INSTANCE;

            boolean errorHasOccured = false;

            if(!message.characterUUID.isEmpty()) {
                character = manager.getCharacter(message.characterUUID);
            } else {
                LOGGER.error("[Personality] There was a attempt to create a Screen for {} a character and the uuid was found to be empty, meaning no screen will be opened.", message.mode);

                errorHasOccured = true;
            }

            if(character == null) {
                LOGGER.error("[Personality] There was a attempt to create a Screen for {} a character and was found to be null, meaning no screen will be opened.", message.mode);

                errorHasOccured = true;
            }

            if(errorHasOccured) return;
        }

        CharacterViewScreen characterScreen = new CharacterViewScreen(message.mode, access.player().getGameProfile(), character)
                .setOriginScreen((message.fromAdminScreen() && access.runtime().currentScreen instanceof AdminCharacterScreen screen) ? screen : null);

        MinecraftClient.getInstance().setScreen(characterScreen);
    }
}