package net.minecraft.network.datasync;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nullable;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ReportedException;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.fserver.utls.collections.ArrayListE6;


public class EntityDataManager extends Int2ObjectOpenHashMap<EntityDataManager.DataEntry<?>>{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<Class <? extends Entity> , Integer > NEXT_ID_MAP = Maps.newHashMap();
    private final Entity entity;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private boolean empty = true;
    private boolean dirty;

    public EntityDataManager(Entity entityIn) {
        this.entity = entityIn;
    }


    public static <T> DataParameter<T> createKey(Class <? extends Entity > clazz, DataSerializer<T> serializer) {
        if (LOGGER.isDebugEnabled()) {
            try {
                Class<?> oclass = Class.forName(Thread.currentThread().getStackTrace()[2].getClassName());

                if (!oclass.equals(clazz)) {
                    //Forge: log at warn, mods should not add to classes that they don't own, and only add stacktrace when in debug is enabled as it is mostly not needed and consumes time
                    if (LOGGER.isDebugEnabled()) LOGGER.warn("defineId called for: {} from {}", clazz, oclass, new RuntimeException());
                    else LOGGER.warn("defineId called for: {} from {}", clazz, oclass);
                }
            } catch (ClassNotFoundException ignored) {}
        }
        int id;

        if (NEXT_ID_MAP.containsKey(clazz)) {
            id = NEXT_ID_MAP.get(clazz).intValue() + 1;
        } else {
            int i = 0;
            Class<?> oclass1 = clazz;

            while (oclass1 != Entity.class) {
                oclass1 = oclass1.getSuperclass();

                if (NEXT_ID_MAP.containsKey(oclass1)) {
                    i = NEXT_ID_MAP.get(oclass1).intValue() + 1;
                    break;
                }
            }
            id = i;
        }
        if (id > 254) {
            throw new IllegalArgumentException("Data value id is too big with " + id + "! (Max is " + 254 + ")");
        } else {
            NEXT_ID_MAP.put(clazz, Integer.valueOf(id));
            return serializer.createKey(id);
        }
    }


