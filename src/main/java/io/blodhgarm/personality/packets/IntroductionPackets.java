package io.blodhgarm.personality.packets;

import com.mojang.logging.LogUtils;
import io.blodhgarm.personality.api.BaseCharacter;
import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.api.CharacterManager;
import io.blodhgarm.personality.api.reveal.KnownCharacter;
import io.blodhgarm.personality.client.ClientCharacters;
import io.wispforest.owo.network.ClientAccess;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;

public class IntroductionPackets {

    private static Logger LOGGER = LogUtils.getLogger();

    public record UnknownIntroduction(String characterUUID){
        @Environment(EnvType.CLIENT)
        public static void unknownIntroduced(UnknownIntroduction message, ClientAccess access) {
            Character targetC = CharacterManager.getManger(access.player()).getCharacter(access.player());

            if(targetC == null) return;

            BaseCharacter c = targetC.getKnownCharacters().get(message.characterUUID);

            if (c == null) return;

            LOGGER.info("[IntroductionPackets] A new Character (Character Name: {}) was revealed to {}", c.getName(), targetC.getName());

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

            KnownCharacter c = targetC.getKnownCharacters().get(message.characterUUID);

            if (c == null) return;

            LOGGER.info("[IntroductionPackets] A already known Character (Character Name: {}) had more info revealed to {}", c.getWrappedCharacter().getName(), targetC.getName());

            MinecraftClient.getInstance().getToastManager()
                    .add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION,
                            Text.literal(c.getName() + " told more about themselves!"),
                            Text.literal("Maybe take a look about what they told you.")));
        }
    }





}
