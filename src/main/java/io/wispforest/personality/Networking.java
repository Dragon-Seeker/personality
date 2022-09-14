package io.wispforest.personality;

import io.wispforest.owo.network.OwoNetChannel;
import io.wispforest.personality.packets.OpenCharacterCreationScreenPacket;
import net.minecraft.util.Identifier;

public class Networking {

    public static final OwoNetChannel CHANNEL = OwoNetChannel.create(new Identifier("personality", "main"));

    public static void registerNetworking(){
        //ClientBound
        Networking.CHANNEL.registerClientbound(OpenCharacterCreationScreenPacket.class, OpenCharacterCreationScreenPacket::openScreen);

        //ServerBound

    }


}
