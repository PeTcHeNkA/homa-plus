package plus.lyn.utl;

import plus.lyn.Word;
import plus.tson.utl.Te4HashMap;


public class WordLib extends Te4HashMap<WordLib.WKey, Word> {
    public static final class WKey {
        private char[] key;
        private int start, end, hash;

        public WKey(){}

        private WKey(Word src){
            char[] chars;
            init(chars = src.toString().toCharArray(), 0, chars.length);
        }


        public WKey init(char[] str, int start, int end){
            hash = hash(this.key = str, this.start = start, this.end = end);
            return this;
        }


        @Override
        public boolean equals(Object obj) {
            WKey other = (WKey) obj;
            if(other.end - other.start != end - start)return false;
            for (int is = start, io = other.start; is < end; ++is){
                if(key[is] != other.key[io++])return false;
            }
            return true;
        }


        @Override
        public int hashCode() {
            return hash;
        }
    }


    public void fput(Word werb){
        Word res = put(new WKey(werb), werb);
        if(res != null)throw new IllegalArgumentException("Key ["+werb.toUTF8Str()+"] ready exists!");
    }


    public static int hash(char[] str, int start, int end){
        int summ = end-start;
        for (;start<end;start++){
            summ = summ * 31 + str[start];
        }
        return summ;
    }
}