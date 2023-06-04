package io.blodhgarm.personality.utils;

import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.api.character.CharacterManager;
import io.blodhgarm.personality.misc.pond.InstanceCreatorStorage;
import io.blodhgarm.personality.utils.gson.ExtraTokenData;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public class CharacterReferenceData implements ExtraTokenData {

    private final CharacterManager<? extends PlayerEntity, ? extends Character> manager;
    private final String uuid;

    public CharacterReferenceData(CharacterManager<? extends PlayerEntity, ? extends Character> manager, String uuid){
        this.manager = manager;
        this.uuid = uuid;
    }

    public Character getCharacter(){
        return this.manager.getCharacter(uuid);
    }

    @Nullable
    public static Character attemptGetCharacter(Type type){
        if(type instanceof InstanceCreatorStorage<?> storage && !storage.isEmpty() && storage.getExtraData() instanceof CharacterReferenceData data){
            return data.getCharacter();
        }

        return null;
    }
}
