package net.minecraftforge.common.util;

import net.minecraft.util.datafix.*;
import com.google.common.collect.*;
import java.util.*;
import net.minecraft.nbt.*;


public class CompoundDataFixer extends DataFixer {
    private final ModFixs vanilla;
    private final Map<String, ModFixs> modFixers;
    private final Map<IFixType, List<IDataWalker>> walkers;

    public CompoundDataFixer(final DataFixer vanilla) {
        super(0);
        this.modFixers = Maps.newHashMap();
        this.walkers = Maps.newHashMap();
        this.vanilla = this.init("minecraft", vanilla.version);
    }


    @Override
    public NBTTagCompound process(final IFixType type, final NBTTagCompound nbt) {
        final Map<String, Integer> versions = this.getVersions(nbt);
        final int mcversion = (versions.get("minecraft") == null) ? -1 : versions.get("minecraft");
        final IDataFixerData holder = new IDataFixerData() {
            public NBTTagCompound process(final IFixType type, NBTTagCompound nbt, final int version) {
                for (final Map.Entry<String, ModFixs> e : CompoundDataFixer.this.modFixers.entrySet()) {
                    final ModFixs fixer = e.getValue();
                    final int ver = this.getVersion(e.getKey());
                    if (ver < fixer.version) {
                        for (final IFixableData fix : fixer.getFixes(type)) {
                            if (fix.getFixVersion() > ver) {
                                nbt = fix.fixTagCompound(nbt);
                            }
                        }
                        for (final IDataWalker walker : CompoundDataFixer.this.getWalkers(type)) {
                            nbt = walker.process(this, nbt, version);
                        }
                    }
                }
                return nbt;
            }

            @Override
            public int getVersion(final String mod) {
                final Integer ret = versions.get(mod);
                return (ret == null) ? -1 : ret;
            }
        };
        return holder.process(type, nbt, mcversion);
    }


    @Deprecated
    public NBTTagCompound process(final IFixType type, NBTTagCompound nbt, final int mcversion) {
        if (type != FixTypes.OPTIONS) {
            throw new IllegalStateException("Do not call recursive process directly on DataFixer!");
        }
        for (final IFixableData fix : this.vanilla.getFixes(type)) {
            if (fix.getFixVersion() > mcversion) {
                nbt = fix.fixTagCompound(nbt);
            }
        }
        return nbt;
    }


    private List<IDataWalker> getWalkers(final IFixType type) {
        return this.walkers.computeIfAbsent(type, k -> Lists.newArrayList());
    }


    @Deprecated
    public void registerFix(final IFixType type, final IFixableData fixable) {
        this.vanilla.registerFix(type, fixable);
    }


    @Deprecated
    public void registerWalker(final FixTypes type, final IDataWalker walker) {
        this.registerVanillaWalker(type, walker);
    }


    @Override
    public void registerVanillaWalker(final IFixType type, final IDataWalker walker) {
        this.getWalkers(type).add(walker);
    }


    private void validateModId(final String mod) {
        if (!mod.equals(mod.toLowerCase(Locale.ENGLISH))) {
            throw new IllegalArgumentException("Mod ID is not lower case: " + mod);
        }
        if (mod.length() > 64) {
            throw new IllegalArgumentException("Mod ID is to long: " + mod);
        }
    }

    public ModFixs init(final String modid, final int version) {
        this.validateModId(modid);
        if (this.modFixers.containsKey(modid)) {
            throw new IllegalStateException("Attempted to initalize DataFixer for " + modid + " twice");
        }
        final ModFixs ret = new ModFixs(modid, version);
        this.modFixers.put(modid, ret);
        return ret;
    }


    private Map<String, Integer> getVersions(final NBTTagCompound nbt) {
        int ver = -1;
        if(nbt.hasKey("DataVersion", 99)){
            ver = nbt.getInteger("DataVersion");
        }
        l1: if (nbt.hasKey("ForgeDataVersion", 10)) {
            final NBTTagCompound sub = nbt.getCompoundTag("ForgeDataVersion");
            if(sub.isEmpty())break l1;
            HashMap<String,Integer> ret = new HashMap<>(sub.getSize(),1);

            for (final String key : sub.getKeySet()) {
                ret.put(key, sub.hasKey(key, 99) ? sub.getInteger(key) : -1);
            }
            if(ver != -1)ret.put("minecraft", ver);
            return ret;
        }
        if(ver == -1)return ImmutableMap.of();
        return ImmutableMap.of("minecraft", ver);
    }


    public void writeVersionData(final NBTTagCompound nbt) {
        Collection<ModFixs> values;
        if((values = this.modFixers.values()).isEmpty())return;
        final NBTTagCompound sub = new NBTTagCompound(values.size(), 1);
        nbt.setTag("ForgeDataVersion", sub);
        for (final ModFixs mod : values) {
            sub.setIntegerU(mod.mod, mod.version);
        }
    }
}