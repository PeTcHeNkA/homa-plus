package plus.fmsg;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.fmsg.cmd.TsonCommand;
import plus.tson.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Mod(modid = "tson_messages", name = "TsonMessages", version = "1.0")
public class TsonMessagesMain {
    private static List<TsonCommand> readCommands(FMLServerStartingEvent event){
        Logger logger = LogManager.getLogger("TsonMessages");
        try {
            String data = TsonFile.remBadChars(TsonFile.read("commands.tson"));
            TsonMap map = new STsonParser(data).getMap();
            if(map==null) throw new RuntimeException("Invalid commands.tson");

            ArrayList<TsonCommand> commands = new ArrayList<>();

            for (TsonObj command : map.getList("commands")) {
                TsonMap cmd;
                try {
                    cmd = command.getMap();
                    commands.add(new TsonCommand(cmd));
                } catch (Exception e) {
                    logger.error("Failed to load command: \n-------------\n{}\n-------------\n", formatCmd(command), e);
                }
            }
            return commands;
        } catch (Exception e) {
            logger.error("Failed to read commands.tson", e);
            return Collections.emptyList();
        }
    }


    private static String formatCmd(TsonObj obj){
        if(obj==null) return "null";
        return new TJsonWriter(obj, 2, 48, true).toString();
    }


    @Mod.EventHandler
    public void onServerStart(FMLServerStartingEvent event) {
        for (TsonCommand command : readCommands(event)) {
            event.registerServerCommand(command);
        }
    }
}