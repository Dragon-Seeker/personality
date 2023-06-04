package io.blodhgarm.personality.api.reveal;

import com.mojang.logging.LogUtils;
import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.character.BaseCharacter;
import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.api.character.CharacterManager;
import io.blodhgarm.personality.api.addon.BaseAddon;
import io.blodhgarm.personality.api.utils.InfoRevealResult;
import io.blodhgarm.personality.utils.DebugCharacters;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KnownCharacter implements BaseCharacter {

    private static final Logger LOGGER = LogUtils.getLogger();

    public final String ownerCharacterUUID;
    public final String wrappedCharacterUUID;

    public int aliasIndex = -1;

    public InfoLevel level;
    public final List<Identifier> specificKnownInfo;

    private LocalDateTime dateOfDiscovery = LocalDateTime.MIN;

    public transient CharacterManager<? extends PlayerEntity, ? extends Character> manager;

    public KnownCharacter(String ownerCharacterUUID, String wrappedCharacterUUID) {
        this.ownerCharacterUUID = ownerCharacterUUID;

        this.wrappedCharacterUUID = wrappedCharacterUUID;
        this.level = InfoLevel.UNDISCOVERED;
        this.specificKnownInfo = new ArrayList<>();
    }

    @Override
    public BaseCharacter setCharacterManager(CharacterManager<? extends PlayerEntity, ? extends Character> manager){
        this.manager = manager;

        return this;
    }

    public KnownCharacter updateInfoLevel(InfoLevel level){
        this.level = level;

        return this;
    }

    public InfoLevel getLevel(){
        InfoLevel baseLevel = PersonalityMod.CONFIG.minimumBaseInfo();

        if(!baseLevel.shouldUpdateLevel(this.level)) return baseLevel;

        return this.level;
    }

    public KnownCharacter setDiscoveredTime(){
        this.dateOfDiscovery = LocalDateTime.now();

        return this;
    }

    public LocalDateTime getDiscoveredTime(){
        return this.dateOfDiscovery;
    }

    public Character getWrappedCharacter(){
        Character character = DebugCharacters.ERROR;

        if(manager != null){
            character = manager.getCharacter(this.wrappedCharacterUUID);

            if(character == null) {
                LOGGER.error("[KnownCharacter] Seems that a Known Character was attempted to be accessed but was not found!");

                character = DebugCharacters.ERROR;
            }
        } else {
            LOGGER.error("[KnownCharacter] Seems that a Known Character was attempted to be accessed but that manager was not set yet!");
        }

        return character;
    }

    @Override
    public void beforeEvent(String event) {
        if(!event.equals("save")) return;

        specificKnownInfo.removeIf(valueId -> InfoRevealRegistry.INSTANCE.showInformation(level, valueId));
    }

    @Override
    public Map<Identifier, BaseAddon> getAddons() {
        return getWrappedCharacter().getAddons();
    }

    @Override
    public Map<String, KnownCharacter> getKnownCharacters(){
        return getWrappedCharacter().getKnownCharacters();
    }

    @Override
    public BaseAddon getAddon(Identifier identifier) {
        return getResult(identifier, BaseCharacter.super.getAddon(identifier)).result();
    }

    @Override
    public String getUUID() {
        return getWrappedCharacter().getUUID();
    }

    @Override
    public String getPlayerUUID() {
        return getWrappedCharacter().getPlayerUUID();
    }

    @Override
    public String getName() {
        return getResult(PersonalityMod.id("name"), getWrappedCharacter().getName()).result();
    }

    @Override
    public Text getFormattedName() {
        InfoRevealResult<String> name = getResult(PersonalityMod.id("name"), getWrappedCharacter().getName());

        String defaultAlias = getWrappedCharacter().getAlias();

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
        String alias = getWrappedCharacter().getAlias();

        return getResult(PersonalityMod.id("alias"), alias == null ? "none" : alias).result();
    }

    @Override
    public String getGender() {
        return getResult(PersonalityMod.id("gender"), getWrappedCharacter().getGender()).result();
    }

    @Override
    public String getDescription() {
        return getResult(PersonalityMod.id("description"), getWrappedCharacter().getDescription()).result();
    }

    @Override
    public String getBiography() {
        return getResult(PersonalityMod.id("biography"), getWrappedCharacter().getBiography()).result();
    }

    @Override
    public int getAge() {
        return getResult(PersonalityMod.id("age"), getWrappedCharacter().getAge()).result();
    }

    @Override
    public Health getHealthStage() {
        return getResult(PersonalityMod.id("health"), getWrappedCharacter().getHealthStage()).result();
    }

    @Override
    public boolean isDead() {
        return getWrappedCharacter().isDead();
    }

    @Override
    public int getDeathWindow() {
        return getWrappedCharacter().getDeathWindow();
    }

    @Override
    public int getTotalPlaytime() {
        return getWrappedCharacter().getTotalPlaytime();
    }

    @Override
    public int getCurrentPlaytime() {
        return getWrappedCharacter().getCurrentPlaytime();
    }

    private <T> InfoRevealResult<T> getResult(Identifier valueId, T defaultValue){
        return specificKnownInfo.contains(valueId)
                ? new InfoRevealResult<>(false, defaultValue)
                : InfoRevealRegistry.INSTANCE.defaultOrReplace(level, valueId, defaultValue);
    }


}
