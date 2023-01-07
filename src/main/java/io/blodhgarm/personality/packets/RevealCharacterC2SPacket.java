package io.blodhgarm.personality.packets;

import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.api.CharacterManager;
import io.blodhgarm.personality.impl.RevelCharacterInfo;
import io.blodhgarm.personality.impl.ServerCharacters;
import io.blodhgarm.personality.api.reveal.InfoRevealLevel;
import io.wispforest.owo.network.ServerAccess;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public record RevealCharacterC2SPacket(InfoRevealLevel level, RevelCharacterInfo.RevealRange range, String uuid) {

    public RevealCharacterC2SPacket(InfoRevealLevel level, RevelCharacterInfo.RevealRange range){
        this(level, range, "");
    }

    public static void revealInformationToPlayers(RevealCharacterC2SPacket message, ServerAccess access){
        CharacterManager<ServerPlayerEntity> manager = CharacterManager.getManger(access.player());

        Character sourceCharacter = manager.getCharacter(access.player());

        if(sourceCharacter == null) return;

        if(message.range != RevelCharacterInfo.RevealRange.DIRECTED){
            ServerPlayerEntity target = access.runtime().getPlayerManager().getPlayer(UUID.fromString(message.uuid));

            if(target == null) return;

            manager.revealCharacterInfo(access.player(), List.of(target), message.level);
        } else {
            manager.revealCharacterInfo(access.player(), message.range, message.level);
        }
    }


}

