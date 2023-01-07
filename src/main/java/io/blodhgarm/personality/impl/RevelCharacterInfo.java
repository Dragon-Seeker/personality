package io.blodhgarm.personality.impl;

import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.api.reveal.InfoRevealLevel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public interface RevelCharacterInfo<P extends PlayerEntity> {

    default void revealCharacterInfo(P source, RevealRange range, InfoRevealLevel level){
        this.revealCharacterInfo(source, range.range, level);
    }

    default void revealCharacterInfo(P source, int range, InfoRevealLevel level){
        List<P> players = (List<P>) source.getWorld().getPlayers().stream()
                .filter(player -> source.getPos().distanceTo(player.getPos()) <= range)
                .toList();

        revealCharacterInfo(source, players, level);
    }

    void revealCharacterInfo(P source, List<P> targets, InfoRevealLevel level);

    SuccessfulRevealReturn<P> revealCharacterInfo(Character source, Character targetCharacter, InfoRevealLevel level);

    enum RevealRange {
        LARGE("large", 15),
        MEDIUM("medium", 7),
        SMALL("small", 3),
        DIRECTED("directed", 0);

        public int range;
        public String name;

        RevealRange(String name, int range){
            this.name = name;
            this.range = range;
        }

        public Text getTranslation(){
            return Text.translatable("personality.reveal_range." + this.name);
        }
    }

    interface SuccessfulRevealReturn<P> { void finishEvent(P target); }
}
