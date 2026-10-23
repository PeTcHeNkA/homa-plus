package plus.gen.struct;

import net.minecraft.world.World;
import plus.gen.Te4WorldGen;
import plus.gen.utl.StructPower;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import java.util.Random;


public class SubMultiStructure extends SubStructure{
    public static int maxUsages = 500;
    public static int usages = 0;

    private final int[] subStructures;

    public SubMultiStructure(TsonMap map){
        super(map);
        subStructures = getSubStructures(map);
    }


    protected static int[] getSubStructures(TsonMap map){
        TsonList list = map.getList("add");
        int[] subStructures = new int[list.size()];
        for(int i=0;i<list.size();i++){
            subStructures[i] = list.getInt(i);
        }
        return subStructures;
    }


    @Override
    public void generate(World world, Random rand, int x, int y, int z, StructPower power) {
        if(y>250 || power.use())return;
        if(usages++>maxUsages){
            Te4WorldGen.buffer.put(() -> generate(world, rand, x, y, z, power));
            return;
        }
        int fx = x + shiftX;
        int fy = y + shiftY;
        int fz = z + shiftZ;

        template.build(world, fx, fy, fz, settings, rand);

        for (int id : subStructures) {
            IStruct s = buffer[id];
            if (s != null && !power.stop()) {
                if (rand.nextFloat() < s.getSpawnChance()) {
                    s.generate(world, rand, fx, fy, fz, power);
                }
            }
        }
    }
}