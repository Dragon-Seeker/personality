package io.blodhgarm.personality.api.reveal;

import com.mojang.logging.LogUtils;
import io.blodhgarm.personality.api.core.DelayedRegistry;
import net.minecraft.util.Identifier;
import org.apache.commons.collections4.map.LinkedMap;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class InfoRevealRegistry extends DelayedRegistry {

    private static Logger LOGGER = LogUtils.getLogger();

    public static InfoRevealRegistry INSTANCE = new InfoRevealRegistry();

    public List<Consumer<InfoRevealRegistry>> DELAYED_REGISTERS = new ArrayList<>();

    public final LinkedMap<InfoRevealLevel, List<Identifier>> REGISTRY = new LinkedMap<>();

    private final Map<Identifier, ObfuscatedReplacement<?>> OBFUSCATED_REPLACEMENT = new HashMap<>();

    public InfoRevealRegistry(){
        super();

        Arrays.stream(InfoRevealLevel.values()).forEach(level -> REGISTRY.put(level, new ArrayList<>()));
    }

    public InfoRevealRegistry registerValueForRevealing(InfoRevealLevel level, Identifier valueId, ObfuscatedReplacement<?> replacementInfo){
        List<Identifier> valueForLevel = REGISTRY.get(level);

        boolean alreadyRegisteredRevealLevel = valueForLevel.contains(valueId);
        boolean alreadyExistingReplacement = OBFUSCATED_REPLACEMENT.containsKey(valueId);

        if(!alreadyRegisteredRevealLevel && !alreadyExistingReplacement){
            valueForLevel.add(valueId);
            OBFUSCATED_REPLACEMENT.put(valueId, replacementInfo);
        }

        if(alreadyRegisteredRevealLevel || alreadyExistingReplacement){
            LOGGER.error("[InfoRevealRegistry]: The given Value Identifier ({}) has already been registered, meaning such won't be registered again!", valueId);
        }

        return this;
    }

    public InfoRevealRegistry registerDelayedInfoRevealing(Consumer<InfoRevealRegistry> registerConsumer){
        DELAYED_REGISTERS.add(registerConsumer);

        return this;
    }

    @Override
    public void runDelayedRegistration() {
        DELAYED_REGISTERS.forEach(infoRevealLevelConsumer -> infoRevealLevelConsumer.accept(this));
    }

    public boolean showInformation(InfoRevealLevel level, Identifier identifier){
        if(REGISTRY.get(level).contains(identifier)) return true;

        int currentRevelLevel = REGISTRY.indexOf(level);

        for(int i = 0; i < currentRevelLevel; i++){
            if(REGISTRY.getValue(i).contains(identifier)) return true;
        }

        return false;
    }

    public <T> InfoRevealResult<T> defaultOrReplace(InfoRevealLevel level, Identifier identifier, T defaultValue){
        if(showInformation(level, identifier)) return new InfoRevealResult<T>(false, defaultValue);

        ObfuscatedReplacement<T> replacement = getReplacement(identifier);

        if(replacement == null) {
            LOGGER.error("[InfoRevealRegistry]: It seems that a Character Info [Id: {}] needed to Obfuscated but the Replacement was not found, meaning default info is shown!", identifier);

            return new InfoRevealResult<>(false, defaultValue);
        } else {
            return new InfoRevealResult<>(true, replacement.getReplacement());
        }
    }

    @Nullable
    public <T> ObfuscatedReplacement<T> getReplacement(Identifier identifier){
        return (ObfuscatedReplacement<T>) OBFUSCATED_REPLACEMENT.get(identifier);
    }

    public interface ObfuscatedReplacement<T> {
        T getReplacement();
    }

    @Override
    public Identifier getRegistryId() {
        return new Identifier(PersonalityMod.MODID, "info_reveal_registry");
    }

    @Override
    public List<Identifier> getRegisteredIds() {
        List<Identifier> registeredIds = new ArrayList<>();

        REGISTRY.forEach((level, identifiers) -> registeredIds.addAll(identifiers));

        return registeredIds;
    }
}
