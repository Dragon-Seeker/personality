package io.blodhgarm.personality.client.gui.utils;

import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface ModifiableCollectionHelper<T, V> {

    void setFilter(FilterFunc<V> filter);
    void setComparator(Comparator<V> comparator);

    FilterFunc<V> getFilter();
    Comparator<V> getComparator();

    default T sortAndFilterEntries(Comparator<V> comparator, FilterFunc<V> filter){
        boolean updateListFromComp = !(comparator == null && getComparator() == null);
        boolean updateListFromFilter = !(filter == null && getFilter() == null);

        this.setComparator(comparator);
        this.setFilter(filter);

        if(updateListFromComp || updateListFromFilter) applyFiltersAndSorting();

        return (T) this;
    }

    default T sortEntries(@Nullable Comparator<V> comparator){
        boolean updateList = !(comparator == null && this.getComparator() == null);

        this.setComparator(comparator);

        if(updateList) applyFiltersAndSorting();

        return (T) this;
    }

    default T filterEntries(@Nullable Predicate<V> filter){
        return filterEntriesFunc(filter != null
                ? (helper) -> helper.getDefaultList().stream().filter(filter).toList()
                : null
        );
    }

    default T filterEntriesFunc(@Nullable FilterFunc<V> filter){
        boolean updateList = !(filter == null && this.getFilter() == null);

        this.setFilter(filter);

        if(updateList) applyFiltersAndSorting();

        return (T) this;
    }

    default void applyFiltersAndSorting(){
        getList().clear();

        List<V> filteredList = (this.getFilter() != null)
                ? this.getFilter().filter(this)
                : getDefaultList();

        getList().addAll(filteredList);

        if(this.getComparator() != null) getList().sort(this.getComparator());
    }

    List<V> getList();
    List<V> getDefaultList();

    interface FilterFunc<V> {
        List<V> filter(ModifiableCollectionHelper<?, V> helper);
    }
}
