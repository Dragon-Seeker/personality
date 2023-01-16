package io.blodhgarm.personality.api.core;

import io.blodhgarm.personality.api.BaseCharacter;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface KnownCharacterLookup {

    void addKnownCharacter(String playerUUID, BaseCharacter character);

    void removeKnownCharacter(String playerUUID);

    @Nullable
    default BaseCharacter getKnownCharacter(PlayerEntity player){
        return getKnownCharacter(player.getUuid().toString());
    }

    @Nullable BaseCharacter getKnownCharacter(String UUID);
}
