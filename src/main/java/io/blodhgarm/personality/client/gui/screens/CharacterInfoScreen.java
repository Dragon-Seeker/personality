package io.blodhgarm.personality.client.gui.screens;

import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.BaseCharacter;
import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.api.reveal.KnownCharacter;
import io.blodhgarm.personality.client.ClientCharacters;
import io.blodhgarm.personality.client.ThemeHelper;
import io.blodhgarm.personality.client.gui.CharacterScreenMode;
import io.blodhgarm.personality.client.gui.utils.CustomSurfaces;
import io.blodhgarm.personality.client.gui.builders.ListedCharactersView;
import io.blodhgarm.personality.utils.DebugCharacters;
import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CharacterInfoScreen extends TabbedScreen {

    private CharacterScreen characterScreen;

    private Character currentCharacter;

    public TabComponentBuilder<BaseParentComponent> characterInformation;
    public TabComponentBuilder<BaseParentComponent> knownCharacters;
    public TabComponentBuilder<BaseParentComponent> heritageInfo;

    public CharacterInfoScreen(){
        super(Text.of("test"));
    }

    @Override
    public void registerTabs(Map<Identifier, TabComponentBuilder<BaseParentComponent>> registeredBuilders) {
        characterInformation = new TabComponentBuilder<>(PersonalityMod.id("character_details"), Text.of("Character Info"), this::buildCharacterInfoTab);
        knownCharacters = new TabComponentBuilder<>(PersonalityMod.id("known_characters"), Text.of("Known Characters"), this::buildKnownCharacterTab);
        heritageInfo = new TabComponentBuilder<>(PersonalityMod.id("heritage_info"), Text.of("Family Tree"), this::buildHeritageInfo);

        registeredBuilders.put(characterInformation.id, characterInformation);
        registeredBuilders.put(knownCharacters.id, knownCharacters);
        registeredBuilders.put(heritageInfo.id, heritageInfo);
    }

    public void buildCharacterInfoTab(FlowLayout layout){
        Character playerCharacter = ClientCharacters.INSTANCE.getCharacter(MinecraftClient.getInstance().player);

        this.characterScreen = new CharacterScreen(CharacterScreenMode.VIEWING, MinecraftClient.getInstance().player, playerCharacter != null ? playerCharacter : DebugCharacters.DEBUG_5);

        characterScreen.buildAsChild(layout);
    }

    public void buildKnownCharacterTab(FlowLayout layout){
        FlowLayout mainLayout = Containers.verticalFlow(Sizing.content(), Sizing.fixed(182));

        this.currentCharacter = ClientCharacters.INSTANCE.getCharacter(MinecraftClient.getInstance().player);

        if(this.currentCharacter == null) this.currentCharacter = DebugCharacters.REVEAL_TEST;

        List<BaseCharacter> knownCharacters = new ArrayList<>(this.currentCharacter.getKnownCharacters().values());

        if(knownCharacters.isEmpty() && !DebugCharacters.KNOWN_CHARACTERS.isEmpty()) knownCharacters.addAll(DebugCharacters.KNOWN_CHARACTERS);

        FlowLayout knownCharacterLayout = Containers.verticalFlow(Sizing.content(), Sizing.content()); //140

        ScrollContainer<FlowLayout> knownCharacterContainer = Containers.verticalScroll(Sizing.content(), Sizing.fill(100), knownCharacterLayout);

        new ListedCharactersView(this, () -> knownCharacters)
                .addComponent(Text.of("Friendliness"), (baseCharacter, mode, isParentVertical) -> Components.label(((KnownCharacter) baseCharacter).level.getTranslation()))
                .buildLayout(knownCharacterLayout, knownCharacterContainer);

        mainLayout.child(
                knownCharacterContainer
                        .surface(CustomSurfaces.INVERSE_PANEL)
                        .padding(Insets.of(4))
        );

        mainLayout
                .surface(ThemeHelper.dynamicSurface())
                .padding(Insets.of(6));

        layout.child(mainLayout);
    }

    public void buildHeritageInfo(FlowLayout layout){
        layout.child(Containers.verticalFlow(Sizing.content(), Sizing.content()));
    }
}
