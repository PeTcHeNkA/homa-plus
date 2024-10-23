package plus.fserver.utls.debug;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import plus.fserver.utls.collections.ArrayListEV;
import plus.tson.utl.Te4HashSet;
import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.IdentityHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class ScanMem {
    interface Checker{
        void check(ScanMem ref, Object obj);
    }

    private static final Int2IntOpenHashMap TABLE = new Int2IntOpenHashMap();
    private static final Int2ObjectOpenHashMap<Checker> ALT_CHECK = new Int2ObjectOpenHashMap<>();
    private static final int refSize = 4;

    static {
        TABLE.put(byte.class.hashCode(),      1);
        TABLE.put(short.class.hashCode(),     2);
        TABLE.put(int.class.hashCode(),       4);
        TABLE.put(long.class.hashCode(),      8);
        TABLE.put(float.class.hashCode(),     4);
        TABLE.put(double.class.hashCode(),    8);
        TABLE.put(char.class.hashCode(),      2);
        TABLE.put(boolean.class.hashCode(),   1);
        TABLE.put(Class.class.hashCode(),     refSize);
        TABLE.put(Field.class.hashCode(),     refSize);
        TABLE.put(Method.class.hashCode(),    refSize);
        TABLE.put(Reference.class.hashCode(), refSize*2);
        TABLE.defaultReturnValue(-1);
        TABLE.trim();

        ALT_CHECK.put(String.class.hashCode(),    ScanMem::checkString   );
        ALT_CHECK.put(boolean[].class.hashCode(), ScanMem::checkArrBool  );
        ALT_CHECK.put(byte[].class.hashCode(),    ScanMem::checkArrByte  );
        ALT_CHECK.put(int[].class.hashCode(),     ScanMem::checkArrInt   );
        ALT_CHECK.put(short[].class.hashCode(),   ScanMem::checkArrShort );
        ALT_CHECK.put(char[].class.hashCode(),    ScanMem::checkArrChar  );
        ALT_CHECK.put(float[].class.hashCode(),   ScanMem::checkArrFloat );
        ALT_CHECK.put(double[].class.hashCode(),  ScanMem::checkArrDouble);
        ALT_CHECK.put(long[].class.hashCode(),    ScanMem::checkArrLong  );

        ALT_CHECK.put(Class.class.hashCode(),     ScanMem::ignoreCheck   );
        ALT_CHECK.put(Field.class.hashCode(),     ScanMem::ignoreCheck   );
        ALT_CHECK.put(Method.class.hashCode(),    ScanMem::ignoreCheck   );

        ALT_CHECK.trim();
    }

    private static void checkString(ScanMem ref, Object obj){
        ref.total += ((String)obj).length()* 2L;
    }
    private static void checkArrBool(ScanMem ref, Object obj){
        ref.total += ((boolean[])obj).length;
    }
    private static void checkArrByte(ScanMem ref, Object obj){
        ref.total += ((byte[])obj).length;
    }
    private static void checkArrInt(ScanMem ref, Object obj){
        ref.total += ((int[])obj).length * 4L;
    }
    private static void checkArrShort(ScanMem ref, Object obj){
        ref.total += ((short[])obj).length * 2L;
    }
    private static void checkArrChar(ScanMem ref, Object obj){
        ref.total += ((char[])obj).length * 2L;
    }
    private static void checkArrFloat(ScanMem ref, Object obj){
        ref.total += ((float[])obj).length * 4L;
    }
    private static void checkArrDouble(ScanMem ref, Object obj){
        ref.total += ((double[])obj).length * 8L;
    }
    private static void checkArrLong(ScanMem ref, Object obj){
        ref.total += ((long[])obj).length * 8L;
    }
    private static void ignoreCheck(ScanMem ref, Object ojb){}

    private final IdentityHashMap<Object,Object> cache = new IdentityHashMap<>();
    private final Te4HashSet<Class<?>> noCheckRefs = new Te4HashSet<>();
    //private final ArrayList<String> excludePackets = new ArrayList<>();
    private long total = 0;


    private boolean ignoreRef(Type type){
        return noCheckRefs.contains(type);
    }


    public ScanMem exclude(Class<?>... classes){
        for (Class<?> clazz:classes) noCheckRefs.add(clazz);
        return this;
    }


    public ScanMem exclude(Class<?> clazz){
        noCheckRefs.add(clazz);
        return this;
    }


    private void scanMemSafe(Class<?> clazz, Object obj, AtomicInteger num){
        if(obj==null || clazz == Object.class || clazz == Class.class)return;
        IdentityHashMap<Object,Object> cache;
        if((cache = this.cache).containsKey(obj))return;
        Checker alt;

        cache.put(obj, obj);
        if((alt = ALT_CHECK.get(clazz.hashCode())) != null){
            alt.check(this, obj);
        } else {
            scanMem(clazz, obj, num);
        }
    }


    private static class Args{
        Class<?> clazz; Object obj;
        private Args(Class<?> clazz, Object obj) {
            this.clazz = clazz;
            this.obj = obj;
        }
    }

    private final ArrayListEV<Args> buf = new ArrayListEV<>(32);
    private void scanMem(Class<?> clazz, Object obj, AtomicInteger num){
        if(clazz == Object.class || clazz == Class.class)return;
        if(num.addAndGet(1)>1000){
            ArrayListEV<Args> buf = this.buf;
            Args prev = buf.getSafe(buf.size() - 1);
            if(prev == null){
                buf.addF(new Args(clazz, obj));
            } else {
                prev.clazz = clazz;
                prev.obj   = obj;
            }
            return;
        }
        if(obj instanceof Object[]){
            Class<?> type = clazz.getComponentType();
            Object[] data = (Object[]) obj;
            int size = TABLE.get(type.hashCode());
            if(size==-1){
                if(ignoreRef(type))return;
                total += (data.length* 4L);
                for (Object obj0:data){
                    if(obj0 != null) scanMemSafe(obj0.getClass(), obj0, num);
                }
            } else {
                total += ((long) data.length * size);
            }
            return;
        }
        for (Field field:clazz.getDeclaredFields()) checkField(obj, field, num);

        scanMem(clazz.getSuperclass(), obj, num);
    }


    private void checkField(Object obj, Field field, AtomicInteger num){
        if((field.getModifiers()&8)==8)return;
        Type type;
        int size;
        if((size = TABLE.get((type = field.getGenericType()).hashCode()))==-1){
            if(ignoreRef(type))return;
            boolean prev = field.isAccessible();
            try {
                if(!prev) field.setAccessible(true);
                Object obj0 = field.get(obj);
                if(obj0 != null) scanMemSafe(obj0.getClass(), obj0, num);
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                try {
                    if(!prev){
                        field.setAccessible(false);
                    }
                } catch (Exception ignored){}
            }
            total += refSize;
        } else {
            total += size;
        }
    }


    public ScanMem scan(Object obj){
        if(obj==null)return this;
        AtomicInteger num = new AtomicInteger();
        scanMemSafe(obj.getClass(), obj, num);
        ArrayListEV<Args> buf = this.buf;
        while (!buf.isEmptyC()){
            Runtime.getRuntime().gc();
            int cursor = buf.size()-1;
            Args args = buf.get(cursor);
            buf.setSize(cursor);
            num.set(0);
            scanMem(args.clazz, args.obj, num);
        }
        return this;
    }


    @Override
    public String toString() {
        return memFormatted(total);
    }


    public static String memFormatted(long mem){
        long bytes  = mem % 1024;
        long kbytes = mem / 1024;
        long mbytes = 0;
        if(kbytes >= 1024){
            mbytes = kbytes / 1024;
            kbytes = kbytes % 1024;
        }
        if(kbytes == 0)return bytes+"b";
        if(mbytes == 0){
            long num = ((long)(bytes*0.9765625));
            if(num==0)return kbytes+"kb";
            return kbytes+"."+((long)(bytes*0.9765625))+"kb";
        }
        long num = ((long)(kbytes*0.9765625));
        if(num==0)return mbytes+"mb";
        return mbytes+"."+num+"mb";
    }


    public long size() {
        return total;
    }
}