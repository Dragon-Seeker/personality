package io.blodhgarm.personality.client.glisco;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Objects;

/*
 * A 1:1-ish copy from the mod Affinity by glisco. Link to github: https://github.com/wisp-forest/affinity/blob/master/src/main/java/io/wispforest/affinity/client/render/InWorldTooltipProvider.java
 *
 * I am not the original author and this follows the MIT license from the original mod
 */

/**
 * An interface to be implemented on {@link net.minecraft.block.entity.BlockEntity} types that
 * should display some stats in the world when targeted by the player
 */
public interface InWorldTooltipProvider {

    /**
     * Update the tooltip entries display by this provider,
     * usually used for visually interpolated values
     *
     * @param force Whether all interpolated values should instantaneously
     *              be updated to their real current value, usually because the
     *              player's targeted block has changed
     * @param delta The duration of the last frame, in partial ticks
     */
    default void updateTooltipEntries(boolean force, float delta) {}

    /**
     * The statistics this provider should currently
     * display, wrapped in {@link Entry}
     */
    void appendTooltipEntries(List<Entry> entries);

    default Identifier getTooltipId(){
        return new Identifier("DEFAULT");
    }

    //------

    boolean equals(Object o);

    int hashCode();

    //------

    interface Entry {
        Text label();

        static Entry icon(Text text, Identifier texture, int u, int v) {
            return new TextAndIconEntry(text, texture, u, v);
        }

        static Entry text(Text icon, Text text) {
            return new TextEntry(icon, text);
        }
    }

    record TextEntry(Text icon, Text label) implements Entry {}
    record TextAndIconEntry(Text label, Identifier texture, int u, int v) implements Entry {}
}
