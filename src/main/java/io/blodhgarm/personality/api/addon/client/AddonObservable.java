package io.blodhgarm.personality.api.addon.client;

/**
 * Simple interface to allow access to the Personality Addon Equipped Screen without Direct access
 */
public interface AddonObservable {

    boolean isAddonOpen(PersonalityScreenAddon addon);

    void pushScreenAddon(PersonalityScreenAddon screenAddon);
}
