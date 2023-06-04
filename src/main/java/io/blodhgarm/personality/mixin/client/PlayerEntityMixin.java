package io.blodhgarm.personality.mixin.client;

import com.mojang.authlib.GameProfile;
import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.character.BaseCharacter;
import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.api.character.CharacterManager;
import io.blodhgarm.personality.api.core.KnownCharacterLookup;
import io.blodhgarm.personality.client.ClientCharacters;
import io.blodhgarm.personality.client.glisco.InWorldTooltipProvider;
import io.blodhgarm.personality.misc.pond.CharacterToPlayerLink;
import io.blodhgarm.personality.utils.Constants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements InWorldTooltipProvider, CharacterToPlayerLink {

    @Shadow public abstract Text getDisplayName();

    @Shadow public abstract Text getName();

    @Shadow @Final private GameProfile gameProfile;

    public boolean personality$onlyShowCharacterName = false;

    public boolean personality$addCharacterName = false;

    @Inject(method = "getDisplayName", at = @At("HEAD"))
    private void personality$activeCharacterNameInject(CallbackInfoReturnable<Text> cir){
        personality$addCharacterName = true;
    }

    @Inject(method = "getDisplayName", at = @At("RETURN"))
    private void personality$disableCharacterNameInject(CallbackInfoReturnable<Text> cir){
        personality$addCharacterName = false;
    }

    @Inject(method = "getName", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Text;literal(Ljava/lang/String;)Lnet/minecraft/text/MutableText;"), cancellable = true)
    private void personality$injectCharacterName(CallbackInfoReturnable<Text> cir){
        PlayerEntity player = (PlayerEntity) (Object) this;

        CharacterManager<PlayerEntity, Character> manager = CharacterManager.getManger(player);

        Character clientCharacter = manager.getClientCharacter();

        if(!personality$addCharacterName || clientCharacter == null) return;

        BaseCharacter wrappedCharacter = clientCharacter.getKnownCharacter(player);

        if(Objects.equals(manager.getPlayerUUID(clientCharacter), player.getUuidAsString())) wrappedCharacter = clientCharacter;

        String characterName = (wrappedCharacter != null) ? wrappedCharacter.getName() : (clientCharacter != null && manager.hasCharacter(player) ? "Obscured" : null);

        if(characterName == null) return;

        MutableText name = personality$onlyShowCharacterName
                ? Text.empty()
                : Text.literal(this.gameProfile.getName())
                    .append(Text.literal(" | ").formatted(Formatting.WHITE));

        name.append(Text.literal(characterName).formatted(Constants.CHARACTER_FORMATTING));

        personality$onlyShowCharacterName = false;

        cir.setReturnValue(name);
    }

    @Override
    public PlayerEntity toggleOnlyCharacterName(boolean value) {
        this.personality$onlyShowCharacterName = value;

        return (PlayerEntity) (Object) this;
    }

    @Override
    public Text getChatDisplayName(boolean onlyCharacterName) {
        toggleOnlyCharacterName(onlyCharacterName);

        return getDisplayName();
    }

    @Override
    public void appendTooltipEntries(List<Entry> entries) {
        CharacterManager<PlayerEntity, Character> manager = CharacterManager.getManger((PlayerEntity) (Object) this);

        BaseCharacter c = this.getCharacter(true);

        if(c == null || manager.getClientCharacter() == null) return;

        //entries.add(Entry.text(Text.empty(), Text.of("Description")));

        for(String substring : c.getDescription().split("\n")){
            entries.add(Entry.text(Text.empty(), Text.of(substring)));
        }
    }

    @Override
    public Identifier getTooltipId() {
        return PersonalityMod.id("description");
    }
}
