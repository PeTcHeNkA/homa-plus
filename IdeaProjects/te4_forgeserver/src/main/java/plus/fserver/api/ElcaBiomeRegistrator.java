package plus.fserver.api;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistry;
import plus.tson.utl.Tuple;
import java.util.ArrayList;


public final class ElcaBiomeRegistrator {
    private final ArrayList<Tuple<Biome, Integer>> biomes = new ArrayList<>();

    private ElcaBiomeRegistrator(){}


    public static void addToQueue(Biome biome, int id){
        if(INST == null)throw new RuntimeException("READY REGISTERED!");
        INST.biomes.add(new Tuple<>(biome, id));
    }


    public static boolean isRegistered(){
        return INST == null;
    }


    public static void registerAll(RegistryEvent.Register<Biome> e){
        if(INST == null){
            System.out.println("This biomes lib is ready registered!");
            return;
        }
        ArrayList<Tuple<Biome, Integer>> biomes = INST.biomes;
        INST = null;
        ForgeRegistry<Biome> registry = (ForgeRegistry<Biome>) e.getRegistry();
        for(Tuple<Biome, Integer> pair:biomes){
            ElcaServerUtils.unsafeRegister(registry, pair.B, pair.A);
        }
        System.out.println("PATCHED BIOMES LOADED");
    }


    private static ElcaBiomeRegistrator INST = new ElcaBiomeRegistrator();
}