package io.blodhgarm.personality.utils.gson;

import com.google.gson.reflect.TypeToken;
import io.blodhgarm.personality.misc.pond.InstanceCreatorStorage;
import org.jetbrains.annotations.Nullable;

public class WrappedTypeToken<T> extends TypeToken<T> implements InstanceCreatorStorage<WrappedTypeToken<T>> {

    private ExtraTokenData data = null;

    @Override
    public WrappedTypeToken<T> setExtraData(ExtraTokenData data) {
        this.data = data;

        return this;
    }

    @Override
    public @Nullable ExtraTokenData getExtraData() {
        return data;
    }
}
