package io.blodhgarm.personality.compat.trinkets;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketItem;
import dev.emi.trinkets.api.TrinketsApi;
import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.client.BlurryVisionShaderEffect;
import io.blodhgarm.personality.misc.PersonalityTags;
import io.wispforest.owo.registration.reflect.ItemRegistryContainer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.event.GameEvent;

public class TrinketsGlasses implements ItemRegistryContainer {

    public static void init(){
        GlassesTrinket trinket = new GlassesTrinket();

        TrinketsApi.registerTrinket(PersonalityMod.BASIC_GLASSES, trinket);

        PersonalityMod.BASIC_GLASSES.equipItem = trinket::equipItem;
    }

    @Environment(EnvType.CLIENT)
    public static void clientInit(){
        BlurryVisionShaderEffect.registerChecker(player -> {
            return TrinketsApi.getTrinketComponent(player)
                    .map(component -> component.isEquipped(stack -> stack.isIn(PersonalityTags.Items.VISION_GLASSES)))
                    .orElse(false);
        });
    }

    private static class GlassesTrinket implements Trinket {
        private boolean disableEquipSound = false;

        @Override
        public void onEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
            SoundEvent soundEvent = ((ArmorItem) stack.getItem()).getEquipSound();

            if (soundEvent == null || disableEquipSound){
                disableEquipSound = false;

                return;
            }

            entity.emitGameEvent(GameEvent.EQUIP);
            entity.playSound(soundEvent, 1.0F, 1.0F);
        }

        private boolean equipItem(PlayerEntity user, ItemStack stack){
            disableEquipSound = true;

            return TrinketItem.equipItem(user, stack);
        }
    }
}
