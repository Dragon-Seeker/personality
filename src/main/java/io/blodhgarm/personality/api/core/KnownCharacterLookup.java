package io.blodhgarm.personality.api.core;

import io.blodhgarm.personality.api.character.BaseCharacter;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

public interface KnownCharacterLookup {

    void addKnownCharacter(String playerUUID, BaseCharacter character);

    void removeKnownCharacter(String playerUUID);

    @Nullable
    default BaseCharacter getKnownCharacter(PlayerEntity player){
        return getKnownCharacter(player.getUuid().toString());
    }

    @Nullable BaseCharacter getKnownCharacter(String UUID);
}
