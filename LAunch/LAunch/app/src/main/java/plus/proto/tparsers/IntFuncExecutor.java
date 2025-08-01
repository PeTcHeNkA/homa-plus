package plus.proto.tparsers;

import plus.tson.TsonFile;
import java.io.File;
import static plus.proto.LangPackV2.copy;
import static plus.proto.LangPackV2.readInt;


public interface IntFuncExecutor {
    default void readFrom(File file) throws Exception {
        byte[] data = TsonFile.readFile(file);
        int len = readInt(data);

        byte[] chunk2 = copy(data, len + 4, data.length - (len + 4));
        onReadFrom(chunk2);
    }


    void onReadFrom(byte[] data) throws Exception;


    void apply(IntConsumer consumer);
}