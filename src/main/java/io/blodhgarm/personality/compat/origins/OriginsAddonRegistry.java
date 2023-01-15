package io.blodhgarm.personality.compat.origins;

import com.mojang.logging.LogUtils;
import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.PersonalityEntrypoint;
import io.blodhgarm.personality.api.addon.AddonRegistry;
import io.blodhgarm.personality.api.addon.BaseAddon;
import io.blodhgarm.personality.api.reveal.InfoRevealLevel;
import io.blodhgarm.personality.api.reveal.InfoRevealRegistry;
import io.blodhgarm.personality.mixin.OriginLayerAccessor;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.origin.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OriginsAddonRegistry implements PersonalityEntrypoint {

    private static final ItemStack QUESTION_MARK_HEAD = new ItemStack(Items.PLAYER_HEAD);

    static {
        NbtCompound nbt = QUESTION_MARK_HEAD.getOrCreateNbt();

        //--------------------------

        NbtCompound name = new NbtCompound();
        name.putString("Text", "Question Mark");

        NbtCompound display = new NbtCompound();
        display.put("Name", name);

        nbt.put("display", display);

        //--------------------------

        NbtList idList = new NbtList();
        idList.addAll(Stream.of(-263118006,-1942141483,-1904681102,-1647256948).map(NbtInt::of).toList());

        NbtCompound texture1 = new NbtCompound();
        texture1.putString("Value", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjcwNWZkOTRhMGM0MzE5MjdmYjRlNjM5YjBmY2ZiNDk3MTdlNDEyMjg1YTAyYjQzOWUwMTEyZGEyMmIyZTJlYyJ9fX0=");

        NbtList textures = new NbtList();
        textures.add(texture1);

        NbtCompound properties = new NbtCompound();
        properties.put("textures", textures);

        NbtCompound skullOwner = new NbtCompound();
        skullOwner.put("Id", idList);
        skullOwner.put("Properties", properties);

        nbt.put("SkullOwner", skullOwner);

        //--------------------------

        /*
            display:{
                Name:"{\"text\":\"Question Mark\"}"
            },
            SkullOwner:{
                Id:[-263118006, -1942141483, -1904681102, -1647256948 ],
                Properties:{
                    textures:[
                        {
                            Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjcwNWZkOTRhMGM0MzE5MjdmYjRlNjM5YjBmY2ZiNDk3MTdlNDEyMjg1YTAyYjQzOWUwMTEyZGEyMmIyZTJlYyJ9fX0="
                        }
                    ]
                }
            }
         */
    }

    public static final Origin UNKNOWN = new Origin(PersonalityMod.id("unknown"), QUESTION_MARK_HEAD, Impact.NONE, -1, Integer.MAX_VALUE).setSpecial();

    //------------------------

    public static OriginsAddonRegistry INSTANCE = new OriginsAddonRegistry();

    private static final Logger LOGGER = LogUtils.getLogger();

    public <T extends BaseAddon> void addonRegistry(AddonRegistry<T> registry) {
        registry.registerDelayedAddon(delayedRegistery -> {
            Predicate<T> addonValidation = t -> {
                if (t instanceof OriginAddon originAddon) {
                    try {
                        OriginLayer layer = OriginLayers.getLayer(originAddon.getOriginLayerId());

                        if (Objects.equals(originAddon.getOriginId().getPath(), "random")) {
                            List<Identifier> randomOrigins = layer.getOrigins().stream()
                                    .filter(o -> !((OriginLayerAccessor) layer).personality$OriginsExcludedFromRandom().contains(o))
                                    .filter(id -> ((OriginLayerAccessor) layer).personality$DoesRandomAllowUnchoosable() || OriginRegistry.get(id).isChoosable())
                                    .collect(Collectors.toList());

                            Identifier origin = Origin.EMPTY.getIdentifier();

                            if (layer.isRandomAllowed() && randomOrigins.size() > 0) {
                                origin = randomOrigins.get(new Random().nextInt(randomOrigins.size()));
                            }

                            originAddon.setOrigin(origin, layer.getIdentifier());

                            return true;
                        } else if (layer.getOrigins().contains(originAddon.getOriginId())) {
                            return true;
                        }
                    } catch (IllegalArgumentException e) {
                        LOGGER.error("[OriginAddon] It seems that there was a issue attempting to validate a Personality Addon leading to addon being thrown out");
                    }
                }

                return false;
            };

            OriginLayers.getLayers().forEach(originLayer -> {
                Origin origin = getChoosableSortedOrigins(originLayer).get(0);

                registry.registerAddon(originLayer.getIdentifier(),
                    (Class<T>) OriginAddon.class,
                    () -> (T) new OriginAddon(origin.getIdentifier(), originLayer.getIdentifier()),
                    addonValidation
                );
            });
        });
    }

    @Override
    public void infoRevealRegistry(InfoRevealRegistry registry) {
        registry.registerDelayedInfoRevealing(revealRegistry -> {
            if(!OriginRegistry.contains(UNKNOWN)) OriginRegistry.register(UNKNOWN);

            OriginLayers.getLayers().forEach(layer -> {
                Identifier layerId = layer.getIdentifier();
                InfoRevealLevel level = InfoRevealLevel.TRUSTED;

                if(layerId.equals(new Identifier("origins-classes", "class"))) {
                    level = InfoRevealLevel.GENERAL;
                } else if(layerId.equals(new Identifier("origins", "origin"))) {
                    level = InfoRevealLevel.ASSOCIATE;
                }

                revealRegistry.registerValueForRevealing(level, layerId, () -> new OriginAddon(UNKNOWN.getIdentifier(), layerId));
            });
        });
    }

    public static List<Origin> getChoosableSortedOrigins(OriginLayer currentLayer) {
        return getChoosableSortedOrigins(currentLayer, true);
    }

    public static List<Origin> getChoosableSortedOrigins(OriginLayer currentLayer, boolean alwaysIncludeForRandom){
        List<Origin> originSelection = new ArrayList<>(10);

        List<Identifier> originIdentifiers = currentLayer.getOrigins().stream()
                .filter(o -> alwaysIncludeForRandom || !((OriginLayerAccessor) currentLayer).personality$OriginsExcludedFromRandom().contains(o))
                .filter(id -> ((OriginLayerAccessor) currentLayer).personality$DoesRandomAllowUnchoosable() || OriginRegistry.get(id).isChoosable())
                .collect(Collectors.toList());

        originIdentifiers.forEach(originId -> {
            Origin origin = OriginRegistry.get(originId);

            if(origin.isChoosable()) originSelection.add(origin);
        });

        originSelection.sort((a, b) -> {
            int impDelta = a.getImpact().getImpactValue() - b.getImpact().getImpactValue();
            return impDelta == 0 ? a.getOrder() - b.getOrder() : impDelta;
        });

        return originSelection;
    }
}
