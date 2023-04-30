package io.blodhgarm.personality.packets;

import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.api.character.CharacterManager;
import io.blodhgarm.personality.api.reveal.InfoLevel;
import io.blodhgarm.personality.api.reveal.RevelInfoManager;
import io.wispforest.owo.network.ServerAccess;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.UUID;

public record RevealCharacterC2SPacket(InfoLevel level, RevelInfoManager.RevealRange range, String uuid) {

    public static void revealInformationToPlayers(RevealCharacterC2SPacket message, ServerAccess access){
        CharacterManager<ServerPlayerEntity> manager = CharacterManager.getManger(access.player());

        Character sourceCharacter = manager.getCharacter(access.player());

        if(sourceCharacter == null) return;

        if(message.range == RevelInfoManager.RevealRange.DIRECTED){
            ServerPlayerEntity target = access.runtime().getPlayerManager().getPlayer(UUID.fromString(message.uuid));

            if(target == null) return;

            manager.revealCharacterInfo(access.player(), List.of(target), message.level);
        } else {
            manager.revealCharacterInfo(access.player(), message.range, message.level);
        }
    }


}

