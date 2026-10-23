import plus.gen.utl.WeightLinckedIndexes;

public class test {

    public static long count = 9000000000000000000L;
    private static int targs = 0;

    public static void main(String... args){
        try {
            main();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private final int val = 1;


    public static void main() throws NoSuchFieldException, IllegalAccessException, InterruptedException {

        WeightLinckedIndexes r = new WeightLinckedIndexes(new int[]{1, 2, 3}, new float[]{0.5f});

        System.out.println(r.getIndexOfWeight(0.05f));
        System.out.println(r.getIndexOfWeight(0.3f));
        System.out.println(r.getIndexOfWeight(0.99f));
    }


    public static void test(Runnable r){
        long time = System.currentTimeMillis();
        r.run();
        System.out.println("Time: "+(System.currentTimeMillis()-time));
    }
}
