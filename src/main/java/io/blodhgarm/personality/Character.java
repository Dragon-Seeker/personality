package io.blodhgarm.personality;

import io.blodhgarm.personality.server.ServerCharacters;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Character {

    public enum Stage {YOUTH, PRIME, OLD}
    public static final int WEEK_IN_MILLISECONDS = 604_800_000;
    public static final int HOUR_IN_MILLISECONDS =   3_600_000;
    public final int format = 1;

    private String uuid;
    private String name;
    private String gender;
    private String description;

    private float heightOffset;

    private int ageOffset;
    private long created;

    private int playtimeOffset;

    public List<String> knowCharacters;

    public Character() {}

    public Character(String name, String gender, String description, float heightOffset, int ageOffset, int activityOffset) {
        this.uuid = UUID.randomUUID().toString();
        this.name = name;
        this.gender = gender;
        this.description = description;
        this.heightOffset = heightOffset;
        this.ageOffset = ageOffset;
        this.created = System.currentTimeMillis();
        this.playtimeOffset = activityOffset;
        this.knowCharacters = new ArrayList<>();
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

    public float getHeightOffset() {
        return heightOffset;
    }

    public void setHeightOffset(float heightOffset) {
        this.heightOffset = heightOffset;
    }

    public int getAge() {
        return ageOffset + (int)((System.currentTimeMillis()-created)/WEEK_IN_MILLISECONDS);
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
        ServerPlayerEntity player = ServerCharacters.getPlayer(uuid);
        if (player == null)
            return 0;
        return player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME)) - playtimeOffset;
    }

    public boolean setPlaytime(int playtime) {
        ServerPlayerEntity player = ServerCharacters.getPlayer(uuid);
        if (player == null)
            return false;
        playtimeOffset = playtime - player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME));
        return true;
    }

    public int getMaxAge() {
        return PersonalityMod.CONFIG.BASE_MAXIMUM_AGE() + Math.min(PersonalityMod.CONFIG.MAX_EXTRA_YEARS_OF_LIFE(), getPlaytime() / HOUR_IN_MILLISECONDS / PersonalityMod.CONFIG.HOURS_PER_EXTRA_YEAR_OF_LIFE());
    }

    public String getInfo() {
        return name + "§r\n"
                + "\n§lUUID§r: " + uuid
                + "\n§lGender§r: " + gender
                + "\n§lDescription§r: " + description
                + "\n§lAge§r: " + getAge() + " / " + getMaxAge() + " (" + getStage() + ")"
                + "\n§lPlaytime§r: " + (getPlaytime()/HOUR_IN_MILLISECONDS)
                + "\n§lHeight§r: " + (2 - heightOffset);
    }

    @Override
    public String toString() {
        return "Character{" +
                "\nuuid=" + uuid +
                ",\n name=" + name +
                ",\n gender=" + gender +
                ",\n description=" + description +
                ",\n heightOffset=" + heightOffset +
                ",\n ageOffset=" + ageOffset +
                ",\n created=" + created +
                ",\n activityOffset=" + playtimeOffset +
                ",\n knowCharacters=" + knowCharacters +
                "\n}";
    }

}
