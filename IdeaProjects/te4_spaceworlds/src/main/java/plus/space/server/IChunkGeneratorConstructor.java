package plus.space.server;

import net.minecraft.world.gen.IChunkGenerator;
import plus.space.server.world.SolarWorld;


public interface IChunkGeneratorConstructor {
    IChunkGenerator newGenerator(SolarWorld world, SolarWorldItem item);
}