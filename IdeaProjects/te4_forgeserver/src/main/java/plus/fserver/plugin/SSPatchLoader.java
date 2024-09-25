package plus.fserver.plugin;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import static plus.fserver.ElcaCV.*;


public class SSPatchLoader implements ITweaker{
    public static final String ELCA_RELEASE = "Elca Enterprise Edition";
    public static final String ELCA_VERSION;

    static {
        String version = "3.0.0.41";
        boolean isDev = true;

        long num  = 0;
        boolean[] flags = {
            ENABLE_COLLISION_EVENTS      ,
            ENABLE_EXPLOSION_EVENT       ,
            AABB_MEMORY_IMPROVEMENT      ,
            ENABLE_NEIGHBOR_NOTIFY_EVENT ,
            CHECK_LEGAL_CHUNK_CHANGE     ,
            MORE_MOBS_OPTIMIZER          ,
            DISABLE_FORGE_FARMLAND       ,
            ALLOW_ANY_PROJECTIVE_TNT     ,
            CHECK_BI_ARRAY               ,
            ENABLE_NOTCH_SECRET          ,
            ENTITY_FAST_WET              ,
            FIX_NULL_HASH_PALETTE        ,
            SERVER_COMPILE               ,
            INTERN_NBT_KEYS              ,
            ENABLE_PACKET_DEBUG
        };
        for (int i=0;i< flags.length;i++)
            if(flags[i]) num |= 1<<i;

        version += "-x"+Long.toHexString(num);
        if(isDev){
            version += "-dev";
        }
        ELCA_VERSION = version;
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
            classLoader.registerTransformer("plus.fserver.plugin.SSPathTransformer");
        }
    }


    private List<IClassTransformer> transformers(LaunchClassLoader classLoader) throws NoSuchFieldException, IllegalAccessException {
        Field field = LaunchClassLoader.class.getDeclaredField("transformers");
        field.setAccessible(true);
        return (List<IClassTransformer>) field.get(classLoader);
    }


    private void editTransformers(LaunchClassLoader classLoader) throws NoSuchFieldException, IllegalAccessException {
        List<IClassTransformer> res = transformers(classLoader);
        SSPathTransformer transformer = new SSPathTransformer(false);
        res.add(transformer);
        transformer.init();
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