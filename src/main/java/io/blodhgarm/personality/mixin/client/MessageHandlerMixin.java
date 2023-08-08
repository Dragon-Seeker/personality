package io.blodhgarm.personality.mixin.client;

import com.mojang.authlib.GameProfile;
import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.character.BaseCharacter;
import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.client.ClientCharacters;
import io.blodhgarm.personality.utils.Constants;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Uuids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Objects;
import java.util.UUID;

@Mixin(MessageHandler.class)
public abstract class MessageHandlerMixin {

    @ModifyVariable(method = "onChatMessage", at = @At(value = "HEAD"), argsOnly = true)
    private MessageType.Parameters personality$adjustParameterForChat(MessageType.Parameters value, @NotNull SignedMessage message, GameProfile sender){
        if(!message.isSenderMissing()){
            String playerUUID = Uuids.getUuidFromProfile(sender).toString();

            Character clientCharacter = ClientCharacters.INSTANCE.getClientCharacter();

            if(clientCharacter != null) {
                BaseCharacter otherCharacter = clientCharacter.getKnownCharacter(playerUUID, true);

                MutableText mutableText;

                if (otherCharacter != null) {
                    mutableText = Text.literal(otherCharacter.getName());
                } else {
                    mutableText = Text.literal(
                            Objects.equals(clientCharacter.getUUID(), ClientCharacters.INSTANCE.getCharacterUUID(playerUUID))
                                    ? clientCharacter.getName()
                                    : "Obscured"
                    );
                }

                mutableText = Text.literal("*")
                        .append(mutableText);

                if (PersonalityMod.CONFIG.showPlayerNameInChat()) {
                    mutableText = mutableText.formatted(Constants.CHARACTER_FORMATTING);

                    mutableText = value.name()
                            .copy()
                            .append(Text.literal(" | ").formatted(Formatting.WHITE))
                            .append(mutableText);
                } else {
                    mutableText.setStyle(value.name().getStyle());

                    mutableText = mutableText.formatted(Constants.CHARACTER_FORMATTING);
                }

                return new MessageType.Parameters(
                        value.type(),
                        mutableText,
                        value.targetName()
                );
            }
        }

        return value;
    }
}
