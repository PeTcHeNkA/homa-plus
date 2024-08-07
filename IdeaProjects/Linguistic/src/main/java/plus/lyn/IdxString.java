package plus.lyn;

import plus.lyn.utl.UnLinker;
import java.nio.charset.StandardCharsets;


public final class IdxString {
    private static final UnLinker<IdxString> UN_LINKER = new UnLinker<>();
    public final String value;
    public final int index;

    public static IdxString of(String value){
        return UN_LINKER.unLink(new IdxString(value));
    }


    static IdxString of(String value, int idx){
        return UN_LINKER.unLink(new IdxString(value, idx));
    }


    private IdxString(String value) {
        this.value = value;
        this.index = WordIndex.inst.werbToIndex6(value.toCharArray(), 0);
    }


    private IdxString(String value, int idx){
        this.value = value;
        this.index = idx;
    }


    public IdxString(char[] str, int count, int offset) {
        this.value = new String(str, offset, count);
        this.index = WordIndex.inst.werbToIndex6(value.toCharArray(), offset, count);
    }


    public String toUTF8Str(){
        return new String(value.getBytes(StandardCharsets.UTF_8));
    }


    @Override
    public int hashCode() {
        return index;
    }


    @Override
    public String toString() {
        return value;
    }


    @Override
    public boolean equals(Object obj) {
        IdxString other = (IdxString) obj;
        return index == other.index && value.equals(other.value);
    }


    public int length(){
        return value.length();
    }
}