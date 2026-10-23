package plus.mobs;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.EntityEntry;
import plus.mobs.utl.Delegate;
import plus.mobs.utl.MobRegManager;


@Mod(
        modid = Te4mobsMain.MOD_ID,
        name = Te4mobsMain.MOD_NAME,
        version = Te4mobsMain.VERSION
)
@Mod.EventBusSubscriber
public class Te4mobsMain {

    public static final String MOD_ID = "te4mobs";
    public static final String MOD_NAME = "Te4mobs";
    public static final String VERSION = "1.0-SNAPSHOT";
    public static final boolean PROD = false;

    @Mod.Instance(MOD_ID)
    public static Te4mobsMain INSTANCE;

    public static Delegate<MobRegManager> onInitTasks = new Delegate<>();


    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {

    }


    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {

    }


    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event) {

    }


    @SubscribeEvent
    public static void registryEntity(RegistryEvent.Register<EntityEntry> event) {
        try {
            new MobRegManager(event).buildAllEntities();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}