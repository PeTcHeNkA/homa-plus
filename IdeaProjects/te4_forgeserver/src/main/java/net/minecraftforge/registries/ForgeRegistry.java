package net.minecraftforge.registries;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.util.*;
import java.util.*;
import javax.annotation.*;
import org.apache.commons.lang3.*;
import com.google.common.base.*;
import net.minecraftforge.fml.common.*;
import java.lang.reflect.*;
import com.google.common.collect.*;
import net.minecraftforge.event.*;
import net.minecraft.nbt.*;
import plus.tson.utl.Te4HashSet;


public class ForgeRegistry<V extends IForgeRegistryEntry<V>> implements IForgeRegistryInternal<V>, IForgeRegistryModifiable<V> {
    public static final boolean DEBUG;
    private final RegistryManager stage;
    private final BiMap<Integer, V> ids;
    private final BiMap<ResourceLocation, V> names;
    private final Class<V> superType;
    private final Map<ResourceLocation, ResourceLocation> aliases;
    final Map<ResourceLocation, Object> slaves;
    private final ResourceLocation defaultKey;
    private final IForgeRegistry.CreateCallback<V> create;
    private final IForgeRegistry.AddCallback<V> add;
    private final IForgeRegistry.ClearCallback<V> clear;
    private final IForgeRegistry.ValidateCallback<V> validate;
    private final IForgeRegistry.MissingFactory<V> missing;
    private final BitSet availabilityMap;
    private final Set<ResourceLocation> dummies;
    private final IntOpenHashSet blocked;
    private final Multimap<ResourceLocation, V> overrides;
    private final BiMap<OverrideOwner, V> owners;
    private final IForgeRegistry.DummyFactory<V> dummyFactory;
    private final boolean isDelegated;
    private final int min;
    private final int max;
    private final boolean allowOverrides;
    private final boolean isModifiable;
    private V defaultValue;
    boolean isFrozen;

    ForgeRegistry(final Class<V> superType, final ResourceLocation defaultKey, final int min, final int max, @Nullable final IForgeRegistry.CreateCallback<V> create, @Nullable final IForgeRegistry.AddCallback<V> add, @Nullable final IForgeRegistry.ClearCallback<V> clear, @Nullable final IForgeRegistry.ValidateCallback<V> validate, final RegistryManager stage, final boolean allowOverrides, final boolean isModifiable, @Nullable final IForgeRegistry.DummyFactory<V> dummyFactory, @Nullable final IForgeRegistry.MissingFactory<V> missing) {
        this.ids = HashBiMap.create();
        this.names = HashBiMap.create();
        this.aliases = Maps.newHashMap();
        this.slaves = Maps.newHashMap();
        this.dummies = new Te4HashSet<>();
        this.blocked = new IntOpenHashSet();
        this.overrides = ArrayListMultimap.create();
        this.owners = HashBiMap.create();
        this.defaultValue = null;
        this.isFrozen = false;
        this.stage = stage;
        this.superType = superType;
        this.defaultKey = defaultKey;
        this.min = min;
        this.max = max;
        this.availabilityMap = new BitSet(Math.min(max + 1, 4095));
        this.create = create;
        this.add = add;
        this.clear = clear;
        this.validate = validate;
        this.missing = missing;
        this.isDelegated = IForgeRegistryEntry.Impl.class.isAssignableFrom(superType);
        this.allowOverrides = allowOverrides;
        this.isModifiable = isModifiable;
        this.dummyFactory = dummyFactory;
        if (this.create != null) {
            this.create.onCreate(this, stage);
        }
    }

    @Override
    public void register(final V value) {
        this.add(-1, value);
    }


    @Override
    public Iterator<V> iterator() {
        return new Iterator<V>() {
            int cur = -1;
            V next = null;
            {
                this.next();
            }

            @Override
            public boolean hasNext() {
                return this.next != null;
            }

            @Override
            public V next() {
                final V ret = this.next;
                do {
                    this.cur = ForgeRegistry.this.availabilityMap.nextSetBit(this.cur + 1);
                    this.next = ForgeRegistry.this.ids.get(this.cur);
                } while (this.next == null && this.cur != -1);
                return ret;
            }
        };
    }


