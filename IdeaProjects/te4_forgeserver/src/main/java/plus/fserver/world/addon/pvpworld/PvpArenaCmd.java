package plus.fserver.world.addon.pvpworld;

import com.google.common.collect.ImmutableList;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.DimensionManager;
import org.jetbrains.annotations.Nullable;
import plus.fserver.api.ServerCommand;
import plus.fserver.api.WorldConstructor;
import plus.tson.TsonFile;
import java.io.File;
import java.util.*;


public class PvpArenaCmd extends ServerCommand {
    private final Map<String, MinigameController> data = Collections.synchronizedMap(new HashMap<>());
    private final List<String> completions = ImmutableList.of(
            "clean-memory", "read", "save", "place"
    );

    public PvpArenaCmd() {
        super("pvparena", "/pvparena <args>", 4);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        switch (args[0]){
            case "clean-memory": {
                data.clear();
                return;
            }
            case "read":{
                readFromCmd(server, sender, args);
                return;
            }
            case "save":{
                saveTo(server, sender, args);
                return;
            }
            case "place":{
                place(server, sender, args);
            }
        }
    }


    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return this.completions;
    }


    private void saveTo(MinecraftServer server, ICommandSender sender, String[] args){
        String id = args[1];
        String fileName = args[2];

        MinigameController arena = data.get(id);
        if(arena == null){
            onUnknown(sender, id);
            return;
        }
        File file = new File(fileName);
        if(file.isDirectory()){
            reply(sender, "&cCant save arena: target path is directory");
            return;
        }
        TsonFile.createPathIfNeed(file);
        try {
            MinigameController.saveTo(arena, file);
        } catch (Throwable e){
            onError(sender, e);
            e.printStackTrace();
            return;
        }
        onSuccess(sender);
    }


    private void onUnknown(ICommandSender sender, String id){
        reply(sender, "&cCant save arena: unknown ID [%s], select one of %s", Arrays.toString(data.keySet().toArray()));
    }


    private static void onSuccess(ICommandSender sender){
        reply(sender, "&aSuccess!");
    }


    private void readFromCmd(MinecraftServer server, ICommandSender sender, String[] args){
        String id = args[1];
        String fileName = args[2];

        File file = new File(fileName);
        if(file.isDirectory()){
            reply(sender, "&cCant load arena from dir: Select path to NBT file");
            return;
        }
        if(!file.exists()){
            reply(sender, "&cCant load arena: File dont exists");
            return;
        }
        try {
            //MinigameController arena = MinigameController.readFrom(file);
            //data.put(id, arena);
        } catch (Throwable e){
            onError(sender, e);
            e.printStackTrace();
            return;
        }
        onSuccess(sender);
    }


    private void place(MinecraftServer server, ICommandSender sender, String[] args) throws NumberInvalidException {
        String id = args[1];
        MinigameController arena = data.get(id);
        if(arena == null){
            onUnknown(sender, id);
            return;
        }
        int dimId = parseInt(args[2]);

        try {
            WorldConstructor.register(dimId, arena.constructor(), arena.getDimensionType());
            DimensionManager.registerDimension(dimId, arena.getDimensionType());
        } catch (Throwable e){
            onError(sender, e);
            e.printStackTrace();
        }
    }
}
