package io.blodhgarm.personality.packets;

import com.mojang.logging.LogUtils;
import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.api.addon.AddonRegistry;
import io.blodhgarm.personality.api.addon.BaseAddon;
import io.blodhgarm.personality.client.ClientCharacters;
import io.wispforest.owo.network.ClientAccess;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.util.Map;

public class SyncS2CPackets {

    private static final Logger LOGGER = LogUtils.getLogger();

    public record Initial(Map<String, String> characters, Map<String, String> associations) {
        @Environment(EnvType.CLIENT)
        public static void initialSync(Initial message, ClientAccess access) {
            ClientCharacters.INSTANCE.init(message.characters, message.associations);
        }
    }

    public record SyncCharacter(String characterJson, Map<Identifier, String> addonJsons) {
        @Environment(EnvType.CLIENT)
        public static void syncCharacter(SyncCharacter message, ClientAccess access) {
            Character c = PersonalityMod.GSON.fromJson(message.characterJson, Character.class);

            if(!message.addonJsons.isEmpty()) {
                message.addonJsons.forEach((addonId, s) -> {
                    Class<BaseAddon> addonClass = AddonRegistry.INSTANCE.getAddonClass(addonId);

                    if(addonClass != null) {
                        c.characterAddons.put(addonId, PersonalityMod.GSON.fromJson(s, addonClass));
                    } else {
                        LOGGER.warn("[SyncCharacterPacket]: The given Identifier [{}] wasn't found within the AddonRegistry meaning it wasn't able to deserialize the info meaning such will be skipped.", addonId);
                    }
                });
            } else {
                Character oldCharacter = ClientCharacters.INSTANCE.getCharacter(c.getUUID());

                if(oldCharacter != null) c.characterAddons.putAll(oldCharacter.characterAddons);
            }

            ClientCharacters.INSTANCE.characterLookupMap().put(c.getUUID(), c);
        }
    }

    public record SyncAddonData(String characterUUID, Map<Identifier, String> addonJsons){

        @Environment(EnvType.CLIENT)
        public static void syncAddons(SyncAddonData message, ClientAccess access) {
            Character c = ClientCharacters.INSTANCE.getCharacter(message.characterUUID());

            if(c == null){
                LOGGER.error("[SyncAddons] It seems that there was no Character [UUID: {}] to sync addons with, such will be ignored.", message.characterUUID());

                return;
            }

            message.addonJsons.forEach((addonId, addonJson) -> {
                Class<BaseAddon> addonClass = AddonRegistry.INSTANCE.getAddonClass(addonId);

                if(addonClass != null) {
                    c.characterAddons.put(addonId, PersonalityMod.GSON.fromJson(addonJson, addonClass));
                } else {
                    LOGGER.warn("[SyncAddons]: The given Identifier [{}] wasn't found within the AddonRegistry meaning it wasn't able to deserialize the info meaning such will be skipped.", addonId);
                }
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
