package io.blodhgarm.personality.packets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.client.ClientCharacters;
import io.wispforest.owo.network.ClientAccess;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
import java.util.Map;

public class SyncS2CPackets {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public record Initial(List<String> characters, Map<String,String> associations) {
        @Environment(EnvType.CLIENT)
        public static void initialSync(Initial message, ClientAccess access) {
            ClientCharacters.INSTANCE.init(message.characters, message.associations);
        }
    }

    public record SyncCharacter(String characterJson) {
        @Environment(EnvType.CLIENT)
        public static void syncCharacter(SyncCharacter message, ClientAccess access) {
            Character c = GSON.fromJson(message.characterJson, Character.class);
            ClientCharacters.INSTANCE.characterLookupMap().put(c.getUUID(), c);
        }
    }

    public record RemoveCharacter(String characterUUID) {
        @Environment(EnvType.CLIENT)
        public static void removeCharacter(RemoveCharacter message, ClientAccess access) {
            ClientCharacters.INSTANCE.removeCharacter(message.characterUUID);
        }
    }

    public record Association(String characterUUID, String newPlayerUUID) {
        @Environment(EnvType.CLIENT)
        public static void syncAssociation(Association message, ClientAccess access) {
            ClientCharacters.INSTANCE.associateCharacterToPlayer(message.characterUUID, message.newPlayerUUID);
        }
    }

    public record Dissociation(String uuid, boolean characterUUID) {
        @Environment(EnvType.CLIENT)
        public static void syncDissociation(Dissociation message, ClientAccess access) {
            ClientCharacters.INSTANCE.dissociateUUID(message.uuid, message.characterUUID);
        }
    }

}
