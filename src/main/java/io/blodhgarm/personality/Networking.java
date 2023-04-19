package io.blodhgarm.personality;

import com.mojang.logging.LogUtils;
import io.blodhgarm.personality.client.gui.screens.AdminCharacterScreen;
import io.blodhgarm.personality.packets.*;
import io.blodhgarm.personality.server.ServerCharacters;
import io.wispforest.owo.Owo;
import io.wispforest.owo.network.OwoNetChannel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.impl.util.StringUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

public class Networking {

    private static Logger LOGGER = LogUtils.getLogger();

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

        CHANNEL.registerClientboundDeferred(ServerCharacters.ReturnInformation.class);

        //C2S - Client to Server
        CHANNEL.registerServerbound(SyncC2SPackets.ModifyBaseCharacterData.class, SyncC2SPackets.ModifyBaseCharacterData::modifyCharacter);
        CHANNEL.registerServerbound(SyncC2SPackets.ModifyAddonData.class, SyncC2SPackets.ModifyAddonData::modifyAddons);
        CHANNEL.registerServerbound(SyncC2SPackets.ModifyEntireCharacter.class, SyncC2SPackets.ModifyEntireCharacter::modifyEntireCharacter);

        CHANNEL.registerServerbound(SyncC2SPackets.NewCharacter.class, SyncC2SPackets.NewCharacter::newCharacter);

        CHANNEL.registerServerbound(SyncC2SPackets.RegistrySync.class, SyncC2SPackets.RegistrySync::registriesSync);

        CHANNEL.registerServerbound(RevealCharacterC2SPacket.class, RevealCharacterC2SPacket::revealInformationToPlayers);

        CHANNEL.registerServerbound(AdminActionPackets.AssociateAction.class, AdminActionPackets.AssociateAction::attemptAssociateAction);
        CHANNEL.registerServerbound(AdminActionPackets.DisassociateAction.class, AdminActionPackets.DisassociateAction::attemptDisassociateAction);

        CHANNEL.registerServerbound(AdminActionPackets.EditAction.class, AdminActionPackets.EditAction::attemptEditAction);

        CHANNEL.registerServerbound(AdminActionPackets.CharacterBasedAction.class, AdminActionPackets.CharacterBasedAction::attemptAction);
    }

    @Environment(EnvType.CLIENT)
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

        CHANNEL.registerClientbound(ServerCharacters.ReturnInformation.class, (message, access) -> {
            SystemToast.add(access.runtime().getToastManager(), SystemToast.Type.CHAT_PREVIEW_WARNING, Text.of(StringUtil.capitalize(message.action())), Text.of(message.returnMessage()));

            if(MinecraftClient.getInstance().currentScreen instanceof AdminCharacterScreen screen){
                screen.clearSelectedEntries();
            }

            LOGGER.info("Action: {}, Message: {}", message.action(), message.returnMessage());
        });
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