    @Override
    public Class<V> getRegistrySuperType() {
        return this.superType;
    }


    @Override
    public void registerAll(final V... values) {
        for (final V value : values) this.register(value);
    }


    @Override
    public boolean containsKey(ResourceLocation key) {
        while (key != null) {
            if (this.names.containsKey(key)) {
                return true;
            }
            key = this.aliases.get(key);
        }
        return false;
    }


    @Override
    public boolean containsValue(final V value) {
        return this.names.containsValue(value);
    }


    @Override
    public V getValue(ResourceLocation key) {
        V ret;
        for (ret = this.names.get(key), key = this.aliases.get(key); ret == null && key != null; ret = (V)this.names.get((Object)key), key = this.aliases.get(key)) {}
        return (ret == null) ? this.defaultValue : ret;
    }


    @Override
    public ResourceLocation getKey(final V value) {
        final ResourceLocation ret = this.names.inverse().get(value);
        return (ret == null) ? this.defaultKey : ret;
    }


    @Override
    public Set<ResourceLocation> getKeys() {
        return Collections.unmodifiableSet(this.names.keySet());
    }


    @Deprecated
    @Override
    public List<V> getValues() {
        return ImmutableList.copyOf(this.names.values());
    }


    @Nonnull
    @Override
    public Collection<V> getValuesCollection() {
        return Collections.unmodifiableSet(this.names.values());
    }


    @Override
    public Set<Map.Entry<ResourceLocation, V>> getEntries() {
        return Collections.unmodifiableSet(this.names.entrySet());
    }


    @Override
    public <T> T getSlaveMap(final ResourceLocation name, final Class<T> type) {
        return (T)this.slaves.get(name);
    }


    @Override
    public void setSlaveMap(final ResourceLocation name, final Object obj) {
        this.slaves.put(name, obj);
    }


    public int getID(final V value) {
        Integer ret = this.ids.inverse().get(value);
        if (ret == null && this.defaultValue != null) {
            ret = this.ids.inverse().get(this.defaultValue);
        }
        return (ret == null) ? -1 : ret;
    }


    public int getID(final ResourceLocation name) {
        return this.getID(this.names.get(name));
    }


    private int getIDRaw(final V value) {
        final Integer ret = this.ids.inverse().get(value);
        return (ret == null) ? -1 : ret;
    }


    private int getIDRaw(final ResourceLocation name) {
        return this.getIDRaw(this.names.get(name));
    }


    public V getValue(final int id) {
        final V ret = (V)this.ids.get((Object)id);
        return (ret == null) ? this.defaultValue : ret;
    }


    void validateKey() {
        if (this.defaultKey != null) {
            Validate.notNull((Object)this.defaultValue, "Missing default of ForgeRegistry: " + this.defaultKey + " Type: " + this.superType, new Object[0]);
        }
    }


    ForgeRegistry<V> copy(final RegistryManager stage) {
        return new ForgeRegistry<>(this.superType, this.defaultKey, this.min, this.max, this.create, this.add, this.clear, this.validate, stage, this.allowOverrides, this.isModifiable, this.dummyFactory, this.missing);
    }


    int add(final int id, final V value) {
        final ModContainer mc = Loader.instance().activeModContainer();
        final String owner = (mc == null || (mc instanceof InjectedModContainer && ((InjectedModContainer)mc).wrappedContainer instanceof FMLContainer)) ? null : mc.getModId().toLowerCase();
        return this.add(id, value, owner);
    }


    public void addU(final int id, final V value){
        add(id, value);
    }


