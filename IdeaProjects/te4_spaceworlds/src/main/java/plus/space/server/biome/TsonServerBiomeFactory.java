package plus.space.server.biome;

import plus.space.common.world.biome.TsonBiome;
import plus.space.common.world.biome.TsonClientBiomeFactory;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import static plus.fserver.utls.TsonThreeRegistry.getOrCreateOf;


public class TsonServerBiomeFactory extends TsonClientBiomeFactory {
    @Override
    public TsonBiome newBiome(TsonMap map) {
        if(map.containsKey("noises")){
            return new TsonBiomeNPlus(map);
        }else if(map.containsKey("generator")){
            return new TsonBiomePlus(map);
        } else {
            return new TsonBiomeServer(map);
        }
    }


    @Override
    public void loadEtcObjects(TsonMap pack) {
        TsonObj threes = pack.get("threes");
        if(threes != null && threes.isList()){
            TsonList threes0 = threes.getList();
            for(TsonObj obj:threes0) getOrCreateOf(obj.getMap());
        }
    }
}