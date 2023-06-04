package io.blodhgarm.personality.api.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Predicate;

/**
 * Helper Record that is useful to interact with Player data
 * @param uuid
 * @param player
 * @param <P>
 */
public record PlayerAccess<P extends PlayerEntity>(String uuid, @Nullable P player) {

    public static PlayerAccess<PlayerEntity> EMPTY = new PlayerAccess<>("INVALID", null);

    public PlayerAccess(P player) {
        this(player.getUuid().toString(), player);
    }

    public boolean isNotEmpty() {
        return !this.isEmpty();
    }

    public boolean isEmpty() {
        return this.equals(EMPTY);
    }

    public boolean playerValid(){
        return playerValid(p -> true);
    }

    public boolean playerValid(Predicate<P> check){
        return this.isNotEmpty() && player() != null && check.test(player());
    }

    public GameProfile getProfile(MinecraftSessionService sessionService){
        if(player != null) return player.getGameProfile();

        return sessionService.fillProfileProperties(getProfile(), false);
    }

    public GameProfile getProfile(){
        if(player != null) return player.getGameProfile();

        return new GameProfile(UUID.fromString(uuid()), "");
    }
}
