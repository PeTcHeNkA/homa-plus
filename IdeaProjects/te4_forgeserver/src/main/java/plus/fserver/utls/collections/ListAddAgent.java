package plus.fserver.utls.collections;

import org.jetbrains.annotations.NotNull;
import java.util.*;


@FunctionalInterface
public interface ListAddAgent<T> extends List<T> {
    @Override
    default void add(int index, T element) {
        add(element);
    }


    @Override
    default T remove(int index) {
        throw new UnsupportedOperationException("Agent not supports this");
    }


    @Override
    default T get(int index) {
        throw new UnsupportedOperationException("Agent not supports this");
    }


    @Override
    default T set(int index, T element) {
        throw new UnsupportedOperationException("Agent not supports this");
    }


    @Override
    default int size() {return 0;}
    @Override
    default boolean isEmpty() {return true;}
    @Override
    default boolean contains(Object o) {return false;}
    @Override
    default Iterator<T> iterator() {return ArrayListE6.EMPTY_ITR;}
    @Override
    default Object[] toArray() {return ArrayListE6.EMPTY_ARR;}
    @Override
    default <T1> T1[] toArray(@NotNull T1[] a) {return (T1[]) ArrayListE6.EMPTY_ARR;}
    @Override
    default boolean remove(Object o) {return false;}
    @Override
    default boolean containsAll(@NotNull Collection<?> c) {return false;}
    @Override
    default boolean addAll(@NotNull Collection<? extends T> c) {return false;}
    @Override
    default boolean addAll(int index, @NotNull Collection<? extends T> c) {return false;}
    @Override
    default boolean removeAll(@NotNull Collection<?> c) {return false;}
    @Override
    default boolean retainAll(@NotNull Collection<?> c) {return false;}
    @Override
    default void clear() {}
    @Override
    default int indexOf(Object o) {return -1;}
    @Override
    default int lastIndexOf(Object o) {return -1;}
    @Override
    default ListIterator<T> listIterator() {return HoleList.EMPTY_ITER;}
    @Override
    default ListIterator<T> listIterator(int index) {return HoleList.EMPTY_ITER;}
    @Override
    default List<T> subList(int fromIndex, int toIndex) {return this;}
}
