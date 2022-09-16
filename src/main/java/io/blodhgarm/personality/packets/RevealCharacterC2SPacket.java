package io.blodhgarm.personality.packets;

import io.blodhgarm.personality.server.ServerCharacters;
import io.wispforest.owo.network.ServerAccess;

import java.util.UUID;

public class RevealCharacterC2SPacket {

    public record InRange(int range) {
        public static void revealToPlayersInRange(InRange message, ServerAccess access) {
            ServerCharacters.revealToPlayersInRange(access.player(), message.range);
        }
    }

    public record ToPlayer(String uuid) {
        public static void revealToPlayer(ToPlayer message, ServerAccess access) {
            ServerCharacters.revealToPlayer(access.player().getUuidAsString(), access.runtime().getPlayerManager().getPlayer(UUID.fromString(message.uuid)));
        }
    }

}

