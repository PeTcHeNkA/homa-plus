package plus.fserver.world.generator.biome;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeManager;
import plus.fserver.world.generator.biome.layers.EGenLayerBiomePlus;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.TsonStr;
import java.util.ArrayList;


public class BiomeLib {
    public final ArrayList<EGenLayerBiomePlus.BiomeEntry>[] biomes =
            new ArrayList[BiomeManager.BiomeType.values().length];

    public final int[][][] biomeBounds;
    public final TsonMap settings;


    public static class Creator{
        private final IRandTsonObjTableGen warm;
        private final IRandTsonObjTableGen cool;
        private final IRandTsonObjTableGen hot;
        private final IRandTsonObjTableGen cold;
        private final IRandTsonObjTableGen bounds;
        private final IRandTsonObjTableGen settings;
        private final TsonMap frame;

        public Creator(TsonMap map){
            warm = create(map.get("normal"));
            cool = create(map.get("cool"));
            hot = create(map.get("hot"));
            cold = create(map.get("cold"));
            bounds = create(map.get("bounds"));
            settings = create(map.get("settings"));
            if(map.containsKey("def")){
                frame = map.getMap("def");
            } else {
                frame = new TsonMap();
            }
        }


        public TsonMap createRandomSettings(){
            TsonMap map = frame.clone();
            add0(map, "normal", warm);
            add0(map, "cool", cool);
            add0(map, "hot", hot);
            add0(map, "cold", cold);
            add0(map, "bounds", bounds);
            add0(map, "settings", settings);
            return map;
        }


        private void add0(TsonMap map, String key, IRandTsonObjTableGen gen){
            if(gen==null)return;
            TsonList list = gen.getRandObjects();
            if(list.size()==0)return;
            map.put(key, list);
        }


        private static IRandTsonObjTableGen create(TsonObj obj){
            if(obj==null)return null;
            return IRandTsonObjTableGen.build(obj);
        }
    }


    public BiomeLib(TsonMap map){
        for(int i=0;i<biomes.length;i++){
            biomes[i] = new ArrayList<>();
        }
        fillType(BiomeManager.BiomeType.WARM, map.get("normal"));
        fillType(BiomeManager.BiomeType.COOL, map.get("cool"));
        fillType(BiomeManager.BiomeType.DESERT, map.get("hot"));
        fillType(BiomeManager.BiomeType.ICY, map.get("cold"));
        biomeBounds = readBounds(this, map.get("bounds"));
        settings = map.getOrCreateMap("settings");
    }


    private static int[][][] readBounds(BiomeLib lib, TsonObj obj0){
        if(obj0 == null)return null;
        TsonList list1 = obj0.getList();
        ArrayList<int[][]> result1 = new ArrayList<>(list1.size());

        for(int cur=0;cur<result1.size();cur++) {
            TsonList list = list1.getList(cur);
            ArrayList<int[]> result = new ArrayList<>(list.size());

            for (int i = 0; i < result.size(); i++) {
                TsonObj obj = list.get(i);
                int[] res;
                if (obj.isList()) {
                    TsonList subList = obj.getList();
                    res = new int[]{getBiomeId(subList.get(0)), getBiomeId(subList.get(1))};
                } else if (obj.isMap()) {
                    TsonMap subMap = obj.getMap();
                    res = new int[]{getBiomeId(subMap.get("check")), getBiomeId(subMap.get("set"))};
                } else {
                    throw new IllegalArgumentException(obj.toString());
                }
                boolean needAdd = false;
                for (ArrayList<EGenLayerBiomePlus.BiomeEntry> secBiomes:lib.biomes){
                    for (EGenLayerBiomePlus.BiomeEntry entry:secBiomes){
                        if(entry.id == res[0]){
                            needAdd = true;
                            break;
                        }
                    }
                }
                if(needAdd) result.add(res);
            }
            if(!result.isEmpty()) result1.add(result.toArray(new int[][]{}));
        }
        return result1.toArray(new int[][][]{});
    }


    private void fillType(BiomeManager.BiomeType type, TsonObj obj){
        if(obj==null){
            switch (type){
                case WARM: throw new NullPointerException();
                case COOL:
                case DESERT:
                    obj = new TsonStr("warm");
                    break;
                case ICY:
                    obj = new TsonStr("cool");
                    break;
            }
        }
        if(obj.isString()){
            switch (obj.getStr()){
                case "warm":
                case "normal":
                    biomes[type.ordinal()] = biomes[BiomeManager.BiomeType.WARM.ordinal()];
                    break;
                case "desert":
                case "hot":
                    biomes[type.ordinal()] = biomes[BiomeManager.BiomeType.DESERT.ordinal()];
                    break;
                case "cool":
                    biomes[type.ordinal()] = biomes[BiomeManager.BiomeType.COOL.ordinal()];
                    break;
                case "cold":
                case "icy":
                    biomes[type.ordinal()] = biomes[BiomeManager.BiomeType.ICY.ordinal()];
                    break;
                default: throw new IllegalArgumentException();
            }
        } else if(obj.isList()){
            ArrayList<EGenLayerBiomePlus.BiomeEntry> entries = biomes[type.ordinal()];
            for(TsonObj obj0:obj.getList()) entries.add(biomeEntryOfTson(obj0));

        } else throw new IllegalArgumentException();
    }


    private static EGenLayerBiomePlus.BiomeEntry biomeEntryOfTson(TsonObj obj){
        if(obj.isNumber()){
            return new EGenLayerBiomePlus.BiomeEntry(obj.getInt(), 10);
        }
        if(obj.isList()){
            return new EGenLayerBiomePlus.BiomeEntry(
                    getBiomeId(obj.getList().get(0)),
                    obj.getList().getInt(1)
            );
        } else if(obj.isMap()){
            return new EGenLayerBiomePlus.BiomeEntry(
                    getBiomeId(obj.getMap().get("biome")),
                    obj.getMap().getInt("weight")
            );
        }
        throw new IllegalArgumentException();
    }


    private static int getBiomeId(TsonObj obj){
        if(obj.isNumber()){
            int id = obj.getInt();
            if(Biome.getBiome(id)==null)throw new NullPointerException("Biome ["+id+"] not exists!");
            return id;
        } else if(obj.isString()){
            String id = obj.getStr();
            Biome biome;
            if((biome = Biome.REGISTRY.getObject(new ResourceLocation(id)))==null)
                throw new NullPointerException("Biome ["+id+"] not exists!");
            return Biome.getIdForBiome(biome);
        }
        throw new IllegalArgumentException("Not valid type: ["+obj.getClass().getSimpleName()+"]");
    }
}