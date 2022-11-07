package io.blodhgarm.personality.api.client;

import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.api.addon.client.PersonalityScreenAddon;
import io.blodhgarm.personality.client.screens.CharacterScreenMode;
import io.blodhgarm.personality.client.screens.PersonalityCreationScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * A Class where all {@link PersonalityScreenAddon} implementations can register
 * the needed Factory to build the addon for the {@link PersonalityCreationScreen}
 */
public class PersonalityScreenAddonRegistry {

    public static Map<Identifier, AddonFactory<PersonalityScreenAddon>> ALL_SCREEN_ADDONS = new HashMap<>();

    /**
     * Method to which you can register the Addon Factory for your addon
     * @param addonId The Screen Addons Identifier
     * @param factory The Factory to build the Screen Addon
     */
    public static void registerScreenAddon(Identifier addonId, AddonFactory<PersonalityScreenAddon> factory){
        ALL_SCREEN_ADDONS.put(addonId, factory);
    }

    public interface AddonFactory<T extends PersonalityScreenAddon> {
        T buildAddon(CharacterScreenMode mode, @Nullable Character character, @Nullable PlayerEntity player);
    }
}
