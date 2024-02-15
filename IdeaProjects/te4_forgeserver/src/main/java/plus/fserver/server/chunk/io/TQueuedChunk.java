package plus.fserver.server.chunk.io;

import net.minecraft.world.World;


public class TQueuedChunk {
    final int x;
    final int z;
    final World world;

    public TQueuedChunk(int x, int z, World world) {
        this.x = x;
        this.z = z;
        this.world = world;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TQueuedChunk that = (TQueuedChunk) o;
        if (x != that.x) return false;
        if (z != that.z) return false;
        return world.equals(that.world);
    }


    @Override
    public int hashCode() {
        return 31 * (31 * x + z) + world.hashCode();
    }


    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        String NEW_LINE = System.getProperty("line.separator");
        result.append(this.getClass().getName() + " {" + NEW_LINE);
        result.append(" x: " + x + NEW_LINE);
        result.append(" z: " + z + NEW_LINE);
        result.append(" world: " + world.getWorldInfo().getWorldName() + NEW_LINE);
        result.append(" dimension: " + world.provider.getDimension() + NEW_LINE);
        result.append(" provider: " + world.provider.getClass().getName() + NEW_LINE);
        result.append("}");
        return result.toString();
    }
}