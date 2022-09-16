package io.blodhgarm.personality.client;

import io.blodhgarm.personality.Character;
import io.blodhgarm.personality.PersonalityMod;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.Identifier;

public class BlurryVisionShaderEffect implements ShaderEffectRenderCallback {

    private static final MinecraftClient client = MinecraftClient.getInstance();

    private final ManagedShaderEffect blur = ShaderEffectManager.getInstance().manage(new Identifier("blur", "shaders/post/fade_in_blur.json"),
            shader -> shader.setUniformValue("Radius", 8F));

    private float progress = 0F;

    @Override
    public void renderShaderEffects(float tickDelta) {
        if (client.player != null) {
            Character c = ClientCharacters.getCharacter(client.player);

            if (c != null && c.getStage() == Character.Stage.OLD
                    && !client.player.getEquippedStack(EquipmentSlot.HEAD).isIn(PersonalityMod.VISION_GLASSES) && progress < 1)
                progress += 0.05;
            else if (progress > 0)
                progress -= 0.05;
        }

        if (progress > 0) {
            blur.findUniform1f("Progress").set(progress);
            blur.render(tickDelta);
        }
    }
}
