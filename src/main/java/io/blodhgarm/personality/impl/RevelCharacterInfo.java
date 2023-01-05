package io.blodhgarm.personality.impl;

import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.api.reveal.InfoRevealLevel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public interface RevelCharacterInfo<P extends PlayerEntity> {

    void revealCharacterInfo(Character source, P target, Character targetCharacter, InfoRevealLevel level);

    default void revealCharacterInfo(P source, RevealRange range, InfoRevealLevel level){
        this.revealCharacterInfo(source, range.range, level);
    }

    void revealCharacterInfo(P source, int range, InfoRevealLevel level);

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
}
