package io.blodhgarm.personality.client.gui.screens;

import com.mojang.authlib.GameProfile;
import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.api.character.CharacterManager;
import io.blodhgarm.personality.client.ClientCharacters;
import io.blodhgarm.personality.client.gui.ThemeHelper;
import io.blodhgarm.personality.client.gui.builders.EnhancedGridLayout;
import io.blodhgarm.personality.client.gui.components.ButtonAddon;
import io.blodhgarm.personality.client.gui.utils.CustomSurfaces;
import io.blodhgarm.personality.client.gui.utils.ModifiableCollectionHelper;
import io.blodhgarm.personality.client.gui.utils.owo.VariantButtonSurface;
import io.blodhgarm.personality.misc.pond.owo.ButtonAddonDuck;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
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

    @Nullable
    private Predicate<Character> cached_filter = null;

    @Nullable
    private Comparator<Character> cached_comparator = null;

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
                } catch (NumberFormatException e) {
                    throw new RuntimeException(e);
                }
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

        FlowLayout topActionBar = Containers.horizontalFlow(Sizing.content(), Sizing.content());

        topActionBar.child(
                Components.textBox(Sizing.fixed(200), "")
                        .configure(component -> {
                            ((TextBoxComponent) component).onChanged()
                                    .subscribe(value -> {
                                        Predicate<Character> filter = null;

                                        if(!value.isEmpty()) {
                                            String regex = Arrays.stream(value.toLowerCase()
                                                            .split(" "))
                                                    .filter(s -> !s.trim().equals(""))
                                                    .collect(Collectors.joining("|"));

                                            var pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

                                            filter = character -> pattern.asPredicate().test(character.getName());
                                        }

                                        this.filterCharacters(filter);
                                    });
                        })
                        .margins(Insets.of(3, 3, 4, 4))
        );

        //TODO: Future development to add functionality to the Screen with the similar functions to the already existing commands
        if(CharacterManager.hasModerationPermissions(MinecraftClient.getInstance().player)){}
        if(CharacterManager.hasAdministrationPermissions(MinecraftClient.getInstance().player)){}

        mainLayout.child(topActionBar);

        mainLayout.child(Containers.verticalScroll(Sizing.content(), Sizing.fixed(155),
                new EnhancedGridLayout(Sizing.content(), Sizing.content(), this, (characterScreenMode, baseCharacter) -> new CharacterScreen(characterScreenMode, null, baseCharacter, true))
                        .addBuilder(Text.of("Created By"), (character, mode, isParentVertical) -> {
                            MutableText name;

                            AtomicBoolean hasErrored = new AtomicBoolean(false);

                            try {
                                GameProfile profile = MinecraftClient.getInstance().getSessionService()
                                        .fillProfileProperties(new GameProfile(UUID.fromString(character.getPlayerUUID()), ""), false);

                                name = Text.literal(profile.getName());
                            } catch (IllegalArgumentException ignored){
                                hasErrored.set(true);

                                name = (!character.getPlayerUUID().isBlank()
                                        ? Text.literal(character.getPlayerUUID())
                                        : Text.literal("Error: Character missing Player UUID Info!"))
                                            .formatted(Formatting.RED);
                            }

                            return Components.label(name)
                                    .maxWidth(100)
                                    .margins(Insets.of(2))
                                    .configure(component -> {
                                        if(hasErrored.get()) component.tooltip(Text.of("Could not find the given Players name, showing UUID"));
                                    });
                        })
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
        EnhancedGridLayout gridLayout = this.uiAdapter.rootComponent.childById(EnhancedGridLayout.class, "character_list");

        int startingIndex = (newPageNumber - 1) * this.charactersPerPage;
        int endingIndex = Math.min(startingIndex + this.charactersPerPage, this.characterView.size());

        gridLayout.clearCharacters();

        if(this.characterView.size() > 0) {
            gridLayout.addCharacters(this.characterView
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


    //-------------------------------------------------------


    @Override public void setFilter(Predicate<Character> filter) { this.cached_filter = filter; }
    @Override public Predicate<Character> getFilter() { return this.cached_filter; }

    @Override public void setComparator(Comparator<Character> comparator) { this.cached_comparator = comparator; }
    @Override public Comparator<Character> getComparator() { return this.cached_comparator; }

    @Override
    public void applyFiltersAndSorting() {
        ModifiableCollectionHelper.super.applyFiltersAndSorting();

        buildPageBar(this.uiAdapter.rootComponent);

        travelToSelectPage(1);
    }

    @Override public List<Character> getList() { return this.characterView; }
    @Override public List<Character> getDefaultList() { return this.defaultCharacterView; }
}
