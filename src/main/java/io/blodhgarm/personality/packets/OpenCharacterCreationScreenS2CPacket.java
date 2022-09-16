package io.blodhgarm.personality.packets;

import io.blodhgarm.personality.client.compat.origins.OriginsSupportLoader;
import io.blodhgarm.personality.client.screens.PersonalityCreationScreen;
import io.wispforest.owo.network.ClientAccess;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

public record OpenCharacterCreationScreenS2CPacket() {

    @Environment(EnvType.CLIENT)
    public static void openScreen(OpenCharacterCreationScreenS2CPacket message, ClientAccess access){
        PersonalityCreationScreen screen = new PersonalityCreationScreen();

        if(FabricLoader.getInstance().isModLoaded("origins")){
            OriginsSupportLoader.addToPersonalityScreen(screen, access.player());
        }

        MinecraftClient.getInstance().setScreen(screen);
    }
}