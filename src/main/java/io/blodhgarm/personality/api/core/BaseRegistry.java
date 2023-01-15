package io.blodhgarm.personality.api.core;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseRegistry {

    public static Map<Identifier, BaseRegistry> REGISTRIES = new HashMap<>();

    public BaseRegistry(){
        REGISTRIES.put(this.getRegistryId(), this);
    }

    @Nullable
    public static BaseRegistry getRegistry(Identifier registryId){
        return REGISTRIES.get(registryId);
    }

    public abstract List<Identifier> getRegisteredIds();

    public abstract Identifier getRegistryId();

    public Text getTranslation(){
        return Text.translatable(
                getRegistryId().toString()
                        .replace(":", ".") + ".name"
        );
    };
}
