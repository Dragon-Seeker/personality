package io.blodhgarm.personality.packets;

import io.blodhgarm.personality.Character;
import io.blodhgarm.personality.client.ClientCharacters;
import io.wispforest.owo.network.ClientAccess;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;

public record IntroductionPacket(String characterUUID) {

    @Environment(EnvType.CLIENT)
    public static void beenIntroduced(IntroductionPacket message, ClientAccess access) {
        Character c = ClientCharacters.getCharacter(message.characterUUID);
        if (c == null)
            return;

        MinecraftClient.getInstance().getToastManager()
                .add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION,
                        Text.literal(c.getName() + " introduced themselves"),
                        Text.literal("Say hi!")));
    }

}
