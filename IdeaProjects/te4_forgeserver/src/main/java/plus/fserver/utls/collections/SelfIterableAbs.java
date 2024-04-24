package plus.fserver.utls.collections;

import com.google.common.collect.AbstractIterator;
import org.jetbrains.annotations.NotNull;
import java.util.Iterator;


public abstract class SelfIterableAbs<T> extends AbstractIterator<T> implements Iterable<T>, Iterator<T> {
    @NotNull
    @Override
    public final Iterator<T> iterator() {
        return this;
    }
}