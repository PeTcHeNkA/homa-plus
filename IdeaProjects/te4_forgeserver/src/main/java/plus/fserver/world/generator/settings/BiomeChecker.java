package plus.fserver.world.generator.settings;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import plus.tson.TsonList;
import plus.tson.TsonObj;
import java.util.HashSet;


public abstract class BiomeChecker {
    private static boolean vanilla = true;
    public static BiomeChecker EMPTY = new BiomeChecker() {
        @Override
        public boolean check(Biome biome) {
            return true;
        }
    };


    public static void setMode(boolean isVanilla){
        vanilla = isVanilla;
    }


    public abstract boolean check(Biome biome);


    public static BiomeChecker build(TsonList list){
        if(list==null || list.isEmpty())return EMPTY;
        if(vanilla){
            return new Vanilla(list);
        } else {
            return new Modded(list);
        }
    }
}

abstract class BiomeCheckerB<T> extends BiomeChecker{
    protected final HashSet<T> biomes = new HashSet<>();
}

class Vanilla extends BiomeCheckerB<Class<?>>{
    private static Class<? extends Biome> getBiome(String id){
        Biome biome = Biome.REGISTRY.getObject(new ResourceLocation(id));
        if(biome==null)return null;
        return biome.getBiomeClass();
    }

    Vanilla(TsonList list){
        for (TsonObj o : list) {
            Class<?> biome = getBiome(o.getStr());
            if (biome != null) {
                biomes.add(biome);
            }
        }
    }


    @Override
    public boolean check(Biome biome) {
        return biomes.contains(biome.getBiomeClass());
    }
}
class Modded extends BiomeCheckerB<String>{
    Modded(TsonList list){
        for (TsonObj o : list) {
            biomes.add(o.getStr());
        }
    }


    @Override
    public boolean check(Biome biome) {
        return biomes.contains(biome.getBiomeName());
    }
}