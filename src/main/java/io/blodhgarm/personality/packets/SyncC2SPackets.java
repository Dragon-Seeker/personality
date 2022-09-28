package io.blodhgarm.personality.packets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.impl.ServerCharacters;
import io.wispforest.owo.network.ServerAccess;

public class SyncC2SPackets {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public record ModifyCharacter(String characterJson) {
        public static void modifyCharacter(ModifyCharacter message, ServerAccess access) {
            Character c = GSON.fromJson(message.characterJson, Character.class);
            ServerCharacters.INSTANCE.characterLookupMap().put(c.getUUID(), c);
            ServerCharacters.INSTANCE.saveCharacter(c);
        }
    }

    public record NewCharacter(String characterJson) {
        public static void newCharacter(NewCharacter message, ServerAccess access) {
            Character c = GSON.fromJson(message.characterJson, Character.class);
            ServerCharacters.INSTANCE.characterLookupMap().put(c.getUUID(), c);
            ServerCharacters.INSTANCE.saveCharacter(c);
            //TODO: Other New Character Stuff, like Associations. Maybe chat messages?
        }
    }

    public record AssociatePlayerToCharacter(String characterUUID){
        public static void associate(AssociatePlayerToCharacter message, ServerAccess access){
            if(ServerCharacters.INSTANCE.characterLookupMap().containsKey(message.characterUUID())){
                ServerCharacters.INSTANCE.associateCharacterToPlayer(message.characterUUID(), access.player().getUuidAsString());
            }
        }
    }

}
