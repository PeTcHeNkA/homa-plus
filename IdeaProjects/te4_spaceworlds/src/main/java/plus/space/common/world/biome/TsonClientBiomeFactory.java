package plus.space.common.world.biome;

import plus.tson.TsonMap;


public class TsonClientBiomeFactory implements ITsonBiomeFactory{
    @Override
    public TsonBiome newBiome(TsonMap map) {
        return new TsonBiome(map);
    }


    @Override
    public void loadEtcObjects(TsonMap pack) {}
}