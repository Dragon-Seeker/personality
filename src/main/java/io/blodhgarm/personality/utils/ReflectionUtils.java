package io.blodhgarm.personality.utils;

import io.wispforest.owo.ui.component.EntityComponent;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.apache.commons.collections4.map.ListOrderedMap;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.List;

public class ReflectionUtils {

    private static final VarHandle INSERT_ORDER_ACCESS;

    private static final Class<?> renderablePlayerEntityClass;

    private static final VarHandle renderablePlayerEntity$skinTextureId;
    private static final VarHandle renderablePlayerEntity$model;

    static {
        try {
            Field field = ListOrderedMap.class.getDeclaredField("insertOrder");

            field.setAccessible(true);

            INSERT_ORDER_ACCESS = MethodHandles.privateLookupIn(ListOrderedMap.class, MethodHandles.lookup()).unreflectVarHandle(field);
        }
        catch (NoSuchFieldException | SecurityException exception){ throw new RuntimeException("Attempting to get the Field [insertOrder] within [ListOrderedMap] has failed!", exception); }
        catch (IllegalAccessException exception){ throw new RuntimeException("Attempting to get the Field [insertOrder] unreflected VarHandle within [ListOrderedMap] has failed!", exception); }

        try {
            Class<?> clazz = EntityComponent.class.getDeclaredClasses()[0];

            Field field1 = clazz.getDeclaredField("skinTextureId");
            Field field2 = clazz.getDeclaredField("model");

            field1.setAccessible(true);
            field2.setAccessible(true);

            renderablePlayerEntity$skinTextureId = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup()).unreflectVarHandle(field1);
            renderablePlayerEntity$model = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup()).unreflectVarHandle(field2);

            renderablePlayerEntityClass = clazz;
        }
        catch (NoSuchFieldException | SecurityException exception){ throw new RuntimeException("Attempting to get the Field [skinTextureId or model] within [RenderablePlayerEntity] has failed!", exception); }
        catch (IllegalAccessException exception){ throw new RuntimeException("Attempting to get the Field [skinTextureId or model] unreflected VarHandle within [RenderablePlayerEntity] has failed!", exception); }
    }

    public static <K, V> List<K> getMapInsertOrder(ListOrderedMap<K, V> map){
        return ((List<K>)INSERT_ORDER_ACCESS.get(map));
    }

    public static <E extends Entity> E editRenderablePlayerEntity(E playerEntity, Identifier id, String model){
        if(renderablePlayerEntityClass.isInstance(playerEntity)){
            renderablePlayerEntity$skinTextureId.set(playerEntity, id);
            renderablePlayerEntity$model.set(playerEntity, model);
        }

        return playerEntity;
    }

}
