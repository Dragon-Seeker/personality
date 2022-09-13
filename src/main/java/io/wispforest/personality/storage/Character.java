package io.wispforest.personality.storage;

import net.minecraft.stat.Stats;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Character {

    public enum Gender {MALE, FEMALE, NONBINARY}

    public int format;

    public String uuid;
    public String name;
    public Gender gender;
    public String description;

    public float heightOffset;

    public int ageOffset;
    public long created;

    public int activityOffset;

    public List<String> knowCharacters;

    public Character() {}

    public Character(String name, Gender gender, String description, float heightOffset, int ageOffset, int activityOffset) {
        this.format = 1;
        this.uuid = UUID.randomUUID().toString();
        this.name = name;
        this.gender = gender;
        this.description = description;
        this.heightOffset = heightOffset;
        this.ageOffset = ageOffset;
        this.created = System.currentTimeMillis();
        this.activityOffset = activityOffset;
        this.knowCharacters = new ArrayList<>();
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
                ",\n activityOffset=" + activityOffset +
                ",\n knowCharacters=" + knowCharacters +
                "\n}";
    }

    public int getAge(long timeOffset) {
        return ageOffset + (int)((System.currentTimeMillis()-created+timeOffset)/604_800_000);
    }

    public enum Stage {YOUTH, PRIME, OLD}

    public Stage getStage(long timeOffset) {
        int age = getAge(timeOffset);
        return age < 25 ? Stage.YOUTH : age < 60 ? Stage.PRIME : Stage.OLD;
    }

    public int getPlaytime() {
        return CharacterManager.playerToCharacter.inverse().get(this).getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME)) - activityOffset;
    }

}
