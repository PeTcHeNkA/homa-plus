public class TestIdx {


    public static void main(String[] args) {
        idx(-15, 5);
        idx(-5, 0);
        idx(-1, 0);
        idx(0, -1);//
        idx(9, 10);
        idx(10, 70);
        idx(170, 10);
    }


    public static void idx(int x, int y){

        //int num = (((x >> 6) & 0xFFFE) << 15) | ((y >> 6) & 0xFFFE);


        long num = (((long) x) << 32) | y;



        System.out.println(num);
    }
}