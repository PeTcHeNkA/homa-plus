package ter;

import plus.fserver.world.save.ERegionFile;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Random;


public class TestERF {
    public static void main(String[] args) throws Throwable {
        testIO();

        for (int i = 0; i < 20; i++){
            testIO();
        }
    }


    public static void testIO() throws IOException {
        ERegionFile file = new ERegionFile(new RandomAccessFile("testdata.bin", "rw"));

        Random rand = new Random(1);

        //test1
        byte[] data = new byte[2100];
        rand.nextBytes(data);
        file.write(12,17, data);
        byte[] data2 = file.read(12,17);

        System.out.println(Arrays.equals(data, data2));

        //test2
        byte[] data3 = new byte[300];
        file.write(13,17, data3);
        byte[] data4 = file.read(13,17);

        System.out.println(Arrays.equals(data3, data4));


        //test3
        file.write(12,17, data3);
        file.write(11,17, data3);

        data4 = file.read(12,17);
        System.out.println(Arrays.equals(data3, data4));

        data4 = file.read(11,17);
        System.out.println(Arrays.equals(data3, data4));


        file.close();
        //test4
        file = new ERegionFile(new RandomAccessFile("testdata.bin", "rw"));

        data4 = file.read(12,17);
        System.out.println(Arrays.equals(data3, data4));

        data4 = file.read(11,17);
        System.out.println(Arrays.equals(data3, data4));
        file.close();
    }


    public static void testPoss() throws IOException {
        ERegionFile file = new ERegionFile(null);

        Random rand = new Random(1);
        int max = (1<<24)-1;
        int off = 100;

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
