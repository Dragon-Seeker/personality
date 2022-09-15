package io.wispforest.personality.packets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.wispforest.owo.network.ClientAccess;
import io.wispforest.owo.network.ServerAccess;
import io.wispforest.personality.Character;
import io.wispforest.personality.client.ClientCharacters;
import io.wispforest.personality.server.ServerCharacters;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class SyncC2SPackets {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public record ModifyCharacter(String characterJson) {
        public static void modifyCharacter(ModifyCharacter message, ServerAccess access) {
            Character c = GSON.fromJson(message.characterJson, Character.class);
            ServerCharacters.characterIDToCharacter.put(c.getUUID(), c);
            ServerCharacters.saveCharacter(c);
        }
    }

    public record NewCharacter(String characterJson) {
        public static void newCharacter(NewCharacter message, ServerAccess access) {
            Character c = GSON.fromJson(message.characterJson, Character.class);
            ServerCharacters.characterIDToCharacter.put(c.getUUID(), c);
            ServerCharacters.saveCharacter(c);
            //TODO: Other New Character Stuff, like Associations. Maybe chat messages?
        }
    }

}
