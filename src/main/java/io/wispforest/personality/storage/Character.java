package io.wispforest.personality.storage;

import net.minecraft.stat.Stats;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Character {

    public enum Gender {MALE, FEMALE, NONBINARY}
    public enum Stage {YOUTH, PRIME, OLD}

    public final int format = 1;

    private String uuid;
    private String name;
    private Gender gender;
    private String description;

    private float heightOffset;

    private int ageOffset;
    private long created;

    private int playtimeOffset;

    public List<String> knowCharacters;

    public Character() {}

    public Character(String name, Gender gender, String description, float heightOffset, int ageOffset, int activityOffset) {
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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setGender(String gender) {
        this.gender = switch (gender.toLowerCase()) {
            case  "male" -> Gender.MALE;
            case  "female" -> Gender.FEMALE;
            default -> Gender.NONBINARY;
        };
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
        return ageOffset + (int)((System.currentTimeMillis()-created)/604_800_000);
    }

    public void setAge(int age) {
        ageOffset = age - (int)((System.currentTimeMillis()-created)/604_800_000);
    }

    public Stage getStage() {
        int age = getAge();
        return age < 25 ? Stage.YOUTH : age < 60 ? Stage.PRIME : Stage.OLD;
    }

    public long getCreatedAt() {
        return created;
    }

    public int getPlaytime() {
        return 0;
//        return CharacterManager.playerToCharacter.inverse().get(this).getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME)) - playtimeOffset;
    }

    public void setPlaytime(int playtime) {
//        playtimeOffset = playtime - CharacterManager.playerToCharacter.inverse().get(this).getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME));
    }

    @Override
    public String toString() {
        return "Character{" +
                "\nuuid=" + uuid +
                ",\n name=" + name +
                ",\n gender=" + gender.toString() +
                ",\n description=" + description +
                ",\n heightOffset=" + heightOffset +
                ",\n ageOffset=" + ageOffset +
                ",\n created=" + created +
                ",\n activityOffset=" + playtimeOffset +
                ",\n knowCharacters=" + knowCharacters +
                "\n}";
    }



}