    int add(final int id, final V value, final String owner) {
        final ResourceLocation key = (value == null) ? null : value.getRegistryName();
        Preconditions.checkNotNull(key, "Can't use a null-name for the registry, object %s.", (Object)value);
        Preconditions.checkNotNull(value, "Can't add null-object to the registry, name %s.", (Object)key);
        int idToUse = id;
        if (idToUse < 0 || this.availabilityMap.get(idToUse)) {
            idToUse = this.availabilityMap.nextClearBit(this.min);
        }
        if (idToUse > this.max) {
            throw new RuntimeException(String.format("Invalid id %d - maximum id range exceeded.", idToUse));
        }
        final V oldEntry = this.getRaw(key);
        if (oldEntry == value) {
            FMLLog.bigWarning("Registry {}: The object {} has been registered twice for the same name {}.", this.superType.getSimpleName(), value, key);
            return this.getID(value);
        }
        if (oldEntry != null) {
            if (!this.allowOverrides) {
                throw new IllegalArgumentException(String.format("The name %s has been registered twice, for %s and %s.", key, this.getRaw(key), value));
            }
            if (owner == null) {
                throw new IllegalStateException(String.format("Could not determine owner for the override on %s. Value: %s", key, value));
            }
            if (ForgeRegistry.DEBUG) {
                FMLLog.log.debug("Registry {} Override: {} {} -> {}", this.superType.getSimpleName(), key, oldEntry, value);
            }
            idToUse = this.getID(oldEntry);
        }
        final Integer foundId = this.ids.inverse().get(value);
        if (foundId != null) {
            final V otherThing = this.ids.get(foundId);
            throw new IllegalArgumentException(String.format("The object %s{%x} has been registered twice, using the names %s and %s. (Other object at this id is %s{%x})", value, System.identityHashCode(value), this.getKey(value), key, otherThing, System.identityHashCode(otherThing)));
        }
        if (this.isLocked()) {
            throw new IllegalStateException(String.format("The object %s (name %s) is being added too late.", value, key));
        }
        if (this.defaultKey != null && this.defaultKey.equals(key)) {
            if (this.defaultValue != null) {
                throw new IllegalStateException(String.format("Attemped to override already set default value. This is not allowed: The object %s (name %s)", value, key));
            }
            this.defaultValue = value;
        }
        this.names.put(key, value);
        this.ids.put(idToUse, value);
        this.availabilityMap.set(idToUse);
        this.owners.put(new OverrideOwner((owner == null) ? key.getNamespace() : owner, key), value);
        if (this.isDelegated) {
            this.getDelegate(value).setName(key);
            if (oldEntry != null) {
                if (!this.overrides.get(key).contains(oldEntry)) {
                    this.overrides.put(key, oldEntry);
                }
                this.overrides.get(key).remove(value);
                if (this.stage == RegistryManager.ACTIVE) {
                    this.getDelegate(oldEntry).changeReference(value);
                }
            }
        }
        if (this.add != null) {
            this.add.onAdd(this, this.stage, idToUse, value, oldEntry);
        }
        if (this.dummies.remove(key) && ForgeRegistry.DEBUG) {
            FMLLog.log.debug("Registry {} Dummy Remove: {}", this.superType.getSimpleName(), key);
        }
        if (ForgeRegistry.DEBUG) {
            FMLLog.log.trace("Registry {} add: {} {} {} (req. id {})", this.superType.getSimpleName(), key, idToUse, value, id);
        }
        return idToUse;
    }


    private V getRaw(ResourceLocation key) {
        V ret;
        for (ret = (V)this.names.get((Object)key), key = this.aliases.get(key); ret == null && key != null; ret = this.names.get(key), key = this.aliases.get(key)) {}
        return ret;
    }


    @Deprecated
    public V getRaw(final int id) {
        return this.ids.get(id);
    }


    void addAlias(final ResourceLocation from, final ResourceLocation to) {
        if (this.isLocked()) {
            throw new IllegalStateException(String.format("Attempted to register the alias %s -> %s to late", from, to));
        }
        this.aliases.put(from, to);
        if (ForgeRegistry.DEBUG) {
            FMLLog.log.trace("Registry {} alias: {} -> {}", this.superType.getSimpleName(), from, to);
        }
    }


    void addDummy(final ResourceLocation key) {
        if (this.isLocked()) {
            throw new IllegalStateException(String.format("Attempted to register the dummy %s to late", key));
        }
        this.dummies.add(key);
        if (ForgeRegistry.DEBUG) {
            FMLLog.log.trace("Registry {} dummy: {}", this.superType.getSimpleName(), key);
        }
    }

    private RegistryDelegate<V> getDelegate(final V thing) {
        if (this.isDelegated) {
            return (RegistryDelegate<V>) ((IForgeRegistryEntry.Impl)thing).delegate;
        }
        throw new IllegalStateException("Tried to get existing delegate from registry that is not delegated.");
    }

