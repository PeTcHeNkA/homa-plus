package plus.fserver.world.generator.struct;

import plus.fserver.world.generator.settings.BiomeChecker;
import plus.tson.TsonMap;


public class MultiStructure extends WorldMultiStructure implements IGlobStruct{
    public final BiomeChecker biomeChecker;

    public MultiStructure(TsonMap map) {
        super(map);
        if(map.containsKey("biomes")) {
            biomeChecker = BiomeChecker.build(map.getList("biomes"));
        } else {
            biomeChecker = BiomeChecker.EMPTY;
        }
    }





    @Override
    public BiomeChecker getBiomeChecker() {
        return null;
    }
}