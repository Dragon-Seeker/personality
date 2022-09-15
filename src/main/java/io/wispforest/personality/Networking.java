package io.wispforest.personality;

import io.wispforest.owo.network.OwoNetChannel;
import io.wispforest.personality.packets.OpenCharacterCreationScreenS2CPacket;
import io.wispforest.personality.packets.SyncS2CPackets;
import io.wispforest.personality.server.PersonalityServer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class Networking {

    public static final OwoNetChannel CHANNEL = OwoNetChannel.create(new Identifier("personality", "main"));

    public static void registerNetworking(){
        //S2C - Server to Client
        CHANNEL.registerClientbound(OpenCharacterCreationScreenS2CPacket.class, OpenCharacterCreationScreenS2CPacket::openScreen);
        CHANNEL.registerClientbound(SyncS2CPackets.Initial.class, SyncS2CPackets.Initial::initialSync);
        CHANNEL.registerClientbound(SyncS2CPackets.RemoveCharacter.class, SyncS2CPackets.RemoveCharacter::syncCharacter);
        CHANNEL.registerClientbound(SyncS2CPackets.Association.class, SyncS2CPackets.Association::syncAssociation);

        //C2S - Client to Server

    }

    public static <R extends Record> void sendC2S(R packet) {
        CHANNEL.clientHandle().send(packet);
    }

    public static <R extends Record> void sendToAll(R packet) {
        CHANNEL.serverHandle(PersonalityServer.server).send(packet);
    }

    public static <R extends Record> void sendS2C(PlayerEntity player, R packet) {
        CHANNEL.serverHandle(player).send(packet);
    }


}
