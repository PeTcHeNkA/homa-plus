package test;

import com.google.protobuf.*;
import plus.proto.lang.TableData;


public class Proto2 {
    public static void main(String[] args) throws InvalidProtocolBufferException {
//        System.out.println("PROTO2");
//
//        byte[] data1 = TableData.Data.newBuilder()
//                .addItem(newItem(121443, "Hostiles 1"))
//                .addItem(newItem(121444, "Hostiles 2"))
//                .addItem(newItem(121445, "Hostiles 3"))
//        .build().toByteArray();
//
//        TsonFile.write(new File("test.bytes"), data1);
//
//        TableData.Data data2 = TableData.Data.parseFrom(data1);
//        TestSelf.main(args);
    }

    /**
Current

     a1018e4
     b407220a
     a1018e5
     b407220a

==========================
Right

     a1008e4
     b407120a
     a1008e5
     b407120a

     */


    private static TableData.Item newItem(int id, String lore){
        return TableData.Item.newBuilder()
                //.setId(id)
                //.setContent(ByteString.copyFrom(lore.getBytes(StandardCharsets.UTF_8)))
//                .setFlag(true)
//                .setU1N(121444)
                .build();
    }
}