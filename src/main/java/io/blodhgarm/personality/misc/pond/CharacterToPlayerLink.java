package io.blodhgarm.personality.misc.pond;

import io.blodhgarm.personality.api.character.BaseCharacter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public interface CharacterToPlayerLink {

    default PlayerEntity toggleOnlyCharacterName(boolean value){
        throw new IllegalStateException("Has yet to be overridden!");
    }

    default Text getChatDisplayName(boolean onlyCharacterName){
        throw new IllegalStateException("Has yet to be overridden!");
    }

    default @Nullable BaseCharacter getCharacter(){
        return getCharacter(true);
    }

    default @Nullable BaseCharacter getCharacter(boolean prioritizeCL){
        throw new IllegalStateException("Has yet to be overridden!");
    }

}
