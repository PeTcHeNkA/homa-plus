import java.util.concurrent.atomic.AtomicInteger;

public class TestIvocations {
    public static void main(String[] args) {
        int size = 300_0000;
        int count = 59;
        Obj1F[] fastObj = new Obj1F[size];
        AbsInvokator[] slowObj = new AbsInvokator[size];

        int flag = 0;

        for (int i=0;i<size;i++){
            fastObj[i] = new Obj1F(i, -i);
            if(flag == 0){
                slowObj[i] = new Obj1(i+1, -1-i);
                ++flag;
            } else if(flag == 1){
                slowObj[i] = new Obj2(i+1, -1-i);
                ++flag;
            } else if(flag == 2){
                slowObj[i] = new Obj3(i+1, -1-i);
                ++flag;
            } else {
                slowObj[i] = new Obj4(i+1, -1-i);
                flag = 0;
            }
        }

        AtomicInteger total = new AtomicInteger();

        for (int i=0;i<10;i++){
            testTime("Fast", count, () -> {
                Obj1F[] fastObj0 = fastObj;
                int num = 1;
                for (Obj1F obj:fastObj0){
                    num += obj.invoke(num, obj);
                }
                total.addAndGet(num);
            });

            testTime("Slow", count, () -> {
                AbsInvokator[] slowObj0 = slowObj;
                int num = 1;
                for (AbsInvokator obj:slowObj0){
                    num += obj.invoke(num, obj);
                }
                total.addAndGet(num);
            });
        }

        if(total.get() == 0)throw new RuntimeException();
    }


    static void testTime(String label, int count, Runnable r) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            r.run();
        }
        long end = System.currentTimeMillis();
        System.out.println(label+" time: " + (end - start));
    }


    private static class Obj1F{
        Object lastRef;
        long id, id2;
        private int num1, num2;

        private Obj1F(int num1, int num2) {
            this.num1 = num1;
            this.num2 = num2;
            this.id = (long) num1 << 16 | num2;
            this.id2 = id+2;
        }

        public long invoke(long num3, Object obj){
            lastRef = obj;
            return (num1 *= num3) + (num2 += num3);
        }
    }

    private static interface AbsInvokator{
        long invoke(long num3, Object obj);
    }


    private static class Obj1 implements AbsInvokator{
        Object lastRef;
        long id, id2;
        int num1, num2;

        private Obj1(int num1, int num2) {
            this.num1 = num1;
            this.num2 = num2;
            this.id = (long) num1 << 16 | num2;
            this.id2 = id+2;
        }

        public long invoke(long num3, Object obj){
            lastRef = obj;
            return (num1 *= num3) + (num2 += num3);
        }
    }


    private static class Obj2 extends Obj1{
        private Obj2(int num1, int num2) {
            super(num1, num2);
        }


        @Override
        public long invoke(long num3, Object obj){
            lastRef = obj;
            return (num2 *= num3) + (num1 += num3);
        }
    }

    private static class Obj3 extends Obj1{
        private Obj3(int num1, int num2) {
            super(num1, num2);
        }


        @Override
        public long invoke(long num3, Object obj){
            lastRef = obj;
            return (num3 *= num2) + (num1 += num3);
        }
    }

    private static class Obj4 extends Obj1{
        private Obj4(int num1, int num2) {
            super(num1, num2);
        }


        @Override
        public long invoke(long num3, Object obj){
            lastRef = obj;
            return (num3 *= num1) + (num2 += num3);
        }
    }
}