package io.blodhgarm.personality.client.screens;

public enum CharacterScreenMode {
    VIEWING,
    CREATION,
    EDITING;

    public boolean isModifiableMode() {
        return this != VIEWING;
    }

    public boolean importFromCharacter() {
        return this != CREATION;
    }
}
