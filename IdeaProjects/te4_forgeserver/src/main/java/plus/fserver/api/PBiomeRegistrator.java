package plus.fserver.api;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import plus.tson.utl.Tuple;
import java.util.ArrayList;
import java.util.function.BiConsumer;


public final class PBiomeRegistrator {
    private final ArrayList<Tuple<Biome, Integer>> biomes = new ArrayList<>();

    private PBiomeRegistrator(){}


    public static void addToQueue(Biome biome, int id){
        INST.biomes.add(new Tuple<>(biome, id));
    }


    public static void registerAll(RegistryEvent.Register<Biome> e){
        if(INST == null){
            System.out.println("This biomes lib is ready registered!");
            return;
        }
        ArrayList<Tuple<Biome, Integer>> biomes = INST.biomes;
        INST = null;
        PForgeRegistry<Biome> registry = (PForgeRegistry<Biome>) e.getRegistry();
        for(Tuple<Biome, Integer> pair:biomes){
            registry.register(pair.B, pair.A);
        }
        System.out.println("PATCHED BIOMES LOADED");
    }


    private static PBiomeRegistrator INST = new PBiomeRegistrator();
}