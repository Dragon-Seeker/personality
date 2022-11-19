package io.blodhgarm.personality.client;

import com.google.common.collect.Lists;
import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.misc.PersonalityTags;
import io.blodhgarm.personality.misc.config.ConfigHelper;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;

import static io.blodhgarm.personality.PersonalityMod.CONFIG;

public class BlurryVisionShaderEffect implements ShaderEffectRenderCallback {

    private static final MinecraftClient client = MinecraftClient.getInstance();

    private static List<GlassesCheck> GLASSES_CHECKERS = Lists.newArrayList(player -> {
        return client.player.getEquippedStack(EquipmentSlot.HEAD).isIn(PersonalityTags.VISION_GLASSES);
    });

    private final ManagedShaderEffect blur = ShaderEffectManager.getInstance().manage(new Identifier("blur", "shaders/post/fade_in_blur.json"),
            shader -> shader.setUniformValue("Radius", CONFIG.NO_GLASSES_BLURRINESS.END_VALUE()));

    private float progress = 0F;

    public static void registerChecker(GlassesCheck checker){
        GLASSES_CHECKERS.add(checker);
    }

    @Override
    public void renderShaderEffects(float tickDelta) {
        if (client.player != null) {
            Character c = ClientCharacters.INSTANCE.getCharacter(client.player);

            if (ConfigHelper.shouldApply(CONFIG.NO_GLASSES_BLURRINESS, c) && progress < getStrength(c) && !hasGlasses())
                progress += 0.05;
            else if (progress > 0)
                progress -= 0.05;
        }

        if (progress > 0) {
            blur.findUniform1f("Progress").set(progress);
            blur.render(tickDelta);
        }
    }

    private float getStrength(Character c) {
        return ConfigHelper.apply(CONFIG.NO_GLASSES_BLURRINESS, c) / CONFIG.NO_GLASSES_BLURRINESS.END_VALUE();
    }

    private boolean hasGlasses() {
        for(GlassesCheck checker : GLASSES_CHECKERS){
            if(checker.hasGlasses(client.player)) return true;
        }

        return false;
    }

    public interface GlassesCheck {
        boolean hasGlasses(PlayerEntity player);
    }
}
