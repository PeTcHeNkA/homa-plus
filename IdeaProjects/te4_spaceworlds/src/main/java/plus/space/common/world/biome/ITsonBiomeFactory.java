package plus.space.common.world.biome;

import plus.tson.TsonMap;


public interface ITsonBiomeFactory {
    TsonBiome newBiome(TsonMap map);
    void loadEtcObjects(TsonMap pack);
}