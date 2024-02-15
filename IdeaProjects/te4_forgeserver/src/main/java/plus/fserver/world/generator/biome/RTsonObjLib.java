package plus.fserver.world.generator.biome;

import plus.fserver.utls.math.FastRandom;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import java.util.Random;


class RTsonObjLib implements IRandTsonObjTableGen {
    private final TsonObj[] items;
    private final int delta;
    private final int max;

    public RTsonObjLib(TsonMap map){
        items = map.getList("biomes").toArray(new TsonObj[0]);
        int max = map.getInt("max");
        int min = map.getOrDefaultInt("min", 0);
        delta = min;

        max = max-min+1;
        if(max>items.length)max = items.length;
        this.max = max;
    }


    @Override
    public TsonObj[] getRandBiomes0(){
        Random rnd = FastRandom.INST;
        for (int i = items.length; i>1; i--)
            swap(i-1, rnd.nextInt(i));

        TsonObj[] biomes = new TsonObj[delta+FastRandom.INST.nextInt(max)];
        System.arraycopy(items, 0, biomes, 0, biomes.length);
        return biomes;
    }


    private void swap(int i, int j){
        TsonObj tmp = items[i];
        items[i] = items[j];
        items[j] = tmp;
    }
}