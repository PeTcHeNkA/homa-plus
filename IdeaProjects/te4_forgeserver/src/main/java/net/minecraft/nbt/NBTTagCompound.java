package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.fserver.ElcaCV;
import plus.fserver.utls.NbtUtils;
import plus.tson.utl.Te4HashMap;


public class NBTTagCompound extends NBTBase {
    private static final byte[] empty = {};
    private static final int[] emptyI = {};
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Pattern SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+");
    private final Te4HashMap<String, NBTBase> tagMap = new Te4HashMap<>();

    @Override
    void write(DataOutput output) throws IOException {
        for (Map.Entry<String, NBTBase> node : tagMap.entrySet())
            writeEntry(node.getKey(), node.getValue(), output);
        output.writeByte(0);
    }


    @Override
    void read(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException {
        sizeTracker.read(384L);
        if (depth > 512) {
            throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
        } else {
            this.tagMap.clear();
            byte b0;

            while ((b0 = readType(input, sizeTracker)) != 0) {
                String s = readKey(input);
                sizeTracker.read(224 + 16L * s.length());
                NBTBase nbtbase = readNBT(b0, s, input, depth + 1, sizeTracker);

                if (this.tagMap.put(s, nbtbase) != null) {
                    sizeTracker.read(288L);
                }
            }
        }
    }


    public Set<String> getKeySet()
    {
        return this.tagMap.keySet();
    }


    @Override
    public byte getId()
    {
        return 10;
    }


    public int getSize()
    {
        return this.tagMap.size();
    }


    public void setTag(String key, NBTBase value) {
        if (value == null) throw new IllegalArgumentException("Invalid null NBT value with key " + key);
        this.tagMap.fput(key, value);
    }


    public void setByte(String key, byte value) {
        this.tagMap.fput(key, new NBTTagByte(value));
    }


    public void setByteU(String key, byte value){
        this.tagMap.fput(key, NbtUtils.create(value));
    }


    public void setShort(String key, short value) {
        this.tagMap.fput(key, new NBTTagShort(value));
    }


    public void setShortU(String key, short value) {
        this.tagMap.fput(key, NbtUtils.create(value));
    }


    public void setInteger(String key, int value) {
        this.tagMap.fput(key, new NBTTagInt(value));
    }


    public void setIntegerU(String key, int value) {
        this.tagMap.fput(key, NbtUtils.create(value));
    }


    public void setLong(String key, long value) {
        this.tagMap.fput(key, new NBTTagLong(value));
    }


    public void setUniqueId(String key, UUID value) {
        this.setLong(key + "Most", value.getMostSignificantBits());
        this.setLong(key + "Least", value.getLeastSignificantBits());
    }


    @Nullable
    public UUID getUniqueId(String key) {
        return new UUID(this.getLong(key + "Most"), this.getLong(key + "Least"));
    }


    public boolean hasUniqueId(String key) {
        return this.hasKey(key + "Most", 99) && this.hasKey(key + "Least", 99);
    }


    public void setFloat(String key, float value) {
        this.tagMap.fput(key, new NBTTagFloat(value));
    }


    public void setDouble(String key, double value) {
        this.tagMap.fput(key, new NBTTagDouble(value));
    }


    public void setString(String key, String value) {
        this.tagMap.fput(key, new NBTTagString(value));
    }


    public void setByteArray(String key, byte[] value) {
        this.tagMap.fput(key, new NBTTagByteArray(value));
    }


    public void setIntArray(String key, int[] value) {
        this.tagMap.fput(key, new NBTTagIntArray(value));
    }


    public void setBoolean(String key, boolean value) {
        this.tagMap.fput(key, NbtUtils.create(value));
    }


    public NBTBase getTag(String key) {
        return this.tagMap.get(key);
    }


    public byte getTagId(String key) {
        NBTBase nbtbase = this.tagMap.get(key);
        return nbtbase == null ? 0 : nbtbase.getId();
    }


    public boolean hasKey(String key) {
        return this.tagMap.containsKey(key);
    }


    public boolean hasKey(String key, int type) {
        int i = this.getTagId(key);
        if (i == type) return true;
        else if (type != 99) return false;
        else return i == 1 || i == 2 || i == 3 || i == 4 || i == 5 || i == 6;
    }


    public byte getByte(String key) {
        try {
            NBTBase nbtbase = this.tagMap.get(key);
            if(nbtbase==null)return 0;
            int id = nbtbase.getId();
            if (id > 0 && id < 7) return ((NBTPrimitive)nbtbase).getByte();
        }
        catch (ClassCastException ignored) {}
        return 0;
    }


    public short getShort(String key) {
        try {
            NBTBase nbtbase = this.tagMap.get(key);
            if(nbtbase==null)return 0;
            int id = nbtbase.getId();
            if (id > 0 && id < 7) return ((NBTPrimitive)nbtbase).getShort();
        }
        catch (ClassCastException ignored) {}
        return 0;
    }


    public int getInteger(String key) {
        try {
            NBTBase nbtbase = this.tagMap.get(key);
            if(nbtbase==null)return 0;
            int id = nbtbase.getId();
            if (id > 0 && id < 7) return ((NBTPrimitive)nbtbase).getInt();
        }
        catch (ClassCastException ignored) {}
        return 0;
    }


    public long getLong(String key) {
        try {
            NBTBase nbtbase = this.tagMap.get(key);
            if(nbtbase==null)return 0;
            int id = nbtbase.getId();
            if (id > 0 && id < 7) return ((NBTPrimitive)nbtbase).getLong();
        }
        catch (ClassCastException ignored) {}
        return 0;
    }


    public float getFloat(String key) {
        try {
            NBTBase nbtbase = this.tagMap.get(key);
            if(nbtbase==null)return 0;
            int id = nbtbase.getId();
            if (id > 0 && id < 7) return ((NBTPrimitive)nbtbase).getFloat();
        }
        catch (ClassCastException ignored) {}
        return 0;
    }


    public double getDouble(String key) {
        try {
            NBTBase nbtbase = this.tagMap.get(key);
            if(nbtbase==null)return 0;
            int id = nbtbase.getId();
            if (id > 0 && id < 7) return ((NBTPrimitive)nbtbase).getDouble();
        }
        catch (ClassCastException ignored) {}
        return 0;
    }


    public String getString(String key) {
        try {
            NBTBase nbtbase = this.tagMap.get(key);
            if(nbtbase==null)return "";
            if (nbtbase.getId() == 8) return nbtbase.getString();
        }
        catch (ClassCastException ignored) {}
        return "";
    }


    public byte[] getByteArray(String key) {
        try {
            NBTBase nbtbase = this.tagMap.get(key);
            if(nbtbase==null)return empty;
            if (nbtbase.getId() == 7)
                return ((NBTTagByteArray)nbtbase).getByteArray();

        } catch (ClassCastException classcastexception) {
            throw new ReportedException(this.createCrashReport(key, 7, classcastexception));
        }
        return empty;
    }


    public int[] getIntArray(String key) {
        try {
            NBTBase nbtbase = this.tagMap.get(key);
            if(nbtbase==null)return emptyI;
            if (nbtbase.getId() == 11) {
                return ((NBTTagIntArray)nbtbase).getIntArray();
            }
        } catch (ClassCastException classcastexception) {
            throw new ReportedException(this.createCrashReport(key, 11, classcastexception));
        }
        return emptyI;
    }


    public NBTTagCompound getCompoundTag(String key) {
        try {
            NBTBase nbtbase = this.tagMap.get(key);
            if(nbtbase==null)return new NBTTagCompound();
            if (nbtbase.getId() == 10) return (NBTTagCompound)nbtbase;
        }
        catch (ClassCastException classcastexception) {
            throw new ReportedException(this.createCrashReport(key, 10, classcastexception));
        }
        return new NBTTagCompound();
    }


    public NBTTagList getTagList(String key, int type) {
        try {
            NBTBase nbtbase = this.tagMap.get(key);
            if(nbtbase==null)return new NBTTagList();
            if (nbtbase.getId() == 9) {
                NBTTagList nbttaglist = (NBTTagList)this.tagMap.get(key);

                if (!nbttaglist.isEmpty() && nbttaglist.getTagType() != type) {
                    return new NBTTagList();
                }
                return nbttaglist;
            }
        }
        catch (ClassCastException classcastexception) {
            throw new ReportedException(this.createCrashReport(key, 9, classcastexception));
        }

        return new NBTTagList();
    }


    public boolean getBoolean(String key) {
        return this.getByte(key) != 0;
    }


    public void removeTag(String key) {
        this.tagMap.remove(key);
    }


    public String toString() {
        StringBuilder stringbuilder = new StringBuilder("{");
        Collection<String> collection = this.tagMap.keySet();

        for (String s : collection) {
            if (stringbuilder.length() != 1) {
                stringbuilder.append(',');
            }
            stringbuilder.append(handleEscape(s)).append(':').append(this.tagMap.get(s));
        }
        return stringbuilder.append('}').toString();
    }


    public boolean isEmpty() {
        return this.tagMap.isEmpty();
    }


    private CrashReport createCrashReport(final String key, final int expectedType, ClassCastException ex) {
        CrashReport crashreport = CrashReport.makeCrashReport(ex, "Reading NBT data");
        CrashReportCategory crashreportcategory = crashreport.makeCategoryDepth("Corrupt NBT tag", 1);
        crashreportcategory.addDetail("Tag type found",
                () -> NBTBase.NBT_TYPES[NBTTagCompound.this.tagMap.get(key).getId()]);
        crashreportcategory.addDetail("Tag type expected",
                () -> NBTBase.NBT_TYPES[expectedType]);
        crashreportcategory.addCrashSection("Tag name", key);
        return crashreport;
    }


    @Override
    public NBTTagCompound copy() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        for (Map.Entry<String, NBTBase> node: this.tagMap.entrySet())
            nbttagcompound.tagMap.fput(node.getKey(), node.getValue().copy());

        return nbttagcompound;
    }


