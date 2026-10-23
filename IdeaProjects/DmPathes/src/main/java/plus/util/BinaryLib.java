package plus.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class BinaryLib {

    //не трогать!!!
    private static final char[] simvols = "qwertyuiopasdfghjklzxcvbnm1234567890QWERTYUIOPASDFGHJKLZXCVBNM#$".toCharArray();
    private static final HashMap<String, Character> codeLib = new HashMap<>();
    private static final HashMap<Character,boolean[]>unCodeLib = new HashMap<>();
    private static int maxuid = 1;

    private static void createMatrix(){
        char[] v = new char[]{'0', '1'};
        int flag = 0;
        for(char a:v){
            for(char b:v){
                for(char c:v){
                    for(char d:v){
                        for(char e:v){
                            for(char f:v){
                                codeLib.put(new String(new char[]{a,b,c,d,e,f}), simvols[flag]);
                                unCodeLib.put(simvols[flag], new boolean[]{a=='0',b=='0',c=='0',d=='0',e=='0',f=='0'});
                                ++flag;
                            }
                        }
                    }
                }
            }
        }
    }
    //не трогать!!!
    private static List<Boolean> EmptyStandard = new ArrayList<>();

    public static void Initialize(int MaxUid){
        createMatrix();
        if(MaxUid<1)maxuid=1;
        else maxuid = MaxUid;
        EmptyStandard = Standartalize(new ArrayList<>());

    }
    public static List<Boolean> getDefaultHistory(){
        return EmptyStandard;
    }

    public static String BooleanListToString(List<Boolean> bits){
        if (bits == null || bits.isEmpty()) return "";
        char[] key = new char[6];
        int flag = 0;
        List<Character> ch = new ArrayList();

        for (boolean b : bits) {
            key[flag] = b?'0':'1';
            ++flag;
            if (flag == 6) {
                ch.add(codeLib.get(new String(key)));
                key = new char[6];
                flag = 0;
            }
        }
        if(flag != 0){
            while (flag<6){
                key[flag] = '1';
                ++flag;
            }
            ch.add(codeLib.get(new String(key)));
        }
        return ch.stream().map(Object::toString).collect(Collectors.joining());
    }

    public static List<Boolean> stringToBoleanList(String value){
        if(value == null || value.equals(""))return EmptyStandard;
        char[] ch = value.toCharArray();
        List<Boolean> b = new ArrayList<>();
        for(char c:ch){
            for(boolean bl:unCodeLib.get(c)){
                b.add(bl);
            }
        }
        while (b.size()<EmptyStandard.size()){
            b.add(false);
        }
        return b;
    }

    public static List<Boolean> Standartalize(List<Boolean> b){
        while (b.size()<maxuid+1){
            b.add(false);
        }
        return b;
    }
}