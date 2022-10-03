package io.blodhgarm.personality.api;

import io.blodhgarm.personality.api.addons.BaseAddon;

public interface PersonalityEntrypoint {

    <T extends BaseAddon<?>> void addonRegistry(AddonRegistry<T> registry);
}
