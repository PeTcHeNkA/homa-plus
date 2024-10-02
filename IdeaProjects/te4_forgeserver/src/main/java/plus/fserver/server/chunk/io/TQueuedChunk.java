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
        StringBuilder result = new StringBuilder();
        String NEW_LINE = System.getProperty("line.separator");
        result.append(this.getClass().getName() + " {" + NEW_LINE);
        result.append(" x: " + x + NEW_LINE);
        result.append(" z: " + z + NEW_LINE);
        result.append("}");
        return result.toString();
    }
}