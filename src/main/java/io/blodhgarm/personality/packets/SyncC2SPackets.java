package io.blodhgarm.personality.packets;

import com.mojang.logging.LogUtils;
import io.blodhgarm.personality.Networking;
import io.blodhgarm.personality.api.addon.AddonRegistry;
import io.blodhgarm.personality.api.addon.BaseAddon;
import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.api.core.BaseRegistry;
import io.blodhgarm.personality.server.ServerCharacters;
import io.wispforest.owo.network.ServerAccess;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SyncC2SPackets {

    private static Logger LOGGER = LogUtils.getLogger();

    public record ModifyBaseCharacterData(String characterJson, List<String> elementsChanges) {
        public static void modifyCharacter(ModifyBaseCharacterData message, ServerAccess access) {
            Character modifiedData = ServerCharacters.INSTANCE.deserializeCharacter(message.characterJson);

            modifiedData.setCharacterManager(ServerCharacters.INSTANCE);

            if(message.elementsChanges != null) ServerCharacters.INSTANCE.logCharacterEditing(access.player(), modifiedData, message.elementsChanges);

            Character oldData = ServerCharacters.INSTANCE.getCharacter(modifiedData.getUUID());

            if(oldData != null){
                modifiedData.getAddons().putAll(oldData.getAddons());
            } else {
                LOGGER.warn("[ModifyBaseCharacterData]: It seems that this packet was sent to modify a Character but such was not found on the server! [uuid: {}]", modifiedData.getUUID());
            }

            ServerCharacters.INSTANCE.characterLookupMap().put(modifiedData.getUUID(), modifiedData);

            ServerCharacters.INSTANCE.sortCharacterLookupMap();

            ServerCharacters.INSTANCE.pushToSaveQueue(modifiedData);
        }
    }

    public record ModifyAddonData(String characterUUID, Map<Identifier, String> addonData, List<String> elementsChanges) {
        public static void modifyAddons(ModifyAddonData message, ServerAccess access) {
            Character c = ServerCharacters.INSTANCE.getCharacter(message.characterUUID);

            if(c == null){
                LOGGER.warn("[ModifyAddonData]: It seems that this packet was sent to modify a Addons for a Character but such was not found on the server! [uuid: {}]", message.characterUUID);

                return;
            }

            if(message.elementsChanges != null) ServerCharacters.INSTANCE.logCharacterEditing(access.player(), c, message.elementsChanges);

            Map<Identifier, BaseAddon> modifiedAddonsMap = AddonRegistry.INSTANCE.deserializesAddons(c, message.addonData, true);

            ServerCharacters.INSTANCE.saveAddonsForCharacter(c, modifiedAddonsMap, true);

            c.getAddons().putAll(modifiedAddonsMap);

            ServerCharacters.INSTANCE.attemptApplyAddonOnSave(c);

            //TODO: Should we reapply addons and how should such be handled for clients or not?
        }
    }

    public record ModifyEntireCharacter(String characterJson, String characterUUID, Map<Identifier, String> addonData, List<String> elementsChanges){
        public static void modifyEntireCharacter(ModifyEntireCharacter message, ServerAccess access){
            ModifyBaseCharacterData.modifyCharacter(new ModifyBaseCharacterData(message.characterJson(), message.elementsChanges()), access);
            ModifyAddonData.modifyAddons(new ModifyAddonData(message.characterUUID(), message.addonData(), null), access);
        }
    }

    public record NewCharacter(String characterJson, Map<Identifier, String> addonsData, boolean immediateAssociation) {
        public static void newCharacter(NewCharacter message, ServerAccess access) {
            Character c = ServerCharacters.INSTANCE.deserializeCharacter(message.characterJson);

            c.setCharacterManager(ServerCharacters.INSTANCE);

            if(!c.getPlayerUUID().equals(access.player().getUuidAsString())){
                //TODO: More handling for this or nah? Maybe ask Team about what they think
                LOGGER.warn("[New Character]: It seems that a Character was created on the Client and was found to be having mismatching Player uuid: [Character uuid: {}]", c.getUUID());
            }

            Map<Identifier, BaseAddon> addonMap = AddonRegistry.INSTANCE.deserializesAddons(c, message.addonsData, true);

            ServerCharacters.INSTANCE.characterLookupMap().put(c.getUUID(), c);

            ServerCharacters.INSTANCE.sortCharacterLookupMap();

            String characterData = ServerCharacters.INSTANCE.pushToSaveQueue(c, false);

            Map<Identifier, String> addonsData = ServerCharacters.INSTANCE.saveAddonsForCharacter(c, addonMap, false);

            c.getAddons().putAll(addonMap);

            //Should such matter if there are less addons within the servers' registry?
            if(characterData != null || addonsData.size() < AddonRegistry.INSTANCE.getRegisteredIds().size()) {
                Networking.sendToAll(new SyncS2CPackets.SyncCharacterData(characterData, addonsData));
            }

            ServerCharacters.INSTANCE.attemptApplyAddonOnSave(c);

            if(message.immediateAssociation){
                ServerCharacters.INSTANCE.associateCharacterToPlayer(c.getUUID(), access.player().getUuidAsString());
            }

            //TODO: Other New Character Stuff, like Associations. Maybe chat messages?
        }
    }

    @SuppressWarnings("unchecked")
    public record RegistrySync(Map<Identifier, DelayedRegistryData> registryData){

        public static RegistrySync of(Map<Identifier, BaseRegistry> registriesLoaded){
            return new RegistrySync(
                    Map.ofEntries(registriesLoaded.entrySet()
                            .stream()
                            .map(entry -> Map.entry(entry.getKey(), new DelayedRegistryData(entry.getKey(), entry.getValue().getRegisteredIds())))
                            .toArray(Map.Entry[]::new)
                    )
            );
        }

        public static void registriesSync(RegistrySync message, ServerAccess access){
            List<MutableText> registryMissingClient = new ArrayList<>();
            List<MutableText> registryMissingServer = new ArrayList<>();

            for (BaseRegistry serverRegistry : BaseRegistry.REGISTRIES.values()) {
                if(message.registryData().get(serverRegistry.getRegistryId()) == null) {
                    registryMissingClient.add(Text.literal(serverRegistry.getRegistryId().toString() + "\n"));
                }
            }

            for (DelayedRegistryData data : message.registryData().values()) {
                if(BaseRegistry.getRegistry(data.registryId) == null) {
                    registryMissingServer.add(Text.literal(data.registryId.toString() + "\n"));
                }
            }

            MutableText mainMissingText = Text.literal("The following Registries were found to be missing on the server: \n");

            boolean missingRegistries = false;

            MutableText clientRegistryMismatch = Text.empty()
                    .append(Text.literal("\nRegistries Client Missing: \n\n").formatted(Formatting.BOLD));

            if(!registryMissingClient.isEmpty()){
                for (MutableText missingClientText : registryMissingClient) clientRegistryMismatch.append(missingClientText.formatted().formatted(Formatting.DARK_RED));

                mainMissingText.append(clientRegistryMismatch);

                missingRegistries = true;
            }

            MutableText serverRegistryMismatch = Text.empty()
                    .append(Text.literal("\nRegistries Server Missing: \n\n").formatted(Formatting.BOLD));

            if(!registryMissingServer.isEmpty()){
                for (MutableText missingServerText : registryMissingServer) serverRegistryMismatch.append(missingServerText.formatted().formatted(Formatting.DARK_PURPLE));

                mainMissingText.append(serverRegistryMismatch);

                missingRegistries = true;
            }

            if(missingRegistries){
                access.netHandler().disconnect(mainMissingText);

                return;
            }

            //--------------------------------------------------

            List<MutableText> registryMismatchTexts = new ArrayList<>();

            for (BaseRegistry serverRegistry : BaseRegistry.REGISTRIES.values()) {
                DelayedRegistryData data = message.registryData().get(serverRegistry.getRegistryId());

                if(serverRegistry.getRegisteredIds().hashCode() != data.registryIds.hashCode()) {
                    MutableText mainMismatchText = Text.empty();

                    MutableText serverAddonMismatch = Text.empty().append(
                            Text.literal("Server ")
                                    .append(serverRegistry.getTranslation())
                                    .append(Text.literal(" Missing: \n"))
                                    .formatted(Formatting.BOLD)
                    );

                    MutableText clientAddonMismatch = Text.empty().append(
                            Text.literal("Client ")
                                    .append(serverRegistry.getTranslation())
                                    .append(Text.literal(" Missing: \n"))
                                    .formatted(Formatting.BOLD)
                    );

                    boolean mismatchDetected = false;

                    if(mismatchTextBuilder(serverRegistry.getRegisteredIds(), data.registryIds, serverAddonMismatch, Formatting.DARK_RED)){
                        mainMismatchText.append(serverAddonMismatch);

                        mismatchDetected = true;
                    }

                    if(mismatchTextBuilder(data.registryIds, serverRegistry.getRegisteredIds(), clientAddonMismatch, Formatting.DARK_PURPLE)){
                        mainMismatchText.append(clientAddonMismatch);

                        mismatchDetected = true;
                    }

                    if(mismatchDetected) registryMismatchTexts.add(mainMismatchText);
                }
            }

            if(!registryMismatchTexts.isEmpty()){
                MutableText mainMismatchText = Text.literal("Personality Addon Registry seems to contain a mismatch: \n\n");

                for (MutableText registryMismatchText : registryMismatchTexts) mainMismatchText.append(registryMismatchText);

                access.netHandler().disconnect(mainMismatchText);
            }
        }

        private static boolean mismatchTextBuilder(List<Identifier> primaryRegistryList, List<Identifier> secoundaryRegistryList, MutableText mismatchText, Formatting... formattings){
            MutableText missingIdentifiers = Text.empty();

            for (Identifier identifier : primaryRegistryList) {
                if (secoundaryRegistryList.contains(identifier)) continue;

                missingIdentifiers.append(identifier.toString() + "\n");
            }

            if (missingIdentifiers.getSiblings().isEmpty()) return false;

            mismatchText
                    .append(missingIdentifiers)
                    .append(Text.of("\n"))
                    .formatted(formattings);

            return true;
        }

    }

    public record DelayedRegistryData(Identifier registryId, List<Identifier> registryIds){};

}
