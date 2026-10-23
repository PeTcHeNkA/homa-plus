package plus.fserver.world.generator.struct;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import plus.fserver.world.generator.vr.MultiChunkCache;
import plus.tson.TsonMap;
import static plus.fserver.world.generator.struct.WorldStructure.getSpawnAt;


public class WorldOneOfSeveralStruct extends OneOfSeveralStructure implements IWorldStruct{
    public final Block[] spawnAt;
    public final boolean underGroundSpawn;
    protected final int maxPower;

    public WorldOneOfSeveralStruct(TsonMap map) {
        super(map);
        spawnAt = getSpawnAt(map);
        underGroundSpawn = map.getOrDefaultBool("underground", false);
        maxPower = map.getOrDefaultInt("max-pow", 50);
    }


    @Override
    public boolean canPlaceOn(Block check){
        if(spawnAt==null)return true;
        for (Block b:spawnAt) if(b==check)return true;
        return false;
    }


    @Override
    public void generate(World world, int x, int y, int z) {
        generate(world, x, y, z, new StructPower(maxPower));
    }


    @Override
    public void generate(MultiChunkCache gen, int x, int y, int z) {
        generate(gen, x, y, z, new StructPower(maxPower));
    }


    @Override
    public boolean isUnderground() {
        return underGroundSpawn;
    }
}