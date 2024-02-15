package plus.fserver.world.generator.struct;

import net.minecraft.world.World;
import plus.fserver.world.generator.vr.MultiChunkCache;
import plus.fserver.utls.math.FastRandom;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;

import java.util.Arrays;

import static plus.fserver.world.generator.struct.SimpleStruct.onInit;


public class OneOfSeveralStructure extends WeightLinckedIndexes implements IStruct{
    public final float spawnChance;
    protected final int maxPower;

    public OneOfSeveralStructure(TsonMap map) {
        super(SubMultiStructure.getSubStructures(map), getWeights(map));
        onInit(this, map.getInt("id"));
        spawnChance = SubStructure.getChance(map);
        if(map.containsKey("max-pow")){
            maxPower = map.getInt("max-pow");
        } else {
            maxPower = 50;
        }
    }


    protected static float[] getWeights(TsonMap map){
        TsonObj obj = map.get("weights");
        if(obj.isList()) {
            TsonList list = obj.getList();
            float[] weights = new float[list.size()];
            for (int i = 0; i < list.size(); i++) {
                weights[i] = list.getFloat(i);
            }
            return weights;
        } else if(obj.isString()){

        } else if(obj.isNumber()){
            float[] result = new float[obj.getInt()];
            Arrays.fill(result, 1);
            return result;
        }
        throw new IllegalArgumentException();
    }


    @Override
    public float getSpawnChance() {
        return spawnChance;
    }


    @Override
    public void generate(World world, int x, int y, int z, StructPower power) {
        IStruct struct = SimpleStruct.buffer[getIndexOfWeight(world.rand.nextFloat())];
        if(struct==null)return;
        if(world.rand.nextFloat()<struct.getSpawnChance()){
            struct.generate(world, x, y, z, power);
        }
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
    public void generate(MultiChunkCache gen, int x, int y, int z, StructPower power) {
        IStruct struct = SimpleStruct.buffer[getIndexOfWeight(FastRandom.INST.nextFloat())];
        if(struct==null)return;
        if(FastRandom.INST.nextFloat()<struct.getSpawnChance()){
            struct.generate(gen, x, y, z, power);
        }
    }


    @Override
    public String toString() {
        return "1of:"+super.toString();
    }
}