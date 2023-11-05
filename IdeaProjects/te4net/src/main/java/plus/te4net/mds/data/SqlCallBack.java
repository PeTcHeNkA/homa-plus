package plus.te4net.mds.data;

import java.sql.ResultSet;
import java.sql.SQLException;


public interface SqlCallBack {
    void onResult(ResultSet result) throws SQLException;
}