    @Override
    public boolean equals(Object other) {
        return super.equals(other) && Objects.equals(this.tagMap.entrySet(), ((NBTTagCompound)other).tagMap.entrySet());
    }


    @Override
    public int hashCode() {
        return super.hashCode() ^ this.tagMap.hashCode();
    }


    private static void writeEntry(String name, NBTBase data, DataOutput output) throws IOException {
        output.writeByte(data.getId());
        if (data.getId() != 0) {
            output.writeUTF(name);
            data.write(output);
        }
    }


    private static byte readType(DataInput input, NBTSizeTracker sizeTracker) throws IOException {
        sizeTracker.read(8);
        return input.readByte();
    }


    private static String readKey(DataInput input) throws IOException {
        if(ElcaCV.INTERN_NBT_KEYS) return input.readUTF().intern();
        return input.readUTF();
    }


    static NBTBase readNBT(byte id, String key, DataInput input, int depth, NBTSizeTracker tracker) throws IOException {
        tracker.read(32); //Forge: 4 extra bytes for the object allocation.
        NBTBase nbtbase = NBTBase.create(id);

        try {
            nbtbase.read(input, depth, tracker);
            return nbtbase;
        }
        catch (IOException ioexception) {
            CrashReport crashreport = CrashReport.makeCrashReport(ioexception, "Loading NBT data");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("NBT Tag");
            crashreportcategory.addCrashSection("Tag name", key);
            crashreportcategory.addCrashSection("Tag type", id);
            throw new ReportedException(crashreport);
        }
    }


