package plus.space.common.world.biome;

import net.minecraft.world.biome.Biome;
import plus.fserver.api.ElcaBiomeRegistrator;
import plus.space.common.IHasId;
import plus.space.common.world.pro.BiomeOrbit;
import plus.tson.TsonClass;
import plus.tson.TsonFile;
import plus.tson.TsonMap;
import plus.tson.TsonObj;


public class TsonBiomePack extends TsonFile implements ITsonBiomeFactory{
    private final ITsonBiomeFactory factory;
    public TsonBiomePack(String fileName, ITsonBiomeFactory factory) {
        super(fileName);
        this.factory = factory;
    }


    @Override
    public TsonBiome newBiome(TsonMap map) {
        return factory.newBiome(map);
    }


    @Override
    public void loadEtcObjects(TsonMap pack) {
        factory.loadEtcObjects(pack);
    }


    public void loadBiomes(){
        TsonObj list = get("biomes");
        if(list==null || !(list.isList() && !list.getList().isEmpty()))return;

        for(TsonObj obj:list.getList()) {
            Biome biome;
            ElcaBiomeRegistrator.addToQueue(biome = newBiome(obj.getMap()), ((IHasId)biome).getId());
        }
    }


    private static void preLoadPack(){
        BiomeOrbit.addToRegBiomes();
    }


    public static void loadPack(boolean client){
        ITsonBiomeFactory factory = (ITsonBiomeFactory) new TsonClass(
                client?"plus.space.common.world.biome.TsonClientBiomeFactory":
                        "plus.space.server.biome.TsonServerBiomeFactory"
        ).createInst();
        preLoadPack();
        TsonBiomePack pack = new TsonBiomePack("biomes.tson", factory);
        pack.loadBiomes();
    }
}