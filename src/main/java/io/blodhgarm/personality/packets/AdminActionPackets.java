package io.blodhgarm.personality.packets;

import com.mojang.logging.LogUtils;
import io.blodhgarm.personality.Networking;
import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.client.gui.CharacterViewMode;
import io.blodhgarm.personality.server.PrivilegeManager;
import io.blodhgarm.personality.server.ServerCharacters;
import io.wispforest.owo.network.ClientAccess;
import io.wispforest.owo.network.ServerAccess;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdminActionPackets {

    public static Logger LOGGER = LogUtils.getLogger();

    public record AssociateAction(String characterUUID, String playerUUID){
        public static void attemptAssociateAction(AssociateAction message, ServerAccess access){
            ServerPlayerEntity operator = access.player();

            if(!PrivilegeManager.getLevel("associate").test(operator)) return;

            //----------------------

            boolean success = ServerCharacters.INSTANCE.associateCharacterToPlayer(message.characterUUID(), message.playerUUID());

            String returnMessage = success ? "Player associated to selected character!" : "§cUnable to locate the selected character!";

            Networking.sendS2C(access.player(), new ServerCharacters.ReturnInformation(returnMessage, "associate", success));
        }
    }

    public record DisassociateAction(String UUID, boolean isCharacter){
        public static void attemptDisassociateAction(DisassociateAction message, ServerAccess access){
            ServerPlayerEntity operator = access.player();

            if(!PrivilegeManager.getLevel("disassociate").test(operator)) return;

            //-----------------------

            boolean success = ServerCharacters.INSTANCE.dissociateUUID(message.UUID, message.isCharacter) != null;

            String targetType = (message.isCharacter ? "Character" : "Player");

            String returnMessage = success
                    ? targetType + " disassociated!"
                    : "§cTargeted " + targetType + " was not found to be Associated to anything";

            Networking.sendS2C(access.player(), new ServerCharacters.ReturnInformation(returnMessage, "disassociate", success));
        }
    }

    public record EditAction(String characterUUID){
        public static void attemptEditAction(EditAction message, ServerAccess access){
            ServerPlayerEntity operator = access.player();

            if(!PrivilegeManager.getLevel("screen_edit_uuid").test(operator)) return;

            Character character = ServerCharacters.INSTANCE.getCharacter(message.characterUUID());

            String returnMessage;
            boolean success;

            if(character == null) {
                returnMessage = "Could not locate the Character though the given selection method";
                success = false;
            } else {
                Networking.sendS2C(access.player(), new OpenPersonalityScreenS2CPacket(CharacterViewMode.EDITING, character.getUUID(), true));

                returnMessage = "Opening Screen Editing Screen on client!";
                success = true;
            }

            Networking.sendS2C(access.player(), new ServerCharacters.ReturnInformation(returnMessage, "edit", success));
        }
    }

    public record CharacterBasedAction(List<String> characterUUID, String actionType, String playerUUID){

        public CharacterBasedAction(List<String> characterUUID, String actionType){
            this(characterUUID, actionType, "");
        }

        public static void attemptAction(CharacterBasedAction message, ServerAccess access){
            Networking.sendS2C(access.player(), ServerCharacters.INSTANCE.attemptActionOn(message.characterUUID(), message.actionType(), access.player(), message.playerUUID()));
        }
    }

}
