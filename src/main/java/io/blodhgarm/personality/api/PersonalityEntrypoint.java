package io.blodhgarm.personality.api;

import io.blodhgarm.personality.api.addon.AddonRegistry;
import io.blodhgarm.personality.api.addon.BaseAddon;
import io.blodhgarm.personality.api.reveal.InfoRevealRegistry;

/**
 * The given entrypoint allowing for the Registration of a given Addon Loader
 *
 * Key for fabric.mod.json is "personality"
 */
public interface PersonalityEntrypoint {

    <T extends BaseAddon> void addonRegistry(AddonRegistry<T> registry);

    void infoRevealRegistry(InfoRevealRegistry registry);
}
