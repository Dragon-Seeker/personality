package io.blodhgarm.personality.client.gui.screens;

import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.client.ClientCharacters;
import io.blodhgarm.personality.client.ThemeHelper;
import io.blodhgarm.personality.client.gui.CharacterScreenMode;
import io.blodhgarm.personality.client.gui.TabbedScreen;
import io.blodhgarm.personality.client.gui.components.CustomSurfaces;
import io.blodhgarm.personality.client.gui.components.ListedCharactersComponentBuilder;
import io.blodhgarm.personality.utils.DebugCharacters;
import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
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

        if(this.currentCharacter == null) this.currentCharacter = DebugCharacters.DEBUG_5;

        List<Character> knownCharacters = new ArrayList<>();

        this.currentCharacter.knowCharacters.forEach((s, knownCharacter) -> {
            Character character = ClientCharacters.INSTANCE.getCharacter(knownCharacter.characterUUID);

            if(character != null){
                knownCharacters.add(character);
            }
        });

        if(knownCharacters.isEmpty()) knownCharacters.addAll(DebugCharacters.DEBUG_CHARACTERS);

        FlowLayout knownCharacterLayout = Containers.verticalFlow(Sizing.fixed(140), Sizing.content());

        ScrollContainer<FlowLayout> knownCharacterContainer = Containers.verticalScroll(Sizing.content(), Sizing.fill(100), knownCharacterLayout);

        new ListedCharactersComponentBuilder(() -> knownCharacters)
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
