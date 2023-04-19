package io.blodhgarm.personality.client.gui.components.grid;

import io.blodhgarm.personality.client.gui.components.CustomButtonComponent;
import io.blodhgarm.personality.client.gui.utils.ModifiableCollectionHelper;
import io.blodhgarm.personality.client.gui.utils.SearchType;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.HorizontalFlowLayout;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import me.xdrop.fuzzywuzzy.ToStringFunction;
import net.minecraft.text.Text;

public class SearchbarComponent<T> extends HorizontalFlowLayout {

    private SearchType type = SearchType.STRICT;

    private final Runnable updateAction;

    private final ModifiableCollectionHelper<?, T> helper;
    private final ToStringFunction<T> toStringFunc;

    private Sizing textboxWidth = Sizing.fixed(80);

    public SearchbarComponent(ModifiableCollectionHelper<?, T> helper, ToStringFunction<T> toStringFunc, Runnable updateAction) {
        super(Sizing.content(), Sizing.content());

        this.helper = helper;
        this.toStringFunc = toStringFunc;

        this.updateAction = updateAction;
    }

    public SearchbarComponent(ModifiableCollectionHelper<?, T> helper, ToStringFunction<T> toStringFunc) {
        this(helper, toStringFunc, () -> helper.applyFiltersAndSorting());
    }

    public SearchbarComponent<T> adjustTextboxWidth(Sizing textboxWidth){
        this.textboxWidth = textboxWidth;

        return this;
    }

    public SearchbarComponent<T> build(){
        this.child(
                Components.textBox(textboxWidth, "")
                        .configure((TextBoxComponent component) -> {
                            component.onChanged()
                                    .subscribe(value -> {
                                        type.filterAndSort(value, toStringFunc, helper);

                                        updateAction.run();
                                    });
                        })
                        .id("main_search_box")
                        .margins(Insets.of(3, 3, 4, 4))
        ).child(
                new CustomButtonComponent(Text.empty(), buttonComponent -> {
                    type = type.getNextType();
                    type.setButtonTextForNext(buttonComponent);

                    type.filterAndSort(this.childById(TextBoxComponent.class, "main_search_box").getText(), toStringFunc, helper);

                    updateAction.run();
                }).configure((CustomButtonComponent c) -> {
                    c.sizing(Sizing.fixed(11), Sizing.fixed(11));

                    type.setButtonTextForNext(c);

                    c.setYTextOffset(2);
                    c.setFloatPrecision(true);
                })
        );

        return this;
    }
}
