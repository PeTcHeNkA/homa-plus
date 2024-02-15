package plus.fserver.plugin;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import plus.fserver.api.PBiomeRegistrator;


public class ElcaBiomes {
    private static ElcaBiomes INST;
    static void registerIfNeed(){
        if(INST != null)return;
        MinecraftForge.EVENT_BUS.register(INST = new ElcaBiomes());
    }


    @SubscribeEvent
    public void regBiomes(RegistryEvent.Register<Biome> e){
        PBiomeRegistrator.registerAll(e);
    }
}