package plus.fserver.world.generator.struct;

import net.minecraft.world.World;
import plus.fserver.world.generator.vr.MultiChunkCache;
import plus.fserver.utls.math.FastRandom;
import plus.tson.TsonMap;
import java.util.Arrays;


public class WorldMultiStructure extends WorldStructure{
    private final int[] subStructures;
    protected final int maxPower;

    public WorldMultiStructure(TsonMap map){
        super(map);
        subStructures = SubMultiStructure.getSubStructures(map);
        maxPower = map.getOrDefaultInt("max-pow", 50);
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
        if(y>255 || power.use())return;
        int fx = x + shiftX;
        int fy = y + shiftY;
        int fz = z + shiftZ;
        if(template!=null) {
            template.placeInGen(gen,fx,fy,fz,settings);
        }
        for(int id:subStructures){
            IStruct s = buffer[id];
            if(s!=null && !power.stop()) {
                if (FastRandom.INST.nextFloat()<s.getSpawnChance()) {
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

        if(template!=null) {
            template.build(world,fx,fy,fz,settings);
        }

        for(int id:subStructures){
            IStruct s = buffer[id];
            if(s!=null && !power.stop()) {
                if (world.rand.nextFloat()<s.getSpawnChance()) {
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