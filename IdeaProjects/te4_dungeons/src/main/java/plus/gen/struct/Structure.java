package plus.gen.struct;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import plus.gen.settings.BiomeChecker;
import plus.gen.settings.SettingsFolder;
import plus.gen.utl.StructPower;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import java.util.ArrayList;

//НЕ ТРОГАТЬ
//IBlockState state = world.getBlockState(pos);
//world.notifyBlockUpdate(pos, state, state, spawnChance);
public class Structure extends SubStructure{
    public final Block[] spawnAt;
    public final BiomeChecker biomeChecker;
    public final boolean underGroundSpawn;
    protected final int maxPower;

    public Structure(TsonMap map){
        super(map);

        if(map.containsKey("spawn-at")) {
            TsonList list = map.getList("spawn-at");
            ArrayList<Block> blocks = new ArrayList<>();
            for (TsonObj o : list) {
                Block b = Block.getBlockFromName(o.getStr());
                if (b != null) {
                    blocks.add(b);
                }
            }
            if(!blocks.isEmpty()) {
                spawnAt = blocks.toArray(new Block[0]);
            } else {
                spawnAt = null;
            }
        } else {
            spawnAt = null;
        }
        if(map.containsKey("biomes")) {
            biomeChecker = BiomeChecker.build(map.getList("biomes"));
        } else {
            biomeChecker = BiomeChecker.EMPTY;
        }
        if(map.containsKey("underground")){
            underGroundSpawn = map.getBool("underground");
        } else {
            underGroundSpawn = false;
        }
        if(map.containsKey("max-pow")){
            maxPower = map.getInt("max-pow");
        } else {
            maxPower = 50;
        }
    }


    public void generate(World world, int x, int y, int z) {
        if(template==null || y>250)return;
        template.build(world,x+shiftX,y+shiftY,z+shiftZ,settings,lazyRandom);
    }
}