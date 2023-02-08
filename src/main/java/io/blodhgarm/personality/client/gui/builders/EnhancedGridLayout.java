package io.blodhgarm.personality.client.gui.builders;

import com.mojang.logging.LogUtils;
import io.blodhgarm.personality.api.character.BaseCharacter;
import io.blodhgarm.personality.client.gui.CharacterScreenMode;
import io.blodhgarm.personality.client.gui.components.LineComponent;
import io.blodhgarm.personality.client.gui.components.owo.CustomEntityComponent;
import io.blodhgarm.personality.client.gui.screens.CharacterScreen;
import io.blodhgarm.personality.client.gui.utils.ListWithinListView;
import io.blodhgarm.personality.client.gui.utils.ModifiableCollectionHelper;
import io.blodhgarm.personality.client.gui.utils.owo.LineEvent;
import io.blodhgarm.personality.misc.pond.owo.LineManageable;
import io.blodhgarm.personality.utils.ReflectionUtils;
import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class EnhancedGridLayout extends BaseParentComponent implements LineManageable<EnhancedGridLayout>, ModifiableCollectionHelper<EnhancedGridLayout, BaseCharacter> {

    private static final Logger LOGGER = LogUtils.getLogger();

    //--------------------------------------------------------------------

    protected final List<PerCharacterComponentHelper> builders = new ArrayList<>();

    protected final ListOrderedMap<BaseCharacter, List<Component>> characterToComponents = new ListOrderedMap<>();
    protected final List<BaseCharacter> originalCharactersList = new ArrayList<>();

    public boolean builtYet = false;

    private CharacterScreenMode mode = CharacterScreenMode.VIEWING;

    private BiFunction<CharacterScreenMode, BaseCharacter, CharacterScreen> screenBuilder = (characterScreenMode, baseCharacter) -> {
        return new CharacterScreen(characterScreenMode, null, baseCharacter);
    };

    private Screen originScreen;

    private boolean isVertical = true;

    protected int rowDividingLineWidth = -1;
    protected int columnDividingLineWidth = -1;

    protected Color rowDividingLineColor = Color.BLACK;
    protected Color columnDividingLineColor = Color.BLACK;

    @Nullable protected Predicate<BaseCharacter> cached_filter = null;
    @Nullable protected Comparator<BaseCharacter> cached_comparator = null;

    //--------------------------------------------------------------------

    protected List<Component> lines = new ArrayList<>();

    private final List<LineEvent> lineEvents = new ArrayList<>();

    private boolean setupInteractionEvents = false;

    //--------------------------------------------------------------------

    public int[] prevColumnSizes = new int[]{};
    public int[] prevRowSizes = new int[]{};

    protected final ListWithinListView<Component> gridChildrenView = new ListWithinListView<>(characterToComponents.valueList());
    protected final ListWithinListView<Component> finalChildrenView = new ListWithinListView<>();

    protected Size contentSize = Size.zero();

    //--------------------------------------------------------------------

    public EnhancedGridLayout(Sizing horizontalSizing, Sizing verticalSizing, Screen originScreen, BiFunction<CharacterScreenMode, BaseCharacter, CharacterScreen> screenBuilder) {
        this(horizontalSizing, verticalSizing, originScreen);

        this.screenBuilder = screenBuilder;
    }

    public EnhancedGridLayout(Sizing horizontalSizing, Sizing verticalSizing, Screen originScreen) {
        super(horizontalSizing, verticalSizing);

        this.originScreen = originScreen;

        this.characterToComponents.put(null, new ArrayList<>());

        finalChildrenView.addList(gridChildrenView);
        finalChildrenView.addList(lines);

        builders.add(PerCharacterComponentHelper.of(Text.empty(),
                        (character, mode, isParentVertical) ->{
                                FlowLayout mainLayout = Containers.verticalFlow(Sizing.fixed(28), Sizing.fixed(24));

                                mainLayout.child(Components.button(Text.of(mode.isModifiableMode() ? "✎" : "☉"), (ButtonComponent component) -> {
                                                    CharacterScreen screen = screenBuilder.apply(this.mode, character);

                                                    screen.originScreen = this.originScreen;

                                                    MinecraftClient.getInstance().setScreen(screen);
                                                }).sizing(Sizing.fixed(10)) //13
                                                .positioning(Positioning.absolute(16, 1))
                                                .zIndex(10)
                                );

                                mainLayout.child(CustomEntityComponent.playerEntityComponent(Sizing.fixed(20), null)
                                        .scale(0.4f)
                                        .allowMouseRotation(true)
//                                        .tooltip(character.getFormattedName())
                                        .margins(Insets.of(4,0,4,0))
                                );

                                return mainLayout;
//                                        .margins(Insets.of(2));
                        }
                )
        );

        builders.add(PerCharacterComponentHelper.of(Text.of("Name"),
                        (character, mode1, isParentVertical) -> Containers.verticalFlow(Sizing.fixed(96), Sizing.content())
                                .child(Components.label(character.getFormattedName())
                                        .maxWidth(100)
                                        .margins(Insets.of(2))
                                )
//                                .margins(Insets.of(2))
                ).onlyAllowWhenVertical(true)
        );
    }

    //-----

    public EnhancedGridLayout setRowDividingLine(int width){
        this.rowDividingLineWidth = width;

        return this;
    }

    public EnhancedGridLayout setColumnDividingLine(int width){
        this.columnDividingLineWidth = width;

        return this;
    }

    public EnhancedGridLayout setRowDividingLineColor(Color color){
        this.rowDividingLineColor = color;

        return this;
    }

    public EnhancedGridLayout setColumnDividingLineColor(Color color){
        this.columnDividingLineColor = color;

        return this;
    }

    //-----

    public EnhancedGridLayout changeMode(CharacterScreenMode mode){
        this.mode = mode;

        return this;
    }

    public EnhancedGridLayout changeDirection(boolean vertical){
        this.isVertical = vertical;

        return this;
    }

    public EnhancedGridLayout addBuilder(Text text, PerCharacterComponentHelper.PerCharacterBuilder builder){
        this.builders.add(PerCharacterComponentHelper.of(text, builder));

        return this;
    }

    public EnhancedGridLayout addBuilder(PerCharacterComponentHelper builder){
        this.builders.add(builder);

        return this;
    }

    public EnhancedGridLayout addCharacter(BaseCharacter character){
        if(character != null) {
            List<Component> characterComponents = new ArrayList<>();

            this.characterToComponents.put(character, characterComponents);

            this.originalCharactersList.add(character);
        }

        return this;
    }

    public <T extends BaseCharacter> EnhancedGridLayout removeCharacter(T character){
        List<Component> characterComponents = this.characterToComponents.remove(character);

        this.originalCharactersList.remove(character);

        return this;
    }

    public <T extends BaseCharacter> EnhancedGridLayout addCharacters(Collection<T> characters){
        for(BaseCharacter character : characters) addCharacter(character);

        return this;
    }

    public <T extends BaseCharacter> EnhancedGridLayout removeCharacters(Collection<T> characters){
        for(BaseCharacter character : characters) removeCharacter(character);

        return this;
    }

    public EnhancedGridLayout clearCharacters(){
        this.characterToComponents.valueList().forEach(components -> {
            components.forEach(component -> component.dismount(DismountReason.REMOVED));
        });

        List<Component> headerInfo = this.characterToComponents.get(null);

        this.characterToComponents.clear();

        this.characterToComponents.put(null, headerInfo);

        this.updateLayout();

        return this;
    }

    @Override public void setFilter(Predicate<BaseCharacter> filter) { this.cached_filter = filter; }
    @Override public Predicate<BaseCharacter> getFilter() { return this.cached_filter; }
    @Override public void setComparator(Comparator<BaseCharacter> comparator) { this.cached_comparator = comparator; }
    @Override public Comparator<BaseCharacter> getComparator() { return this.cached_comparator; }

    @Override public List<BaseCharacter> getList() { return getMapInsertOrder();}
    @Override public List<BaseCharacter> getDefaultList() { return originalCharactersList; }

    @Override
    public void applyFiltersAndSorting(){
        if(!this.builtYet) this.buildComponents();

        ModifiableCollectionHelper.super.applyFiltersAndSorting();

        getList().add(0, null);

        this.updateLayout();
    }

    protected List<BaseCharacter> getMapInsertOrder(){
        return ReflectionUtils.getMapInsertOrder(this.characterToComponents);
    }

    //--------------------------------------------------------------------

    public void rebuildComponents(){
        this.builtYet = false;

        this.updateLayout();
    }

    public void buildComponents(){
        if(this.builtYet) return;

//        boolean isVertical = true; //((ScrollContainerAccessor) parent).personality$direction() == ScrollContainer.ScrollDirection.VERTICAL;

        if (isVertical) {
            this.verticalAlignment(VerticalAlignment.CENTER);
        } else {
            this.horizontalAlignment(HorizontalAlignment.CENTER);
        }

        List<PerCharacterComponentHelper> builders = this.builders.stream()
                .filter(wrapper -> !wrapper.onlyShowWhenVertical || isVertical)
                .toList();

        builders.forEach(helper -> {
            characterToComponents.get(null).add(helper.buildLabel(false));
        });

        characterToComponents.forEach((character, components) -> {
            if(character != null) components.addAll(characterComponent(builders, character, isVertical));
        });

        this.builtYet = true;
    }

    public List<Component> characterComponent(List<PerCharacterComponentHelper> wrappers, BaseCharacter character, boolean isParentVertical){
        List<Component> mainComponentList = new ArrayList<>();

        wrappers.forEach(componentBuilder -> {
            mainComponentList.add(componentBuilder.buildPerCharacterComponent(character, this.mode, isParentVertical));
        });

        return mainComponentList;
    }

    private int getColumnSize(){
        return this.isVertical ? this.builders.size() : getMapInsertOrder().size();
    }

    private int getRowSize(){
        return this.isVertical ? getMapInsertOrder().size() : this.builders.size();
    }

    //--------------------------------------------------------------------

    @Override
    public List<Component> getLines() {
        return this.lines;
    }

    @Override
    public List<LineEvent> getLineEvents() {
        return this.lineEvents;
    }

    @Override
    public boolean hasSetupInteractionEvents() {
        return setupInteractionEvents;
    }

    @Override
    public void toggleSetupInteractionEvents() {
        setupInteractionEvents = !setupInteractionEvents;
    }

    //--------------------------------------------------------------------

    //This section is taken from GridLayout and modified to work with the Custom Map and List

    @Override
    protected int determineHorizontalContentSize(Sizing sizing) {
        return this.contentSize.width() + this.padding.get().right();
    }

    @Override
    protected int determineVerticalContentSize(Sizing sizing) {
        return this.contentSize.height() + this.padding.get().bottom();
    }

    @Override
    public void layout(Size space) {
        boolean rebuildSizingArrays = !this.builtYet;

        this.buildComponents();

        this.getLines().forEach(component -> component.dismount(DismountReason.REMOVED));
        this.getLines().clear();

        int[] columnSizes = new int[getColumnSize()];
        int[] rowSizes = new int[getRowSize()];

        var childSpace = this.calculateChildSpace(space);

        this.finalChildrenView.forEach((child) -> {
            if (child != null) {
                child.inflate(childSpace);
            }
        });

        if(rebuildSizingArrays) {
            this.determineSizes(columnSizes, false);
            this.determineSizes(rowSizes, true);

            this.prevColumnSizes = columnSizes;
            this.prevRowSizes = rowSizes;
        } else {
            columnSizes = prevColumnSizes;
            rowSizes = prevRowSizes;
        }

        var mountingOffset = this.childMountingOffset();
        var layoutX = new MutableInt(this.x + mountingOffset.width());
        var layoutY = new MutableInt(this.y + mountingOffset.height());

        //------------

        var addedColumnDividers = false;

        var addColumnDividers = columnDividingLineWidth > 0;
        var addRowDividers = rowDividingLineWidth > 0;

        Insets columnMargin = Insets.horizontal(3);
        Insets rowMargin = Insets.vertical(3);

        int totalColumnSize = Arrays.stream(columnSizes).sum() + ((columnMargin.horizontal() + columnDividingLineWidth) * this.getColumnSize());
        int totalRowSize = Arrays.stream(rowSizes).sum() + ((rowMargin.vertical() + rowDividingLineWidth) * this.getRowSize());
        //------------

        for (int row = 0; row < getRowSize(); row++) {
            layoutX.setValue(this.x + mountingOffset.width());

            int rowSize = rowSizes[row];

            for (int column = 0; column < getColumnSize(); column++) {
                int columnSize = columnSizes[column];

                this.mountChild(this.getChild(row, column), childSpace, child -> {
                    child.mount(
                            this,
                            layoutX.intValue() + child.margins().get().left() + this.horizontalAlignment().align(child.fullSize().width(), columnSize),
                            layoutY.intValue() + child.margins().get().top() + this.verticalAlignment().align(child.fullSize().height(), rowSize)
                    );
                });

                layoutX.add(columnSizes[column]);

                if(addColumnDividers && column + 1 != this.getColumnSize()) {
                    if(!addedColumnDividers) {
                        int x1 = layoutX.intValue() + Math.round(columnDividingLineWidth / 2f) - this.x();
                        int y1 = layoutY.intValue() - this.y();

                        int x2 = x1;
                        int y2 = y1 + totalRowSize;

                        LineComponent columnComponent = (LineComponent) new LineComponent(x1, y1, x2, y2)
                                .color(columnDividingLineColor)
                                .setLineWidth(columnDividingLineWidth)
                                .margins(columnMargin.add(0,0, 0, 0));

                        if(lines.isEmpty()){
//                            columnComponent.id("debug_line_on");

//                            LOGGER.info(" -- -- ");
//                            LOGGER.info("[DebugLineRender]: X: {}, Y: {}", this.x(), this.y());
//                            LOGGER.info("[DebugLineRender]: Line Specs [x1: {}, y1: {}, x2: {}, y2: {}]", x1, y1, x2, y2);
                        }

                        this.addLine(columnComponent);

                        this.mountChild(columnComponent, childSpace, child -> {
                            child.mount(this,
                                    layoutX.intValue(),
                                    layoutY.intValue()
                            );
                        });
                    }

                    layoutX.add(columnDividingLineWidth + columnMargin.horizontal());
                }
            }

            layoutY.add(rowSizes[row]);

            if(addColumnDividers && !addedColumnDividers) addedColumnDividers = true;

            if(addRowDividers && row + 1 != this.getRowSize()){
                layoutX.setValue(this.x + mountingOffset.width());

                int x1 = layoutX.intValue() - this.x();
                int y1 = layoutY.intValue() + Math.round(rowDividingLineWidth / 2f) - this.y(); //+ (row == 0 ? 0 : Math.round(rowMargin.vertical() / 2f));

                int x2 = x1 + totalColumnSize;
                int y2 = y1;

                LineComponent rowComponent = (LineComponent) new LineComponent(x1, y1, x2, y2)
                        .color(rowDividingLineColor)
                        .setLineWidth(rowDividingLineWidth)
                        .margins(row == 0 ? Insets.of(0) : rowMargin.add(0,0, 0,0));

                this.addLine(rowComponent);

                this.mountChild(rowComponent, childSpace, child -> {
                    child.mount(this,
                            layoutX.intValue(),
                            layoutY.intValue()
                    );
                });

                layoutY.add(rowDividingLineWidth + rowMargin.vertical());
            }
        }

        this.contentSize = Size.of(layoutX.intValue() - this.x, layoutY.intValue() - this.y);
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(matrices, mouseX, mouseY, partialTicks, delta);

        this.lines.forEach(component -> {
            if(component.id() != null && Objects.equals(component.id(), "debug_line_on")){
//                LOGGER.info("x: {}, y: {}, width: {}, height:{}", component.x(), component.y(), component.width(), component.height());

//                component.id("debug_line_off");
            }
        });

        this.drawChildren(matrices, mouseX, mouseY, partialTicks, delta, this.finalChildrenView);
    }

    protected @Nullable Component getChild(int row, int column) {
        return this.characterToComponents.getValue(isVertical ? row : column).get(isVertical ? column : row);
    }

    protected void determineSizes(int[] sizes, boolean rows) {
        if ((rows ? this.verticalSizing : this.horizontalSizing).get().method != Sizing.Method.CONTENT) {
            Arrays.fill(sizes, (rows ? this.height - this.padding().get().vertical() : this.width - this.padding().get().horizontal()) / (rows ? getRowSize() : getColumnSize()));
        } else {
            for (int row = 0; row < getRowSize(); row++) {
                for (int column = 0; column < getColumnSize(); column++) {
                    final var child = this.getChild(row, column);
                    if (child == null) continue;

                    if (rows) {
                        sizes[row] = Math.max(sizes[row], child.fullSize().height());
                    } else {
                        sizes[column] = Math.max(sizes[column], child.fullSize().width());
                    }
                }
            }
        }
    }

    @Override
    public EnhancedGridLayout removeChild(Component child) {
        return this;
    }

    @Override
    public List<Component> children() { return this.finalChildrenView; }

    //-----------------------------------------
}
