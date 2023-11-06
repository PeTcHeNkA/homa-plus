package plus.te4net.mds.cap4a;

import plus.te4net.core.api.PublicUsage;
import plus.te4net.mds.util.FastRandom;


@PublicUsage
public class CharLib {
    public static final CharLib INST = new CharLib();
    private final char[] codeTable;
    private final char[] encodeTable;
    private final char[] generatorTable;

    protected CharLib(){
        this(new char[][]{
                {'0','7'},{'1','E'},{'2','c'},{'3','H'},
                {'4','D'},{'5','1'},{'6','u'},{'7','0'},
                {'8','G'},{'9','r'},{'A','2'},{'B','g'},
                {'C','9'},{'D','w'},{'E','S'},{'F','A'},
                {'G','i'},{'H','5'},{'I','f'},{'J','W'},
                {'K','C'},{'L','a'},{'M','3'},{'N','K'},
                {'O','n'},{'P','I'},{'Q','B'},{'R','M'},
                {'S','z'},{'T','J'},{'U','q'},{'V','y'},
                {'W','O'},{'X','N'},{'Y','6'},{'Z','V'},
                {'a','X'},{'b','s'},{'c','U'},{'d','L'},
                {'e','F'},{'f','m'},{'g','Z'},{'h','4'},
                {'i','x'},{'j','T'},{'k','h'},{'l','e'},
                {'m','d'},{'n','b'},{'o','Y'},{'p','Q'},
                {'q','8'},{'r','k'},{'s','P'},{'t','l'},
                {'u','R'},{'v','o'},{'w','t'},{'x','p'},
                {'y','j'},{'z','v'},{' ',' '},{'-','-'},
                {'=','='},{':',':'},{';',';'},{',',','}
        });
    }


    public char[] genCode(int size){
        char[] chars = new char[size];
        for(int i=0;i<chars.length;i++){
            chars[i] = nextRChar();
        }
        return chars;
    }


    public char nextRChar(){
        return generatorTable[FastRandom.INST.nextInt(generatorTable.length)];
    }


    protected CharLib(char[][] tableOverrides){
        codeTable = compileCodeTable(tableOverrides);
        encodeTable = compileEncodeTable(tableOverrides);
        generatorTable = compileGeneratorTable(tableOverrides);
    }


    public final String code(String s){
        char[] chars = s.toCharArray();
        for(int i=0;i<chars.length;i++){
            chars[i] = codeTable[chars[i]];
        }
        return new String(chars);
    }


    public final String encode(String s){
        char[] chars = s.toCharArray();
        for(int i=0;i<chars.length;i++){
            chars[i] = encodeTable[chars[i]];
        }
        return new String(chars);
    }


    private static char[] compileEncodeTable(char[][] chars){
        char[] data = new char[123];
        for(char[] pair:chars){
            data[pair[1]] = pair[0];
        }
        return data;
    }


    private static char[] compileCodeTable(char[][] chars){
        char[] data = new char[123];
        for(char[] pair:chars){
            data[pair[0]] = pair[1];
        }
        return data;
    }


    private static char[] compileGeneratorTable(char[][] chars){
        char[] result = new char[chars.length-6];
        for(int i=0;i<result.length;i++){
            result[i] = chars[i][1];
        }
        return result;
    }
}