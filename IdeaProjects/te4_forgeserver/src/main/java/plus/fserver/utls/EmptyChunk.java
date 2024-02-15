package plus.fserver.utls;

import net.minecraft.world.chunk.Chunk;


public class EmptyChunk extends Chunk {
    public static final EmptyChunk INSTANCE = new EmptyChunk();
    private EmptyChunk() {
        super(null, 0, 0);
    }
    @Override
    public boolean isEmpty() {
        return true;
    }
}