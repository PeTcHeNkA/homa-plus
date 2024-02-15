package plus.fserver.world.generator.struct;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import plus.fserver.world.generator.vr.MultiChunkCache;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import java.util.ArrayList;


public class WorldStructure extends SubStructure implements IWorldStruct{
    public final Block[] spawnAt;
    public final boolean underGroundSpawn;

    public WorldStructure(TsonMap map){
        super(map);
        spawnAt = getSpawnAt(map);
        underGroundSpawn = map.getOrDefaultBool("underground", false);
    }


    protected static Block[] getSpawnAt(TsonMap map){
        if(map.containsKey("spawn-at")) {
            TsonList list = map.getList("spawn-at");
            ArrayList<Block> blocks = new ArrayList<>();
            for (TsonObj o : list) {
                Block b = Block.getBlockFromName(o.getStr());
                if (b != null) blocks.add(b);
            }
            if(!blocks.isEmpty()) return blocks.toArray(new Block[0]);
        }
        return null;
    }


    @Override
    public boolean canPlaceOn(Block check){
        if(spawnAt==null)return true;
        for (Block b:spawnAt) if(b==check)return true;
        return false;
    }


    @Override
    public boolean isUnderground() {
        return underGroundSpawn;
    }
}