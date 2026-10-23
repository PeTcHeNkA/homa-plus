package plus.sol.utls.chr;


public class CharSet extends CharSetBase{
    public static final CharSet VOID = new CharSet('\n',' ');

    public CharSet(String s) {
        super(s);
    }

    public CharSet(char... chars) {
        super(chars);
    }


    //оптимизировать
    //отсортировать символы по номеру и бинарным поиском
    public boolean contains(char c){
        for(char cc:chars){
            if(cc == c)return true;
        }
        return false;
    }
}