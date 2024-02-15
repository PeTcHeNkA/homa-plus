package plus.fserver.world.generator.struct;

import net.minecraft.world.World;
import plus.fserver.world.generator.vr.MultiChunkCache;
import plus.fserver.utls.math.FastRandom;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;

import java.util.Arrays;


public class SubMultiStructure extends SubStructure{
    private final int[] subStructures;

    public SubMultiStructure(TsonMap map){
        super(map);
        subStructures = getSubStructures(map);
    }


    protected static int[] getSubStructures(TsonMap map){
        TsonObj obj = map.get("add");
        if(obj==null)return new int[0];
        if(obj.isList()) {
            TsonList list = obj.getList();
            int[] subStructures = new int[list.size()];
            for (int i = 0; i < list.size(); i++) {
                subStructures[i] = list.getInt(i);
            }
            return subStructures;
        } else if (obj.isNumber()) {
            return new int[]{obj.getInt()};
        }
        throw new IllegalArgumentException(obj.toString());
    }


    @Override
    public void generate(MultiChunkCache gen, int x, int y, int z, StructPower power) {
        if(y>255 || power.use())return;

        int fx = x + shiftX;
        int fy = y + shiftY;
        int fz = z + shiftZ;

        if(template != null) template.placeInGen(gen, fx, fy, fz, settings);

        for (int id : subStructures) {
            IStruct s = buffer[id];
            if (s != null && !power.stop()) {
                if (FastRandom.INST.nextFloat() < s.getSpawnChance()) {
                    s.generate(gen, fx, fy, fz, power);
                }
            }
        }
    }


    @Override
    public void generate(World world, int x, int y, int z, StructPower power) {
        if(y>255 || power.use())return;

        int fx = x + shiftX;
        int fy = y + shiftY;
        int fz = z + shiftZ;

        if(template != null) template.build(world, fx, fy, fz, settings);

        for (int id : subStructures) {
            IStruct s = buffer[id];
            if (s != null && !power.stop()) {
                if (world.rand.nextFloat() < s.getSpawnChance()) {
                    s.generate(world, fx, fy, fz, power);
                }
            }
        }
    }


    @Override
    public String toString() {
        return super.toString()+"+"+ Arrays.toString(subStructures);
    }
}