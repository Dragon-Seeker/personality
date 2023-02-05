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

    public record ModifyCharacter(String characterJson) {
        public static void modifyCharacter(ModifyCharacter message, ServerAccess access) {
            Character c = PersonalityMod.GSON.fromJson(message.characterJson, Character.class);

            ServerCharacters.INSTANCE.characterLookupMap().put(c.getUUID(), c);

            ServerCharacters.INSTANCE.sortCharacterLookupMap();

            ServerCharacters.INSTANCE.saveCharacter(c);
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

    public record RegistrySync(Identifier registryId, List<Identifier> registryIds){
        public static void registrySync(RegistrySync message, ServerAccess access){
            if(access.runtime().isHost(access.player().getGameProfile())) return;

            BaseRegistry serverRegistry = BaseRegistry.getRegistry(message.registryId());

            if(serverRegistry == null) {
                access.netHandler().disconnect(Text.of("A Registry sync was attempted and found the server lacking the given client registry! [RegistryId: " +  message.registryId() + "]"));

                return;
            }

            List<Identifier> serverRegistryIds = serverRegistry.getRegisteredIds();

            if(serverRegistryIds.hashCode() != message.registryIds().hashCode()){
                MutableText mainText = Text.literal("Personality Addon Registry seems to contain a mismatch: \n\n");

                MutableText serverAddonMismatch = Text.empty().append(
                        Text.literal("Server ")
                                .append(serverRegistry.getTranslation())
                                .append(Text.literal(" Missing: \n"))
                                .formatted(Formatting.BOLD)
                );
                AtomicBoolean appendToMainText1 = new AtomicBoolean(false);

                serverRegistryIds.forEach(identifier -> {
                    if(!message.registryIds().contains(identifier)){
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

                message.registryIds().forEach(identifier -> {
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

}
