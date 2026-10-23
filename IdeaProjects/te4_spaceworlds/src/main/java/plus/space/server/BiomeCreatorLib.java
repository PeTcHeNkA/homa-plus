package plus.space.server;

import plus.fserver.world.generator.biome.BiomeLib;
import plus.fserver.utls.math.FastRandom;
import plus.tson.TsonFile;
import plus.tson.TsonList;
import plus.tson.TsonMap;


public class BiomeCreatorLib {
    private final BiomeLib.Creator[][] planets =
            new BiomeLib.Creator[SolarRepository.Race.values().length][];

    private final BiomeLib.Creator[][] moons =
            new BiomeLib.Creator[SolarRepository.Race.values().length][];

    public BiomeCreatorLib(TsonFile map){
        boolean flag1 = fillLib(planets, map, "PLANETS" );
        boolean flag2 = fillLib(moons  , map, "MOONS"   );

        if(flag1 || flag2) map.save();
    }


    private boolean fillLib(BiomeLib.Creator[][] lib, TsonMap src, String key0){
        boolean needSave = false;
        TsonMap map;
        if(key0==null)map = src;
        else {
            if(src.containsKey(key0)){
                map = src.getMap(key0);
            } else {
                map = src.addMap(key0);
                needSave = true;
            }
        }
        for(SolarRepository.Race race: SolarRepository.Race.values()){
            String key = race.toString();
            if(map.containsKey(key)) {
                TsonList list = map.getList(key);
                BiomeLib.Creator[] list0 = lib[race.ID] = new BiomeLib.Creator[list.size()];

                for (int i = 0; i < list0.length; i++)
                    list0[i] = new BiomeLib.Creator(list.getMap(i));
            } else {
                map.addList(key);
                needSave = true;
            }
        }
        return needSave;
    }


    public TsonMap create(SolarRepository.Race race, boolean isMoon){
        BiomeLib.Creator[] curCreators = (isMoon?moons: planets)[race.ID];
        if(curCreators.length == 0)return new TsonMap();
        return curCreators[FastRandom.INST.nextInt(curCreators.length)].createRandomSettings();
    }
}