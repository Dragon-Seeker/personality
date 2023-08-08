package io.blodhgarm.personality.item;

import io.blodhgarm.personality.misc.PersonalitySoundEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.function.BiPredicate;

public class GlassesItem extends ArmorItem {

    public BiPredicate<PlayerEntity, ItemStack> equipItem = (p, s) -> false;

    public GlassesItem(Settings settings) {
        super(GLASSES_MATERIAL, Type.HELMET, settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        return (equipItem.test(user, stack))
                ? TypedActionResult.success(stack, world.isClient())
                : super.use(world, user, hand);
    }

    public static final ArmorMaterial GLASSES_MATERIAL = new ArmorMaterial() {
        @Override public int getDurability(ArmorItem.Type type) { return 0; }
        @Override public int getProtection(ArmorItem.Type type) { return 1; }
        @Override public int getEnchantability() { return 20; }
        @Override public SoundEvent getEquipSound() { return PersonalitySoundEvents.ITEM_GLASSES_EQUIP; }
        @Override public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
        @Override public String getName() { return "personality:glasses"; }
        @Override public float getToughness() { return 0; }
        @Override public float getKnockbackResistance() { return 0; }
    };
}
