package io.blodhgarm.personality.packets;

import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.api.CharacterManager;
import io.blodhgarm.personality.impl.ServerCharacters;
import io.blodhgarm.personality.api.reveal.InfoRevealLevel;
import io.wispforest.owo.network.ServerAccess;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class RevealCharacterC2SPacket {

    public record InRange(int range) {
        public static void revealToPlayersInRange(InRange message, ServerAccess access) {
            ServerCharacters.INSTANCE.revealCharacterInfo(access.player(), message.range, InfoRevealLevel.GENERAL);
        }
    }

    public record ToPlayer(String uuid) {
        public static void revealToPlayer(ToPlayer message, ServerAccess access) {
            CharacterManager<ServerPlayerEntity> manager = CharacterManager.getManger(access.player());

            Character sourceCharacter = manager.getCharacter(access.player());

            if(sourceCharacter == null) return;

            ServerPlayerEntity target = access.runtime().getPlayerManager().getPlayer(UUID.fromString(message.uuid));

            if(target == null) return;

            Character targetCharacter = manager.getCharacter(target);

            if(targetCharacter == null) return;

            ServerCharacters.INSTANCE.revealCharacterInfo(sourceCharacter, target, targetCharacter, InfoRevealLevel.GENERAL);
        }
    }

}

