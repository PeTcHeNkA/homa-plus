package test;

import com.google.protobuf.CodedOutputStream;
import plus.proto.LangPackV2;
import plus.proto.lang.TableData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class Test2 {
    public static void main(String[] args) throws IOException {

        ByteArrayOutputStream bOut =  new ByteArrayOutputStream();
        CodedOutputStream out = CodedOutputStream.newInstance(bOut);

        LangPackV2.Line line = new LangPackV2.Line(9, 9, "a");

        LangPackV2.writeNext10(0, new LangPackV2.Line[]{
            line, line
        }, out, new LangPackV2.WriteCallback());

        out.flush();

        System.out.println(Arrays.toString(bOut.toByteArray()));

        TableData.Item msg = TableData.Item.newBuilder().setId(line.id).setName(line.getActual()).build();

        System.out.println(Arrays.toString(msg.toByteArray()));
    }
}
