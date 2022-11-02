package io.blodhgarm.personality.api.client;

import io.blodhgarm.personality.api.addon.client.PersonalityScreenAddon;

public interface AddonObservable {

    boolean isAddonOpen(PersonalityScreenAddon addon);

    void pushScreenAddon(PersonalityScreenAddon screenAddon);
}
