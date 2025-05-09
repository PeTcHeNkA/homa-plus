package test;

import plus.tson.TsonFile;
import java.io.File;
import static test.Main.*;


public class TestSelf {
    public static void main(String[] args) {
        data = TsonFile.readFile(new File("test.bytes"));

        int i1 = indexOf("Hostiles 1");
        int i2 = indexOf("Hostiles 2");

        int len = "Hostile 2".length();

        print("Len %s | %s", len, (i2 - i1) - len);


        print(i1);
        print(i2);

        byte[] copy   = copy(data , i1+10, 8);
        byte[] copy2  = copy(data , i2+10, 8);

        print(Integer.toHexString(num(copy , 0)));
        print(Integer.toHexString(num(copy , 4)));

        print(Integer.toHexString(num(copy2, 0)));
        print(Integer.toHexString(num(copy2, 4)));
    }
}
