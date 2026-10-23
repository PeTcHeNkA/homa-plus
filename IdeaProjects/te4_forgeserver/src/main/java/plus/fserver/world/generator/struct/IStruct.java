package plus.fserver.world.generator.struct;

import net.minecraft.world.World;
import plus.fserver.world.generator.schem.ITSchem;
import plus.fserver.world.generator.vr.MultiChunkCache;


public interface IStruct {
    default float getSpawnChance(){return 1f;};


    void generate(World world, int x, int y, int z);


    void generate(World world, int x, int y, int z, StructPower power);


    void generate(MultiChunkCache generator, int x, int y, int z);


    void generate(MultiChunkCache generator, int x, int y, int z, StructPower power);


     default ITSchem getSchem(){return null;};
}
