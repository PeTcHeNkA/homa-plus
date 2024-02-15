package plus.fserver.world.generator.struct;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.Loader;
import plus.fserver.api.ServerCommand;
import plus.fserver.world.generator.settings.BiomeChecker;
import plus.tson.TsonClass;
import plus.tson.TsonFile;
import plus.tson.TsonMap;
import plus.tson.security.ClassManager;
import java.io.File;

//НЕ ТРОГАТЬ
//IBlockState state = world.getBlockState(pos);
//world.notifyBlockUpdate(pos, state, state, spawnChance);
public class Structure extends WorldStructure implements IGlobStruct{
    public final BiomeChecker biomeChecker;

    public Structure(TsonMap map){
        super(map);
        if(map.containsKey("biomes")) {
            biomeChecker = BiomeChecker.build(map.getList("biomes"));
        } else {
            biomeChecker = BiomeChecker.EMPTY;
        }
    }


    public static IStruct build(TsonMap map){
        String type = map.getOrDefaultStr("type","#");
        return (IStruct) new TsonClass(manager(), type).createInst(map);
    }


    static {
        loadRepository();
        new ServerCommand("tstruct", "<name/list> <pos? x,y,z>", 4) {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
                Entity e = (Entity) sender;
                if(args.length==0){
                    sender.sendMessage(new TextComponentString("structure is null!"));
                    return;
                }
                String name = args[0];
                if(name.equalsIgnoreCase("list")){
                    StringBuilder list = new StringBuilder();
                    int id = 0;
                    for(IStruct struct:buffer){
                        if(struct != null){
                            list.append(id).append("|").append(struct).append("\n");
                        }
                        ++id;
                    }
                    sender.sendMessage(new TextComponentString(list.toString()));
                    return;
                } else {
                    int x = (int) e.posX, y = (int) e.posY, z = (int) e.posZ;
                    if(args.length==4){
                        x = Integer.parseInt(args[1]);
                        y = Integer.parseInt(args[2]);
                        z = Integer.parseInt(args[3]);
                    }
                    getById(Integer.parseInt(name)).generate(e.world, x, y, z);
                    e.world.markBlocksDirtyVertical(x, z, y-18, y+18);

                }
            }
        }.register();
    }


    public static ClassManager manager() {
        return new ClassManager() {
            @Override
            public Class<?> forName(String clazz) {
                switch (clazz) {
                    case "#":
                    case "normal":
                        return Structure.class;
                    case "##":
                    case "single":
                    case "simple":
                        return SimpleStruct.class;
                    case "<%>":
                    case "choose":
                    case "o-ofs":
                        return OneOfSeveralStructure.class;
                    case "<w>":
                    case "wo-ofs":
                        return WorldOneOfSeveralStruct.class;
                    case "#+":
                    case "multi":
                        return MultiStructure.class;
                    case ">#+":
                    case "sub-multi":
                        return SubMultiStructure.class;
                    case ">#":
                    case "sub-single":
                    case "sub-simple":
                    case "sub":
                        return SubStructure.class;
                    case "world":
                    case "w":
                        return WorldStructure.class;
                    case "w+":
                        return WorldMultiStructure.class;
                }
                return ClassManager.super.forName(clazz);
            }
        };
    }


    public static void loadRepository() {
        new TsonFile(new File(Loader.instance().getConfigDir(), "struct-gen.tson"))
                .load(manager(),
                        "{structures=[<(normal), {id=(1), chance=(0.5), biomes=[\"plains\"], " +
                                "spawn-at=[\"grass\"], structure=\"test\", max-pow=(50), pos=[(0),(1),(0)], add=[(0)], " +
                                "settings=[[\"rot-90\"], [\"struct-block\"], [\"entities\", \"m-left\", \"m-front\", \"ignore:air\"]]}>], " +
                                "classes=[(normal), (sub-simple), (w), (w+)" +
                                "(multi), (sub-multi),(choose)]}"
                );
    }


    public static void clearWithEmptyTemplates(){
        for(int i=0;i<buffer.length;i++) {
            IStruct istruct = buffer[i];
            if(istruct != null) {
                if (istruct instanceof SimpleStruct) {
                    SimpleStruct s = (SimpleStruct) istruct;
                    if (s.template == null || s instanceof Structure) {
                        buffer[i] = null;
                    }
                }
            }
        }
    }


    @Override
    public BiomeChecker getBiomeChecker() {
        return biomeChecker;
    }
}