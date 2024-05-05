package plus.fserver.utls.collections;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTrackerEntry;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;


public class ChunkedEntityList {
    private static final int SHIFT = 6;

    private Chunk[] freeChunks = new Chunk[8];
    private int freeCursor = -1;

    private Chunk[] allChunks = new Chunk[16];
    private int countChunks = 0;

    private final NearIter nearIter = new NearIter();


    private Chunk allocChunk(long index) {
        if(freeCursor == -1)return new Chunk(index);
        return freeChunks[freeCursor--].init(index);
    }


    private void freeChunk(Chunk chunk) {
        if(++freeCursor == freeChunks.length) {
            Chunk[] newChunks = new Chunk[freeChunks.length + 8];
            System.arraycopy(freeChunks, 0, newChunks, 0, freeChunks.length);
            freeChunks = newChunks;
        }
        chunk.clear();
        freeChunks[freeCursor] = chunk;
    }


    public long getEffectiveIndex(Entity entity) {
        return indexOf(((int) entity.posX) >> SHIFT, ((int) entity.posZ) >> SHIFT);
    }


    public long getLastIndex(Entity entity) {
        EntityTrackerEntry entry = entity.trackerEntry;
        if(entry == null)return -1;
        return indexOf(((int) entry.lastTrackedEntityPosX) >> SHIFT, ((int) entry.lastTrackedEntityPosZ) >> SHIFT);
    }

    private long indexOf(int x, int z){
        return (((long) x) << 32) | z;
    }


    public Chunk addChunk(long index){
        Chunk chunk = allocChunk(index);
        int count;
        if ((count = countChunks++) == allChunks.length){
            Chunk[] newChunks = new Chunk[allChunks.length + 8];
            System.arraycopy(allChunks, 0, newChunks, 0, allChunks.length);
            allChunks = newChunks;
        }
        allChunks[count] = chunk;
        Arrays.sort(allChunks, 0, count+1, CHUNK_COMPARATOR);
        return chunk;
    }


    private int getChunkCursorByIndex(int index) {
        return binarySearch(0, countChunks, index);
    }


