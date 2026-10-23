package test;


public class TestPF {
    private static int count = 1000_000;

    public static void main(String[] args) {
        BitArray[] arr1 = new BitArray[]{
                new BitArray(3, 100),
                new BitArray(4, 100),
                new BitArray(5, 100)
        };

        BitArrayF4[] arr2 = new BitArrayF4[]{
                new BitArrayF3(100),
                new BitArrayF4(100),
                new BitArrayF5(100)
        };

        BitArrayV[] arr3 = new BitArrayV[]{
                new BitArrayV(3, 100),
                new BitArrayV(4, 100),
                new BitArrayV(5, 100)
        };

        for (int x = 0; x < 4; x++){
            testTime("CLA", () -> {
                for (int i = 0; i < count; i++){
                    for (BitArray ar:arr1){
                        test(ar);
                    }
                }
            });

            testTime("PLY", () -> {
                for (int i = 0; i < count; i++){
                    for (BitArrayF4 ar:arr2){
                        test4(ar);
                    }
                }
            });

            testTime("VLA", () -> {
                for (int i = 0; i < count; i++){
                    for (BitArrayV ar:arr3){
                        testV(ar);
                    }
                }
            });
        }
    }


    private static void testTime(String name, Runnable r){
        long start = System.currentTimeMillis();
        r.run();
        long end0 = System.currentTimeMillis();
        System.out.println(name+ " spend time: "+(end0-start)+"ms");
    }


    public static void test4(BitArrayF4 arr){
        for (int i = 0, s = arr.size(); i < s; i++){
            if(arr.getAt(i) == 10)throw new RuntimeException();
            arr.setAt(i, 1);
        }
    }


    public static void testV(BitArrayV arr){
        for (int i = 0, s = arr.size(); i < s; i++){
            if(arr.getAt(i) == 10)throw new RuntimeException();
            arr.setAt(i, 1);
        }
    }


    public static void test(BitArray arr){
        for (int i = 0, s = arr.size(); i < s; i++){
            if(arr.getAt(i) == 10)throw new RuntimeException();
            arr.setAt(i, 1);
        }
    }
}
