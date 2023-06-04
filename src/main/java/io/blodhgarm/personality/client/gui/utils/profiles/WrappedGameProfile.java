package io.blodhgarm.personality.client.gui.utils.profiles;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import io.blodhgarm.personality.utils.Constants;

import java.util.UUID;

public class WrappedGameProfile extends GameProfile {
    protected GameProfile wrappedProfile;

    public WrappedGameProfile(UUID id, String name) {
        super(Constants.NONE_PROFILE.getId(), Constants.NONE_PROFILE.getName());

        this.wrappedProfile = new GameProfile(id, name);
    }

    public WrappedGameProfile(GameProfile wrappedProfile) {
        super(Constants.NONE_PROFILE.getId(), Constants.NONE_PROFILE.getName());

        this.wrappedProfile = wrappedProfile;
    }

    public WrappedGameProfile setProfile(GameProfile wrappedProfile) {
        this.wrappedProfile = wrappedProfile;

        return this;
    }

    @Override
    public UUID getId() {
        return wrappedProfile.getId();
    }

    @Override
    public String getName() {
        return wrappedProfile.getName();
    }

    @Override
    public PropertyMap getProperties() {
        return wrappedProfile.getProperties();
    }

    @Override
    public boolean isComplete() {
        return wrappedProfile.isComplete();
    }

    @Override
    public boolean equals(Object o) {
        return wrappedProfile.equals(o);
    }

    @Override
    public int hashCode() {
        return wrappedProfile.hashCode();
    }

    @Override
    public String toString() {
        return wrappedProfile.toString();
    }

    @Override
    public boolean isLegacy() {
        return wrappedProfile.isLegacy();
    }
}
