package io.blodhgarm.personality.packets;

import com.mojang.logging.LogUtils;
import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.api.addon.AddonRegistry;
import io.blodhgarm.personality.api.core.BaseRegistry;
import io.blodhgarm.personality.server.ServerCharacters;
import io.wispforest.owo.network.ServerAccess;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SyncC2SPackets {

    private static Logger LOGGER = LogUtils.getLogger();

    public record ModifyBaseCharacterData(String characterJson, List<String> elementsChanges) {
        public static void modifyCharacter(ModifyBaseCharacterData message, ServerAccess access) {
            Character modifiedData = PersonalityMod.GSON.fromJson(message.characterJson, Character.class);

            if(message.elementsChanges != null) ServerCharacters.INSTANCE.logCharacterEditing(access.player(), modifiedData, message.elementsChanges);

            Character oldData = ServerCharacters.INSTANCE.getCharacter(modifiedData.getUUID());

            if(oldData != null){
                modifiedData.getAddons().putAll(oldData.getAddons());
            } else {
                LOGGER.warn("[ModifyBaseCharacterData]: It seems that this packet was sent to modify a Character but such was not found on the server! [UUID: {}]", modifiedData.getUUID());
            }

            ServerCharacters.INSTANCE.characterLookupMap().put(modifiedData.getUUID(), modifiedData);

            ServerCharacters.INSTANCE.sortCharacterLookupMap();

            ServerCharacters.INSTANCE.saveCharacter(modifiedData);
        }
    }

    public record ModifyAddonData(String characterUUID, Map<Identifier, String> addonData, List<String> elementsChanges) {
        public static void modifyAddons(ModifyAddonData message, ServerAccess access) {
            Character c = ServerCharacters.INSTANCE.getCharacter(message.characterUUID);

            if(c == null){
                LOGGER.warn("[ModifyAddonData]: It seems that this packet was sent to modify a Addons for a Character but such was not found on the server! [UUID: {}]", message.characterUUID);

                return;
            }

            if(message.elementsChanges != null) ServerCharacters.INSTANCE.logCharacterEditing(access.player(), c, message.elementsChanges);

            AddonRegistry.INSTANCE.deserializesAddons(c, message.addonData);

            ServerCharacters.INSTANCE.saveAddonsForCharacter(c, true);

            //TODO: Should we reapply addons and how should such be handled for clients or not?
        }
    }

    public record ModifyEntireCharacter(String characterJson, String characterUUID, Map<Identifier, String> addonData, List<String> elementsChanges){
        public static void modifyEntireCharacter(ModifyEntireCharacter message, ServerAccess access){
            ModifyBaseCharacterData.modifyCharacter(new ModifyBaseCharacterData(message.characterJson(), message.elementsChanges()), access);
            ModifyAddonData.modifyAddons(new ModifyAddonData(message.characterUUID(), message.addonData(), null), access);
        }
    }

    public record NewCharacter(String characterJson, Map<Identifier, String> addonData, boolean immediateAssociation) {
        public static void newCharacter(NewCharacter message, ServerAccess access) {


            Character c = PersonalityMod.GSON.fromJson(message.characterJson, Character.class);

            if(!c.getPlayerUUID().equals(access.player().getUuidAsString())){
                //TODO: More handling for this or nah? Maybe ask Team about what they think
                LOGGER.warn("[New Character]: It seems that a Character was created on the Client and was found to be having mismatching Player UUID: [Character UUID: {}]", c.getUUID());
            }

            AddonRegistry.INSTANCE.deserializesAddons(c, message.addonData);

            ServerCharacters.INSTANCE.characterLookupMap().put(c.getUUID(), c);

            ServerCharacters.INSTANCE.sortCharacterLookupMap();

            ServerCharacters.INSTANCE.saveCharacter(c);

            ServerCharacters.INSTANCE.saveAddonsForCharacter(c, true);

            if(message.immediateAssociation){
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

    public record RegistrySync(List<DelayedRegistryData> registryData){

        public RegistrySync(Map<Identifier, BaseRegistry> registriesLoaded){
            this(registriesLoaded.entrySet().stream().map(entry -> new DelayedRegistryData(entry.getKey(), entry.getValue().getRegisteredIds())).toList());
        }

        public static void registriesSync(RegistrySync message, ServerAccess access){
            message.registryData().forEach(data -> {
                registrySync(data.registryId, data.registryIds, access);
            });
        }

        public static void registrySync(Identifier registryId, List<Identifier> registryIds,  ServerAccess access){
            if(access.runtime().isHost(access.player().getGameProfile())) return;

            BaseRegistry serverRegistry = BaseRegistry.getRegistry(registryId);

            if(serverRegistry == null) {
                access.netHandler().disconnect(Text.of("A Registry sync was attempted and found the server lacking the given client registry! [RegistryId: " +  registryId + "]"));

                return;
            }

            List<Identifier> serverRegistryIds = serverRegistry.getRegisteredIds();

            if(serverRegistryIds.hashCode() != registryIds.hashCode()){
                MutableText mainText = Text.literal("Personality Addon Registry seems to contain a mismatch: \n\n");

                MutableText serverAddonMismatch = Text.empty().append(
                        Text.literal("Server ")
                                .append(serverRegistry.getTranslation())
                                .append(Text.literal(" Missing: \n"))
                                .formatted(Formatting.BOLD)
                );
                AtomicBoolean appendToMainText1 = new AtomicBoolean(false);

                serverRegistryIds.forEach(identifier -> {
                    if(!registryIds.contains(identifier)){
                        serverAddonMismatch.append(identifier.toString() + "\n");

                        appendToMainText1.set(true);
                    }
                });

                if(appendToMainText1.get()) mainText.append(
                        serverAddonMismatch
                                .append(Text.of("\n"))
                                .formatted(Formatting.DARK_RED)
                );

                MutableText clientAddonMismatch = Text.empty().append(
                        Text.literal("Client ")
                                .append(serverRegistry.getTranslation())
                                .append(Text.literal(" Missing: \n"))
                                .formatted(Formatting.BOLD)
                );
                AtomicBoolean appendToMainText2 = new AtomicBoolean(false);

                registryIds.forEach(identifier -> {
                    if(!serverRegistryIds.contains(identifier)){
                        clientAddonMismatch.append(identifier.toString() + "\n");

                        appendToMainText2.set(true);
                    }
                });

                if(appendToMainText2.get()) mainText.append(
                        clientAddonMismatch
                                .formatted(Formatting.DARK_PURPLE)
                );

                access.netHandler().disconnect(mainText);
            }
        }
    }

    public record DelayedRegistryData(Identifier registryId, List<Identifier> registryIds){};

}
