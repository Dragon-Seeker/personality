package io.blodhgarm.personality.api;

import io.blodhgarm.personality.api.addon.BaseAddon;
import io.blodhgarm.personality.api.reveal.KnownCharacter;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Map;

public interface BaseCharacter {

    Map<Identifier, BaseAddon> getAddons();

    Map<String, KnownCharacter> getKnownCharacters();

    void beforeSaving();

    default BaseAddon getAddon(Identifier identifier){
        return getAddons().get(identifier);
    }

    String getUUID();

    String getName();

    Text getFormattedName();

    String getAlias();

    String getGender();

    String getDescription();

    String getBiography();

    int getAge();
}
