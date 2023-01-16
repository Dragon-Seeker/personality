package io.blodhgarm.personality.api;

import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

public record PlayerAccess<P extends PlayerEntity>(String UUID, @Nullable P player) {

    public PlayerAccess(P player) {
        this(player.getUuid().toString(), player);
    }

    public static PlayerAccess<PlayerEntity> EMPTY = new PlayerAccess<>("INVALID", null);

    public boolean valid() {
        return !this.equals(EMPTY);
    }
}
