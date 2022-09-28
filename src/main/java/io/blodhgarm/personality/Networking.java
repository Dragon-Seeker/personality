package io.blodhgarm.personality;

import io.blodhgarm.personality.impl.ServerCharacters;
import io.blodhgarm.personality.packets.*;
import io.blodhgarm.personality.utils.ServerAccess;
import io.wispforest.owo.network.OwoNetChannel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class Networking {

    public static final OwoNetChannel CHANNEL = OwoNetChannel.create(new Identifier("personality", "main"));

    public static void registerNetworking(){
        //S2C - Server to Client
        CHANNEL.registerClientboundDeferred(OpenCharacterCreationScreenS2CPacket.class);

        CHANNEL.registerClientboundDeferred(SyncS2CPackets.Initial.class);
        CHANNEL.registerClientboundDeferred(SyncS2CPackets.SyncCharacter.class);
        CHANNEL.registerClientboundDeferred(SyncS2CPackets.RemoveCharacter.class);
        CHANNEL.registerClientboundDeferred(SyncS2CPackets.Association.class);
        CHANNEL.registerClientboundDeferred(SyncS2CPackets.Dissociation.class);

        CHANNEL.registerClientboundDeferred(IntroductionPacket.class);

        //C2S - Client to Server
        CHANNEL.registerServerbound(SyncC2SPackets.ModifyCharacter.class, SyncC2SPackets.ModifyCharacter::modifyCharacter);
        CHANNEL.registerServerbound(SyncC2SPackets.NewCharacter.class, SyncC2SPackets.NewCharacter::newCharacter);
        CHANNEL.registerServerbound(SyncC2SPackets.AssociatePlayerToCharacter.class, SyncC2SPackets.AssociatePlayerToCharacter::associate);

        CHANNEL.registerServerbound(RevealCharacterC2SPacket.ToPlayer.class, RevealCharacterC2SPacket.ToPlayer::revealToPlayer);
        CHANNEL.registerServerbound(RevealCharacterC2SPacket.InRange.class, RevealCharacterC2SPacket.InRange::revealToPlayersInRange);
    }

    public static void registerNetworkingClient(){
        //S2C - Server to Client
        CHANNEL.registerClientbound(OpenCharacterCreationScreenS2CPacket.class, OpenCharacterCreationScreenS2CPacket::openScreen);

        CHANNEL.registerClientbound(SyncS2CPackets.Initial.class, SyncS2CPackets.Initial::initialSync);
        CHANNEL.registerClientbound(SyncS2CPackets.SyncCharacter.class, SyncS2CPackets.SyncCharacter::syncCharacter);
        CHANNEL.registerClientbound(SyncS2CPackets.RemoveCharacter.class, SyncS2CPackets.RemoveCharacter::removeCharacter);
        CHANNEL.registerClientbound(SyncS2CPackets.Association.class, SyncS2CPackets.Association::syncAssociation);
        CHANNEL.registerClientbound(SyncS2CPackets.Dissociation.class, SyncS2CPackets.Dissociation::syncDissociation);

        CHANNEL.registerClientbound(IntroductionPacket.class, IntroductionPacket::beenIntroduced);
    }

    public static <R extends Record> void sendC2S(R packet) {
        CHANNEL.clientHandle().send(packet);
    }

    public static <R extends Record> void sendToAll(R packet) {
        CHANNEL.serverHandle(ServerAccess.getServer()).send(packet);
    }

    public static <R extends Record> void sendS2C(PlayerEntity player, R packet) {
        CHANNEL.serverHandle(player).send(packet);
    }


}
