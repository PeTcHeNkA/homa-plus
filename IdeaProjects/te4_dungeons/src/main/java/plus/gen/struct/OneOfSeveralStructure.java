package plus.gen.struct;

import net.minecraft.world.World;
import plus.gen.utl.StructPower;
import plus.gen.utl.WeightLinckedIndexes;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import java.util.Random;
import static plus.gen.struct.AbcStruct.onInit;


public class OneOfSeveralStructure extends WeightLinckedIndexes implements IStruct{

    public final float spawnChance;

    public OneOfSeveralStructure(TsonMap map) {
        super(SubMultiStructure.getSubStructures(map), getWeights(map));
        onInit(this, map.getInt("id"));
        spawnChance = SubStructure.getChance(map);
    }


    protected static float[] getWeights(TsonMap map){
        TsonList list = map.getList("weights");
        float[] weights = new float[list.size()];
        for(int i=0;i<list.size();i++){
            weights[i] = list.getFloat(i);
        }
        return weights;
    }


    @Override
    public float getSpawnChance() {
        return spawnChance;
    }


    @Override
    public void generate(World world, Random rand, int x, int y, int z, StructPower power) {
        IStruct struct = AbcStruct.buffer[getIndexOfWeight(rand.nextFloat())];
        if(rand.nextFloat()<struct.getSpawnChance()){
            struct.generate(world, rand, x, y, z, power);
        }
    }
}