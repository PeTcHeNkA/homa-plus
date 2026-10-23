package plus.util.sql;

import java.util.List;


public interface CommandExecutor {
    String execute(List<String> args);
}
