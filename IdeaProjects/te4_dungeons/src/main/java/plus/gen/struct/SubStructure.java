package plus.gen.struct;

import plus.tson.TsonMap;


public class SubStructure extends AbcStruct{
    public final float spawnChance;

    public SubStructure(TsonMap map){
        super(map);
        spawnChance = getChance(map);
    }

    protected static float getChance(TsonMap map){
        if(map.containsKey("chance")){
            return map.getFloat("chance");
        } else return 1f;
    }


    @Override
    public float getSpawnChance() {
        return spawnChance;
    }
}