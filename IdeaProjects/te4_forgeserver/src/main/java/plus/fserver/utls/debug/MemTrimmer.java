package plus.fserver.utls.debug;

import plus.fserver.utls.collections.IdentityTe4HashSet;
import plus.tson.utl.Te4HashMap;
import sun.misc.Unsafe;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import static plus.tson.utl.uns.UnsafeUtils.UNSAFE;


public class MemTrimmer extends Te4HashMap<Object,Object> {
    private static final IdentityTe4HashSet<Class<?>> BAD_LIST = new IdentityTe4HashSet<>();
    private static final IdentityTe4HashSet<Class<?>> NO_ASSIGN = new IdentityTe4HashSet<>();

    static {
        BAD_LIST.add(Class.class);
        BAD_LIST.add(Object.class);
        BAD_LIST.add(Field.class);
        BAD_LIST.add(Method.class);
        BAD_LIST.add(MethodHandle.class);
        BAD_LIST.add(Modifier.class);
        BAD_LIST.add(Constructor.class);
    }

    public void trim(Object obj){
        if(obj == null)return;
        if(super.get(obj) != null)return;
        if(BAD_LIST.contains(obj.getClass()))return;
        trimS(obj);
    }


    private void trimS(Object obj){
        super.fput(obj, obj);
        Class<?> clazz = obj.getClass();

        if(obj instanceof Object[]){
            Object[] arr = (Object[]) obj;
            trimArr(arr);
        } else {
            while (clazz != Object.class) {
                trimU(clazz, obj);
                clazz = clazz.getSuperclass();
            }
        }
    }


    private void trimArr(Object[] arr) {
        Class<?> arrType = arr.getClass().getComponentType();
        for (int i = 0, s = arr.length; i < s; i++) {
            Object sub = arr[i];
            if(sub == null)continue;
            Object curVal = super.get(sub);

            if(curVal == null){
                if(!BAD_LIST.contains(sub.getClass())) trimS(sub);
            } else {
                if(curVal != sub) {
                    Class<?> clazz;
                    if(NO_ASSIGN.contains(clazz = curVal.getClass()))continue;
                    if (arrType.isAssignableFrom(clazz)) {
                        arr[i] = curVal;
                    } else {
                        super.fput(sub, sub);
                    }
                }
            }
        }
    }


    private void trimU(Class<?> clazz, Object obj){
        Unsafe unsafe = UNSAFE;

        for (Field field:clazz.getDeclaredFields()){
            Class<?> type = field.getType();
            if(type.isPrimitive())continue;
            if(Modifier.isStatic(field.getModifiers()))continue;

            long off = unsafe.objectFieldOffset(field);

            Object cur  = unsafe.getObject(obj, off);
            if(cur == null)continue;

            Object prev = super.get(cur);

            if(prev != null) {
                if (prev != cur) {
                    unsafe.putObject(obj, off, prev);
                }
            } else {
                trim(cur);
            }
        }
    }
}