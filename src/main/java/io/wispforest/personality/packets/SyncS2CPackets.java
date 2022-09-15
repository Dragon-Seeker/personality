package io.wispforest.personality.packets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.wispforest.owo.network.ClientAccess;
import io.wispforest.personality.Character;
import io.wispforest.personality.client.ClientCharacters;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
import java.util.Map;

public class SyncS2CPackets {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public record Initial(List<String> characters, Map<String,String> associations) {
        @Environment(EnvType.CLIENT)
        public static void initialSync(Initial message, ClientAccess access) {
            ClientCharacters.init(message.characters, message.associations);
        }
    }

    public record SyncCharacter(String character) {
        @Environment(EnvType.CLIENT)
        public static void addCharacter(SyncCharacter message, ClientAccess access) {
            Character c = GSON.fromJson(message.character, Character.class);
            ClientCharacters.characterIDToCharacter.put(c.getUUID(), c);
        }
    }

    public record RemoveCharacter(String characterUUID) {
        @Environment(EnvType.CLIENT)
        public static void syncCharacter(RemoveCharacter message, ClientAccess access) {
            ClientCharacters.playerIDToCharacterID.inverse().remove(message.characterUUID);
            ClientCharacters.characterIDToCharacter.remove(message.characterUUID);
        }
    }

    public record Association(String characterUUID, String newPlayerUUID) {
        @Environment(EnvType.CLIENT)
        public static void syncAssociation(Association message, ClientAccess access) {
            ClientCharacters.playerIDToCharacterID.inverse().remove(message.characterUUID);
            ClientCharacters.playerIDToCharacterID.put(message.newPlayerUUID, message.characterUUID);
        }
    }

}
