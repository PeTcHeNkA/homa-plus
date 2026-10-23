package plus.gen.struct;

import net.minecraft.world.World;
import plus.gen.utl.StructPower;
import java.util.Random;


public interface IStruct {
    float getSpawnChance();
    void generate(World world, Random rand, int x, int y, int z, StructPower power);
}
