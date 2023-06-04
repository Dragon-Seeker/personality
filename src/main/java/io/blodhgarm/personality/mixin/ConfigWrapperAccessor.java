package io.blodhgarm.personality.mixin;

import io.wispforest.owo.config.ConfigWrapper;
import io.wispforest.owo.config.Option;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.lang.invoke.MethodHandle;
import java.util.Map;

@Mixin(ConfigWrapper.class)
public interface ConfigWrapperAccessor {
    @Invoker("collectFieldValues") void personality$collectFieldValues(Option.Key parent, Object instance, Map<Option.Key, Option.BoundField<Object>> fields) throws IllegalAccessException;
    @Invoker("invokePredicate") boolean personality$invokePredicate(MethodHandle predicate, Object value);
}
