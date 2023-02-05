package io.blodhgarm.personality.client.gui.screens;

import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.character.BaseCharacter;
import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.api.reveal.KnownCharacter;
import io.blodhgarm.personality.client.ClientCharacters;
import io.blodhgarm.personality.client.gui.ThemeHelper;
import io.blodhgarm.personality.client.gui.CharacterScreenMode;
import io.blodhgarm.personality.client.gui.builders.EnhancedGridLayout;
import io.blodhgarm.personality.client.gui.utils.CustomSurfaces;
import io.blodhgarm.personality.utils.DebugCharacters;
import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
//        registeredBuilders.put(heritageInfo.id, heritageInfo);
    }

    public void buildCharacterInfoTab(FlowLayout layout){
        Character playerCharacter = ClientCharacters.INSTANCE.getCharacter(MinecraftClient.getInstance().player);

        this.characterScreen = new CharacterScreen(CharacterScreenMode.VIEWING, MinecraftClient.getInstance().player, playerCharacter != null ? playerCharacter : DebugCharacters.DEBUG_5);

        characterScreen.buildAsChild(layout);
    }

    public void buildKnownCharacterTab(FlowLayout layout){
        FlowLayout mainLayout = Containers.verticalFlow(Sizing.content(), Sizing.fixed(182));

        this.currentCharacter = ClientCharacters.INSTANCE.getCharacter(MinecraftClient.getInstance().player);

        if(this.currentCharacter == null) this.currentCharacter = DebugCharacters.getRevealTest(ClientCharacters.INSTANCE);

        List<BaseCharacter> knownCharacters = new ArrayList<>(this.currentCharacter.getKnownCharacters().values());

        if(knownCharacters.isEmpty() && !DebugCharacters.KNOWN_CHARACTERS.isEmpty()) knownCharacters.addAll(DebugCharacters.getKnownCharacters(ClientCharacters.INSTANCE));

        FlowLayout knownCharacterLayout = Containers.verticalFlow(Sizing.content(), Sizing.content()); //140

        ScrollContainer<FlowLayout> knownCharacterContainer = Containers.verticalScroll(Sizing.content(), Sizing.fill(85), knownCharacterLayout);

        knownCharacterLayout.child(
            new EnhancedGridLayout(Sizing.content(), Sizing.content(),this)
//                    .changeDirection(((ScrollContainerAccessor) knownCharacterContainer).personality$direction() == ScrollContainer.ScrollDirection.VERTICAL)
                    .setRowDividingLine(1)
                    .setColumnDividingLine(1)
                    .addCharacters(knownCharacters)
                    .addBuilder(Text.of("Friendliness"), (baseCharacter, mode, isParentVertical) -> Components.label(((KnownCharacter) baseCharacter).level.getTranslation()))
                    .id("knownCharacterList")
        );

        mainLayout.child(
                knownCharacterContainer
                        .surface(CustomSurfaces.INVERSE_PANEL)
                        .padding(Insets.of(4))
        );

        mainLayout
                .surface(ThemeHelper.dynamicSurface())
                .padding(Insets.of(6));

        mainLayout.child(
                Components.textBox(Sizing.fill(40), "")
                        .configure(component -> {
                            ((TextBoxComponent) component).onChanged()
                                    .subscribe(value -> {
                                        EnhancedGridLayout listLayout = layout.childById(EnhancedGridLayout.class, "knownCharacterList");

                                        Predicate<BaseCharacter> filter = null;

                                        if(!value.isEmpty()) {
                                            String regex = Arrays.stream(value.toLowerCase()
                                                            .split(" "))
                                                    .filter(s -> !s.trim().equals(""))
                                                    .collect(Collectors.joining("|"));

                                            var pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

                                            filter = character -> pattern.asPredicate().test(character.getName());
                                        } else {
                                            System.out.println("weeeeeeeeeeeeeeeeeeeeeee");
                                        }

                                        listLayout.filterCharacters(filter);
                                    });
                        })
                        .margins(Insets.vertical(3))

        );

        mainLayout.horizontalAlignment(HorizontalAlignment.CENTER);

        layout.child(mainLayout);
    }

    public void buildHeritageInfo(FlowLayout layout){
        layout.child(Containers.verticalFlow(Sizing.content(), Sizing.content()));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
