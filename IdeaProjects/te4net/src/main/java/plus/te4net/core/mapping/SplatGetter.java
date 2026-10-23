package plus.te4net.core.mapping;

import plus.te4net.core.api.RequestSession;
import plus.tson.format.IVarGetter;


public class SplatGetter implements IVarGetter<RequestSession> {
    private final int index;

    public SplatGetter(int index) {
        this.index = index;
    }


    @Override
    public String get(RequestSession session) {
        String[] data = session.splat();
        if(index>=data.length)return null;
        return data[index];
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return index == ((SplatGetter) o).index;
    }
}
