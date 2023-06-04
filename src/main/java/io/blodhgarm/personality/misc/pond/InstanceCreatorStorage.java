package io.blodhgarm.personality.misc.pond;

import io.blodhgarm.personality.utils.gson.ExtraTokenData;
import org.jetbrains.annotations.Nullable;

/**
 * This class acts as a way to implement extra information onto a given TypeToken for use within a InstanceCreator
 */
public interface InstanceCreatorStorage<T> {

    T setExtraData(ExtraTokenData data);

    @Nullable ExtraTokenData getExtraData();

    default boolean isEmpty(){
        return this.getExtraData() == null;
    }
}
