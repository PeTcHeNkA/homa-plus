package plus;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.GameRegistry;
import plus.gen.Te4WorldGen;
import plus.tson.TsonFile;
import plus.tson.TsonList;
import java.io.File;


@Mod(
        modid = MainDungeons.MOD_ID,
        name = MainDungeons.MOD_NAME,
        version = MainDungeons.VERSION,
        acceptableRemoteVersions = "*"
)
public class MainDungeons {
    public static final String MOD_ID = "te4_dungeons";
    public static final String MOD_NAME = "Te4_dungeons";
    public static final String VERSION = "1.0-SNAPSHOT";
    public static final boolean PROD = false;


    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        File file = new File(Minecraft.getMinecraft().gameDir, "templates");
        if(file.exists()){
            file.mkdirs();
        }
        GameRegistry.registerWorldGenerator(new Te4WorldGen(), 0);
    }


    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {}


    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event) {
        if(PROD==true)return;
        TsonFile file = new TsonFile(new File(Loader.instance().getConfigDir(), "biomes"));
        TsonList list = file.addList("biomes");
        for(ResourceLocation loc:Biome.REGISTRY.getKeys()){
            list.add(loc.toString());
        }
        file.save();
    }
}