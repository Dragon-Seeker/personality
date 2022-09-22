package io.blodhgarm.personality.packets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.blodhgarm.personality.Character;
import io.blodhgarm.personality.server.ServerCharacters;
import io.wispforest.owo.network.ServerAccess;

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

    public record AssociatePlayerToCharacter(String characterUUID){
        public static void associate(AssociatePlayerToCharacter message, ServerAccess access){
            if(ServerCharacters.characterIDToCharacter.containsKey(message.characterUUID())){
                ServerCharacters.associateCharacterToPlayer(message.characterUUID(), access.player().getUuidAsString());
            }
        }
    }

}
