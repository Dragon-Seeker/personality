package io.blodhgarm.personality.misc.pond;

import io.blodhgarm.personality.api.BaseCharacter;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

public interface CharacterToPlayerLink<T extends PlayerEntity> {

    @Nullable BaseCharacter getCharacter();

    void setCharacter(BaseCharacter character);

}
