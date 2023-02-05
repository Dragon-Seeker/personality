package io.blodhgarm.personality.compat.trinkets;

import dev.emi.trinkets.api.TrinketItem;
import dev.emi.trinkets.api.TrinketsApi;
import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.client.BlurryVisionShaderEffect;
import io.blodhgarm.personality.misc.PersonalityTags;
import io.wispforest.owo.registration.reflect.FieldRegistrationHandler;
import io.wispforest.owo.registration.reflect.ItemRegistryContainer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

public class TrinketsGlasses implements ItemRegistryContainer {

    public static void init(){
        PersonalityMod.BASIC_GLASSES = new TrinketItem(new Item.Settings().maxCount(1).group(ItemGroup.TOOLS));
    }

    @Environment(EnvType.CLIENT)
    public static void clientInit(){
        BlurryVisionShaderEffect.registerChecker(player -> {
            return TrinketsApi.getTrinketComponent(player)
                    .map(component -> component.isEquipped(stack -> stack.isIn(PersonalityTags.VISION_GLASSES)))
                    .orElse(false);
        });
    }
}
