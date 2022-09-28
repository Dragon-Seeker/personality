package io.blodhgarm.personality.packets;

import io.blodhgarm.personality.impl.ServerCharacters;
import io.wispforest.owo.network.ServerAccess;

import java.util.UUID;

public class RevealCharacterC2SPacket {

    public record InRange(int range) {
        public static void revealToPlayersInRange(InRange message, ServerAccess access) {
            ServerCharacters.INSTANCE.revealToPlayersInRange(access.player(), message.range);
        }
    }

    public record ToPlayer(String uuid) {
        public static void revealToPlayer(ToPlayer message, ServerAccess access) {
            ServerCharacters.INSTANCE.revealToPlayer(access.player(), access.runtime().getPlayerManager().getPlayer(UUID.fromString(message.uuid)));
        }
    }

}

