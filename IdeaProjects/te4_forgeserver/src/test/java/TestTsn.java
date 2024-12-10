import plus.tson.STsonParser;
import plus.tson.TsonFunc;

public class TestTsn {
    public static void main(String[] args) {

        STsonParser parser = new STsonParser("{func=plus.fserver.api.vanillagui.SmartChestGuiTemplate::cansel}").with(TsonFunc.COMPILER);

        TsonFunc func = parser.getMap().getFunc("func");

        func.call(null, null, null, null, null);



    }
}
