package test;

import plus.proto.lang.TableData;
import plus.tson.TsonFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static test.Main.FILE1;


public class ProtoRead {
    public static void main(String[] args) throws IOException {
        byte[] data = TsonFile.readFile(new File(FILE1));

        int shift = 8;  //it work
        //shift += 0x600c;

        byte[] data2 = new byte[data.length - shift];
        byte[] pre   = new byte[shift];

        System.arraycopy(data, shift, data2, 0, data2.length);
        System.arraycopy(data, 0, pre, 0, shift);

        TableData.Data data3 = TableData.Data.parseFrom(data2);

        //TsonFile.write(new File("rdaata.json"), data3.toString());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(pre);

        data3.writeTo(out);

        TsonFile.write(new File("UButes.bytes"), out.toByteArray());

        //System.out.println(data3.getId());
    }
}
