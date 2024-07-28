package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.fserver.utls.collections.ArrayListE6;


public class NBTTagList extends NBTBase implements java.lang.Iterable<NBTBase> {
    private static final int[] EMPTY = new int[0];
    private static final Logger LOGGER = LogManager.getLogger();
    private ArrayListE6<NBTBase> tagList;
    private byte tagType = 0;

    public NBTTagList() {
        tagList = new ArrayListE6<>();
    }

    public NBTTagList(int initSize) {
        tagList = new ArrayListE6<>(initSize);
    }


    @Override
    void write(DataOutput output) throws IOException {
        ArrayListE6<NBTBase> tagList;
        if ((tagList = this.tagList).isEmptyC()) {
            output.writeByte(this.tagType = 0);
        } else {
            output.writeByte(this.tagType = tagList.getC(0).getId());
        }

        final int size;
        output.writeInt(size = tagList.sizeC());

        for (int i = 0; i < size; ++i) {
            tagList.getC(i).write(output);
        }
    }


    @Override
    void read(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException {
        fill(this, input, depth, sizeTracker);
    }


    public static NBTTagList createAndRead(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException {
        NBTTagList list;
        fill(list = new NBTTagList(), input, depth, sizeTracker);
        return list;
    }


    private static void fill(NBTTagList src, DataInput input, int depth, NBTSizeTracker tracker) throws IOException {
        tracker.read(296L);

        if (depth > 512) {
            throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
        } else {
            byte type = src.tagType = input.readByte();
            int size = input.readInt();

            if (type == 0 && size > 0) {
                throw new RuntimeException("Missing type on ListTag");
            } else {
                tracker.read(32L * (long)size);
                ArrayListE6<NBTBase> tagList = src.tagList = new ArrayListE6<>(size);

                for (int j = 0; j < size; ++j) {
                    NBTBase nbtbase = NBTTagCompound.createAndRead(
                            type, input,depth + 1, tracker
                    );
                    tagList.add(nbtbase);
                }
            }
        }
    }


    @Override
    public byte getId() {
        return 9;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("[");

        for (int i = 0, s = this.tagList.sizeC(); i < s; ++i) {
            if (i != 0) {
                builder.append(',');
            }
            builder.append(this.tagList.getC(i));
        }
        return builder.append(']').toString();
    }


    public void appendTag(NBTBase nbt) {
        if (nbt.getId() == 0) {
            LOGGER.warn("Invalid TagEnd added to ListTag");
        } else {
            if (this.tagType == 0) {
                this.tagType = nbt.getId();
            } else if (this.tagType != nbt.getId()) {
                LOGGER.warn("Adding mismatching tag types to tag list");
                return;
            }
            this.tagList.addF(nbt);
        }
    }


    public void set(int idx, NBTBase nbt) {
        if (nbt.getId() == 0) {
            LOGGER.warn("Invalid TagEnd added to ListTag");
        } else if (idx >= 0 && idx < this.tagList.size()) {
            if (this.tagType == 0) {
                this.tagType = nbt.getId();
            } else if (this.tagType != nbt.getId()) {
                LOGGER.warn("Adding mismatching tag types to tag list");
                return;
            }
            this.tagList.set(idx, nbt);
        } else {
            LOGGER.warn("index out of bounds to set tag in tag list");
        }
    }


    public NBTBase removeTag(int i) {
        return tagList.remove(i);
    }


    @Override
    public boolean isEmpty() {
        return tagList.isEmptyC();
    }


    public NBTTagCompound getCompoundTagAt(int i) {
        if (i >= 0 && i < tagList.sizeC()) {
            NBTBase nbtbase = tagList.getC(i);
            if (nbtbase.getId() == 10) {
                return (NBTTagCompound)nbtbase;
            }
        }
        return new NBTTagCompound();
    }


    public int getIntAt(int pos) {
        if (pos >= 0 && pos < tagList.sizeC()) {
            NBTBase nbtbase = tagList.getC(pos);
            if (nbtbase.getId() == 3) {
                return ((NBTTagInt)nbtbase).getInt();
            }
        }
        return 0;
    }


    public int[] getIntArrayAt(int i) {
        if (i >= 0 && i < tagList.sizeC()) {
            NBTBase nbtbase = tagList.getC(i);
            if (nbtbase.getId() == 11) {
                return ((NBTTagIntArray)nbtbase).getIntArray();
            }
        }
        return EMPTY;
    }


    public double getDoubleAt(int i) {
        if (i >= 0 && i < tagList.sizeC()) {
            NBTBase nbtbase = tagList.getC(i);
            if (nbtbase.getId() == 6) {
                return ((NBTTagDouble)nbtbase).getDouble();
            }
        }
        return 0.0D;
    }


    public float getFloatAt(int i) {
        if (i >= 0 && i < tagList.sizeC()) {
            NBTBase nbtbase = tagList.getC(i);
            if (nbtbase.getId() == 5) {
                return ((NBTTagFloat)nbtbase).getFloat();
            }
        }
        return 0.0F;
    }


    public String getStringTagAt(int i) {
        if (i >= 0 && i < tagList.sizeC()) {
            NBTBase nbtbase = tagList.getC(i);
            return nbtbase.getId() == 8 ? nbtbase.getString() : nbtbase.toString();
        } else {
            return "";
        }
    }


    public NBTBase get(int idx) {
        return idx >= 0 && idx < tagList.sizeC() ? tagList.getC(idx) : new NBTTagEnd();
    }


    public int tagCount() {
        return this.tagList.sizeC();
    }


    @Override
    public NBTTagList copy() {
        NBTTagList nbttaglist = new NBTTagList(this.tagList.sizeC());
        nbttaglist.tagType = this.tagType;

        for (NBTBase nbtbase : this.tagList) {
            nbttaglist.tagList.addF(nbtbase.copy());
        }
        return nbttaglist;
    }


    @Override
    public boolean equals(Object other) {
        if (!super.equals(other)) {
            return false;
        } else {
            NBTTagList nbttaglist = (NBTTagList)other;
            return this.tagType == nbttaglist.tagType && Objects.equals(this.tagList, nbttaglist.tagList);
        }
    }


    @Override
    public int hashCode() {
        return super.hashCode() ^ this.tagList.hashCode();
    }


    public int getTagType() {
        return this.tagType;
    }


    @Override
    public java.util.Iterator<NBTBase> iterator() {
        return tagList.iterator();
    }
}