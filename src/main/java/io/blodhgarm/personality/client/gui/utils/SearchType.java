package io.blodhgarm.personality.client.gui.utils;

import io.blodhgarm.personality.client.gui.screens.AdminCharacterScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.ToStringFunction;
import me.xdrop.fuzzywuzzy.model.BoundExtractedResult;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public enum SearchType {
    STRICT("*", "Toggle Strict Filtering"),
    FUZZY("~", "Toggle Fuzzy Filtering");

    public final String buttonText;
    public final String tooltipText;

    SearchType(String buttonText, String tooltipText) {
        this.buttonText = buttonText;
        this.tooltipText = tooltipText;
    }

    public <V> void filterAndSort(String query, ToStringFunction<V> toStringFunc, ModifiableCollectionHelper<?, V> helper) {
        switch (this) {
            case STRICT -> {
                Predicate<V> filter = null;

                if (!query.isEmpty()) {
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
    }

    public SearchType getNextType() {
        SearchType[] types = SearchType.values();

        int nextIndex = this.ordinal() + 1;

        if (nextIndex >= types.length) nextIndex = 0;

        return types[nextIndex];
    }

    public void setButtonTextForNext(ButtonComponent component) {
        SearchType nextType = getNextType();

        component.setMessage(Text.of(nextType.buttonText));
        component.tooltip(Text.of(nextType.tooltipText));
    }
}
