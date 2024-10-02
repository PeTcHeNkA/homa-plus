package plus.fserver.server.chunk.io;

import net.minecraft.world.chunk.Chunk;
import java.util.function.Consumer;


public class WrappedChunkRunnable implements Consumer<Chunk> {
    private final Runnable task;

    public WrappedChunkRunnable(Runnable task) {
        this.task = task;
    }


    @Override
    public void accept(Chunk chunk) {
        task.run();
    }


    @Override
    public boolean equals(Object obj) {
        return task.equals(obj);
    }


    @Override
    public int hashCode() {
        return task.hashCode();
    }
}
