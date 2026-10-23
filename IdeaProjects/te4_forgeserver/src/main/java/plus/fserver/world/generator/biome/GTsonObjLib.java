package plus.fserver.world.generator.biome;

import plus.tson.TsonList;
import plus.tson.TsonObj;


class GTsonObjLib implements IRandTsonObjTableGen {
    private final IRandTsonObjTableGen[] libs;

    public GTsonObjLib(TsonList lst) {
        libs = new IRandTsonObjTableGen[lst.size()];
        for(int i=0;i<lst.size();i++) libs[i] = IRandTsonObjTableGen.build(lst.get(i));
    }


    @Override
    public TsonObj[] getRandBiomes0() {
        TsonObj[][] items = new TsonObj[libs.length][];
        int summSize = 0;

        for(int i=0;i<items.length;i++){
            items[i] = libs[i].getRandBiomes0();
            summSize += items[i].length;
        }

        TsonObj[] result = new TsonObj[summSize];
        int dest = 0;
        int cursor = 0;

        do{
            TsonObj[] cur = items[cursor];
            System.arraycopy(cur, 0, result, dest, cur.length);
            dest += cur.length;
        } while (++cursor<items.length);

        return result;
    }
}










