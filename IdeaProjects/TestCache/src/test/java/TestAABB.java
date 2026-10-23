import plus.fserver.utls.AABKey;

public class TestAABB {

    public static void main(String[] args) {

        int count = 10, c2 = 5000_000;
        AABKey key = new AABKey();

        for (int i = 0; i < 10; i++) {

            testTime("DEF", count, () -> {
                double d1 = 100.0d;
                double d2 = 100.0d;
                double d3 = 100.0d;

                for (int i12 = 0; i12 < c2; i12++) {
                    d1 += i12;
                    d2 -= i12;
                    d3 -= i12;
                    key.init(d1, d2, d3, d1-1, d2-1, d3-1);
                }
            });

            testTime("BST", count, () -> {
                double d1 = 100.0d;
                double d2 = 100.0d;
                double d3 = 100.0d;

                for (int i1 = 0; i1 < c2; i1++) {
                    d1 += i1;
                    d2 -= i1;
                    d3 -= i1;
                    key.initV2(d1, d2, d3, d1-1, d2-1, d3-1);
                }
            });
        }

        System.out.println(key);
    }





    static void testTime(String label, int count, Runnable r) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            r.run();
        }
        long end = System.currentTimeMillis();
        System.out.println(label+" time: " + (end - start));
    }
}
