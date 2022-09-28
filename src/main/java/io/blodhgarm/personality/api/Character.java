package io.blodhgarm.personality.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.impl.ServerCharacters;
import io.blodhgarm.personality.misc.PersonalityTags;
import io.blodhgarm.personality.misc.config.PersonalityConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static java.lang.Math.*;

public class Character {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public enum Stage {YOUTH, PRIME, OLD}
    public static final int WEEK_IN_MILLISECONDS = 604_800_000;
    public static final int HOUR_IN_MILLISECONDS =   3_600_000;
    public final int format = 1;

    public boolean isDead;

    private String uuid;
    private String name;
    private String gender;
    private String description;
    private String biography;

    private float heightOffset;

    private int ageOffset;
    private long created;

    private int playtimeOffset;

    public Set<String> knowCharacters;

    public Character() {}

    public Character(String name, String gender, String description, String biography, float heightOffset, int ageOffset, int activityOffset) {
        this.uuid = UUID.randomUUID().toString();
        this.name = name;
        this.gender = gender;
        this.description = description;
        this.biography = biography;
        this.heightOffset = heightOffset;
        this.ageOffset = ageOffset;
        this.created = System.currentTimeMillis();
        this.playtimeOffset = activityOffset;
        this.knowCharacters = new LinkedHashSet<>();
        this.isDead = false;
    }

    public boolean isDead() {
        return isDead;
    }

    public void setIsDead(boolean isDead) {
        this.isDead = isDead;
    }

    public String getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public float getHeightOffset() {
        return heightOffset;
    }

    public void setHeightOffset(float heightOffset) {
        this.heightOffset = heightOffset;
    }

    public int getAge() {
        return ageOffset + (int)((System.currentTimeMillis()-created)/WEEK_IN_MILLISECONDS);
    }

    public float getPreciseAge() {
        return ageOffset + ((float)(System.currentTimeMillis()-created)/WEEK_IN_MILLISECONDS);
    }

    public void setAge(int age) {
        ageOffset = age - (int)((System.currentTimeMillis()-created)/WEEK_IN_MILLISECONDS);
    }

    public Stage getStage() {
        int age = getAge();
        return age < 25 ? Stage.YOUTH : age < 60 ? Stage.PRIME : Stage.OLD;
    }

    public long getCreatedAt() {
        return created;
    }

    public int getPlaytime() {
        ServerPlayerEntity player = ServerCharacters.INSTANCE.getPlayer(uuid);

        return player != null ? player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME)) - playtimeOffset : 0;
    }

    public boolean setPlaytime(int playtime) {
        ServerPlayerEntity player = ServerCharacters.INSTANCE.getPlayer(uuid);

        if (player != null) {
            playtimeOffset = playtime - player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME));

            return true;
        }

        return false;
    }

    public int getMaxAge() {
        return PersonalityMod.CONFIG.BASE_MAXIMUM_AGE() + Math.min(PersonalityMod.CONFIG.MAX_EXTRA_YEARS_OF_LIFE(), getExtraAge());
    }

    public int getExtraAge() {
        PersonalityConfig.ExtraLife config = PersonalityMod.CONFIG.EXTRA_LIFE;
        double hoursPlayed = (float) getPlaytime() / HOUR_IN_MILLISECONDS;
        int extraYears = 0;

        if (hoursPlayed > config.START_HOURS_PER_EXTRA_LIFE() && getAge() > config.MIN_AGE()) {
            for (int i = 1; ; i++) {
                double hoursNeeded = config.START_HOURS_PER_EXTRA_LIFE() + config.CURVE_MULTIPLIER() * switch (config.CURVE()) {
                    case NONE -> 0;
                    case LINEAR -> i - 1;
                    case QUADRATIC -> pow(i, 2) - 1;
                    case SQRT -> sqrt(i) - 1;
                    case EXPONENTIAL, EXPONENTIAL_EXTREME -> pow(E, i - 1) - 1;
                    case LOGARITHMIC, LOGARITHMIC_EXTREME -> log(i);
                };

                if (hoursPlayed < hoursNeeded) break;

                hoursPlayed -= hoursNeeded;
            }
        }

        return extraYears;
    }

    public boolean isObscured() {
        ServerPlayerEntity player = ServerCharacters.INSTANCE.getPlayer(uuid);

        if (player != null) {
            for (ItemStack stack : player.getItemsEquipped()) {
                if (stack.isIn(PersonalityTags.OBSCURES_IDENTITY)) return true;
            }
        }

        return false;
    }

    public String serialise(){
        return GSON.toJson(this);
    }

    public String getInfo() {
        return name + "§r\n"
                + "\n§lUUID§r: " + uuid
                + "\n§lGender§r: " + gender
                + "\n§lDescription§r: " + description
                + "\n§lBio§r: " + biography
                + "\n§lAge§r: " + getAge() + " / " + getMaxAge() + " (" + getStage() + ")"
                + "\n§lPlaytime§r: " + (getPlaytime()/HOUR_IN_MILLISECONDS)
                + "\n§lHeight§r: " + (1.8 - heightOffset);
    }

    @Override
    public String toString() {
        return "Character{" +
                "\nuuid=" + uuid +
                ",\n name=" + name +
                ",\n gender=" + gender +
                ",\n description=" + description +
                ",\n biography=" + biography +
                ",\n heightOffset=" + heightOffset +
                ",\n ageOffset=" + ageOffset +
                ",\n created=" + created +
                ",\n activityOffset=" + playtimeOffset +
                ",\n knowCharacters=" + knowCharacters +
                "\n}";
    }


}