    void resetDelegates() {
        if (!this.isDelegated) {
            return;
        }
        for (final V value : this) {
            this.getDelegate(value).changeReference(value);
        }
        for (final V value : this.overrides.values()) {
            this.getDelegate(value).changeReference(value);
        }
    }

    V getDefault() {
        return this.defaultValue;
    }

    boolean isDummied(final ResourceLocation key) {
        return this.dummies.contains(key);
    }


    void validateContent(final ResourceLocation registryName) {
        try {
            final Method method = BitSet.class.getDeclaredMethod("trimToSize");
            method.setAccessible(true);
            method.invoke(this.availabilityMap);
        }
        catch (Exception ex) {}
        for (final V obj : this) {
            final int id = this.getID(obj);
            final ResourceLocation name = this.getKey(obj);
            if (name == null) {
                throw new IllegalStateException(String.format("Registry entry for %s %s, id %d, doesn't yield a name.", registryName, obj, id));
            }
            if (id > this.max) {
                throw new IllegalStateException(String.format("Registry entry for %s %s, name %s uses the too large id %d.", registryName, obj, name, id));
            }
            if (this.getValue(id) != obj) {
                throw new IllegalStateException(String.format("Registry entry for id %d, name %s, doesn't yield the expected %s %s.", id, name, registryName, obj));
            }
            if (this.getValue(name) != obj) {
                throw new IllegalStateException(String.format("Registry entry for name %s, id %d, doesn't yield the expected %s %s.", name, id, registryName, obj));
            }
            if (this.getID(name) != id) {
                throw new IllegalStateException(String.format("Registry entry for name %s doesn't yield the expected id %d.", name, id));
            }
            if (this.validate == null) {
                continue;
            }
            this.validate.onValidate(this, this.stage, id, name, obj);
        }
    }


    void sync(final ResourceLocation name, final ForgeRegistry<V> from) {
        if (ForgeRegistry.DEBUG) {
            FMLLog.log.debug("Registry {} Sync: {} -> {}", this.superType.getSimpleName(), this.stage.getName(), from.stage.getName());
        }
        if (this == from) {
            throw new IllegalArgumentException("WTF We are the same!?!?!");
        }
        if (from.superType != this.superType) {
            throw new IllegalArgumentException("Attempted to copy to incompatible registry: " + name + " " + from.superType + " -> " + this.superType);
        }
        this.isFrozen = false;
        if (this.clear != null) {
            this.clear.onClear(this, this.stage);
        }
        this.aliases.clear();
        from.aliases.forEach(this::addAlias);
        this.ids.clear();
        this.names.clear();
        this.availabilityMap.clear(0, this.availabilityMap.length());
        this.defaultValue = null;
        this.overrides.clear();
        this.owners.clear();
        boolean errored = false;
        for (final Map.Entry<ResourceLocation, V> entry : from.names.entrySet()) {
            final List<V> overrides = Lists.newArrayList(from.overrides.get(entry.getKey()));
            final int id = from.getID(entry.getKey());
            if (overrides.isEmpty()) {
                final int realId = this.add(id, entry.getValue());
                if (id == realId || id == -1) {
                    continue;
                }
                FMLLog.log.warn("Registry {}: Object did not get ID it asked for. Name: {} Expected: {} Got: {}", this.superType.getSimpleName(), entry.getKey(), id, realId);
                errored = true;
            }
            else {
                overrides.add(entry.getValue());
                for (final V value : overrides) {
                    final OverrideOwner owner = from.owners.inverse().get(value);
                    if (owner == null) {
                        FMLLog.log.warn("Registry {}: Override did not have an associated owner object. Name: {} Value: {}", this.superType.getSimpleName(), entry.getKey(), value);
                        errored = true;
                    }
                    else {
                        final int realId2 = this.add(id, value, owner.owner);
                        if (id == realId2 || id == -1) {
                            continue;
                        }
                        FMLLog.log.warn("Registry {}: Object did not get ID it asked for. Name: {} Expected: {} Got: {}", this.superType.getSimpleName(), entry.getKey(), id, realId2);
                        errored = true;
                    }
                }
            }
        }
        this.dummies.clear();
        from.dummies.forEach(this::addDummy);
        if (errored) {
            throw new RuntimeException("One of more entry values did not copy to the correct id. Check log for details!");
        }
    }


