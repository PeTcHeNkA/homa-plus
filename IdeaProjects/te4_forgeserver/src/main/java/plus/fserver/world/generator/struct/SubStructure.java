package plus.fserver.world.generator.struct;

import plus.tson.TsonMap;
import plus.tson.TsonObj;


public class SubStructure extends SimpleStruct {
    public final float spawnChance;

    protected SubStructure(String name, Float spawnChance, Integer shiftX, Integer shiftY, Integer shiftZ){
        super(name, shiftX, shiftY, shiftZ);
        this.spawnChance = spawnChance;
    }

    protected SubStructure(TsonMap map){
        super(map);
        spawnChance = getChance(map);
    }


    protected static float getChance(TsonMap map){
        TsonObj chance = map.get("chance");
        if(chance==null)return 1f;
        if(chance.isNumber()){
            return map.getFloat("chance");
        } else if (chance.isString()) {
            String s = chance.getStr();
            if(s.charAt(s.length()-1)=='%'){
                return Float.parseFloat(s.substring(0,s.length()-1))/100f;
            } else {
                return Float.parseFloat(s);
            }
        }
        throw new IllegalArgumentException(chance.getStr());
    }


    @Override
    public float getSpawnChance() {
        return spawnChance;
    }
}