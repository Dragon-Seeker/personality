package io.blodhgarm.personality.api.reveal;

import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.api.reveal.InfoRevealLevel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Inteface used to store all the methods for Revealing Character Info
 */
public interface RevelInfoManager<P extends PlayerEntity> {

    /**
     * Method used to reveal infromation based on value within the {@link RevealRange} Enum
     *
     * @param source Person to be revealed
     * @param range The range at which such person will reveal
     * @param level Level of information to be revealed
     */
    default void revealCharacterInfo(P source, RevealRange range, InfoRevealLevel level){
        this.revealCharacterInfo(source, range.range, level);
    }

    default void revealCharacterInfo(P source, int range, InfoRevealLevel level){
        List<P> players = (List<P>) source.getWorld().getPlayers().stream()
                .filter(player -> source.getPos().distanceTo(player.getPos()) <= range && !player.equals(source))
                .toList();

        revealCharacterInfo(source, players, level);
    }

    default void revealCharacterInfo(P source, Collection<P> targets, InfoRevealLevel level) {}

    default void revealCharacterInfo(Character source, Character targetCharacter, P packetTarget, InfoRevealLevel level) {}

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
