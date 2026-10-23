package plus.space.server.world;

import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.storage.ISaveHandler;
import plus.fserver.api.ElcaWorldProvider;
import plus.fserver.world.generator.biome.BiomeLib;
import plus.fserver.world.generator.biome.BiomeProviderEx;
import plus.space.server.gen.DChunkGeneratorPlanet;
import plus.tson.TsonFile;


@Deprecated
public class TempWorld extends BaseWorld {
    public TempWorld(MinecraftServer server, ISaveHandler saveHandlerIn, int dimensionId, WorldServer delegate, Profiler profilerIn) {
        super(server, saveHandlerIn, dimensionId, delegate, profilerIn);
    }


    private void patchBiomes(){
        ElcaWorldProvider provider01 = (ElcaWorldProvider)this.provider;
        BiomeLib lib = new BiomeLib(new TsonFile("test-biome-lib.tson"));

        provider01.setBiomeProvider(
                new BiomeProviderEx(getWorldInfo(), lib)
        );
    }


    @Override
    public IChunkGenerator createChunkGenerator() {
        patchBiomes();
        return new DChunkGeneratorPlanet(this, null);
    }
}