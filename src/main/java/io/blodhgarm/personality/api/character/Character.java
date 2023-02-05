package io.blodhgarm.personality.api.character;

import com.google.common.reflect.TypeToken;
import com.google.gson.InstanceCreator;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.blodhgarm.personality.api.addon.BaseAddon;
import io.blodhgarm.personality.api.reveal.KnownCharacter;
import io.blodhgarm.personality.api.utils.PlayerAccess;
import io.blodhgarm.personality.server.ServerCharacters;
import io.blodhgarm.personality.misc.PersonalityTags;
import io.blodhgarm.personality.utils.Constants;
import io.blodhgarm.personality.utils.DebugCharacters;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Main Class implementation for a Character within Personality
 */
public class Character implements BaseCharacter {

    public static final Type REF_MAP_TYPE = new TypeToken<Map<Identifier, BaseAddon>>() {}.getType();

    public boolean isDead;

    private final String uuid;
    private final String playerUUID;

    private String name;

    private List<String> aliases = new ArrayList<>();

    @Nullable private String alias = null;

    private String gender;
    private String description;
    private String biography;

    private int ageOffset;
    private long created;

    private int playtimeOffset;

    private Map<String, KnownCharacter> knownCharacters = new HashMap<>();

    private transient Map<Identifier, BaseAddon> characterAddons = new HashMap<>();

    public Character(String playerUUID, String name, String gender, String description, String biography, int ageOffset, int activityOffset) {
        this(UUID.randomUUID().toString(), playerUUID, name, gender, description, biography, ageOffset, activityOffset);
    }

    public Character(String uuid, String playerUUID, String name, String gender, String description, String biography, int ageOffset, int activityOffset) {
        this.uuid = uuid;
        this.playerUUID = playerUUID;

        this.name = name;
        this.gender = gender;
        this.description = description;
        this.biography = biography;
        this.ageOffset = ageOffset;
        this.created = System.currentTimeMillis();
        this.playtimeOffset = activityOffset;
        this.isDead = false;
    }

    @Override
    public void beforeSaving() {
        knownCharacters.values().forEach(KnownCharacter::beforeSaving);
    }

    //---------------------------

    @Override
    public Map<Identifier, BaseAddon> getAddons(){
        if(this.characterAddons == null) this.characterAddons = new HashMap<>();

        return this.characterAddons;
    }

    @Override
    public Map<String, KnownCharacter> getKnownCharacters(){
        if(this.knownCharacters == null) this.knownCharacters = new HashMap<>();

        return this.knownCharacters;
    }

    @Override
    public String getUUID() {
        return uuid;
    }

    @Override
    public String getPlayerUUID() {
        return this.playerUUID;
    }

    //---------------------------

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //---------------------------

    @Override
    @Nullable
    public String getAlias() {
        return this.alias;
    }

    public Character setAlias(String alias) {
        this.alias = alias;

        return this;
    }

    @Override
    public Text getFormattedName() {
        MutableText name = Text.literal(getName());

        if(getAlias() != null){
            name.append(Text.of(" : "));
            name.append(Text.literal(getAlias()).formatted(Formatting.ITALIC));
        }

        return name;
    }

    //---------------------------

    @Override
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    //---------------------------

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    //---------------------------

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    //---------------------------

    public boolean isDead() {
        return isDead;
    }

    public void setIsDead(boolean isDead) {
        this.isDead = isDead;
    }

    //---------------------------

    public long getCreatedAt() {
        return created;
    }

    public int getAge() {
        return ageOffset + (int)((System.currentTimeMillis() - getCreatedAt()) / Constants.WEEK_IN_MILLISECONDS);
    }

    public float getPreciseAge() {
        return ageOffset + ((float)(System.currentTimeMillis() - getCreatedAt()) / Constants.WEEK_IN_MILLISECONDS);
    }

    public void setAge(int age) {
        ageOffset = age - (int)((System.currentTimeMillis() - getCreatedAt()) / Constants.WEEK_IN_MILLISECONDS);
    }

    @Override
    public int getPlaytime() {
        PlayerAccess<ServerPlayerEntity> player = ServerCharacters.INSTANCE.getPlayer(uuid);

        return player.valid() && player.player() != null
                ? player.player().getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME)) - playtimeOffset
                : 0;
    }

    public boolean setPlaytime(int playtime) {
        PlayerAccess<ServerPlayerEntity> player = ServerCharacters.INSTANCE.getPlayer(uuid);

        if (player.valid() && player.player() != null) {
            playtimeOffset = playtime - player.player().getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME));

            return true;
        }

        return false;
    }

    //---------------------------

    public boolean isObscured() {
        PlayerAccess<ServerPlayerEntity> player = ServerCharacters.INSTANCE.getPlayer(uuid);

        if (player.valid() && player.player() != null) {
            for (ItemStack stack : player.player().getItemsEquipped()) {
                if (stack.isIn(PersonalityTags.OBSCURES_IDENTITY)) return true;
            }
        }

        return false;
    }

    public boolean isError(){
        return DebugCharacters.ERROR == this;
    }

    @Override
    public String toString() {
        return "Character{" +
                "\nuuid=" + uuid +
                ",\n name=" + name +
                ",\n gender=" + gender +
                ",\n description=" + description +
                ",\n biography=" + biography +
                ",\n ageOffset=" + ageOffset +
                ",\n created=" + created +
                ",\n activityOffset=" + playtimeOffset +
                ",\n knowCharacters=" + knownCharacters +
                "\n}";
    }

    //--------------------------------------------
}
