package plus.fserver.world.generator.biome.layers;

import plus.fserver.world.generator.biome.EIntCache;


public class EGenLayerFuzzyZoom extends EGenLayerZoom{
    public EGenLayerFuzzyZoom(long seed, EGenLayer parent) {
        super(seed, parent);
    }


    @Override
    protected int selectModeOrRandom(EIntCache cache, int num1, int num2, int num3, int num4) {
        return cache.selectRandom(num1, num2, num3, num4);
    }
}