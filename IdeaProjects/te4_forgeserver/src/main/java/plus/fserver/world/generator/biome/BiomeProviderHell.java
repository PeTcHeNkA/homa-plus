package plus.fserver.world.generator.biome;

import net.minecraft.init.Biomes;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.ChunkGeneratorHell;
import org.jetbrains.annotations.NotNull;
import java.util.concurrent.ConcurrentLinkedQueue;


public class BiomeProviderHell extends BiomeProviderSingle {
    private final ConcurrentLinkedQueue<ChunkGeneratorHell.GenSession> sessionPool = new ConcurrentLinkedQueue<>();
    public BiomeProviderHell() {
        super(Biomes.HELL);
    }


    @NotNull
    @Override
    public ChunkGeneratorHell.GenSession makeGenSession() {
        ChunkGeneratorHell.GenSession session = sessionPool.poll();
        return session==null ? new ChunkGeneratorHell.GenSession() : session;
    }


    @Override
    public void freeSession(@NotNull Object session) {
        sessionPool.add((ChunkGeneratorHell.GenSession) session);
    }


    public void cleanupCache() {
        if(sessionPool.size()==1){
            sessionPool.clear();
        } else {
            while (sessionPool.size()>1)sessionPool.poll();
        }
    }
}