package io.blodhgarm.personality.packets;

import io.blodhgarm.personality.api.BaseCharacter;
import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.api.CharacterManager;
import io.blodhgarm.personality.client.ClientCharacters;
import io.wispforest.owo.network.ClientAccess;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class IntroductionPackets {

    public record UnknownIntroduction(String characterUUID){
        @Environment(EnvType.CLIENT)
        public static void unknownIntroduced(UnknownIntroduction message, ClientAccess access) {
            Character targetC = CharacterManager.getManger(access.player()).getCharacter(access.player());

            if(targetC == null) return;

            BaseCharacter c = targetC.getKnownCharacters().get(message.characterUUID);

            if (c == null) return;

            MinecraftClient.getInstance().getToastManager()
                    .add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION,
                            Text.literal(c.getName() + " introduced themselves for the first time!"),
                            Text.literal("Maybe take a look about what they told you.")));
        }
    }

    public record UpdatedKnowledge(String characterUUID){
        @Environment(EnvType.CLIENT)
        public static void updatedKnowledge(UpdatedKnowledge message, ClientAccess access) {
            Character targetC = CharacterManager.getManger(access.player()).getCharacter(access.player());

            if(targetC == null) return;

            BaseCharacter c = targetC.getKnownCharacters().get(message.characterUUID);

            if (c == null) return;

            MinecraftClient.getInstance().getToastManager()
                    .add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION,
                            Text.literal(c.getName() + " told more about themselves!"),
                            Text.literal("Maybe take a look about what they told you.")));
        }
    }





}
