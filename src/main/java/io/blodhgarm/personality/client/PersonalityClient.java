package io.blodhgarm.personality.client;

import io.blodhgarm.personality.Networking;
import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.client.PersonalityScreenAddonRegistry;
import io.blodhgarm.personality.compat.origins.client.OriginsSupportLoader;
import io.blodhgarm.personality.compat.pehkui.client.PehkuiScaleDisplayAddon;
import io.blodhgarm.personality.misc.config.PersonalityConfigModel;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

public class PersonalityClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Networking.registerNetworkingClient();
		ShaderEffectRenderCallback.EVENT.register(new BlurryVisionShaderEffect());
        ClientTickEvents.END_WORLD_TICK.register(KeyBindings::processKeybindings);

        if(FabricLoader.getInstance().isModLoaded("origins")){
            PersonalityScreenAddonRegistry.registerScreenAddon(new Identifier("origins", "origin_selection_addon"), OriginsSupportLoader::addToPersonalityScreen);
        }

        if(FabricLoader.getInstance().isModLoaded("pehkui")){
            PersonalityScreenAddonRegistry.registerScreenAddon(new Identifier("pehkui", "scale_selection_addon"), PehkuiScaleDisplayAddon::new);
        }
    }

    public static boolean isDarkMode(){
        if(PersonalityMod.CONFIG.THEME_MODE() == PersonalityConfigModel.ThemeMode.SYSTEM){
            return PersonalityMod.detector.isDark();
        }

        return PersonalityMod.CONFIG.THEME_MODE() == PersonalityConfigModel.ThemeMode.DARK_MODE;
    }

    public static boolean guiScale4OrAbove(){
        return MinecraftClient.getInstance().options.getGuiScale().getValue() >= 4 || MinecraftClient.getInstance().options.getGuiScale().getValue() == 0;
    }
}
