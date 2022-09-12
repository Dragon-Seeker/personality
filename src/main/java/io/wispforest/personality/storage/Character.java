package io.wispforest.personality.storage;

import java.util.ArrayList;
import java.util.List;

public class Character {

    public int format;

    public String uuid;
    public String name;
    public String description;

    public float heightOffset;

    public int ageOffset;
    public long dateCreated;

    public int activityOffset;

    public List<String> knowCharacters;

    public Character() {}

    public Character(String uuid, String name, String description, float heightOffset, int ageOffset, int activityOffset) {
        this.format = 1;
        this.uuid = uuid;//UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.heightOffset = heightOffset;
        this.ageOffset = ageOffset;
        this.dateCreated = System.currentTimeMillis();
        this.activityOffset = activityOffset;
        this.knowCharacters = new ArrayList<>();
    }


    @Override
    public String toString() {
        return "Character{" +
                "\nuuid='" + uuid + '\'' +
                ",\n name='" + name + '\'' +
                ",\n description='" + description + '\'' +
                ",\n heightOffset=" + heightOffset +
                ",\n ageOffset=" + ageOffset +
                ",\n dateCreated=" + dateCreated +
                ",\n activityOffset=" + activityOffset +
                ",\n knowCharacters=" + knowCharacters +
                "\n}";
    }
}
