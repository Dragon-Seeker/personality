package io.blodhgarm.personality.client.gui.screens;

import com.mojang.authlib.GameProfile;
import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.api.character.CharacterManager;
import io.blodhgarm.personality.client.ClientCharacters;
import io.blodhgarm.personality.client.gui.ThemeHelper;
import io.blodhgarm.personality.client.gui.components.owo.character.CharacterGridLayout;
import io.blodhgarm.personality.client.gui.components.owo.LabeledGridLayout;
import io.blodhgarm.personality.client.gui.components.ButtonAddon;
import io.blodhgarm.personality.client.gui.components.owo.CustomButtonComponent;
import io.blodhgarm.personality.client.gui.components.owo.MultiToggleButton;
import io.blodhgarm.personality.client.gui.utils.CustomSurfaces;
import io.blodhgarm.personality.client.gui.utils.ModifiableCollectionHelper;
import io.blodhgarm.personality.client.gui.utils.owo.VariantButtonSurface;
import io.blodhgarm.personality.misc.pond.owo.ButtonAddonDuck;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.Drawer;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.ToStringFunction;
import me.xdrop.fuzzywuzzy.model.BoundExtractedResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AdminCharacterScreen extends BaseOwoScreen<FlowLayout> implements ModifiableCollectionHelper<Void, Character> {

    private int charactersPerPage = 100;
    private int waitTimeToUpdatePages = -1;

    private int pageCount = 0;
    private int currentPageNumber = 1;

    private final List<Character> defaultCharacterView = List.copyOf(ClientCharacters.INSTANCE.characterLookupMap().valueList());
    private final List<Character> characterView = new ArrayList<>();

    private List<Character> selectedCharacters = new ArrayList<>();

    private SearchType type = SearchType.STRICT;

    @Nullable private FilterFunc<Character> cached_filter = null;
    @Nullable private Comparator<Character> cached_comparator = null;

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    public void tick() {
        if(waitTimeToUpdatePages == 0){
            TextBoxComponent component = this.uiAdapter.rootComponent.childById(TextBoxComponent.class, "per_page_amount");

            if(!component.getText().isEmpty()) {
                try {
                    charactersPerPage = Integer.parseInt(component.getText());

                    buildPageBar(this.uiAdapter.rootComponent);

                    travelToSelectPage(1);
                } catch (NumberFormatException e) { throw new RuntimeException(e); }
            }

            waitTimeToUpdatePages = -1;
        } else if(waitTimeToUpdatePages > 0){
            waitTimeToUpdatePages--;
        }

        super.tick();
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        FlowLayout mainLayout = Containers.verticalFlow(Sizing.content(), Sizing.content())
                .configure(layout -> {
                    layout.surface(ThemeHelper.dynamicSurface())
                            .horizontalAlignment(HorizontalAlignment.CENTER)
                            .padding(Insets.of(4));
                });

        FlowLayout topActionBar = Containers.horizontalFlow(Sizing.content(), Sizing.content())
                .configure(layout -> {
                    layout.verticalAlignment(VerticalAlignment.CENTER);
                });

        topActionBar.child(
                Components.textBox(Sizing.fixed(200), "")
                        .configure((TextBoxComponent component) -> {
                            component.onChanged()
                                    .subscribe(value -> {
                                        type.filterAndSort(value, Character::getName, this);

                                        updateCharacterListComponent();
                                    });
                        })
                        .id("main_search_box")
                        .margins(Insets.of(3, 3, 4, 4))
        );

        topActionBar.child(
                new CustomButtonComponent(Text.of(""), buttonComponent -> {
                    type = type.getNextType();
                    type.setButtonTextForNext(buttonComponent);

                    type.filterAndSort(rootComponent.childById(TextBoxComponent.class, "main_search_box").getText(), Character::getName, this);

                    updateCharacterListComponent();
                }).configure((CustomButtonComponent c) -> {
                    c.sizing(Sizing.fixed(11), Sizing.fixed(11));

                    type.setButtonTextForNext(c);

                    c.setYTextOffset(2);
                    c.setFloatPrecision(true);
                })
        );

        //TODO: Future development to add functionality to the Screen with the similar functions to the already existing commands
        if(CharacterManager.hasModerationPermissions(MinecraftClient.getInstance().player)){
            //Associate
            //Disassociate
        }

        if(CharacterManager.hasAdministrationPermissions(MinecraftClient.getInstance().player)){
            //Edit
            //Kill
            //Delete
        }

        mainLayout.child(topActionBar);

        List<MultiToggleButton> buttons = new ArrayList<>();

        mainLayout.child(Containers.verticalScroll(Sizing.content(), Sizing.fixed(155),
                new CharacterGridLayout(Sizing.content(), Sizing.content(), this, (mode, baseCharacter) -> new CharacterScreen(mode, null, baseCharacter, true))
                        .addBuilder(0,
                            isParentVertical -> {
                                return Containers.horizontalFlow(Sizing.content(), Sizing.content())
                                        .child(buildButton(
                                            buttons,
                                            component -> {
                                                this.sortEntries(null);
                                            },
                                            component -> {
                                                this.sortEntries((ch1, ch2) -> {
                                                    boolean character1Selected = this.selectedCharacters.contains(ch1);
                                                    boolean character2Selected = this.selectedCharacters.contains(ch2);

                                                    int compValue = 1;

                                                    if(character1Selected == character2Selected) compValue = 0;
                                                    if(character1Selected) compValue = -1;

                                                    return compValue;
                                                });
                                            },
                                            component -> {
                                                this.sortEntries((ch1, ch2) -> {
                                                    boolean character1Selected = this.selectedCharacters.contains(ch1);
                                                    boolean character2Selected = this.selectedCharacters.contains(ch2);

                                                    int compValue = -1;

                                                    if(character1Selected == character2Selected) compValue = 0;
                                                    if(character1Selected) compValue = 1;

                                                    return compValue;
                                                });
                                            }
                                        ));
                            },
                            (character, mode, isParentVertical) -> {
                                return Components.button(Text.of(""), buttonComponent -> {
                                        if(!selectedCharacters.contains(character)) {
                                            selectedCharacters.add((Character) character);
                                        } else {
                                            selectedCharacters.remove((Character) character);
                                        }
                                    }).renderer((matrices, button, delta) -> {
                                        boolean isSelected = selectedCharacters.contains(character);

                                        ButtonComponent.Renderer.VANILLA.draw(matrices, button, delta);

                                        if(isSelected) {
                                            Drawer.drawRectOutline(matrices, button.x + 2, button.y + 2, button.width() - 4, button.height() - 4, new Color(0.95f, 0.95f, 0.95f).argb());
                                        }
                                    })
                                    .sizing(Sizing.fixed(8));
                            }
                        )
                        .addBuilder(
                            isParentVertical -> {
                                return Components.label(Text.of("Created By"));
                            },
                            (character, mode, isParentVertical) -> {
                                MutableText name = Text.literal("");

                                AtomicBoolean wasSuccessful = new AtomicBoolean();

                                try {
                                    GameProfile profile = MinecraftClient.getInstance().getSessionService()
                                            .fillProfileProperties(new GameProfile(UUID.fromString(character.getPlayerUUID()), ""), false);

                                    String playerNameString = profile.getName();

                                    if(!playerNameString.isBlank()){
                                        name = Text.literal(playerNameString);

                                        wasSuccessful.set(true);
                                    }
                                } catch (IllegalArgumentException ignored){}

                                if(!wasSuccessful.get()){
                                    name = (!character.getPlayerUUID().isBlank()
                                            ? Text.literal(character.getPlayerUUID())
                                            : Text.literal("Error: Character missing Player UUID Info!"))
                                            .formatted(Formatting.RED);
                                }

                                return Components.label(name)
                                        .maxWidth(125)
                                        .margins(Insets.of(2))
                                        .configure(component -> {
                                            if(!wasSuccessful.get()) component.tooltip(Text.of("Could not find the given Players name, showing UUID"));
                                        });
                            }
                        )
                        .setRowDividingLine(1)
                        .setColumnDividingLine(1)
                        .id("character_list")
                ).surface(CustomSurfaces.INVERSE_PANEL)
                .padding(Insets.of(4))
        );

        FlowLayout pageBar = Containers.horizontalFlow(Sizing.content(), Sizing.content())
                .configure(layout -> {
                    layout.verticalAlignment(VerticalAlignment.CENTER)
                            .margins(Insets.vertical(2))
                            .id("page_bar");
                });

        mainLayout.child(pageBar);

        FlowLayout bottomActionBar = Containers.horizontalFlow(Sizing.content(), Sizing.content());

        //--------------------------------------------------------


        //--------------------------------------------------------

        mainLayout.child(bottomActionBar);

        rootComponent.child(mainLayout.positioning(Positioning.relative(50, 50)));

        applyFiltersAndSorting();
    }

    public void buildPageBar(FlowLayout rootComponent){
        FlowLayout pageBar = rootComponent.childById(FlowLayout.class, "page_bar");

        pageBar.clearChildren();

        this.pageCount = MathHelper.ceil(characterView.size() / (float) charactersPerPage);

        // 9 Max count

        int startingPageNumber;

        if(pageCount < 9 || this.currentPageNumber <= 4){
            startingPageNumber = 1;
        } else {
            startingPageNumber = this.currentPageNumber - 4;
        }

        int maxPageCount = Math.min(startingPageNumber + 9, pageCount);

        //Used to show least the first page when there is only a single page
        if(pageCount == 1) maxPageCount++;

        for(int currentPageNumber = startingPageNumber; currentPageNumber < maxPageCount; currentPageNumber++){
            int componentPageNumber = currentPageNumber;

            String buttonId = String.valueOf(componentPageNumber);

            pageBar.child(
                    Containers.horizontalFlow(Sizing.fixed(12), Sizing.fixed(12))
                            .child(
                                    Components.label(Text.literal(buttonId))
                                            .id("button_label")
                            )
                            .horizontalAlignment(HorizontalAlignment.CENTER)
                            .verticalAlignment(VerticalAlignment.CENTER)
                            .configure(component -> {
                                ((ButtonAddonDuck<FlowLayout>) component)
                                        .setButtonAddon(layout -> {
                                            return new ButtonAddon<>(layout)
                                                    .useCustomButtonSurface(VariantButtonSurface.surfaceLike(Size.square(3), Size.square(48), false, ThemeHelper.isDarkMode(), false))
                                                    .onPress(button -> {
                                                            if(this.currentPageNumber != componentPageNumber){
                                                                travelToSelectPage(componentPageNumber);

                                                                buildPageBar(this.uiAdapter.rootComponent);
                                                            }
                                                    });
                                        }).margins((componentPageNumber != startingPageNumber) ? Insets.left(2) : Insets.of(0));
                            }).id("page_button_" + buttonId)
            );
        }

        for(int i = 1; i <= pageCount; i++){
            FlowLayout buttonComponent = this.uiAdapter.rootComponent.childById(FlowLayout.class, "page_button_" + i);

            if(buttonComponent != null) {
                buttonComponent.childById(LabelComponent.class, "button_label")
                        .color(i == this.currentPageNumber ? Color.BLUE.interpolate(Color.WHITE, 0.35f) : ThemeHelper.dynamicColor());
            }
        }
    }

    public void travelToSelectPage(int newPageNumber){
        LabeledGridLayout gridLayout = this.uiAdapter.rootComponent.childById(LabeledGridLayout.class, "character_list");

        int startingIndex = (newPageNumber - 1) * this.charactersPerPage;
        int endingIndex = Math.min(startingIndex + this.charactersPerPage, this.characterView.size());

        gridLayout.clearEntries();

        if(this.characterView.size() > 0) {
            gridLayout.addEntries(this.characterView
                    .subList(startingIndex, endingIndex));
        }

        gridLayout.rebuildComponents();

        for(int i = 1; i <= pageCount; i++){
            FlowLayout buttonComponent = this.uiAdapter.rootComponent.childById(FlowLayout.class, "page_button_" + i);

            if(buttonComponent != null) {
                buttonComponent.childById(LabelComponent.class, "button_label")
                        .color(i == newPageNumber ? Color.BLUE.interpolate(Color.WHITE, 0.35f) : ThemeHelper.dynamicColor());
            }
        }

        this.currentPageNumber = newPageNumber;
    }

    @SafeVarargs
    private MultiToggleButton buildButton(List<MultiToggleButton> buttons, Consumer<ButtonComponent> ...onToggleEvents){
        return (MultiToggleButton) MultiToggleButton.of(
                List.of(
                    new MultiToggleButton.ToggleVariantInfo("-", "Normal Order", onToggleEvents[0]),
                    new MultiToggleButton.ToggleVariantInfo("⏶", "Sort Up", onToggleEvents[1]),
                    new MultiToggleButton.ToggleVariantInfo("⏷", "Sort Down", onToggleEvents[2])
                )
        ).linkButton(buttons)
                .sizing(Sizing.fixed(9), Sizing.fixed(9)); //12
    }

    //-------------------------------------------------------


    @Override public void setFilter(FilterFunc<Character> filter) { this.cached_filter = filter; }
    @Override public FilterFunc<Character> getFilter() { return this.cached_filter; }

    @Override public void setComparator(Comparator<Character> comparator) { this.cached_comparator = comparator; }
    @Override public Comparator<Character> getComparator() { return this.cached_comparator; }

    public void updateCharacterListComponent(){
        buildPageBar(this.uiAdapter.rootComponent);

        travelToSelectPage(1);
    }

    @Override
    public void applyFiltersAndSorting() {
        ModifiableCollectionHelper.super.applyFiltersAndSorting();

        updateCharacterListComponent();
    }

    @Override public List<Character> getList() { return this.characterView; }
    @Override public List<Character> getDefaultList() { return this.defaultCharacterView; }

    public enum SearchType {
        STRICT("*", "Toggle Strict Filtering"),
        FUZZY("~", "Toggle Fuzzy Filtering");

        public final String buttonText;
        public final String tooltipText;

        SearchType(String buttonText, String tooltipText){
            this.buttonText = buttonText;
            this.tooltipText = tooltipText;
        }

        public <V> void filterAndSort(String query, ToStringFunction<V> toStringFunc, ModifiableCollectionHelper<?, V> helper){
            switch (this){
                case STRICT -> {
                    Predicate<V> filter = null;

                    if(!query.isEmpty()) {
                        String regex = Arrays.stream(query.toLowerCase().split(" "))
                                .filter(s -> !s.trim().equals(""))
                                .collect(Collectors.joining("|"));

                        filter = v -> Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
                                .asPredicate()
                                .test(toStringFunc.apply(v));
                    }

                    helper.filterEntries(filter);
                }
                case FUZZY -> {
                    helper.filterEntriesFunc(!query.isEmpty()
                            ? helper1 -> FuzzySearch.extractAll(query, helper.getDefaultList(), toStringFunc, 60)
                                    .stream()
                                    .map(BoundExtractedResult::getReferent)
                                    .toList()
                            : null);
                }
            }
        };

        public SearchType getNextType(){
            SearchType[] types = SearchType.values();

            int nextIndex = this.ordinal() + 1;

            if(nextIndex >= types.length) nextIndex = 0;

            return types[nextIndex];
        }

        public void setButtonTextForNext(ButtonComponent component){
            SearchType nextType = getNextType();

            component.setMessage(Text.of(nextType.buttonText));
            component.tooltip(Text.of(nextType.tooltipText));
        }
    }
}
