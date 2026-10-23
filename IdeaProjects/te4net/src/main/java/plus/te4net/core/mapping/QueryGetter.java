package plus.te4net.core.mapping;

import plus.te4net.core.api.RequestSession;
import plus.tson.format.IVarGetter;
import java.util.Objects;


public class QueryGetter implements IVarGetter<RequestSession> {
    private final String label;

    public QueryGetter(String label) {
        this.label = label;
    }


    @Override
    public String get(RequestSession request) {
        return request.queryParams(label);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(label, ((QueryGetter) o).label);
    }
}
