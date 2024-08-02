package utils;

public class TestLeavesV2 {


    public static void main(String[] args) {
        int total = 0;
        //9x9x9   = 729 = 100%
        //??      = 641 =
        for (int x = -4; x <= 4; x++){
            for (int y = -4; y <= 4; y++){
                for (int z = -4; z <= 4; z++){

                    int rsq = rangeToSq(x, y, z);
                    if(rsq <= 26) {
                        System.out.println(of(x) + " " + of(y) + " " + of(z) + "| " + rsq);
                        ++total;
                    }
                }
            }
        }
        System.out.println("Total: " + total);
    }


    private static String of(int n){
        if(n >= 0)return " " + n;
        return "" + n;
    }


    private static int rangeToSq(int x, int y, int z){
        int t = (x * y * z);
        if(t > 0)return t;
        return -t;
    }
}
