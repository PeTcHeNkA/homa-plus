import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import plus.fserver.api.ChatPacketBuilder;
import plus.tson.STsonParser;

public class TestHM {
    public static void main(String[] args) {

        STsonParser parser = new STsonParser("{key = 'v1', key2, key3, key4}");

        System.out.println(parser.getMap().toString());

    }
}
