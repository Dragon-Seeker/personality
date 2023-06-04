package io.blodhgarm.personality.client;

import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.item.WoodenCane;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class WalkingStickModel extends AnimatedGeoModel<WoodenCane> {
    private static final Identifier modelId = PersonalityMod.id("geo/cane.geo.json");
    private static final Identifier textureId = PersonalityMod.id("textures/items/cane_texture.png");
    private static final Identifier animationId = PersonalityMod.id("");

    @Override public Identifier getModelResource(WoodenCane object) {
        return modelId;
    }
    @Override public Identifier getTextureResource(WoodenCane object) {
        return textureId;
    }
    @Override public Identifier getAnimationResource(WoodenCane animatable) {
        return animationId;
    }
}
