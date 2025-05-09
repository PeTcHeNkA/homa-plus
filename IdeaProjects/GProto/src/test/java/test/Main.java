package test;

import plus.tson.TsonFile;
import java.io.File;
import java.nio.charset.StandardCharsets;


public class Main {
    public static String FILE1 = "C:\\Users\\Te4hnoPlus\\Desktop\\GFLT\\LangPackageTableEnusData.bytes";
    public static String FILE2 = "C:\\Users\\Te4hnoPlus\\Desktop\\GFLT\\LangPackageTableEnusDataRES_2.bytes";


    public static byte[] data;

    public static void main(String[] args) throws Throwable {
        data = TsonFile.readFile(new File(FILE1));

        //byte[] data2 = TsonFile.readFile(new File(FILE2));

        //System.out.println(indexOf(data, "Hostiles 1"));
        //System.out.println(indexOf(data, "Hostiles 2"));

        int i1 = indexOf("Hostiles 1"); //id 121443
        int i2 = indexOf("Hostiles 2"); //id 121444  //1 DA64
        int i3 = indexOf("Hostiles 3"); //id 121445

        int len = "Hostiles 2".length();

        print("Len %s | %s", len, (i2 - i1) - len);

//        int a1 = indexOf(data2, "Hostile 1");
//        int a2 = indexOf(data2, "Hostile 2 NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN");
//        int a3 = indexOf(data2, "Hostile 3");

        //int len = "\"Hostile 2 NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN\"".length();

//        print("Off: %s, len: %s",i1, len);
//        print("Off: %s, len: %s",i2, len);
//        print("Off: %s, len: %s",i3, len);

        byte[] copy   = copy(data , i1+10, 8);
        byte[] copy2  = copy(data , i2+10, 8);

        //print("A2: %s Len %s", a2, len);

        print(Integer.toHexString(num(copy , 0)));
        print(Integer.toHexString(num(copy , 4)));

        print(Integer.toHexString(num(copy2, 0)));
        print(Integer.toHexString(num(copy2, 4)));

        //System.out.println(proInt(10));
    }


    private static int proInt(int n){
        return  (n << 1) ^ (n >> 31);
    }


    public static int num(byte[] data, int off){
        return    (data[0+off] & 0xff) << 24
                | (data[1+off] & 0xff) << 16
                | (data[2+off] & 0xff) << 8
                | (data[3+off] & 0xff
        );
    }


    public static byte[] copy(byte[] data, int from, int len){
        byte[] result = new byte[len];
        System.arraycopy(data, from, result, 0, len);
        return result;
    }


    public static void print(String fm, Object... args){
        System.out.printf(fm+"\n", args);
    }


    public static void print(Object obj){
        System.out.println(obj);
    }


    public static int indexOf(String search){
        return indexOf(data, search);
    }


    public static int indexOf(byte[] src, String search){
        return indexOf(src, 0, src.length, search.getBytes(StandardCharsets.UTF_8));
    }


    public static int indexOf(byte[] src, int from, int to, byte[] find){
        byte first = find[0];
        for (int i = from; i < to; i++){
            l1: if(src[i] == first){
                int cursor = 0, l = find.length;
                for (int j = i; j < to && cursor < l; j++){
                    if(src[j] != find[cursor]){
                        break l1;
                    }
                    ++cursor;
                }
                return i;
            }
        }
        return -1;
    }
}










