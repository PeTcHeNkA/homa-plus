package plus.fserver.world.generator.struct;

import plus.fserver.world.generator.settings.BiomeChecker;


public interface IGlobStruct extends IWorldStruct{
    BiomeChecker getBiomeChecker();
}