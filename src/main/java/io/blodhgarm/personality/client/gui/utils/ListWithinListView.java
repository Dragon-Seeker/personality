package io.blodhgarm.personality.client.gui.utils;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Wrapper Class used to turn any List of Lists into a single List of the Lists Generic Type
 * @param <E> Generic of the Inner Lists
 */
public class ListWithinListView<E> extends AbstractList<E> {

    protected final List<List<E>> listWithinList;

    public ListWithinListView(){
        this.listWithinList = new ArrayList<>();
    }

    public ListWithinListView(List<List<E>> listWithinList){
        this.listWithinList = listWithinList;
    }

    public void addList(List<E> list){
        this.listWithinList.add(list);
    }

    public void addList(int index, List<E> list){
        this.listWithinList.add(index, list);
    }

    public boolean removeList(List<E> list){
        return this.listWithinList.remove(list);
    }

    @Override
    public E get(int index) {
        AtomicInteger indexNum = new AtomicInteger(index);

        for(List<E> list : listWithinList){
            if (!list.isEmpty() && (indexNum.get() < list.size())) return list.get(indexNum.get());

            indexNum.set(indexNum.get() - list.size());
        }

        Objects.checkIndex(index, size());

        throw new RuntimeException();
    }

    @Override
    public int size() {
        return listWithinList.stream().mapMultiToInt((es, intConsumer) -> intConsumer.accept(es.size())).sum();
    }
}
