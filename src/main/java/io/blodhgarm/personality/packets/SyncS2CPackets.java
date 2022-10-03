package io.blodhgarm.personality.packets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import io.blodhgarm.personality.api.AddonRegistry;
import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.api.addons.BaseAddon;
import io.blodhgarm.personality.client.ClientCharacters;
import io.wispforest.owo.network.ClientAccess;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

public class SyncS2CPackets {

    private static Logger LOGGER = LogUtils.getLogger();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public record Initial(Map<String, String> characters, Map<String, String> associations) {
        @Environment(EnvType.CLIENT)
        public static void initialSync(Initial message, ClientAccess access) {
            ClientCharacters.INSTANCE.init(message.characters, message.associations);
        }
    }

    public record SyncCharacter(String characterJson, String addonJsons) {
        @Environment(EnvType.CLIENT)
        public static void syncCharacter(SyncCharacter message, ClientAccess access) {
            Character c = GSON.fromJson(message.characterJson, Character.class);

            if(!message.addonJsons.isEmpty()) {
                c.characterAddons.putAll(GSON.fromJson(message.characterJson(), Character.REF_MAP_TYPE));
            } else {
                Character oldCharacter = ClientCharacters.INSTANCE.getCharacter(c.getUUID());

                if(oldCharacter != null) c.characterAddons.putAll(oldCharacter.characterAddons);
            }

            ClientCharacters.INSTANCE.characterLookupMap().put(c.getUUID(), c);
        }
    }

    public record SyncAddonData(String characterUUID, Map<String, String> addonJsons){

        @Environment(EnvType.CLIENT)
        public static void syncAddons(SyncAddonData message, ClientAccess access) {
            Character c = ClientCharacters.INSTANCE.getCharacter(message.characterUUID());

            if(c == null){
                LOGGER.error("[SyncAddons] It seems that there was no Character [UUID: {}] to sync addons with, such will be ignored.", message.characterUUID());

                return;
            }

            message.addonJsons.forEach((addonId, addonJson) -> {
                c.characterAddons.put(addonId, GSON.fromJson(addonJson, AddonRegistry.INSTANCE.getAddonClass(addonId)));
            });
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
