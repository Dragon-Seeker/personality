package io.blodhgarm.personality.client.gui.screens;

import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.character.BaseCharacter;
import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.api.reveal.KnownCharacter;
import io.blodhgarm.personality.client.ClientCharacters;
import io.blodhgarm.personality.client.gui.ThemeHelper;
import io.blodhgarm.personality.client.gui.CharacterViewMode;
import io.blodhgarm.personality.client.gui.components.SponsorComponent;
import io.blodhgarm.personality.client.gui.components.character.CharacterBasedGridLayout;
import io.blodhgarm.personality.client.gui.components.grid.LabeledGridLayout;
import io.blodhgarm.personality.client.gui.components.character.CharacterViewComponent;
import io.blodhgarm.personality.client.gui.utils.owo.ExtraSurfaces;
import io.blodhgarm.personality.utils.DebugCharacters;
import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.fabricmc.loader.api.FabricLoader;
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

    private CharacterViewComponent characterScreen;

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

//        if(!PersonalityMod.CONFIG.disableSponsorComponent()) {
//            this.addGlobalBuilder(layout -> {
//                return new SponsorComponent(Sizing.content(), Sizing.content())
//                        .positioning(Positioning.relative(99, 99));
//            });
//        }

        registeredBuilders.put(characterInformation.id, characterInformation);
        registeredBuilders.put(knownCharacters.id, knownCharacters);
//        registeredBuilders.put(heritageInfo.id, heritageInfo);
    }

    public Component buildCharacterInfoTab(FlowLayout rootComponent){
        Character playerCharacter = ClientCharacters.INSTANCE.getCharacter(MinecraftClient.getInstance().player);

        if(playerCharacter == null && FabricLoader.getInstance().isDevelopmentEnvironment()){
            playerCharacter = DebugCharacters.REVEAL_TEST;
        }

        this.characterScreen = new CharacterViewComponent(CharacterViewMode.VIEWING, MinecraftClient.getInstance().player.getGameProfile(), playerCharacter);

        FlowLayout mainLayout = characterScreen.buildComponent(rootComponent, false);

        mainLayout.positioning(Positioning.relative(250, 65));

        mainLayout.positioning().animate(1000, Easing.CUBIC, Positioning.relative(50, 65)).forwards();

        mainLayout.allowOverflow(true);

        return mainLayout;
    }

    public Component buildKnownCharacterTab(FlowLayout rootComponent){
        this.currentCharacter = ClientCharacters.INSTANCE.getCharacter(MinecraftClient.getInstance().player);

        if(this.currentCharacter == null) this.currentCharacter = DebugCharacters.getRevealTest(ClientCharacters.INSTANCE);

        List<BaseCharacter> knownCharacters = new ArrayList<>(this.currentCharacter.getKnownCharacters().values());

        if(knownCharacters.isEmpty() && !DebugCharacters.KNOWN_CHARACTERS.isEmpty()) knownCharacters.addAll(DebugCharacters.getKnownCharacters(ClientCharacters.INSTANCE));

        FlowLayout knownCharacterLayout = Containers.verticalFlow(Sizing.content(), Sizing.content())
                .child(
                        new CharacterBasedGridLayout(Sizing.content(), Sizing.content(),this)
//                                .changeDirection(((ScrollContainerAccessor) knownCharacterContainer).personality$direction() == ScrollContainer.ScrollDirection.VERTICAL)'
                                .addBuilder(Text.of("Friendliness"), (c, mode, isVertical) -> Components.label(((KnownCharacter) c).level.getTranslation()))
                                .setRowDividingLine(1)
                                .setColumnDividingLine(1)
                                .addEntries(knownCharacters)
                                .id("knownCharacterList")
                );

        FlowLayout mainLayout = Containers.verticalFlow(Sizing.content(), Sizing.fixed(182))
                .configure((FlowLayout layout) -> {
                        layout.horizontalAlignment(HorizontalAlignment.CENTER)
                                .surface(ThemeHelper.dynamicSurface())
                                .padding(Insets.of(6));
                }).child(
                        Containers.verticalScroll(Sizing.content(), Sizing.fill(85), knownCharacterLayout)
                                .surface(ExtraSurfaces.INVERSE_PANEL)
                                .padding(Insets.of(4))
                ).child(
                        Components.textBox(Sizing.fixed(205), "")
                                .configure(component -> {
                                    ((TextBoxComponent) component).onChanged()
                                            .subscribe(value -> {
                                                LabeledGridLayout listLayout = rootComponent.childById(LabeledGridLayout.class, "knownCharacterList");

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

                                                listLayout.filterEntries(filter);
                                            });
                                })
                                .margins(Insets.vertical(3))

                );

        mainLayout.positioning(Positioning.relative(200, 65));

        mainLayout.positioning().animate(1000, Easing.LINEAR, Positioning.relative(50, 65)).forwards();


        return mainLayout;
    }

    public Component buildHeritageInfo(FlowLayout rootComponent){
        return Containers.verticalFlow(Sizing.content(), Sizing.content());
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
