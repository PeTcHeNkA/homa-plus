package plus.fserver.server.chunk.io;


public class TQueuedChunk {
    final int x;
    final int z;

    public TQueuedChunk(int x, int z) {
        this.x = x;
        this.z = z;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TQueuedChunk that = (TQueuedChunk) o;
        if (x != that.x) return false;
        if (z != that.z) return false;
        return true;
    }


    @Override
    public int hashCode() {
        return 31 * (31 * x + z);
    }


    @Override
    public String toString() {
        return "{x = " + x + ", z = " + z + "}";
    }
}