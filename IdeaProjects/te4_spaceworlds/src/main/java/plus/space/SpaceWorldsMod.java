package plus.space;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.Side;
import plus.space.common.proxy.CommonProxy;
import plus.space.common.world.biome.TsonBiomePack;
import plus.tson.TsonClass;


@Mod(modid = SpaceWorldsMod.MOD_ID, name = "Space Worlds", version = "1.1", acceptableSaveVersions = "*")
public final class SpaceWorldsMod {
    public static final String MOD_ID = "spaceworlds";
    private static CommonProxy proxy;


    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        TsonBiomePack.loadPack(Constants.CLIENT);
        if(proxy==null){
            proxy = (CommonProxy) new TsonClass(Constants.CLIENT?"" +
                    "plus.space.common.proxy.ClientProxy":
                    "plus.space.common.proxy.ServerProxy").createInst();
        }
        proxy.preInit(event.getSide() == Side.CLIENT);
    }


    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event.getSide() == Side.CLIENT);
    }


    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event) {
        proxy.postnit(event.getSide() == Side.CLIENT);
    }


    @Mod.EventHandler
    public void onServerStart(FMLServerStartingEvent event){
        proxy.onServerStart(event);
    }


    public static CommonProxy proxy(){
        return proxy;
    }
}