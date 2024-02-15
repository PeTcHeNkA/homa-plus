package plus.fserver.utls;


public class FastValidators {
    public static boolean is_09_az(String name){
        char[] chars = name.toCharArray();
        for (char c:chars){
            if(!((c>=48&c<=57)||(c>=97&c<=122)||c == 95))
                return false;
        }
        return true;
    }


    //"[A-Za-z0-9._+-]+"
    public static boolean is_AZ_az_09_P(String name){
        char[] chars = name.toCharArray();
        for (char c:chars){
            if(c>='A') return c<='Z' || c>='a'&&c<='z' || c == '_';
            else return c == '.' || c == '+' || c == '-';
        }
        return true;
    }
}