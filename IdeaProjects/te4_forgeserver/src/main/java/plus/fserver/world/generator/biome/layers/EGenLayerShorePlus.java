package plus.fserver.world.generator.biome.layers;

import plus.fserver.world.generator.biome.EIntCache;


public class EGenLayerShorePlus extends EGenLayer {
    private final int idTweak;
    private final int idCheck;

    public EGenLayerShorePlus(long seed, EGenLayer parent, int check, int tweak) {
        super(seed);
        this.parent = parent;
        this.idCheck = check;
        this.idTweak = tweak;
    }


    @Override
    public int[] getInts(EIntCache cache, int areaX, int areaY, int areaWidth, int areaHeight) {
        int[] aint = this.parent.getInts(cache, areaX - 1, areaY - 1, areaWidth + 2, areaHeight + 2);
        int[] aint1 = cache.getIntCache(areaWidth * areaHeight);

        for (int i = 0; i < areaHeight; ++i) {
            l1: for (int j = 0; j < areaWidth; ++j) {
                cache.initChunkSeed(this, j + areaX, i + areaY);
                int k = aint[j + 1 + (i + 1) * (areaWidth + 2)];

                if (/*k != 0 && k != 24 && k != 7 && */k == idCheck) {
                    int l1 = aint[j + 1 + (i) * (areaWidth + 2)];
                    int i1 = aint[j + 2 + (i + 1) * (areaWidth + 2)];
                    int j1 = aint[j + (i + 1) * (areaWidth + 2)];
                    int ea1 = aint[j + 1 + (i + 2) * (areaWidth + 2)];

                    if (!(l1 == idCheck && i1 == idCheck && j1 == idCheck && ea1 == idCheck)) {
                        aint1[j + i * areaWidth] = idTweak;
                        continue l1;
                    }
                }
                aint1[j + i * areaWidth] = k;
            }
        }
        return aint1;
    }
}