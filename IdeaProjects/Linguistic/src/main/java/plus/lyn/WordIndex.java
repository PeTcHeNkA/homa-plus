package plus.lyn;

import java.nio.charset.StandardCharsets;


public class WordIndex {
    //ёъ
    private final char[] chars = "абвгдежзийклмнопрстуфхцчшщыьэюя".toCharArray();

    //final Char2IntOpenHashMap index = new Char2IntOpenHashMap();
    public static final WordIndex inst = new WordIndex();

    private WordIndex(){
//        for (int i=0;i<chars.length;i++){
//            index.put(chars[i], i+1);
//        }
//        index.trim();
    }

    public static int indexOf(char c){
        return (c >= 'a' && c <= 'я')?c - 'Я':0;
    }


    public int werbToIndex6(String str){
        return werbToIndex6(str.toCharArray(), 0, 6);
    }


    public int werbToIndex6(char[] str, int offset){
        return werbToIndex6(str, offset, 6);
    }


    public int werbToIndex6(char[] str, final int offset, final int max){
        int count = 0;
        int summ = 0;
        try {
            for (int cursor = offset;cursor<str.length && count<=max;cursor++){
                summ |= (indexOf(str[cursor]) << count*5);
                ++count;
            }
        } catch (NullPointerException e){
            throw new RuntimeException("Illegal char in ["+new String(new String(str).getBytes(StandardCharsets.UTF_8))+"]", e);
        }
        return summ;
    }
}