package plus.fserver.plugin;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;
import plus.tson.TsonCSVParser;
import plus.tson.TsonList;
import plus.tson.TsonObj;
import plus.tson.security.ClassManager;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import static plus.fserver.event.ElcaCV.*;


public class SSPatchLoader extends TweakEngine implements IClassTransformer, ITweaker{
    public static final String ELCA_RELEASE = "Elca Enterprise Edition";
    public static final String ELCA_VERSION;

    static {
        String version = "2.3.7.04";
        boolean isDev = true;

        int num  = 0;
        boolean[] flags = {
            ENABLE_COLLISION_EVENTS       ,
            ENABLE_EXPLOSION_EVENT        ,
            AABB_MEMORY_IMPROVEMENT       ,
            ENABLE_NEIGHBOR_NOTIFY_EVENT  ,
            DEBUG_NO_SERVER               ,
            CHECK_LEGAL_CHUNK_CHANGE      ,
            MORE_MOBS_OPTIMIZER           ,
        };
        for (int i=0;i< flags.length;i++)
            if(flags[i]) num |= 1<<i;

        version += "-x"+Integer.toHexString(num);
        if(isDev){
            version += "-dev";
        }
        ELCA_VERSION = version;
    }

    public SSPatchLoader(){
        super("plus/fserver/plugin/SSPatchLoader.class");
        addPatch();
        addOverrides();
    }


    private void addPatch(){
        byte[] table = getFastSrc("elcapatch.csv");
        TsonList list = new TsonCSVParser(new ClassManager.Empty(), new String(table).toCharArray(), 2).getList();

        for (TsonObj obj:list){
            TsonList line = obj.getList();
            String clazz = line.getStr(0);
            TsonObj safe = line.size()>1?line.get(1):null;
            add(clazz, safe != null && safe.getBool());
        }
    }


    private void addOverrides(){
        add("net.minecraft.server.MinecraftServer", true);
        add("net.minecraft.server.MinecraftServer$1", true);
        add("net.minecraft.server.MinecraftServer$2", true);
        add("net.minecraft.server.dedicated.DedicatedServer", true);
        add("net.minecraft.server.dedicated.DedicatedServer$1", true);
        add("net.minecraft.server.dedicated.DedicatedServer$2", true);
        add("net.minecraftforge.common.DimensionManager", true);
        add("net.minecraftforge.common.chunkio.ChunkIOExecutor", true);
    }


    public static void printLogo(){
        String logo = "\n" +
                "     _____  _             \n" +
                "    | ____|| |  ___  __ _ \n" +
                "    |  _|  | | / __|/ _` |\n" +
                "    | |___ | || (__| (_| |\n" +
                "    |_____||_| \\___|\\__,_|\n" +
                "\n" +
                "  ___ ___  ____ _  __ ___  ____\n" +
                " (_-</ -_)/ __/| |/ // -_)/ __/\n" +
                "/___/\\__//_/   |___/ \\__//_/   v"+ ELCA_VERSION +"\n\n";
        LogManager.getLogger("ELCA").info(logo);
    }


    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {}


    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.fserver.json");
        try {
            editTransformers(classLoader);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            classLoader.registerTransformer("plus.fserver.plugin.SSPatchLoader");
        }
        preLoadClasses();
        try {
            ElcaBiomes.registerIfNeed();
        } catch (Throwable t){
            t.printStackTrace();
        }
    }


    private List<IClassTransformer> transformers(LaunchClassLoader classLoader) throws NoSuchFieldException, IllegalAccessException {
        Field field = LaunchClassLoader.class.getDeclaredField("transformers");
        field.setAccessible(true);
        return  (List<IClassTransformer>) field.get(classLoader);
    }


    private void editTransformers(LaunchClassLoader classLoader) throws NoSuchFieldException, IllegalAccessException {
        List<IClassTransformer> res = transformers(classLoader);
        res.add(this);
    }


    @Override
    public String getLaunchTarget() {
        return "net.minecraft.server.MinecraftServer";
    }


    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }
}