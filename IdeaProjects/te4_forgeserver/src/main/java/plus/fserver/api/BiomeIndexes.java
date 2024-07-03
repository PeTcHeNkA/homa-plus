package plus.fserver.api;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;


public final class BiomeIndexes{
    private static Biome[] INDEX;
    private BiomeIndexes(){};


    public static Biome get(int id){
        Biome[] index;
        if((index = INDEX) == null) return Biome.getBiome(id);
        if(id < 0 || id > 254)return null;
        return index[id];
    }


    public static Biome get(int id, Biome def){
        Biome[] index;
        if((index = INDEX) == null) return Biome.getBiome(id, def);
        if(id < 0 || id > 254)return null;
        Biome result = index[id];
        if(result == null)return def;
        return result;
    }


    public static void makeIndexIfNed(){
        if(INDEX != null)return;
        Biome[] index = INDEX = new Biome[255];

        ForgeRegistry<Biome> fr = (ForgeRegistry<Biome>) ForgeRegistries.BIOMES;
        for(ResourceLocation loc:fr.getKeys()){
            index[fr.getID(loc)] = fr.getValue(loc);
        }
    }
}