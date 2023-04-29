package io.blodhgarm.personality.api.core;

import io.blodhgarm.personality.api.character.BaseCharacter;
import io.blodhgarm.personality.api.reveal.KnownCharacter;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Main interface used on the Client to access and manipulate the KnownCharacter Cache.
 */
public interface KnownCharacterLookup {

    /**
     * Method used to add a known character to the clients storage
     *
     * @param playerUUID
     * @param character
     */
    void addKnownCharacter(String playerUUID, BaseCharacter character);

    /**
     * Method used to remove a known character to the clients storage
     *
     * @param playerUUID
     */
    void removeKnownCharacter(String playerUUID);

    /**
     * Method used to get the possible KnownCharacter linked to the given Player
     *
     * @param player Other Player
     * @return
     */
    @Nullable
    default BaseCharacter getKnownCharacter(PlayerEntity player){
        return getKnownCharacter(player.getUuid().toString());
    }

    /**
     * Method used to get the possible KnownCharacter linked to the given Player UUID
     *
     * @param UUID Players YYUD
     * @return
     */
    @Nullable BaseCharacter getKnownCharacter(String UUID);
}
