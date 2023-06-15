package io.blodhgarm.personality.api.reveal;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import io.blodhgarm.personality.PersonalityMod;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.loot.LootGsons;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;

import java.util.*;

public class InfoRevealLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {

    private static final Map<Identifier, InfoRevealLoadData> DATA_CACHED = new HashMap<>();

    public static Logger LOGGER = LogUtils.getLogger();

    private static final Gson GSON = LootGsons.getConditionGsonBuilder().create();

    public InfoRevealLoader() {
        super(GSON, "info_reveal");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        LOGGER.info("[InfoRevealLoader]: Starting Loading Process!");

        prepared.forEach((id, jsonData) -> {
            try {
                deserializeData(id, (JsonObject) jsonData);
            } catch (IllegalArgumentException | JsonParseException var10) {
                LOGGER.error("[InfoRevealLoader]: Parsing error loading Info Reveal Entry {}", id, var10);
            }
        });

        LOGGER.info("[InfoRevealLoader]: End of Loading Process!");
    }

    public static void deserializeData(Identifier resourceId, JsonObject dataObject){
        for (Map.Entry<String, JsonElement> entry : dataObject.asMap().entrySet()) {
            InfoLevel level;

            try {
                level = InfoLevel.valueOf(entry.getKey().toUpperCase());
            } catch (IllegalArgumentException e){
                LOGGER.error("[InfoRevealLoader]: The given Resource[{}] was found to have a incorrect level[{}]. Such will be skipped!", resourceId, entry.getKey());

                continue;
            }

            if(entry.getValue() instanceof JsonArray jsonArray) {
                jsonArray.forEach(jsonElement -> handlePrimitive(jsonElement, level, resourceId));
            } else if(!handlePrimitive(entry.getValue(), level, resourceId)){
                LOGGER.error("[InfoRevealLoader]: Something has gone wrong when parsing a InfoLevel Data and wasn't found to be valid: Resource[{}], Level:[{}]", resourceId, level);
            }
        }
    }

    public static boolean handlePrimitive(JsonElement element, InfoLevel selectedLevel, Identifier resourceId){
        if(element instanceof JsonPrimitive primitive && Identifier.tryParse(primitive.getAsString()) != null){
            DATA_CACHED.put(Identifier.tryParse(primitive.getAsString()), new InfoRevealLoadData(selectedLevel, resourceId));

            return true;
        }

        LOGGER.error("[InfoRevealLoader]: Something has gone wrong when parsing a InfoLevel Data and wasn't found to be valid JsonData: Resource[{}], Level:[{}]", resourceId, selectedLevel);

        return false;
    }

    public static void handleCachedData(InfoRevealRegistry instance){
        LOGGER.info("[InfoRevealLoader]: Starting Cache Processing!");

        instance.FINALIZED_REGISTRY.clear();

        Arrays.stream(InfoLevel.VALID_VALUES).forEach(level -> {
            instance.FINALIZED_REGISTRY.put(level, new ArrayList<>(instance.REGISTRY.get(level)));
        });

        for (Map.Entry<Identifier, InfoRevealLoadData> cacheEntry : DATA_CACHED.entrySet()) {
            Identifier id = cacheEntry.getKey();
            InfoRevealLoadData data = cacheEntry.getValue();

            if(!instance.getRegisteredIds().contains(id)){
                LOGGER.error("[InfoRevealLoader]: An Identifier[{}] was attempted to be registered to change what Level it belonged to but didn't exist within the registry! Resource[{}]", id, data.resourceId());

                continue;
            }

            if(instance.FINALIZED_REGISTRY.get(data.level()).contains(id)) continue;

            for (Map.Entry<InfoLevel, List<Identifier>> entry : instance.FINALIZED_REGISTRY.entrySet()) {
                boolean isCurrentLevel = entry.getKey().equals(data.level());

                if(entry.getValue().contains(id) && !isCurrentLevel) {
                    entry.getValue().remove(id);
                } else if(isCurrentLevel){
                    entry.getValue().add(id);
                }
            }
        }

        LOGGER.info("[InfoRevealLoader]: End of Cache Processing!");
    }

    @Override
    public Identifier getFabricId() {
        return PersonalityMod.id("info_reveal");
    }

    public record InfoRevealLoadData(InfoLevel level, Identifier resourceId){};
}
