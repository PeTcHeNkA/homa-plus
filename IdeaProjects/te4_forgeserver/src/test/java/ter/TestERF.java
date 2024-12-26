package ter;

import plus.fserver.world.save.ERegionFile;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Random;
import static col.TestLists.testTime;


public class TestERF {
    public static void main(String[] args) throws Throwable {
        //testPoss();
//        testIO();
//
//        for (int i = 0; i < 20; i++){
//            testIO();
//        }
//        testPerf();
//        testPerf();
        //testPerf();
//        testPerf();
        testPerf();
    }


    public static int[] getBits(byte b) {
        int[] bits = new int[8];
        for (int i = 0; i < 8; i++){
            bits[i] = ((b & (1 << (7-i))) == 0) ? 0 : 1;
        }
        return bits;
    }


    public static void testPerf() throws IOException {
        ERegionFile file = new ERegionFile(new RandomAccessFile("testdata.bin", "rw"));

        Random rand = new Random(1);
        byte[] data = new byte[2900];
        rand.nextBytes(data);

        testTime("ERF", () -> {
            for (int i = 0; i < 15; i++){
                for (int x = 0; x < 32; x++){
                    for (int z = 0; z < 32; z++){
                        try {
                            int rs = rand.nextInt(data.length);
                            //System.out.println("Write X:"+x+" Z:"+z+" L:"+rs);
                            file.write(x, z, data, rs);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
        file.close();
    }


    public static void testIO() throws IOException {
        ERegionFile file = new ERegionFile(new RandomAccessFile("testdata.bin", "rw"));

        Random rand = new Random(1);

        int x12 = 12, x13 = 13, x11 = 11, z17 = 17;

        //test1
        byte[] data = new byte[1900];
        //rand.nextBytes(data);

        l1: {
            int i = 0, s = 0;
            while (true) {
                for (int j = 0; j < 100; j++) {
                    if (i >= data.length) break l1;
                    data[i] = (byte) s;
                    ++i;
                }
                ++s;
            }
        }

        file.write(x12, z17, data);
        byte[] data2 = file.read(x12, z17);

        System.out.println(equals1(data, data2));

        //test2
        byte[] data3 = new byte[300];
        rand.nextBytes(data3);
        file.write(x13, z17, data3);
        byte[] data4 = file.read(x13,z17);

        System.out.println(Arrays.equals(data3, data4));


        //test3
        file.write(x12,z17, data3);
        file.write(x11,z17, data3);

        data4 = file.read(x12,z17);
        System.out.println(Arrays.equals(data3, data4));

        data4 = file.read(x11,z17);
        System.out.println(Arrays.equals(data3, data4));


        file.close();
        //test4
        file = new ERegionFile(new RandomAccessFile("testdata.bin", "rw"));

        data4 = file.read(x12,z17);
        System.out.println(Arrays.equals(data3, data4));

        data4 = file.read(x11,z17);
        System.out.println(Arrays.equals(data3, data4));
        file.close();
    }


    public static int equals1(byte[] a, byte[] a2) {
        if (a==a2)
            return -1;
        if (a==null || a2==null)
            return -1;

        int length = a.length;
        if (a2.length != length)
            return 0;

        for (int i=0; i<length; i++)
            if (a[i] != a2[i])
                return i;
        return -1;
    }


    public static void testPoss() throws IOException {
        ERegionFile file = new ERegionFile(new RandomAccessFile("testdata.bin", "rw"));
        file.close();

        Random rand = new Random(1);
        int max = (1<<15)-1;
        int off = 1200;

        file.setPos(0, 0, off);

        int nPos = file.getPos(0, 0);

        if(off != nPos){
            throw new RuntimeException(off+" != "+nPos+" !");
        }


        for (int x = 0; x < 32; x++){
            for (int z = 0; z < 32; z++){
                off = rand.nextInt(max);

                file.setPos(x, z, off);

                nPos = file.getPos(x, z);

                if(off != nPos){
                    throw new RuntimeException(off+" != "+nPos+" !");
                }
            }
        }
    }
}
