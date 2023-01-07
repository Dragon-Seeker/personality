package io.blodhgarm.personality.client;

import io.blodhgarm.personality.Networking;
import io.blodhgarm.personality.api.client.PersonalityScreenAddonRegistry;
import io.blodhgarm.personality.compat.origins.client.gui.OriginSelectionDisplayAddon;
import io.blodhgarm.personality.compat.pehkui.client.PehkuiScaleDisplayAddon;
import io.blodhgarm.personality.compat.trinkets.TrinketsGlasses;
import io.blodhgarm.personality.mixin.client.owo.PositioningMixin;
import io.wispforest.owo.ui.core.Positioning;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

public class PersonalityClient implements ClientModInitializer {

    public static Positioning.Type RELATIVE_WITHOUT_CHILD;

    @Override
    public void onInitializeClient() {
        Networking.registerNetworkingClient();
		ShaderEffectRenderCallback.EVENT.register(new BlurryVisionShaderEffect());

		KeyBindings.init();
        ClientTickEvents.END_WORLD_TICK.register(KeyBindings::processKeybindings);

        if(FabricLoader.getInstance().isModLoaded("trinkets")){
            TrinketsGlasses.init();
        }

        if(FabricLoader.getInstance().isModLoaded("origins")){
            PersonalityScreenAddonRegistry.registerScreenAddon(new Identifier("origins", "origin_selection_addon"), OriginSelectionDisplayAddon::new);
        }

        if(FabricLoader.getInstance().isModLoaded("pehkui")){
            PersonalityScreenAddonRegistry.registerScreenAddon(new Identifier("pehkui", "scale_selection_addon"), PehkuiScaleDisplayAddon::new);
        }
    }


    public static Positioning customRelative(int xPercent, int yPercent){
        return PositioningMixin.personality$invokeNewPosition(xPercent, yPercent, RELATIVE_WITHOUT_CHILD);
    }
}
