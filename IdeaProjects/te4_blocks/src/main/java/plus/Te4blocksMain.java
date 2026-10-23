package plus;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import plus.block.BlockLoader;
import plus.wepather.PathProxy;


@Mod(modid = Te4blocksMain.MOD_ID, name = Te4blocksMain.MOD_NAME, version = Te4blocksMain.VERSION)
public class Te4blocksMain {
    public static final String MOD_ID = "te4blocks";
    public static final String MOD_NAME = "Te4blocks";
    public static final String VERSION = "1.0-SNAPSHOT";

    public static final BlockLoader BLOCKS = new BlockLoader();


    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        Side side = Side.SERVER;
        BLOCKS.register();
        PathProxy.onInit(BLOCKS::onPostInit);
        MinecraftForge.EVENT_BUS.register(this);
    }


    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        BLOCKS.registerRender();
    }


    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {

    }


    private boolean postInitialized = false;
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load e){
        if(e.getWorld().isRemote || postInitialized)return;
        BLOCKS.onPostInit();
        postInitialized = true;
    }
}