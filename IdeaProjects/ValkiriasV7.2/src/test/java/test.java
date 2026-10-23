
import plus.item.stat.StatGenerator;
import plus.item.stat.StatLabel1;
import plus.quest.memory.AbstractMemorySector;
import plus.quest.memory.QuestMemory;
import plus.quest.script.quest.logic.Conduction;
import plus.quest.script.quest.logic.Mather;
import plus.util.cfg.Sector;
import plus.util.sql.CommandExecutor;
import plus.util.sql.QuerryExucator;
import plus.util.sql.YandereDBServer;
import plus.valcirias.data.ClassStats;
import java.lang.reflect.Field;
import plus.util.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class test{

    public static void main(String args[]){
          try {
              //new  test();
              test();
          }catch (Exception e){ e.printStackTrace(); }
    }

    public static double D1;

    public static void test() throws InterruptedException {
        YandereDBServer server = new YandereDBServer(new QuerryExucator() {
            @Override
            public CommandExecutor getExucator(String command) {
                return new CommandExecutor() {
                    @Override
                    public String execute(List<String> args) {
                        return "TEST";
                    }
                };
            }
        }, 4971);



    }

}







