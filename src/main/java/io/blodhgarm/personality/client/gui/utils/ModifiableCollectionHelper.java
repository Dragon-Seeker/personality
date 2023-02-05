package io.blodhgarm.personality.client.gui.utils;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public interface ModifiableCollectionHelper<T, V> {

    void setFilter(Predicate<V> filter);
    void setComparator(Comparator<V> comparator);

    Predicate<V> getFilter();
    Comparator<V> getComparator();

    default T sortAndFilterCharacters(Comparator<V> comparator, Predicate<V> filter){
        boolean updateListFromComp = !(comparator == null && getComparator() == null);
        boolean updateListFromFilter = !(filter == null && getFilter() == null);

        this.setComparator(comparator);
        this.setFilter(filter);

        if(updateListFromComp || updateListFromFilter) applyFiltersAndSorting();

        return (T) this;
    }

    default T sortCharacters(Comparator<V> comparator){
        boolean updateList = !(comparator == null && this.getComparator() == null);

        this.setComparator(comparator);

        if(updateList) applyFiltersAndSorting();

        return (T) this;
    }

    default T filterCharacters(Predicate<V> filter){
        boolean updateList = !(filter == null && this.getFilter() == null);

        this.setFilter(filter);

        if(updateList) applyFiltersAndSorting();

        return (T) this;
    }

    default void applyFiltersAndSorting(){
        getList().clear();

        List<V> filteredList = (this.getFilter() != null)
                ? getDefaultList().stream().filter(this.getFilter()).toList()
                : getDefaultList();

        getList().addAll(filteredList);

        if(this.getComparator() != null) getList().sort(this.getComparator());
    }

    List<V> getList();
    List<V> getDefaultList();
}
