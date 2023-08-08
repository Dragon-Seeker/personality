package io.blodhgarm.personality.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.blodhgarm.personality.client.WalkingStickModel;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Lazy;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class WoodenCane extends ToolItem implements GeoAnimatable, GeoItem {
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
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
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return GeckoLibUtil.createInstanceCache(this);
    }

    @Override
    public double getTick(Object object) {
        return 0;
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private GeoItemRenderer<WoodenCane> renderer;

            @Override
            public BuiltinModelItemRenderer getCustomRenderer() {
                if (this.renderer == null) this.renderer = new GeoItemRenderer<>(new WalkingStickModel());

                return this.renderer;
            }
        });
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return this.renderProvider;
    }
}
