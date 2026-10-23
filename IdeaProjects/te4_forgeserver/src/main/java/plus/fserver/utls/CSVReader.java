package plus.fserver.utls;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.function.Function;


public class CSVReader<T> implements Iterator<T>, Iterable<T> {
    private static final Object EMPTY = new Object();
    private final byte[] bytes;
    private final Function<Object[], T> constructor;
    private final byte sep;
    private T next = null;
    private int cursor = 0;
    private Object[] temp;

    public CSVReader(byte[] bytes, Function<Object[], T> constructor){
        this((byte) ',', 2, bytes, constructor);
    }

    public CSVReader(byte sep, int defLineSize, byte[] bytes, Function<Object[], T> constructor) {
        this.sep = sep;
        this.bytes = bytes;
        this.constructor = constructor;
        temp = new Object[defLineSize];
    }


    public void readNextLine(){
        byte sep = this.sep;
        byte[] bytes = this.bytes;
        int start = cursor;

        Object[] args = temp;
        int argIndex = 0;

        for (int i = start, s = bytes.length-1; i <= s; i++){
            byte b = bytes[i];
            boolean isEnd = b == '\n' || i == s;

            if(b == sep || isEnd){
                if(argIndex == args.length){
                    Object[] newArgs = new Object[argIndex+1];
                    System.arraycopy(args, 0, newArgs, 0, argIndex);
                    args = newArgs;
                }
                Object obj = buildObj(bytes, start, i-1);
                if(obj != EMPTY) {
                    args[argIndex++] = obj;
                }
                start = ++i;
            }

            if(isEnd){
                if(argIndex == 0) continue;
                if(argIndex != args.length){
                    Object[] newArgs = new Object[argIndex];
                    System.arraycopy(args, 0, newArgs, 0, argIndex);
                    args = temp = newArgs;
                }
                cursor = i;
                next = constructor.apply(args);
                return;
            }
        }
        next = null;
        cursor = bytes.length;
    }


    private static Object buildObj(byte[] bytes, int from, int to){
        while (isEmpty(bytes[from]) && from < to) ++from;
        if(from == to)return null;
        while (isEmpty(bytes[to]) && from < to) --to;
        if(from == to)return null;
        if(isTrue(bytes, from, to))
            return Boolean.TRUE;
        if(isFalse(bytes, from, to))
            return Boolean.FALSE;
        return new String(bytes, from, to-from+1, StandardCharsets.UTF_8);
    }


    private static boolean isTrue(byte[] bytes, int from, int to){
        if(to - from != 3)return false;
        return     (bytes[from  ] == 't' || bytes[from  ] == 'T')
                && (bytes[from+1] == 'r' || bytes[from+1] == 'R')
                && (bytes[from+2] == 'u' || bytes[from+2] == 'U')
                && (bytes[from+3] == 'e' || bytes[from+3] == 'E');
    }


    private static boolean isFalse(byte[] bytes, int from, int to){
        if(to - from != 4)return false;
        return     (bytes[from  ] == 'f' || bytes[from  ] == 'F')
                && (bytes[from+1] == 'a' || bytes[from+1] == 'A')
                && (bytes[from+2] == 'l' || bytes[from+2] == 'L')
                && (bytes[from+3] == 's' || bytes[from+3] == 'S')
                && (bytes[from+4] == 'e' || bytes[from+4] == 'E');
    }


    private static boolean isEmpty(byte b){
        return b == ' ' || b == '\r' || b == '\t';
    }


    @Override
    public boolean hasNext() {
        return next != null;
    }


    @Override
    public T next() {
        T result = next;
        readNextLine();
        return result;
    }


    @Override
    public Iterator<T> iterator() {
        cursor = 0;
        readNextLine();
        return this;
    }
}