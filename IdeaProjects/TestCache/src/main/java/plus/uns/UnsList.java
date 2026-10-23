package plus.uns;

import plus.tson.TsonMap;
import plus.tson.TsonStr;
import sun.misc.Unsafe;
import java.lang.reflect.Field;
import static plus.tson.ext.TsonAccessUtils.*;

public class UnsList {
    static Unsafe unsafe = getUnsafe();

    public static void main(String[] args) {
        long addr = unsafe.allocateMemory(1024);

        unsafe.putInt(addr, 0);
        System.out.println(unsafe.getInt(addr-1));
        System.out.println(unsafe.getInt(addr-2));



    }

    static class TestTson{

        public int id;
        public String name;

        public TestTson(TsonMap map) {
            id   = getR(map, "id", 0);
            name = getR(map, new TsonStr("name"), "").getStr();
        }
    }




    /**
     * Gets the Unsafe instance using reflection.
     * @return the Unsafe instance
     */
    private static Unsafe getUnsafe() {
        try {
            // Get the field "theUnsafe" from the Unsafe class
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            // Return the Unsafe instance
            return (Unsafe) f.get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            // Throw a RuntimeException if there are issues accessing the field
            throw new RuntimeException(e);
        }
    }
}