    @Override
    public void clear() {
        if (!this.isModifiable) {
            throw new UnsupportedOperationException("Attempted to clear a non-modifiable Forge Registry");
        }
        if (this.isLocked()) {
            throw new IllegalStateException("Attempted to clear the registry to late.");
        }
        if (this.clear != null) {
            this.clear.onClear(this, this.stage);
        }
        this.aliases.clear();
        this.dummies.clear();
        this.ids.clear();
        this.names.clear();
        this.availabilityMap.clear(0, this.availabilityMap.length());
    }


    @Override
    public V remove(final ResourceLocation key) {
        if (!this.isModifiable) {
            throw new UnsupportedOperationException("Attempted to remove from a non-modifiable Forge Registry");
        }
        if (this.isLocked()) {
            throw new IllegalStateException("Attempted to remove from the registry to late.");
        }
        final V value = this.names.remove(key);
        if (value != null) {
            final Integer id = this.ids.inverse().remove(value);
            if (id == null) {
                throw new IllegalStateException("Removed a entry that did not have an associated id: " + key + " " + value.toString() + " This should never happen unless hackery!");
            }
            if (ForgeRegistry.DEBUG) {
                FMLLog.log.trace("Registry {} remove: {} {}", this.superType.getSimpleName(), (Object)key, (Object)id);
            }
        }
        return value;
    }

    void block(final int id) {
        this.blocked.add(id);
        this.availabilityMap.set(id);
    }

    @Override
    public boolean isLocked() {
        return this.isFrozen;
    }

    public void freeze() {
        this.isFrozen = true;
    }

    public void unfreeze() {
        this.isFrozen = false;
    }


    RegistryEvent.Register<V> getRegisterEvent(final ResourceLocation name) {
        return new RegistryEvent.Register<>(name, this);
    }


    void dump(final ResourceLocation name) {
        final List<Integer> ids = Lists.newArrayList();
        this.getKeys().forEach(n -> ids.add(this.getID(n)));
        Collections.sort(ids);
        FMLLog.log.trace("Registry Name : {}", name);
        ids.forEach(id -> FMLLog.log.trace("  Registry: {} {} {}", id, this.getKey(this.getValue(id)), this.getValue(id)));
    }


