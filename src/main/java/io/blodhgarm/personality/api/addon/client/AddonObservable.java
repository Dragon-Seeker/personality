package io.blodhgarm.personality.api.addon.client;

import io.blodhgarm.personality.api.addon.client.PersonalityScreenAddon;

/**
 * Simple interface to allow access to the Personality Addon Equipped Screen without Direct access
 */
public interface AddonObservable {

    boolean isAddonOpen(PersonalityScreenAddon addon);

    void pushScreenAddon(PersonalityScreenAddon screenAddon);
}
