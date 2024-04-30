package plus.fserver.utls.collections;

import org.jetbrains.annotations.NotNull;
import java.util.Iterator;


public final class EmptySelfIter<S> implements Iterable<S>, Iterator<S> {
    @NotNull
    @Override
    public Iterator<S> iterator() {
        return this;
    }


    @Override
    public boolean hasNext() {
        return false;
    }


    @Override
    public S next() {
        return null;
    }
}