    public void loadIds(final Map<ResourceLocation, Integer> ids, final Map<ResourceLocation, String> overrides, final Map<ResourceLocation, Integer> missing, final Map<ResourceLocation, Integer[]> remapped, final ForgeRegistry<V> old, final ResourceLocation name) {
        final Map<ResourceLocation, String> ovs = Maps.newHashMap(overrides);
        for (final Map.Entry<ResourceLocation, Integer> entry : ids.entrySet()) {
            final ResourceLocation itemName = entry.getKey();
            final int newId = entry.getValue();
            final int currId = old.getIDRaw(itemName);
            if (currId == -1) {
                FMLLog.log.info("Registry {}: Found a missing id from the world {}", this.superType.getSimpleName(), itemName);
                missing.put(itemName, newId);
            }
            else {
                if (currId != newId) {
                    FMLLog.log.debug("Registry {}: Fixed {} id mismatch {}: {} (init) -> {} (map).", this.superType.getSimpleName(), name, itemName, currId, newId);
                    remapped.put(itemName, new Integer[] { currId, newId });
                }
                V obj = old.getRaw(itemName);
                Preconditions.checkState(obj != null, "objectKey has an ID but no object. Reflection/ASM hackery? Registry bug?");
                final List<V> lst = Lists.newArrayList(old.overrides.get(itemName));
                String primaryName = null;
                if (old.overrides.containsKey(itemName)) {
                    if (!overrides.containsKey(itemName)) {
                        lst.add(obj);
                        obj = old.overrides.get(itemName).iterator().next();
                        primaryName = old.owners.inverse().get(obj).owner;
                    }
                    else {
                        primaryName = overrides.get(itemName);
                    }
                }
                for (final V value : lst) {
                    final OverrideOwner owner = old.owners.inverse().get(value);
                    if (owner == null) {
                        FMLLog.log.warn("Registry {}: Override did not have an associated owner object. Name: {} Value: {}", this.superType.getSimpleName(), entry.getKey(), value);
                    }
                    else {
                        if (primaryName.equals(owner.owner)) {
                            continue;
                        }
                        final int realId = this.add(newId, value, owner.owner);
                        if (newId == realId) {
                            continue;
                        }
                        FMLLog.log.warn("Registry {}: Object did not get ID it asked for. Name: {} Expected: {} Got: {}", this.superType.getSimpleName(), entry.getKey(), newId, realId);
                    }
                }
                final int realId2 = this.add(newId, obj, (primaryName == null) ? itemName.getNamespace() : primaryName);
                if (realId2 != newId) {
                    FMLLog.log.warn("Registry {}: Object did not get ID it asked for. Name: {} Expected: {} Got: {}", this.superType.getSimpleName(), entry.getKey(), newId, realId2);
                }
                ovs.remove(itemName);
            }
        }
        for (final Map.Entry<ResourceLocation, String> entry2 : ovs.entrySet()) {
            final ResourceLocation itemName = entry2.getKey();
            final String owner2 = entry2.getValue();
            final String current = this.owners.inverse().get(this.getRaw(itemName)).owner;
            if (!owner2.equals(current)) {
                final V _new = this.owners.get(new OverrideOwner(owner2, itemName));
                if (_new == null) {
                    FMLLog.log.warn("Registry {}: Skipping override for {}, Unknown owner {}", this.superType.getSimpleName(), itemName, owner2);
                }
                else {
                    FMLLog.log.info("Registry {}: Activating override {} for {}", this.superType.getSimpleName(), owner2, itemName);
                    final int newId2 = this.getID(itemName);
                    final int realId3 = this.add(newId2, _new, owner2);
                    if (newId2 == realId3) {
                        continue;
                    }
                    FMLLog.log.warn("Registry {}: Object did not get ID it asked for. Name: {} Expected: {} Got: {}", this.superType.getSimpleName(), entry2.getKey(), newId2, realId3);
                }
            }
        }
    }

    boolean markDummy(final ResourceLocation key, final int id) {
        if (this.dummyFactory == null) {
            return false;
        }
        final V dummy = this.dummyFactory.createDummy(key);
        if (ForgeRegistry.DEBUG) {
            FMLLog.log.debug("Registry Dummy Add: {} {} -> {}", key, id, dummy);
        }
        this.availabilityMap.clear(id);
        if (this.containsKey(key)) {
            final V value = this.names.remove(key);
            if (value == null) {
                throw new IllegalStateException("ContainsKey for " + key + " was true, but removing by name returned no value.. This should never happen unless hackery!");
            }
            final Integer oldid = this.ids.inverse().remove(value);
            if (oldid == null) {
                throw new IllegalStateException("Removed a entry that did not have an associated id: " + key + " " + value + " This should never happen unless hackery!");
            }
            if (oldid != id) {
                FMLLog.log.debug("Registry {}: Dummy ID mismatch {} {} -> {}", this.superType.getSimpleName(), key, oldid, id);
            }
            if (ForgeRegistry.DEBUG) {
                FMLLog.log.debug("Registry {} remove: {} {}", this.superType.getSimpleName(), key, oldid);
            }
        }
        final int realId = this.add(id, dummy);
        if (realId != id) {
            FMLLog.log.warn("Registry {}: Object did not get ID it asked for. Name: {} Expected: {} Got: {}", this.superType.getSimpleName(), key, id, realId);
        }
        this.dummies.add(key);
        return true;
    }


    public Snapshot makeSnapshot() {
        final Snapshot ret = new Snapshot();

        this.ids.forEach((id, value) -> ret.ids.put(this.getKey(value), id));
        ret.aliases.putAll(this.aliases);
        ret.blocked.addAll(this.blocked);
        ret.dummied.addAll(this.dummies);
        ret.overrides.putAll(this.getOverrideOwners());
        return ret;
    }


