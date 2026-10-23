package plus.fserver.utls.collections;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTrackerEntry;
import org.jetbrains.annotations.NotNull;
import java.util.*;


public class ChunkedEntityList implements List<Entity> {
    private static boolean legaciCall = false;

    private static final int SHIFT = 6;

    private Chunk[] freeChunks = new Chunk[8];
    private int freeCursor = -1;

    private Chunk[] allChunks = new Chunk[16];
    private int countChunks = 0;

    private final NearIter nearIter = new NearIter();
    private final EntityIter allIter = new EntityIter();


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

            if(chunk.isEmptyC()){
                freeChunk(chunk);

                int numMoved = countChunks - cIndex - 1;
                if (numMoved > 0) System.arraycopy(allChunks, cIndex+1, allChunks, cIndex, numMoved);

                allChunks[--countChunks] = null;
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
        return nearIter.init(this, entity);
    }


    public EntityIter entityIter() {
        return allIter.init(allChunks, countChunks);
    }


    public boolean removeSafe(Entity entity) {
        Entity cur;
        for (EntityIter iter = entityIter(); iter.hasNext();) {
            cur = iter.next();
            if (cur == entity) {
                iter.remove();
                return true;
            }
        }
        return false;
    }


    @Override
    public int size() {
        return calcSize();
    }


    @Override
    public boolean isEmpty() {
        return countChunks == 0;
    }


    @Override
    public boolean contains(Object o) {
        Entity cur;
        for (EntityIter iter = entityIter(); iter.hasNext();) {
            cur = iter.next();
            if (o.equals(cur)) return true;
        }
        return false;
    }


    @NotNull
    @Override
    public Iterator<Entity> iterator() {
        return entityIter();
    }


    @Override
    public boolean add(Entity entity) {
        addEntity(entity);
        return true;
    }


    @Override
    public boolean remove(Object o) {
        return removeSafe((Entity) o);
    }


    @Override
    @Deprecated //todo maybe faster
    public boolean removeAll(@NotNull Collection<?> c) {
        for (Object o : c) {
            if(!removeSafe((Entity) o)) return false;
        }
        return true;
    }


    @Override
    @Deprecated
    public int indexOf(Object o) {
        int index = 0;
        for (Entity entity : this) {
            if (o.equals(entity)) return index;
            ++index;
        }
        return -1;
    }


