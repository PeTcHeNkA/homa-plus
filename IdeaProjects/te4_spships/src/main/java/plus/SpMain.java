package plus;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import plus.common.mashine.item.ReactCore;
import plus.proxy.ServerProxy;


@Mod.EventBusSubscriber(modid = SpMain.MOD_ID)
@Mod(modid = SpMain.MOD_ID, name = SpMain.MOD_NAME, version = SpMain.VERSION)
public class SpMain {
    public static final String MOD_ID = "te4_spships";
    public static final String MOD_NAME = "Te4_spships";
    public static final String VERSION = "1.0-SNAPSHOT";

//    @Mod.Instance(MOD_ID)
//    public static SpMain INSTANCE;


    @SidedProxy(clientSide = "plus.proxy.ClientProxy", serverSide = "plus.proxy.ServerProxy")
    public static ServerProxy proxy;



    public static void preLoadClasses(Class<?>... classes){
        for(Class<?> clazz:classes){
            try {
                Class.forName(clazz.getName());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }


    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
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


    @SubscribeEvent
    public static void registryEntity(RegistryEvent.Register<EntityEntry> event) {
        proxy.registryEntity(event);
    }


    public static final CreativeTabs SP_TAB = new CreativeTabs("sp_tab") {
        private byte last = 0;
        @Override
        public ItemStack createIcon() {
            ItemStack item;
            switch (++last){
                default:
                    last = 0;
                    item = new ItemStack(ReactCore.CORE_L1);
                    break;
                case 1:
                    item = new ItemStack(ReactCore.CORE_L2);
                    break;
                case 2:
                    item = new ItemStack(ReactCore.CORE_L3);
                    break;
                case 3:
                    item = new ItemStack(ReactCore.CORE_L4);
                    break;
            }
            return item;
        }
    };
}