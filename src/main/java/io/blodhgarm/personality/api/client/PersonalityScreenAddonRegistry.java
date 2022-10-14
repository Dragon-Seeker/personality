package io.blodhgarm.personality.api.client;

import io.blodhgarm.personality.api.addon.client.PersonalityScreenAddon;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PersonalityScreenAddonRegistry {

    public static Map<Identifier, Function<ClientPlayerEntity, PersonalityScreenAddon>> ALL_SCREEN_ADDONS = new HashMap<>();

    public static void registerScreenAddon(Identifier identifier, Function<ClientPlayerEntity, PersonalityScreenAddon> addon){
        ALL_SCREEN_ADDONS.put(identifier, addon);
    }
}