    public <T> void register(DataParameter<T> key, T value) {
        int i = key.getId();
        if (i > 254) {
            throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is " + 254 + ")");
        } else if (super.containsKey(i)) {
            throw new IllegalArgumentException("Duplicate id value for " + i + "!");
        } else if (DataSerializers.getSerializerId(key.getSerializer()) < 0) {
            throw new IllegalArgumentException("Unregistered serializer " + key.getSerializer() + " for " + i + "!");
        } else {
            this.setEntry(key, value);
        }
    }


    public <T> DataEntry<T> registerV(DataParameter<T> key, T value) {
        int i = key.getId();
        if (i > 254) {
            throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is " + 254 + ")");
        } else if (super.containsKey(i)) {
            throw new IllegalArgumentException("Duplicate id value for " + i + "!");
        } else if (DataSerializers.getSerializerId(key.getSerializer()) < 0) {
            throw new IllegalArgumentException("Unregistered serializer " + key.getSerializer() + " for " + i + "!");
        } else {
            DataEntry<T> dataentry = new DataEntry<T>(key, value);
            this.lock.writeLock().lock();
            super.put(key.getId(), dataentry);
            this.empty = false;
            this.lock.writeLock().unlock();
            return dataentry;
        }
    }


    private <T> void setEntry(DataParameter<T> key, T value) {
        DataEntry<T> dataentry = new DataEntry<T>(key, value);
        this.lock.writeLock().lock();
        super.put(key.getId(), dataentry);
        this.empty = false;
        this.lock.writeLock().unlock();
    }


    public <T> DataEntry<T> getEntry(DataParameter<T> key) {
        //this.lock.readLock().lock();
        DataEntry dataentry;
        try {
            dataentry = super.get(key.getId());
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Getting synched entity data");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Synched entity data");
            crashreportcategory.addCrashSection("Data ID", key);
            throw new ReportedException(crashreport);
        }
        //this.lock.readLock().unlock();
        return dataentry;
    }


    public <T> T get(DataParameter<T> key) {
        return this.getEntry(key).getValue();
    }


    public <T> void set(DataParameter<T> key, T value) {
        EntityDataManager.DataEntry<T> entry = this.getEntry(key);
        if (!Objects.equals(value, entry.getValue())) {
            this.lock.writeLock().lock();
            entry.setValue(value);
            this.entity.notifyDataManagerChange(key);
            entry.setDirty(true);
            this.dirty = true;
            this.lock.writeLock().unlock();
        }
    }


    public <T> void setU(DataParameter<T> key, T value) {
        EntityDataManager.DataEntry<T> entry = this.getEntry(key);
        if (!Objects.equals(value, entry.getValue())) {
            entry.setValue(value);
            this.entity.notifyDataManagerChange(key);
            entry.setDirty(true);
            this.dirty = true;
        }
    }


    public <T> void setDirty(DataParameter<T> key) {
        this.getEntry(key).dirty = true;
        this.dirty = true;
    }


    public boolean isDirty() {
        return this.dirty;
    }


    public static void writeEntries(List <DataEntry<? >> entriesIn, PacketBuffer buf) throws IOException {
        if (entriesIn != null) {
            for (int i = 0, s = entriesIn.size(); i < s; ++i) {
                EntityDataManager.DataEntry<?> dataentry = entriesIn.get(i);
                writeEntry(buf, dataentry);
            }
        }
        buf.writeByte(255);
    }


    @Nullable
    public List <EntityDataManager.DataEntry<?>> getDirty() {
        ArrayListE6<EntityDataManager.DataEntry<?>> list = null;

        if (this.dirty) {
            this.lock.readLock().lock();

            for (EntityDataManager.DataEntry<?> dataentry : super.values()) {
                if (dataentry.isDirty()) {
                    dataentry.setDirty(false);
                    if (list == null) {
                        list = new ArrayListE6<>();
                    }
                    list.addF(dataentry.copy());
                }
            }
            this.lock.readLock().unlock();
        }
        this.dirty = false;
        return list;
    }


    public void writeEntries(PacketBuffer buf) throws IOException {
        this.lock.readLock().lock();
        for (EntityDataManager.DataEntry<?> dataentry : super.values()) {
            writeEntry(buf, dataentry);
        }
        this.lock.readLock().unlock();
        buf.writeByte(255);
    }


    @Nullable
    public List<EntityDataManager.DataEntry<?>> getAll() {
        this.lock.readLock().lock();

        ArrayListE6<DataEntry<?>> list = new ArrayListE6<>();
        list.addAll(super.values());

        this.lock.readLock().unlock();
        return list;
    }


    private static <T> void writeEntry(PacketBuffer buf, EntityDataManager.DataEntry<T> entry) throws IOException {
        DataParameter<T> dataparameter = entry.getKey();
        int i = DataSerializers.getSerializerId(dataparameter.getSerializer());

        if (i < 0) {
            throw new EncoderException("Unknown serializer type " + dataparameter.getSerializer());
        } else {
            buf.writeByte(dataparameter.getId());
            buf.writeVarInt(i);
            dataparameter.getSerializer().write(buf, entry.getValue());
        }
    }


    @Nullable
    public static List < EntityDataManager.DataEntry<? >> readEntries(PacketBuffer buf) throws IOException {
        List < EntityDataManager.DataEntry<? >> list = null;
        int i;

        while ((i = buf.readUnsignedByte()) != 255) {
            if (list == null) {
                list = Lists.newArrayList();
            }

            int j = buf.readVarInt();
            DataSerializer<?> dataserializer = DataSerializers.getSerializer(j);

            if (dataserializer == null) {
                throw new DecoderException("Unknown serializer type " + j);
            }
            list.add(new EntityDataManager.DataEntry(dataserializer.createKey(i), dataserializer.read(buf)));
        }
        return list;
    }


    @SideOnly(Side.CLIENT)
    public void setEntryValues(List <DataEntry<? >> entriesIn) {
        this.lock.writeLock().lock();
        for (DataEntry<?> dataentry : entriesIn) {
            DataEntry<?> dataentry1 = super.get(dataentry.getKey().getId());

            if (dataentry1 != null) {
                this.setEntryValue(dataentry1, dataentry);
                this.entity.notifyDataManagerChange(dataentry.getKey());
            }
        }
        this.lock.writeLock().unlock();
        this.dirty = true;
    }


    @SideOnly(Side.CLIENT)
    protected <T> void setEntryValue(EntityDataManager.DataEntry<T> target, EntityDataManager.DataEntry<?> source) {
        target.setValue((T)source.getValue());
    }


    public boolean isEmpty() {
        return this.empty;
    }


    public void setClean() {
        this.dirty = false;
        this.lock.readLock().lock();
        for (DataEntry<?> dataentry : super.values()) {
            dataentry.setDirty(false);
        }
        this.lock.readLock().unlock();
    }


    public static class DataEntry<T> {
        private final DataParameter<T> key;
        private T value;
        private boolean dirty;

        public DataEntry(DataParameter<T> keyIn, T valueIn) {
            this.key = keyIn;
            this.value = valueIn;
            this.dirty = true;
        }


        public void updateAndNotify(EntityDataManager manager, T value){
            manager.entity.notifyDataManagerChange(key);
            setDirty(true);
            manager.dirty = true;
        }


        public DataParameter<T> getKey() {
            return this.key;
        }


        public void setValue(T valueIn) {
            this.value = valueIn;
        }


        public T getValue() {
            return this.value;
        }


        public boolean isDirty() {
            return this.dirty;
        }


        public void setDirty(boolean dirtyIn) {
            this.dirty = dirtyIn;
        }


        public EntityDataManager.DataEntry<T> copy() {
            return new EntityDataManager.DataEntry<T>(this.key, this.key.getSerializer().copyValue(this.value));
        }
    }
}