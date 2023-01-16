package io.blodhgarm.personality.mixin.client;

import io.blodhgarm.personality.api.BaseCharacter;
import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.api.CharacterManager;
import io.blodhgarm.personality.client.ClientCharacters;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.network.message.MessageMetadata;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.dynamic.DynamicSerializableUuid;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Objects;
import java.util.UUID;

@Mixin(MessageHandler.class)
public abstract class MessageHandlerMixin {

    @Shadow @Nullable protected abstract PlayerListEntry getPlayerListEntry(UUID sender);

    @ModifyVariable(method = "onChatMessage", at = @At(value = "HEAD"), argsOnly = true)
    private MessageType.Parameters personality$adjustParameterForChat(MessageType.Parameters value, SignedMessage message){
        MessageMetadata messageMetadata = message.createMetadata();

        if(!messageMetadata.lacksSender()){
            final PlayerListEntry playerListEntry = this.getPlayerListEntry(messageMetadata.sender());

            if(playerListEntry != null){
                String playerUUID = DynamicSerializableUuid.getUuidFromProfile(playerListEntry.getProfile()).toString();

                BaseCharacter character = ClientCharacters.INSTANCE.getKnownCharacter(DynamicSerializableUuid.getUuidFromProfile(playerListEntry.getProfile()).toString());

                MutableText mutableText = null;

                if(character != null){
                    mutableText = Text.literal(character.getName());
                } else {
                    Character clientCharacter = CharacterManager.getClientCharacter();

                    if(clientCharacter != null) {
                        if (Objects.equals(clientCharacter.getUUID(), ClientCharacters.INSTANCE.getCharacterUUID(playerUUID))) {
                            mutableText = Text.literal(clientCharacter.getName());
                        } else {
                            mutableText = Text.literal("Obscured");
                        }
                    }
                }

                if(mutableText != null){
                    return new MessageType.Parameters(
                            value.type(),
                            value.name()
                                    .copy()
                                    .append(" | ")
                                    .append(mutableText),
                            value.targetName()
                    );
                }
            }
        }

        return value;
    }
}
