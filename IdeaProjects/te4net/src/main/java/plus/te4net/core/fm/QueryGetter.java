package plus.te4net.core.fm;

import plus.te4net.core.api.IRequestSession;
import plus.tson.format.IVarGetter;
import java.util.Objects;


public class QueryGetter implements IVarGetter<IRequestSession> {
    private final String label;

    public QueryGetter(String label) {
        this.label = label;
    }

    @Override
    public String get(IRequestSession request) {
        return request.queryParams(label);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(label, ((QueryGetter) o).label);
    }
}
