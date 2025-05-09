package plus.proto;

import com.google.protobuf.InvalidProtocolBufferException;
import plus.proto.lang.TableData;
import plus.tson.TsonFile;
import java.io.File;
import static plus.proto.Main.FILE1;


public class ProtoRead {
    public static void main(String[] args) throws InvalidProtocolBufferException {
        byte[] data = TsonFile.readFile(new File(FILE1));

        int shift = 4;  //it work

        byte[] data2 = new byte[data.length - shift];
        System.arraycopy(data, shift, data2, 0, data2.length);

        Object data3 = TableData.Data.parseFrom(data2);


        TsonFile.write(new File("rdaata.json"), data3.toString());

        //System.out.println(data3.getId());
    }
}