    private int binarySearchUnsafe(int fromIndex, int toIndex,
                                     long key) {
        Chunk[] a = allChunks;
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            long midVal = a[mid].index;

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid;
        }
        return low;
    }


    public void addEntity(Entity entity) {
        long index = getLastIndex(entity);

        int cIndex = binarySearch(0, countChunks, index);
        if(cIndex >= 0){
            allChunks[cIndex].addF(entity);
        } else {
            addChunk(index).addF(entity);
        }
    }


    public void removeEntity(Entity entity) {
        long index = getLastIndex(entity);

        int cIndex = binarySearch(0, countChunks, index);
        if(cIndex >= 0){
            Chunk chunk;
            (chunk = allChunks[cIndex]).remove(entity);

            if(chunk.isEmpty()){
                freeChunk(chunk);

                int numMoved = countChunks - cIndex - 1;
                if (numMoved > 0) System.arraycopy(allChunks, cIndex+1, allChunks, cIndex, numMoved);

                --countChunks;
            }
        } else {
            System.out.println("WARNING: Entity " + entity.getEntityId() + " not found in chunked list");
        }
    }


    public void updateEntity(Entity entity) {
        long effIndex = getEffectiveIndex(entity);
        long lastIndex = getLastIndex(entity);

        int cIndex = binarySearch(0, countChunks, lastIndex);
        if(cIndex >= 0){
            allChunks[cIndex].remove(entity);
        } else {
            System.out.println("WARNING: Entity " + entity.getEntityId() + " not found in chunked list");
        }
        cIndex = binarySearch(0, countChunks, effIndex);
        if(cIndex >= 0){
            allChunks[cIndex].addF(entity);
        } else {
            addChunk(effIndex).addF(entity);
        }
    }


    private int binarySearch(int fromIndex, int toIndex,
                                   long key) {
        Chunk[] a = allChunks;
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            long midVal = a[mid].index;

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid;
        }
        return -(low + 1);
    }


    public NearIter nearEntityIter(Entity entity) {
        return nearIter.init(entity);
    }


    public class NearIter implements Iterator<Entity> {
        private final Chunk[] chunks = new Chunk[9];
        private Iterator<Entity> current;
        private int cursor = 0;

        public NearIter init(Entity entity) {
            cursor = 0;
            int rawX = (((int) entity.posX) >> SHIFT);
            int rawZ = (((int) entity.posZ) >> SHIFT);
            Chunk temp;

            long chunkIndex = indexOf(rawX - 1, rawZ - 1); // -1, -1
            int startIdx    = binarySearchUnsafe(0, countChunks, chunkIndex);

            temp = allChunks[startIdx];
            if(temp != null && chunkIndex == temp.index)chunks[0] = temp;
            else chunks[0] = null;


            chunkIndex      = indexOf(rawX + 1, rawZ + 1); // +1, +1
            int endIdx      = binarySearchUnsafe(startIdx,   countChunks, chunkIndex);

            temp = allChunks[endIdx];
            if(temp != null && chunkIndex == temp.index)chunks[8] = temp;
            else chunks[8] = null;


            chunkIndex = indexOf(rawX - 1, rawZ);       // -1, 0
            int tempIdx = binarySearch(startIdx, endIdx, chunkIndex);
            if (tempIdx >= 0) chunks[1] = allChunks[tempIdx];
            else chunks[1] = null;


            chunkIndex = indexOf(rawX - 1, rawZ + 1);// -1, +1
            tempIdx = binarySearch(startIdx, endIdx, chunkIndex);
            if (tempIdx >= 0) chunks[2] = allChunks[tempIdx];
            else chunks[2] = null;


            chunkIndex = indexOf(rawX, rawZ - 1);       // 0, -1
            tempIdx = binarySearch(startIdx, endIdx, chunkIndex);
            if (tempIdx >= 0) chunks[3] = allChunks[tempIdx];
            else chunks[3] = null;


            chunkIndex = indexOf(rawX, rawZ);               // 0, 0
            tempIdx = binarySearch(startIdx, endIdx, chunkIndex);
            if (tempIdx >= 0) chunks[4] = allChunks[tempIdx];
            else chunks[4] = null;


            chunkIndex = indexOf(rawX, rawZ + 1);        // 0, +1
            tempIdx = binarySearch(startIdx, endIdx, chunkIndex);
            if (tempIdx >= 0) chunks[5] = allChunks[tempIdx];
            else chunks[5] = null;


            chunkIndex = indexOf(rawX + 1, rawZ - 1); // +1, -1
            tempIdx = binarySearch(startIdx, endIdx, chunkIndex);
            if (tempIdx >= 0) chunks[6] = allChunks[tempIdx];
            else chunks[6] = null;


            chunkIndex = indexOf(rawX + 1, rawZ);        // +1, 0
            tempIdx = binarySearch(startIdx, endIdx, chunkIndex);
            if (tempIdx >= 0) chunks[7] = allChunks[tempIdx];
            else chunks[7] = null;


            goToNextChunk();
            return this;
        }


        private void goToNextChunk() {
            for (current = null;cursor<9;cursor++) {
                if (chunks[cursor] != null) {
                    current = chunks[cursor++].iterator();
                    return;
                }
            }
            cursor = 9;
        }


        @Override
        public boolean hasNext() {
            if (current == null)return false;
            if (!current.hasNext()) {
                goToNextChunk();
                return current != null && current.hasNext();
            }
            return true;
        }


        @Override
        public Entity next() {
            return current.next();
        }
    }


    public static final Comparator<Chunk> CHUNK_COMPARATOR = Comparator.comparingLong(o -> o.index);


    public static class Chunk extends ArrayRemSwapListE6<Entity>{
        long index;
        public Chunk(long index) {
            this.index = index;
        }

        public Chunk init(long index) {
            this.index = index;
            return this;
        }

        @Override
        public int hashCode() {
            return (int) index;
        }
    }


    @Override
    public String toString() {
        int summ = 0;
        for (int i = 0;i<countChunks;i++)summ += allChunks[i].size();
        return "ChunkedEntityList{" +
                "chunks=" + countChunks + ", entities=" + summ +
                '}';
    }
}