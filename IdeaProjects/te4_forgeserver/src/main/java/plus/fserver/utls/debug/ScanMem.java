package plus.fserver.utls.debug;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import plus.asyutl.buf.FastBuffer;
import plus.tson.utl.Te4HashSet;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class ScanMem {
    interface Checker{
        void check(ScanMem ref, Object obj);
    }

    private static final Int2IntOpenHashMap table = new Int2IntOpenHashMap();
    private static final Int2ObjectOpenHashMap<Checker> altCheck = new Int2ObjectOpenHashMap<>();
    private static final int refSize = 4;
    static {
        table.put(byte.class.hashCode(),      1);
        table.put(short.class.hashCode(),     2);
        table.put(int.class.hashCode(),       4);
        table.put(long.class.hashCode(),      8);
        table.put(float.class.hashCode(),     4);
        table.put(double.class.hashCode(),    8);
        table.put(char.class.hashCode(),      2);
        table.put(boolean.class.hashCode(),   1);
        table.defaultReturnValue(0);
        table.trim();

        altCheck.put(String.class.hashCode(),    ScanMem::checkString   );
        altCheck.put(boolean[].class.hashCode(), ScanMem::checkArrBool  );
        altCheck.put(byte[].class.hashCode(),    ScanMem::checkArrByte  );
        altCheck.put(int[].class.hashCode(),     ScanMem::checkArrInt   );
        altCheck.put(short[].class.hashCode(),   ScanMem::checkArrShort );
        altCheck.put(char[].class.hashCode(),    ScanMem::checkArrChar );
        altCheck.put(float[].class.hashCode(),   ScanMem::checkArrFloat );
        altCheck.put(double[].class.hashCode(),  ScanMem::checkArrDouble);
        altCheck.put(long[].class.hashCode(),    ScanMem::checkArrLong  );

        altCheck.trim();
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


    private final IdentityHashMap<Object,Object> cache = new IdentityHashMap<>();
    private final Te4HashSet<Class<?>> noCheckRefs = new Te4HashSet<>();
    //private final ArrayList<String> excludePackets = new ArrayList<>();
    private long total = 0;


    private boolean ignoreRef(Type type){
//        String name = type.getTypeName();
//        for (String start:excludePackets){
//            if(name.startsWith(start))return true;
//        }
        return noCheckRefs.contains(type);
    }


    public ScanMem exclude(Class<?>... classes){
        for (Class<?> clazz:classes) noCheckRefs.add(clazz);
        return this;
    }
//
//
//    public ScanMem exclude(String... str){
//        for (String str0:str) excludePackets.add(str0);
//        return this;
//    }


    public ScanMem exclude(Class<?> clazz){
        noCheckRefs.add(clazz);
        return this;
    }


    private void scanMemSafe(Class<?> clazz, Object obj, AtomicInteger num){
        if(obj==null || clazz == Object.class || clazz == Class.class)return;
        if(cache.containsKey(obj))return;
        cache.put(obj, obj);
        scanMem(clazz, obj, num);
    }


    private static class Args{
        final Class<?> clazz; final Object obj;
        private Args(Class<?> clazz, Object obj) {
            this.clazz = clazz;
            this.obj = obj;
        }
    }

    private final FastBuffer<Args> buf = new FastBuffer<>();
    private void scanMem(Class<?> clazz, Object obj, AtomicInteger num){
        if(clazz == Object.class || clazz == Class.class)return;
        if(num.addAndGet(1)>1000){
            buf.put(new Args(clazz, obj));
            return;
        }
        Checker alt = altCheck.get(clazz.hashCode());
        if(alt != null){
            alt.check(this, obj);
            return;
        }
        if(obj instanceof Object[]){
            Class<?> type = clazz.getComponentType();
            Object[] data = (Object[]) obj;
            int size = table.get(type.hashCode());
            if(size==0){
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
        Field[] fields = clazz.getDeclaredFields();

        for (Field field:fields){
            checkField(obj, field, num);
        }
        scanMem(clazz.getSuperclass(), obj, num);
    }


    private void checkField(Object obj, Field field, AtomicInteger num){
        if((field.getModifiers()&8)==8)return;
        Type type = field.getGenericType();
        int size = table.get(type.hashCode());
        if(size==0){
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
        while (buf.hasNext()){
            Runtime.getRuntime().gc();
            Args args = buf.flush();
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