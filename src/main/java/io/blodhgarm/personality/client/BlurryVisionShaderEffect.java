package io.blodhgarm.personality.client;

import io.blodhgarm.personality.Character;
import io.blodhgarm.personality.PersonalityMod;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.Identifier;

import static io.blodhgarm.personality.PersonalityMod.CONFIG;

public class BlurryVisionShaderEffect implements ShaderEffectRenderCallback {

    private static final MinecraftClient client = MinecraftClient.getInstance();

    private final ManagedShaderEffect blur = ShaderEffectManager.getInstance().manage(new Identifier("blur", "shaders/post/fade_in_blur.json"),
            shader -> shader.setUniformValue("Radius", CONFIG.NO_GLASSES_BLURRINESS.END_VALUE()));

    private float progress = 0F;

    @Override
    public void renderShaderEffects(float tickDelta) {
        if (client.player != null) {
            Character c = ClientCharacters.getCharacter(client.player);

            if (c != null && PersonalityMod.shouldGradualValue(CONFIG.NO_GLASSES_BLURRINESS, c)
                    && !client.player.getEquippedStack(EquipmentSlot.HEAD).isIn(PersonalityMod.VISION_GLASSES) && progress < getStrength(c))
                progress += 0.05;
            else if (progress > 0)
                progress -= 0.05;

            if (progress > 0)
                getStrength(c);
        }

        if (progress > 0) {
            blur.findUniform1f("Progress").set(progress);
            blur.render(tickDelta);
        }
    }

    private float getStrength(Character c) {
        if (c != null && PersonalityMod.shouldGradualValue(CONFIG.NO_GLASSES_BLURRINESS, c))
            return PersonalityMod.getGradualValue(CONFIG.NO_GLASSES_BLURRINESS, c) / CONFIG.NO_GLASSES_BLURRINESS.END_VALUE();
        return 1;
    }
}
