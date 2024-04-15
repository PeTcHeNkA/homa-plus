package plus.fserver.world;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.EntityLiving;
import plus.tson.utl.Te4HashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class NavigationCache {
    private Map<Class<? extends EntityLiving>, Integer> keys = ImmutableMap.of();
    private Map<Integer, Field> cache = ImmutableMap.of();
    private final Field root = new Field(new PntMatrix());
    private final ArrayList<Field> allFields = new ArrayList<>();
    public static final int FLY_ID   = 1001;
    public static final int WATER_ID = 1002;

    public NavigationCache(){
        allFields.add(root);
    }


    private static class Field{
        private PntMatrix matrix;
        Field(){}
        Field(PntMatrix pntMatrix) {
            this.matrix = pntMatrix;
        }
    }


    public void clean(boolean all){
        if(all) for (Field field:allFields) field.matrix = null;
        else for (Field field:allFields)
            if(field.matrix != null) field.matrix.clean();
    }


    public void addGroup(Class<? extends EntityLiving>... classes){
        HashMap<Class<? extends EntityLiving>, Integer> copyKeys = new HashMap<>(keys);
        int key = classes[0].hashCode();
        copyKeys.put(classes[0], key);
        Field field = new Field();

        HashMap<Integer, Field> copyCache = new HashMap<>(cache);
        copyCache.put(key, field);
        for (int i=1;i<classes.length;i++){
            copyKeys.put(classes[i], key);
        }

        keys = ImmutableMap.copyOf(copyKeys);
        cache = ImmutableMap.copyOf(copyCache);
    }


    public PntMatrix getBy(EntityLiving entity){
        Field field = getFieldBy(entity.getClass());
        return field.matrix==null?field.matrix = new PntMatrix(): field.matrix;
    }


    public PntMatrix getBy(int id){
        Field field = cache.get(id);
        if(field == null){
            HashMap<Integer, Field> map = new HashMap<>(cache);
            map.put(id, field = new Field());
            cache = ImmutableMap.copyOf(map);
            allFields.add(field);
        }
        return field.matrix==null?field.matrix = new PntMatrix(): field.matrix;
    }


    public PntMatrix getBy(Class<? extends EntityLiving> clazz){
        Field field = getFieldBy(clazz);
        return field.matrix==null?field.matrix = new PntMatrix(): field.matrix;
    }


    public void rem(int x, int y, int z){
        for (Field f:allFields) if(f.matrix != null)f.matrix.getChunked(x, z).rem(x, y, z);
    }


    private Field getFieldBy(Class<? extends EntityLiving> clazz){
        int hs;
        Field matrix = cache.get(hs = clazz.hashCode());
        if(matrix != null)return matrix;

        for (Map.Entry<Class<? extends EntityLiving>, Integer> entry:keys.entrySet()){
            if(clazz.isAssignableFrom(entry.getKey())){
                matrix = cache.get(entry.getValue());

                HashMap<Integer, Field> copyCache = new HashMap<>(cache);
                copyCache.put(hs, matrix);
                cache = ImmutableMap.copyOf(copyCache);

                return matrix;
            }
        }

        HashMap<Integer, Field> copyCache = new HashMap<>(cache);
        copyCache.put(hs, root);
        cache = ImmutableMap.copyOf(copyCache);

        return root;
    }
}