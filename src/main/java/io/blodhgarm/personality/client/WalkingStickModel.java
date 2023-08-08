package io.blodhgarm.personality.client;

import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.item.WoodenCane;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class WalkingStickModel extends GeoModel<WoodenCane> {
    private static final Identifier modelId = PersonalityMod.id("geo/cane.geo.json");
    private static final Identifier textureId = PersonalityMod.id("textures/item/cane_texture.png");
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
