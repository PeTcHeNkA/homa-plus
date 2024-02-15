package plus.fserver.api;

import io.netty.util.collection.IntObjectHashMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;


public final class BiomeIndexes{
    private static IntObjectHashMap<Biome> INDEXES;
    private BiomeIndexes(){};


    public static Biome get(int id){
        if(INDEXES==null){
            INDEXES = new IntObjectHashMap<>();
            ForgeRegistry<Biome> fr = (ForgeRegistry<Biome>) ForgeRegistries.BIOMES;
            for(ResourceLocation loc:fr.getKeys()){
                INDEXES.put(fr.getID(loc), fr.getValue(loc));
            }
        }
        return INDEXES.get(id);
    }
}