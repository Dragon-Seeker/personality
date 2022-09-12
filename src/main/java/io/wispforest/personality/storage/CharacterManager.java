package io.wispforest.personality.storage;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.api.SyntaxError;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class CharacterManager {

    private static final Jankson jankson = Jankson.builder().build();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Type REF_MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();

    private static final Path CHARACTER_PATH = FabricLoader.getInstance().getGameDir();
    private static final Path REFERENCE_PATH = CHARACTER_PATH.resolve("reference.json");
    public static Map<String, String> playerToCharacters = new HashMap<>();

    public static Character readCharacter(PlayerEntity player) {
        try {
            return jankson.fromJson(jankson.load(getPath(player.getUuidAsString()).toFile()), Character.class);
        } catch (SyntaxError | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void saveCharacter(Character character) {
        try {
            Files.writeString(getPath(character.uuid), jankson.toJson(character).toJson(true, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Path getPath(String uuid) {
        return CHARACTER_PATH.resolve(uuid + ".json5");
    }

    public static void loadCharacterReference() {
        try {
            JsonObject o = gson.fromJson(Files.readString(REFERENCE_PATH), JsonObject.class);
            playerToCharacters = gson.fromJson(o.getAsJsonObject("player_to_character"), REF_MAP_TYPE);
        } catch (IOException e) {
            if (e instanceof NoSuchFileException)
                saveCharacterReference();
            else
                e.printStackTrace();
        }
    }

    public static void saveCharacterReference() {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("format", 1);
            json.add("player_to_character", gson.toJsonTree(playerToCharacters, REF_MAP_TYPE));

            Files.writeString(REFERENCE_PATH, gson.toJson(json));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
