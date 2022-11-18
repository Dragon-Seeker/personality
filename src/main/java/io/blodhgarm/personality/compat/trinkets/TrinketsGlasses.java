package io.blodhgarm.personality.compat.trinkets;

import dev.emi.trinkets.api.TrinketsApi;
import io.blodhgarm.personality.client.BlurryVisionShaderEffect;
import io.blodhgarm.personality.misc.PersonalityTags;

public class TrinketsGlasses {

    public static void init(){
        BlurryVisionShaderEffect.registerChecker(player -> {
            return TrinketsApi.getTrinketComponent(player)
                    .map(component -> component.isEquipped(stack -> stack.isIn(PersonalityTags.VISION_GLASSES)))
                    .orElse(false);
        });
    }
}
