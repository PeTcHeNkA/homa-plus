package plus.gen.struct;

import net.minecraft.world.World;
import plus.gen.utl.StructPower;
import plus.tson.TsonMap;
import java.util.Random;


public class MultiStructure extends Structure{

    private final int[] subStructures;

    public MultiStructure(TsonMap map){
        super(map);
        subStructures = SubMultiStructure.getSubStructures(map);
    }


    @Override
    public void generate(World world, Random rand, int x, int y, int z, StructPower power) {
        if(y>250 || power.use())return;
        int fx = x + shiftX;
        int fy = y + shiftY;
        int fz = z + shiftZ;

        if(template!=null) {
            template.build(world,fx,fy,fz,settings,lazyRandom);
        }

        for(int id:subStructures){
            IStruct s = buffer[id];
            if(s!=null && !power.stop()) {
                if (lazyRandom.nextFloat()<s.getSpawnChance()) {
                    s.generate(world, lazyRandom, fx, fy, fz, power);
                }
            }
        }
    }


    @Override
    public void generate(World world, int x, int y, int z) {
        generate(world, lazyRandom, x, y, z, new StructPower(maxPower));
    }
}