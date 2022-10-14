package io.blodhgarm.personality.packets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import io.blodhgarm.personality.api.addon.AddonRegistry;
import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.api.addon.BaseAddon;
import io.blodhgarm.personality.impl.ServerCharacters;
import io.wispforest.owo.network.ServerAccess;
import net.minecraft.util.Identifier;

import java.util.Map;

public class SyncC2SPackets {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public record ModifyCharacter(String characterJson) {
        public static void modifyCharacter(ModifyCharacter message, ServerAccess access) {
            Character c = GSON.fromJson(message.characterJson, Character.class);
            ServerCharacters.INSTANCE.characterLookupMap().put(c.getUUID(), c);
            ServerCharacters.INSTANCE.saveCharacter(c);
        }
    }

    public record NewCharacter(String characterJson, Map<Identifier, String> addonData, boolean immediateAssociation) {
        public static void newCharacter(NewCharacter message, ServerAccess access) {
            Character c = GSON.fromJson(message.characterJson, Character.class);

            message.addonData.forEach((addonId, addonJson) -> {
                BaseAddon addon = null;

                try {
                    addon = GSON.fromJson(addonJson, AddonRegistry.INSTANCE.getAddonClass(addonId));
                } catch (JsonSyntaxException e){
                    e.printStackTrace();
                }

                addon = AddonRegistry.INSTANCE.validateOrDefault(addonId, addon);

                c.characterAddons.put(addonId, addon);
            });

            ServerCharacters.INSTANCE.saveCharacter(c);

            ServerCharacters.INSTANCE.saveAddonsForCharacter(c, true);

            if(message.immediateAssociation){
                if(ServerCharacters.INSTANCE.getCharacterUUID(access.player()) != null){
                    ServerCharacters.INSTANCE.dissociateUUID(access.player().getUuidAsString(), false);
                }

                ServerCharacters.INSTANCE.associateCharacterToPlayer(c.getUUID(), access.player().getUuidAsString());
            }

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