    Map<ResourceLocation, String> getOverrideOwners() {
        final Map<ResourceLocation, String> ret = Maps.newHashMap();
        for (final ResourceLocation key : this.overrides.keySet()) {
            final V obj = this.names.get(key);
            final OverrideOwner owner = this.owners.inverse().get(obj);
            if (owner == null && ForgeRegistry.DEBUG) {
                FMLLog.log.debug("Registry {} {}: Invalid override {} {}", this.superType.getSimpleName(), this.stage.getName(), key, obj);
            }
            ret.put(key, owner.owner);
        }
        return ret;
    }


    public RegistryEvent.MissingMappings<?> getMissingEvent(final ResourceLocation name, final Map<ResourceLocation, Integer> map) {
        final List<RegistryEvent.MissingMappings.Mapping<V>> lst = Lists.newArrayList();
        final ForgeRegistry<V> pool = RegistryManager.ACTIVE.getRegistry(name);
        map.forEach((rl, id) -> lst.add(new RegistryEvent.MissingMappings.Mapping<>(this, pool, rl, id)));
        return new RegistryEvent.MissingMappings<>(name, this, lst);
    }


    void processMissingEvent(final ResourceLocation name, final ForgeRegistry<V> pool, final List<RegistryEvent.MissingMappings.Mapping<V>> mappings, final Map<ResourceLocation, Integer> missing, final Map<ResourceLocation, Integer[]> remaps, final Collection<ResourceLocation> defaulted, final Collection<ResourceLocation> failed, final boolean injectNetworkDummies) {
        FMLLog.log.debug("Processing missing event for {}:", name);
        int ignored = 0;
        for (final RegistryEvent.MissingMappings.Mapping<V> remap : mappings) {
            final RegistryEvent.MissingMappings.Action action = remap.getAction();
            if (action == RegistryEvent.MissingMappings.Action.REMAP) {
                final int currId = this.getID((V)remap.getTarget());
                final ResourceLocation newName = pool.getKey(remap.getTarget());
                FMLLog.log.debug("  Remapping {} -> {}.", remap.key, newName);
                missing.remove(remap.key);
                final int realId = this.add(remap.id, (V)remap.getTarget());
                if (realId != remap.id) {
                    FMLLog.log.warn("Registered object did not get ID it asked for. Name: {} Type: {} Expected: {} Got: {}", newName, this.getRegistrySuperType(), remap.id, realId);
                }
                this.addAlias(remap.key, newName);
                if (currId == realId) {
                    continue;
                }
                FMLLog.log.info("  Fixed id mismatch {}: {} (init) -> {} (map).", newName, currId, realId);
                remaps.put(newName, new Integer[] { currId, realId });
            }
            else {
                if (action == RegistryEvent.MissingMappings.Action.DEFAULT) {
                    final V m = (this.missing == null) ? null : this.missing.createMissing(remap.key, injectNetworkDummies);
                    if (m == null) {
                        defaulted.add(remap.key);
                    }
                    else {
                        this.add(remap.id, m, remap.key.getNamespace());
                    }
                }
                else if (action == RegistryEvent.MissingMappings.Action.IGNORE) {
                    FMLLog.log.debug("  Ignoring {}", (Object)remap.key);
                    ++ignored;
                }
                else if (action == RegistryEvent.MissingMappings.Action.FAIL) {
                    FMLLog.log.debug("  Failing {}!", (Object)remap.key);
                    failed.add(remap.key);
                }
                else if (action == RegistryEvent.MissingMappings.Action.WARN) {
                    FMLLog.log.warn("  {} may cause world breakage!", (Object)remap.key);
                }
                this.block(remap.id);
            }
        }
        if (failed.isEmpty() && ignored > 0) {
            FMLLog.log.debug("There were {} missing mappings that have been ignored", (Object)ignored);
        }
    }

    static {
        DEBUG = Boolean.parseBoolean(System.getProperty("forge.debugRegistryEntries", "false"));
    }


    public static class Snapshot {
        public final Map<ResourceLocation, Integer> ids;
        public final Map<ResourceLocation, ResourceLocation> aliases;
        public final Set<Integer> blocked;
        public final Set<ResourceLocation> dummied;
        public final Map<ResourceLocation, String> overrides;