    @Override
    @Deprecated
    public int lastIndexOf(Object o) {
        if(!legaciCall) {
            System.out.println("WARNING: ChunkedEntityList#lastIndexOf() is deprecated, use ChunkedEntityList#indexOf() instead");
            legaciCall = true;
        }
        return indexOf(o);
    }


    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        for (Object o : c) if(!contains(o)) return false;
        return true;
    }


    @Override
    public boolean addAll(@NotNull Collection<? extends Entity> c) {
        for (Entity entity : c) addEntity(entity);
        return true;
    }


    @Override
    @Deprecated
    public boolean addAll(int index, @NotNull Collection<? extends Entity> c) {
        if(!legaciCall) {
            System.out.println("WARNING: ChunkedEntityList#addAll(int, collection) is deprecated, use ChunkedEntityList#addAll(collection) instead");
            legaciCall = true;
        }
        addAll(c);
        return true;
    }


    @Override
    public void clear() {
        for (int i = 0; i < countChunks; i++) {
            Chunk chunk = allChunks[i];
            chunk.clear();
            freeChunk(chunk);
        }
        countChunks = 0;
    }


    @Override //todo test this
    @Deprecated
    public Entity get(int index) {
        if(countChunks == 0) return null;
        int cCursor = 1;
        Chunk[] chunks = this.allChunks;
        Chunk chunk = chunks[0];

        while (index > chunk.size){
            index -= chunk.size;
            if(cCursor == countChunks) return null;
            chunk = chunks[cCursor++];
        }
        return (Entity) chunk.data[index];
    }


    @Override //todo test this
    @Deprecated
    public Entity set(int index, Entity element) {
        if(countChunks == 0) return null;
        int cCursor = 1;
        Chunk[] chunks = this.allChunks;
        Chunk chunk = chunks[0];

        while (index > chunk.size){
            index -= chunk.size;
            if(cCursor == countChunks) return null;
            chunk = chunks[cCursor++];
        }
        Entity old = (Entity) chunk.data[index];
        chunk.data[index] = element;
        return old;
    }


    @Override
    @Deprecated
    public void add(int index, Entity entity) {
        if(!legaciCall) {
            System.out.println("WARNING: ChunkedEntityList#add() is deprecated, use ChunkedEntityList#addEntity() instead");
            legaciCall = true;
        }
        addEntity(entity);
    }


    @Override
    @Deprecated //todo test this
    public Entity remove(int index) {
        if(countChunks == 0) return null;
        int cCursor = 1;
        Chunk[] chunks = this.allChunks;
        Chunk chunk = chunks[0];

        while (index > chunk.size){
            index -= chunk.size;
            if(cCursor == countChunks) return null;
            chunk = chunks[cCursor++];
        }
        return chunk.remove(index);
    }


    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        T[] arr = Arrays.copyOf(a, size());
        int i = 0;
        for (Entity entity : this) {
            arr[i++] = (T) entity;
        }
        return arr;
    }


    public static final class EntityIter implements Iterator<Entity>, Iterable<Entity> {
        private Chunk[] chunks;
        private int countChunks;
        private Object[] current;
        private int chunkCursor, entityCursor, entityCount;

        public EntityIter init(Chunk[] chunks, int countChunks) {
            this.chunks = chunks;
            this.countChunks = countChunks;
            reset();
            return this;
        }


        public void reset(){
            Chunk chunk = chunks[0];
            if(chunk == null){
                entityCount = -1;
            } else {
                chunkCursor = 0;
                entityCursor = 0;
                current = chunk.data;
                entityCount = chunk.size;
            }
        }


        @NotNull
        @Override
        public Iterator<Entity> iterator() {
            return this;
        }


        @Override
        public boolean hasNext() {
            return entityCount != -1;
        }


        @Override
        public void remove() {
            Chunk chunk = chunks[chunkCursor];
            chunk.removeSwap(entityCursor);
            next();
        }


        @Override
        public Entity next() {
            Entity entity = (Entity) current[entityCursor++];
            if(entityCursor == entityCount){
                if((++chunkCursor) == countChunks){
                    entityCount = -1;
                    return entity;
                }
                Chunk chunk = chunks[chunkCursor];
                current = chunk.data;
                entityCount = chunk.size;
                entityCursor = 0;
            }
            return entity;
        }
    }


    public static class NearIter implements Iterator<Entity>, Iterable<Entity> {
        private final Chunk[] chunks = new Chunk[9];
        private Iterator<Entity> current;
        private int cursor = 0;

        public NearIter init(ChunkedEntityList list, Entity entity) {
            cursor = 0;
            int rawX = (((int) entity.posX) >> SHIFT);
            int rawZ = (((int) entity.posZ) >> SHIFT);
            Chunk temp;

            long chunkIndex = list.indexOf(rawX - 1, rawZ - 1); // -1, -1
            int startIdx    = list.binarySearchUnsafe(0, list.countChunks, chunkIndex);

            temp = list.allChunks[startIdx];
            if(temp != null && chunkIndex == temp.index)chunks[0] = temp;
            else chunks[0] = null;


            chunkIndex = list.indexOf(rawX + 1, rawZ + 1); // +1, +1
            int endIdx = list.binarySearchUnsafe(startIdx,   list.countChunks, chunkIndex);

            temp = list.allChunks[endIdx];
            if(temp != null && chunkIndex == temp.index)chunks[8] = temp;
            else chunks[8] = null;


            chunkIndex = list.indexOf(rawX - 1, rawZ);       // -1, 0
            int tempIdx = list.binarySearch(startIdx, endIdx, chunkIndex);
            if (tempIdx >= 0) chunks[1] = list.allChunks[tempIdx];
            else chunks[1] = null;


            chunkIndex = list.indexOf(rawX - 1, rawZ + 1);// -1, +1
            tempIdx = list.binarySearch(startIdx, endIdx, chunkIndex);
            if (tempIdx >= 0) chunks[2] = list.allChunks[tempIdx];
            else chunks[2] = null;


            chunkIndex = list.indexOf(rawX, rawZ - 1);       // 0, -1
            tempIdx = list.binarySearch(startIdx, endIdx, chunkIndex);
            if (tempIdx >= 0) chunks[3] = list.allChunks[tempIdx];
            else chunks[3] = null;


            chunkIndex = list.indexOf(rawX, rawZ);               // 0, 0
            tempIdx = list.binarySearch(startIdx, endIdx, chunkIndex);
            if (tempIdx >= 0) chunks[4] = list.allChunks[tempIdx];
            else chunks[4] = null;


            chunkIndex = list.indexOf(rawX, rawZ + 1);        // 0, +1
            tempIdx = list.binarySearch(startIdx, endIdx, chunkIndex);
            if (tempIdx >= 0) chunks[5] = list.allChunks[tempIdx];
            else chunks[5] = null;


            chunkIndex = list.indexOf(rawX + 1, rawZ - 1); // +1, -1
            tempIdx = list.binarySearch(startIdx, endIdx, chunkIndex);
            if (tempIdx >= 0) chunks[6] = list.allChunks[tempIdx];
            else chunks[6] = null;


            chunkIndex = list.indexOf(rawX + 1, rawZ);        // +1, 0
            tempIdx = list.binarySearch(startIdx, endIdx, chunkIndex);
            if (tempIdx >= 0) chunks[7] = list.allChunks[tempIdx];
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


        @NotNull
        @Override
        public Iterator<Entity> iterator() {
            return this;
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


    //ignore warning, is faster
    public static final Comparator<Chunk> CHUNK_COMPARATOR = (o1, o2) -> {
        long l1 = o1.index, l2 = o2.index;
        return l1 < l2 ? -1 : l1 == l2 ? 0 : 1;
    };


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


    public final int calcSize(){
        int summ = 0, cs = countChunks;
        Chunk[] ac = allChunks;
        for (int i = 0;i < cs;i++)summ += ac[i].size;
        return summ;
    }


    @Override
    public String toString() {
        return "ChunkedEntityList{" + "chunks=" + countChunks + ", entities=" + calcSize() + '}';
    }


    @NotNull
    @Override
    public ListIterator<Entity> listIterator(int index) {
        throw new UnsupportedOperationException("TODO");
    }


    @NotNull
    @Override
    public ListIterator<Entity> listIterator() {
        return listIterator(0);
    }


    @NotNull
    @Override
    public List<Entity> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("TODO");
    }


    @NotNull
    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("TODO");
    }


    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException("TODO");
    }
}