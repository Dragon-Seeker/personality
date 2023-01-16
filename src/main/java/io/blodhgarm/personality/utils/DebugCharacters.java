package io.blodhgarm.personality.utils;

import io.blodhgarm.personality.api.BaseCharacter;
import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.api.CharacterManager;
import io.blodhgarm.personality.api.addon.AddonRegistry;
import io.blodhgarm.personality.api.reveal.InfoRevealLevel;
import io.blodhgarm.personality.api.reveal.KnownCharacter;
import io.blodhgarm.personality.client.gui.screens.CharacterScreen;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class DebugCharacters {

    public static final Character DEBUG_1 = new Character(
            UUID.nameUUIDFromBytes("debug_1".getBytes(StandardCharsets.UTF_8)).toString(),
            "Mark",
            CharacterScreen.GenderSelection.MALE.translatedString(),
            "Cool Dude who plays fnaf",
            "From Korea",
            25,
            0
    ).setAlias("King Of FNAF");

    public static final Character DEBUG_2 = new Character(
            UUID.nameUUIDFromBytes("debug_2".getBytes(StandardCharsets.UTF_8)).toString(),
            "Cole",
            CharacterScreen.GenderSelection.NON_BINARY.translatedString(),
            "Great person at chess",
            "From the moon",
            29,
            0
    ).setAlias("Moon Man");

    public static final Character DEBUG_3 = new Character(
            UUID.nameUUIDFromBytes("debug_3".getBytes(StandardCharsets.UTF_8)).toString(),
            "Sara",
            CharacterScreen.GenderSelection.FEMALE.translatedString(),
            "Play's Overwatch 2",
            "From nowhere",
            30,
            0
    ).setAlias("Dva");

    public static final Character DEBUG_4 = new Character(
            UUID.nameUUIDFromBytes("debug_4".getBytes(StandardCharsets.UTF_8)).toString(),
            "Lily",
            "Transgender",
            "Also Play's Overwatch 2",
            "From Canada",
            23,
            0
    ).setAlias("Lucio");

    public static final Character DEBUG_5 = new Character(
            UUID.nameUUIDFromBytes("debug_5".getBytes(StandardCharsets.UTF_8)).toString(),
            "glisco",
            CharacterScreen.GenderSelection.MALE.translatedString(),
            "Created owo because he could",
            "From Germany",
            26,
            0
    ).setAlias("Man Of the Wisp");

    public static final Character REVEAL_TEST = new Character(
            UUID.nameUUIDFromBytes("reveal_test".getBytes(StandardCharsets.UTF_8)).toString(),
            "Tim",
            CharacterScreen.GenderSelection.NON_BINARY.translatedString(),
            "Tall person with black hair and brown eyes.",
            "Lives nowhere and everywhere at the same time",
            23,
            0
    );

    public static final Character ERROR = new Character(
            UUID.nameUUIDFromBytes("error".getBytes(StandardCharsets.UTF_8)).toString(),
            "error",
            "none",
            "something has gone wrong",
            "something has gone wrong",
            0,
            0
    );

    public static <T extends PlayerEntity> void loadDebugCharacters(CharacterManager<T> manager){
        DEBUG_CHARACTERS_LIST.forEach(character -> {
            character.getAddons().putAll(AddonRegistry.INSTANCE.getDefaultAddons());

            //manager.characterLookupMap().put(character.getUUID(), character);
        });
    }

    public static final List<BaseCharacter> KNOWN_CHARACTERS = new ArrayList<>();

    public static final List<Character> DEBUG_CHARACTERS_LIST = List.of(
            DEBUG_1,
            DEBUG_2,
            DEBUG_3,
            DEBUG_4,
            DEBUG_5
    );

    public static final Map<String, Character> DEBUG_CHARACTERS_MAP = new HashMap<>();

    public static void init() {
        if(FabricLoader.getInstance().isDevelopmentEnvironment()) {
            for(int i = 0; i < 5; i++) {
                Character character = DEBUG_CHARACTERS_LIST.get(i);

                DEBUG_CHARACTERS_MAP.put(character.getUUID(), character);

                KnownCharacter wrappedCharacter = new KnownCharacter(DEBUG_CHARACTERS_LIST.get(i));

                wrappedCharacter.updateInfoLevel(InfoRevealLevel.VALID_VALUES[i]);

                REVEAL_TEST.getKnownCharacters().put(character.getUUID(), wrappedCharacter);

                KNOWN_CHARACTERS.add(wrappedCharacter);
            }
        }
    }

}
