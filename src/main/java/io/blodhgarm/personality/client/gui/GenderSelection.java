package io.blodhgarm.personality.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.Locale;
import java.util.Objects;

public enum GenderSelection {
    MALE("male"),
    FEMALE("female"),
    NON_BINARY("non-binary"),
    OTHER("other");

    public final String name;

    GenderSelection(String name) {
        this.name = name;
    }

    public static GenderSelection attemptToGetGender(String gender) {
        for (GenderSelection selection : GenderSelection.values()) {
            if (Objects.equals(selection.name, gender.toLowerCase(Locale.ROOT))) {
                return selection;
            }
        }

        return OTHER;
    }

    public static GenderSelection[] valuesWithoutOther(){
        return new GenderSelection[]{ MALE, FEMALE, NON_BINARY };
    }

    public boolean openTextField() {
        return this == GenderSelection.OTHER;
    }

    public GenderSelection getNextSelection() {
        int nextIndex = this.ordinal() + 1;

        return GenderSelection.values()[nextIndex >= GenderSelection.values().length ? 0 : nextIndex];
    }

    public Text translation() {
        return Text.translatable("personality.gender." + name.replace(" ", "_").toLowerCase(Locale.ROOT));
    }

    public String translatedString() {
        return translation().getString();
    }


    public int textSizing() {
        return MinecraftClient.getInstance().textRenderer.getWidth(this.translation().asOrderedText());
    }
}
