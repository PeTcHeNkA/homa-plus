package plus.te4net.core.data;

import plus.te4net.core.api.PublicUsage;
import java.sql.ResultSet;
import java.sql.SQLException;


@PublicUsage
public interface SqlCallBack {
    void onResult(ResultSet result) throws SQLException;
}