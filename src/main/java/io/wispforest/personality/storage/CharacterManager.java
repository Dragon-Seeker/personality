package io.wispforest.personality.storage;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.api.SyntaxError;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CharacterManager {

    private static final Jankson jankson = Jankson.builder().build();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Type REF_MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();

    private static final Path CHARACTER_PATH = FabricLoader.getInstance().getGameDir();
    private static final Path REFERENCE_PATH = CHARACTER_PATH.resolve("reference.json");

    public static Map<String, String> playerIDToCharacterID = new HashMap<>();
    public static BiMap<ServerPlayerEntity, Character> playerToCharacter = HashBiMap.create();
    public static Map<String, Character> characterIDToCharacter = new HashMap<>();

    @Nullable
    public static Character getCharacter(ServerPlayerEntity player) {
        return getCharacter(playerIDToCharacterID.get(player.getUuidAsString()));
    }

    @Nullable
    public static Character getCharacter(String uuid) {
        try {
            Character c = characterIDToCharacter.get(uuid);

            if (c != null)
                return c;

            c = jankson.fromJson(jankson.load(getPath(uuid).toFile()), Character.class);
            characterIDToCharacter.put(uuid, c);
//            playerToCharacter.put(player, c);

            return c;
        } catch (SyntaxError | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void saveCharacter(Character character) {
        try {
            Files.writeString(getPath(character.getUUID()), jankson.toJson(character).toJson(true, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //TODO: Character Deleting
    public static void deleteCharacter(String uuid) {

    }

    public static void deleteCharacter(Character character) {

    }

    private static Path getPath(String uuid) {
        return CHARACTER_PATH.resolve(uuid + ".json5");
    }

    public static void loadCharacterReference() {
        try {
            JsonObject o = gson.fromJson(Files.readString(REFERENCE_PATH), JsonObject.class);
            playerIDToCharacterID = gson.fromJson(o.getAsJsonObject("player_to_character"), REF_MAP_TYPE);
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
            json.add("player_to_character", gson.toJsonTree(playerIDToCharacterID, REF_MAP_TYPE));

            Files.writeString(REFERENCE_PATH, gson.toJson(json));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
