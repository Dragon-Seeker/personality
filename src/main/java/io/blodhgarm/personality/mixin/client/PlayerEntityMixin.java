package io.blodhgarm.personality.mixin.client;

import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.character.BaseCharacter;
import io.blodhgarm.personality.api.character.CharacterManager;
import io.blodhgarm.personality.client.glisco.InWorldTooltipProvider;
import io.blodhgarm.personality.misc.pond.CharacterToPlayerLink;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements InWorldTooltipProvider, CharacterToPlayerLink<PlayerEntity> {

    @Override
    public void appendTooltipEntries(List<Entry> entries) {
        BaseCharacter character = CharacterManager.getManger((PlayerEntity) (Object) this).getCharacter((PlayerEntity) (Object) this);

        if(character != null){
            entries.add(Entry.text(Text.empty(), Text.of("Description")));

            for(String substring : character.getDescription().split("\n")){
                entries.add(Entry.text(Text.empty(), Text.of(substring)));
            }

        }
    }

    @Override
    public Identifier getTooltipId() {
        return PersonalityMod.id("description");
    }
}
