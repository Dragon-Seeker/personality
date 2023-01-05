package io.blodhgarm.personality.api.reveal;

import com.mojang.logging.LogUtils;
import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.BaseCharacter;
import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.api.CharacterManager;
import io.blodhgarm.personality.api.addon.BaseAddon;
import io.blodhgarm.personality.utils.DebugCharacters;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KnownCharacter implements BaseCharacter {

    private static final Logger LOGGER = LogUtils.getLogger();

    public final String characterUUID;

    public InfoRevealLevel level;

    public final List<Identifier> specificKnownInfo;

    public transient Character parentCharacter = DebugCharacters.ERROR;

    public KnownCharacter(String characterUUID) {
        this.characterUUID = characterUUID;
        this.level = InfoRevealLevel.NONE;
        this.specificKnownInfo = new ArrayList<>();
    }

    public KnownCharacter(Character character) {
        this.characterUUID = character.getUUID();
        this.level = InfoRevealLevel.NONE;
        this.specificKnownInfo = new ArrayList<>();

        this.parentCharacter = character;
    }

    public void setParentCharacter(CharacterManager<?> manager){
        Character character = manager.getCharacter(this.characterUUID);

        if(character != null) {
            this.parentCharacter = character;
        } else {
            LOGGER.error("[KnownCharacter] Seems that a know character was initialized");
        }
    }

    public void updateInfoLevel(InfoRevealLevel level){
        this.level = level;
    }

    @Override
    public void beforeSaving() {
        specificKnownInfo.removeIf(valueId -> InfoRevealRegistry.INSTANCE.showInformation(level, valueId));
    }

    @Override
    public Map<Identifier, BaseAddon> getAddons() {
        return parentCharacter.getAddons();
    }

    @Override
    public Map<String, KnownCharacter> getKnownCharacters(){
        return parentCharacter.getKnownCharacters();
    }

    @Override
    public BaseAddon getAddon(Identifier identifier) {
        return getResult(identifier, BaseCharacter.super.getAddon(identifier)).result();
    }

    @Override
    public String getUUID() {
        return parentCharacter.getUUID();
    }

    @Override
    public String getName() {
        return getResult(PersonalityMod.id("name"), parentCharacter.getName()).result();
    }

    @Override
    public Text getFormattedName() {
        InfoRevealResult<String> name = getResult(PersonalityMod.id("name"), parentCharacter.getName());

        String defaultAlias = parentCharacter.getAlias();

        if(!name.replaced()){
            MutableText nameText = Text.literal(name.result());

            if(defaultAlias != null) {
                nameText.append(" : ");
                nameText.append(Text.literal(defaultAlias).formatted(Formatting.ITALIC));
            }

            return nameText;
        }

        InfoRevealResult<String> alias = getResult(PersonalityMod.id("alias"), defaultAlias == null ? "none" : defaultAlias);

        MutableText aliasText = Text.literal(alias.result());

        if(!alias.replaced()) aliasText.formatted(Formatting.ITALIC);

        return aliasText;
    }

    @Override
    public String getAlias() {
        String alias = parentCharacter.getAlias();

        return getResult(PersonalityMod.id("alias"), alias == null ? "none" : alias).result();
    }

    @Override
    public String getGender() {
        return getResult(PersonalityMod.id("gender"), parentCharacter.getGender()).result();
    }

    @Override
    public String getDescription() {
        return getResult(PersonalityMod.id("description"), parentCharacter.getDescription()).result();
    }

    @Override
    public String getBiography() {
        return getResult(PersonalityMod.id("biography"), parentCharacter.getBiography()).result();
    }

    @Override
    public int getAge() {
        return getResult(PersonalityMod.id("age"), parentCharacter.getAge()).result();
    }

    private <T> InfoRevealResult<T> getResult(Identifier valueId, T defaultValue){
        return specificKnownInfo.contains(valueId)
                ? new InfoRevealResult<>(false, defaultValue)
                : InfoRevealRegistry.INSTANCE.defaultOrReplace(level, valueId, defaultValue);
    }


}
