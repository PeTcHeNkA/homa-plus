package net.minecraft.world;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


public class DimensionType {
    private static final Int2ObjectOpenHashMap<DimensionType> dims = new Int2ObjectOpenHashMap<>(7);

    public static DimensionType OVERWORLD = register("overworld", "", 0, WorldProviderSurface.class, true);
    public static DimensionType NETHER    = register("the_nether", "_nether", -1, WorldProviderHell.class, false);
    public static DimensionType THE_END   = register("the_end", "_end", 1, WorldProviderEnd.class, false);

    private final int id;
    private final String name;
    private final String suffix;
    private final Class <? extends WorldProvider > clazz;
    private boolean shouldLoadSpawn = false;


    private DimensionType(int idIn, String nameIn, String suffixIn, Class <? extends WorldProvider > clazzIn) {
        this.id = idIn;
        this.name = nameIn;
        this.suffix = suffixIn;
        this.clazz = clazzIn;
        this.shouldLoadSpawn = idIn == 0;

    }


    public static DimensionType valueOf(String str){
        throw new UnsupportedOperationException();
    }


    public static DimensionType[] values(){
        return dims.values().toArray(new DimensionType[0]);
    }


    public int getId() {
        return this.id;
    }


    public String getName() {
        return this.name;
    }


    public String getSuffix() {
        return this.suffix;
    }


    public WorldProvider createDimension() {
        try {
            Constructor <? extends WorldProvider > constructor = this.clazz.getConstructor();
            return constructor.newInstance();
        } catch (NoSuchMethodException nosuchmethodexception) {
            throw new Error("Could not create new dimension", nosuchmethodexception);
        } catch (InvocationTargetException invocationtargetexception) {
            throw new Error("Could not create new dimension", invocationtargetexception);
        } catch (InstantiationException instantiationexception) {
            throw new Error("Could not create new dimension", instantiationexception);
        } catch (IllegalAccessException illegalaccessexception) {
            throw new Error("Could not create new dimension", illegalaccessexception);
        }
    }


    public static DimensionType getById(int id) {
        for (DimensionType dimensiontype : values()) {
            if (dimensiontype.getId() == id) {
                return dimensiontype;
            }
        }
        throw new IllegalArgumentException("Invalid dimension id " + id);
    }


    public boolean shouldLoadSpawn(){return this.shouldLoadSpawn;}


    public DimensionType setLoadSpawn(boolean value) { this.shouldLoadSpawn = value; return this; }


    private static Class<?>[] ENUM_ARGS = {int.class, String.class, String.class, Class.class};


    public static DimensionType register(String name, String suffix, int id, Class<? extends WorldProvider> provider, boolean keepLoaded) {
        String enum_name = name.replace(" ", "_").toLowerCase();
        DimensionType ret = new DimensionType(id, name, suffix, provider);
        dims.put(id, ret);
        return ret.setLoadSpawn(keepLoaded);
    }

    //TODO: Unregister? There is no way to really delete a enum value...

    public static DimensionType byName(String nameIn) {
        for (DimensionType dimensiontype : values()) {
            if (dimensiontype.getName().equals(nameIn)) {
                return dimensiontype;
            }
        }
        throw new IllegalArgumentException("Invalid dimension " + nameIn);
    }
}