        public Snapshot() {
            this.ids = Maps.newHashMap();
            this.aliases = Maps.newHashMap();
            this.blocked = new Te4HashSet<>();
            this.dummied = new Te4HashSet<>();
            this.overrides = Maps.newHashMap();
        }

        public NBTTagCompound write() {
            final NBTTagCompound data = new NBTTagCompound();
            final NBTTagList ids = new NBTTagList();

            this.ids.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setString("K", e.getKey().toString());
                tag.setIntegerU("V", e.getValue());
                ids.appendTag(tag);
            });
            data.setTag("ids", ids);

            final NBTTagList aliases = new NBTTagList();

            this.aliases.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> {
                NBTTagCompound tag2 = new NBTTagCompound();
                tag2.setString("K", e.getKey().toString());
                tag2.setString("V", e.getKey().toString());
                aliases.appendTag(tag2);
            });

            data.setTag("aliases", aliases);
            final NBTTagList overrides = new NBTTagList();

            this.overrides.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> {
                NBTTagCompound tag3 = new NBTTagCompound();
                tag3.setString("K", e.getKey().toString());
                tag3.setString("V", e.getValue());
                overrides.appendTag(tag3);
            });

            data.setTag("overrides", overrides);
            final int[] blocked = this.blocked.stream().mapToInt(x -> x).sorted().toArray();
            data.setIntArray("blocked", blocked);
            final NBTTagList dummied = new NBTTagList();

            this.dummied.stream().sorted().forEach(e ->
                    dummied.appendTag(new NBTTagString(e.toString()))
            );
            data.setTag("dummied", dummied);
            return data;
        }


        public static Snapshot read(final NBTTagCompound nbt) {
            final Snapshot ret = new Snapshot();
            if (nbt == null) {
                return ret;
            }
            NBTTagList list = nbt.getTagList("ids", 10);

            list.forEach(e -> {
                final NBTTagCompound comp = (NBTTagCompound) e;
                ret.ids.put(new ResourceLocation(comp.getString("K")), comp.getInteger("V"));
            });

            list = nbt.getTagList("aliases", 10);

            list.forEach(e -> {
                ResourceLocation aliask;
                ResourceLocation aliasv;

                NBTTagCompound comp2 = (NBTTagCompound) e;
                String v = comp2.getString("V");
                if (v.indexOf(58) == -1) {
                    ret.overrides.put(new ResourceLocation(comp2.getString("K")), v);
                }
                else {
                    aliask = new ResourceLocation(comp2.getString("K"));
                    aliasv = new ResourceLocation(v);
                    if (aliasv.equals((Object)aliask)) {
                        FMLLog.log.warn("Found unrecoverable 4894 bugged alias/override: {} -> {}, skipping.", (Object)aliask, (Object)aliasv);
                    }
                    else {
                        ret.aliases.put(aliask, aliasv);
                    }
                }
            });
            list = nbt.getTagList("overrides", 10);

            list.forEach(e -> {
                final NBTTagCompound comp3 = (NBTTagCompound) e;
                ret.overrides.put(new ResourceLocation(comp3.getString("K")), comp3.getString("V"));
            });


            final int[] blocked = nbt.getIntArray("blocked");
            for (final int i : blocked) {
                ret.blocked.add(i);
            }
            list = nbt.getTagList("dummied", 10);
            list.forEach(e -> ret.dummied.add(new ResourceLocation(((NBTTagCompound)e).getString("K"))));
            list = nbt.getTagList("dummied", 8);
            list.forEach(e -> ret.dummied.add(new ResourceLocation(((NBTTagString)e).getString())));
            return ret;
        }
    }


    private static class OverrideOwner {
        final String owner;
        final ResourceLocation key;

        private OverrideOwner(final String owner, final ResourceLocation key) {
            this.owner = owner;
            this.key = key;
        }


        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof OverrideOwner)) {
                return false;
            }
            final OverrideOwner oo = (OverrideOwner)o;
            return this.owner.equals(oo.owner) && this.key.equals(oo.key);
        }


        @Override
        public int hashCode() {
            return 31 * this.key.hashCode() + this.owner.hashCode();
        }
    }
}