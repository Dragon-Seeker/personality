package io.blodhgarm.personality.packets;

import io.blodhgarm.personality.Character;
import io.blodhgarm.personality.client.ClientCharacters;
import io.wispforest.owo.network.ServerAccess;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;

public record IntroductionPacket(String characterUUID) {

    @Environment(EnvType.CLIENT)
    public static void beenIntroduced(IntroductionPacket message, ServerAccess access) {
        Character c = ClientCharacters.getCharacter(message.characterUUID);
        if (c == null)
            return;

        MinecraftClient.getInstance().getToastManager()
                .add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION,
                        Text.literal(c.getName() + "introduces themselves"),
                        Text.literal("Example Text")));
    }

}
