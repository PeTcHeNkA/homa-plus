package plus.space.common.proxy;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import plus.fserver.api.ElcaBiomeRegistrator;
import java.util.Random;


public class CommonProxy {
    public static SimpleNetworkWrapper network;

    public CommonProxy(){
        MinecraftForge.EVENT_BUS.register(this);
    }


    public void preInit(boolean flag){}


    public void onServerStart(FMLServerStartingEvent event){}


    public void init(boolean flag){}


    public void postnit(boolean flag){}


    @SubscribeEvent
    public void regBiomes(RegistryEvent.Register<Biome> event){
        ElcaBiomeRegistrator.registerAll(event);
    }


    public boolean genThreePatch(World worldIn, BlockPos pos, IBlockState state, Random rand){
        return false;
    }
}