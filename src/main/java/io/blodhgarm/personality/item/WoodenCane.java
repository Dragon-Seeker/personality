package io.blodhgarm.personality.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.Lazy;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.RawAnimation;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.core.manager.InstancedAnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class WoodenCane extends ToolItem implements IAnimatable {

    private final Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;

    public WoodenCane(Settings settings) {
        super(WALKING_STICK_MATERIAL, settings);

        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();

        builder.put(
                EntityAttributes.GENERIC_ATTACK_DAMAGE,
                new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Tool modifier", 1.5f + WALKING_STICK_MATERIAL.getAttackDamage(), EntityAttributeModifier.Operation.ADDITION)
        );

        builder.put(
                EntityAttributes.GENERIC_ATTACK_SPEED,
                new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Tool modifier", -1.8F, EntityAttributeModifier.Operation.ADDITION)
        );

        this.attributeModifiers = builder.build();
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.attributeModifiers : super.getAttributeModifiers(slot);
    }

    public static final ToolMaterial WALKING_STICK_MATERIAL = new ToolMaterial() {
        private final Lazy<Ingredient> repairIngredient = new Lazy<>(() -> Ingredient.fromTag(ItemTags.PLANKS));

        @Override public int getDurability() { return 300; }
        @Override public float getMiningSpeedMultiplier() { return 0; }
        @Override public float getAttackDamage() { return 0.5f; }
        @Override public int getMiningLevel() { return 2; }
        @Override public int getEnchantability() { return 20; }
        @Override public Ingredient getRepairIngredient() { return repairIngredient.get(); }
    };

    @Override
    public void registerControllers(AnimationData animationData) {
//        AnimationController<WoodenCane> controller = new AnimationController(this, "test", 1000, event -> {
//            if(event.isMoving()){
//                return PlayState.CONTINUE;
//            } else {
//                return PlayState.STOP;
//            }
//        });
//
//        AnimationBuilder builder = new AnimationBuilder();
//
//        builder.getRawAnimationList()
//                .add(RawAnimation)
//
//        controller.setAnimation(builder);
//
//
//        animationData.addAnimationController();
    }

    @Override
    public AnimationFactory getFactory() {
        return GeckoLibUtil.createFactory(this);
    }
}
