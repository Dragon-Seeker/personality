package io.blodhgarm.personality.api;

import io.blodhgarm.personality.api.addon.AddonRegistry;
import io.blodhgarm.personality.api.addon.BaseAddon;

public interface PersonalityEntrypoint {

    <T extends BaseAddon> void addonRegistry(AddonRegistry<T> registry);
}
