package plus.util.sql;

import java.util.List;


public interface DataFloader {

    List<String> getData(String quest);

    void putData(String user, List<String> labels);
}