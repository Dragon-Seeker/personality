package io.blodhgarm.personality;

import io.blodhgarm.personality.packets.*;
import io.wispforest.owo.Owo;
import io.wispforest.owo.network.OwoNetChannel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class Networking {

    public static final OwoNetChannel CHANNEL = OwoNetChannel.create(new Identifier("personality", "main"));

    public static void registerNetworking(){
        //S2C - Server to Client
        CHANNEL.registerClientboundDeferred(OpenPersonalityScreenS2CPacket.class);

        CHANNEL.registerClientboundDeferred(SyncS2CPackets.Initial.class);
        CHANNEL.registerClientboundDeferred(SyncS2CPackets.SyncBaseCharacterData.class);
        CHANNEL.registerClientboundDeferred(SyncS2CPackets.SyncAddonData.class);
        CHANNEL.registerClientboundDeferred(SyncS2CPackets.RemoveCharacter.class);
        CHANNEL.registerClientboundDeferred(SyncS2CPackets.Association.class);
        CHANNEL.registerClientboundDeferred(SyncS2CPackets.Dissociation.class);

        CHANNEL.registerClientboundDeferred(IntroductionPackets.UnknownIntroduction.class);
        CHANNEL.registerClientboundDeferred(IntroductionPackets.UpdatedKnowledge.class);

        CHANNEL.registerClientboundDeferred(UpdateDelayedRegistry.class);

        //C2S - Client to Server
        CHANNEL.registerServerbound(SyncC2SPackets.ModifyBaseCharacterData.class, SyncC2SPackets.ModifyBaseCharacterData::modifyCharacter);
        CHANNEL.registerServerbound(SyncC2SPackets.ModifyAddonData.class, SyncC2SPackets.ModifyAddonData::modifyAddons);
        CHANNEL.registerServerbound(SyncC2SPackets.ModifyEntireCharacter.class, SyncC2SPackets.ModifyEntireCharacter::modifyEntireCharacter);

        CHANNEL.registerServerbound(SyncC2SPackets.NewCharacter.class, SyncC2SPackets.NewCharacter::newCharacter);

        CHANNEL.registerServerbound(SyncC2SPackets.AssociatePlayerToCharacter.class, SyncC2SPackets.AssociatePlayerToCharacter::associate);
        CHANNEL.registerServerbound(SyncC2SPackets.RegistrySync.class, SyncC2SPackets.RegistrySync::registrySync);

        CHANNEL.registerServerbound(RevealCharacterC2SPacket.class, RevealCharacterC2SPacket::revealInformationToPlayers);
    }

    public static void registerNetworkingClient(){
        //S2C - Server to Client
        CHANNEL.registerClientbound(OpenPersonalityScreenS2CPacket.class, OpenPersonalityScreenS2CPacket::openScreen);

        CHANNEL.registerClientbound(SyncS2CPackets.Initial.class, SyncS2CPackets.Initial::initialSync);
        CHANNEL.registerClientbound(SyncS2CPackets.SyncBaseCharacterData.class, SyncS2CPackets.SyncBaseCharacterData::syncCharacter);
        CHANNEL.registerClientbound(SyncS2CPackets.SyncAddonData.class, SyncS2CPackets.SyncAddonData::syncAddons);
        CHANNEL.registerClientbound(SyncS2CPackets.RemoveCharacter.class, SyncS2CPackets.RemoveCharacter::removeCharacter);
        CHANNEL.registerClientbound(SyncS2CPackets.Association.class, SyncS2CPackets.Association::syncAssociation);
        CHANNEL.registerClientbound(SyncS2CPackets.Dissociation.class, SyncS2CPackets.Dissociation::syncDissociation);

        CHANNEL.registerClientbound(IntroductionPackets.UnknownIntroduction.class, IntroductionPackets.UnknownIntroduction::unknownIntroduced);
        CHANNEL.registerClientbound(IntroductionPackets.UpdatedKnowledge.class, IntroductionPackets.UpdatedKnowledge::updatedKnowledge);

        CHANNEL.registerClientbound(UpdateDelayedRegistry.class, UpdateDelayedRegistry::update);
    }

    public static <R extends Record> void sendC2S(R packet) {
        CHANNEL.clientHandle().send(packet);
    }

    public static <R extends Record> void sendToAll(R packet) {
        CHANNEL.serverHandle(Owo.currentServer()).send(packet);
    }

    public static <R extends Record> void sendS2C(PlayerEntity player, R packet) {
        CHANNEL.serverHandle(player).send(packet);
    }


}
