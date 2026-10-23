package plus;

import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.Mod;
import plus.inv.proxy.ServerProxy;

@Mod(
        modid = main.MOD_ID,
        name = main.MOD_NAME,
        version = main.VERSION
)
public class main {

    public static final String MOD_ID = "test_inv";
    public static final String MOD_NAME = "Test_inv";
    public static final String VERSION = "1.0-SNAPSHOT";
//    public static ENetworkHandler NETWORK;

    @Mod.Instance(MOD_ID)
    public static main INSTANCE;


    @SidedProxy(clientSide = "plus.inv.proxy.ClientProxy", serverSide = "plus.inv.proxy.ServerProxy")
    public static ServerProxy proxy;


    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        //NetworkHandler.init();
        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }

    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event) {
        proxy.postInit();
    }
}