    protected static NBTBase createAndRead(byte b, DataInput input, int depth, NBTSizeTracker tracker) throws IOException {
        switch (b) {
            case 0:
                return NBTTagEnd.read(tracker);
            case 1:
                return NBTTagByte.read(input, tracker);
            case 2:
                return NBTTagShort.read(input, tracker);
            case 3:
                return NBTTagInt.read(input, tracker);
            case 4:
                return NBTTagLong.read(input, tracker);
            case 5:
                return NBTTagFloat.read(input, tracker);
            case 6:
                return NBTTagDouble.read(input, tracker);
            case 7: {
                NBTTagByteArray arr = new NBTTagByteArray();
                arr.read(input, depth, tracker);
                return arr;
            }
            case 8: {
                NBTTagString str = new NBTTagString();
                str.read(input, depth, tracker);
                return str;
            }
            case 9:
                return NBTTagList.createAndRead(input, depth, tracker);
            case 10: {
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.read(input, depth, tracker);
                return nbt;
            }
            case 11: {
                NBTTagIntArray nbt = new NBTTagIntArray();
                nbt.read(input, depth, tracker);
                return nbt;
            }
            case 12:{
                NBTTagLongArray arr = new NBTTagLongArray();
                arr.read(input, depth, tracker);
                return arr;
            }
            default:
                return null;
        }
    }


    public void merge(NBTTagCompound other) {
        for (String s : other.tagMap.keySet()) {
            NBTBase nbtbase = other.tagMap.get(s);

            if (nbtbase.getId() == 10) {
                if (this.hasKey(s, 10)) {
                    NBTTagCompound nbttagcompound = this.getCompoundTag(s);
                    nbttagcompound.merge((NBTTagCompound)nbtbase);
                } else {
                    this.setTag(s, nbtbase.copy());
                }
            }
            else {
                this.setTag(s, nbtbase.copy());
            }
        }
    }


    protected static String handleEscape(String str) {
        return SIMPLE_VALUE.matcher(str).matches() ? str : NBTTagString.quoteAndEscape(str);
    }
}