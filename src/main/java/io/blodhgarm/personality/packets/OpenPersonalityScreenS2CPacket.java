package io.blodhgarm.personality.packets;

import com.mojang.logging.LogUtils;
import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.api.CharacterManager;
import io.blodhgarm.personality.client.screens.CharacterScreenMode;
import io.blodhgarm.personality.client.screens.PersonalityCreationScreen;
import io.wispforest.owo.network.ClientAccess;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.slf4j.Logger;

public record OpenPersonalityScreenS2CPacket(CharacterScreenMode mode, String characterUUID) {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Environment(EnvType.CLIENT)
    public static void openScreen(OpenPersonalityScreenS2CPacket message, ClientAccess access){
        Character character = null;

        if(message.mode.importFromCharacter()){
            CharacterManager<ClientPlayerEntity> manager = CharacterManager.getManger(access.player());

            boolean errorHasOccured = false;

            if(!message.characterUUID.isEmpty()) {
                character = message.characterUUID.equals("personality$packet_target")
                        ? manager.getCharacter(access.player())
                        : manager.getCharacter(message.characterUUID);
            } else {
                LOGGER.error("[Personality] There was a attempt to create a Screen for {} a character and the UUID was found to be empty, meaning no screen will be opened.", message.mode);

                errorHasOccured = true;
            }

            if(character == null) {
                LOGGER.error("[Personality] There was a attempt to create a Screen for {} a character and was found to be null, meaning no screen will be opened.", message.mode);

                errorHasOccured = true;
            }

            if(errorHasOccured) return;
        }

        MinecraftClient.getInstance().setScreen(new PersonalityCreationScreen(message.mode, access.player(), character));
    }
}