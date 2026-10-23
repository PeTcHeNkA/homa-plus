package plus.space.common.world.biome;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.space.common.IHasId;
import plus.tson.TsonMap;
import plus.tson.TsonObj;


public class TsonBiome extends Biome implements IHasId, ITsonBiomeClient {
    public final int id;


    protected TsonBiome(TsonMap map) {
        super(readProperties(map.getMap("properties")));
        preInit(map);
        this.id = map.getInt("id");
        if(!map.ifContainsStr("name", this::setRegistryName)){
            throw new NullPointerException("name is null!");
        };
        map.ifContainsBool("add_to_def",(b)->{
            if(b)BiomeManager.addSpawnBiome(TsonBiome.this);
        });
        map.getMap("properties").ifContainsMap("color",(stn)->{
            setCliColors(getColor(stn,"grass",0), getColor(stn,"foliage",0));
        });
    }


    void preInit(TsonMap map){}


    private static BiomeProperties readProperties(TsonMap map){
        BiomeProperties properties = new BiomeProperties(map.getStr("display"));
        map.ifContainsFloat("height", properties::setBaseHeight);
        map.ifContainsFloat("height_var", properties::setHeightVariation);

        properties.setTemperature(getParam(map, "temp", 0.5F));

        map.ifContainsStr("base_biome", properties::setBaseBiome);
        map.ifContainsFloat("rain_fall", properties::setRainfall);

        map.ifContainsBool("snow", (b)->{
            if(b)properties.setSnowEnabled();
        });
        map.ifContainsBool("rain", (b)->{
            if(!b)properties.setRainDisabled();
        });
        map.ifContainsMap("color",(cls)->{
            properties.setWaterColor(getColor(cls, "water", 16777215));
        });
        return properties;
    }


    @Override
    public int getId() {
        return id;
    }


    @SideOnly(Side.CLIENT)
    private int grassColor = 9470285;
    @SideOnly(Side.CLIENT)
    private int foliageColor = 6975545;
    @Override
    @SideOnly(Side.CLIENT)
    public void setCliColors(int grass, int foliage){
        grassColor = grass;foliageColor = foliage;
    }


    @SideOnly(Side.CLIENT)
    public int getGrassColorAtPos(BlockPos pos) {
        if(grassColor==-1) {
            double d0 = GRASS_COLOR_NOISE.getValue((double) pos.getX() * 0.0225D, (double) pos.getZ() * 0.0225D);
            return getModdedBiomeGrassColor(d0 < -0.1D ? 5011004 : 6975545);
        } else if(grassColor==0){
            return super.getGrassColorAtPos(pos);
        } else {
            return getModdedBiomeGrassColor(grassColor);
        }
    }


    @SideOnly(Side.CLIENT)
    public int getFoliageColorAtPos(BlockPos pos) {
        if(foliageColor != 0)
            return getModdedBiomeFoliageColor(foliageColor);
        else return super.getFoliageColorAtPos(pos);
    }


    @Override
    public String toString() {
        return "ID:"+id+" ["+getRegistryName().getPath()+"]";
    }


    private static int getColor(TsonMap map, String key, int def){
        TsonObj obj = map.get(key);
        if(obj==null)return def;
        if(obj.isNumber()){
            return obj.getInt();
        } else if(obj.isString()){
            return Integer.parseInt(obj.getStr(), 16);
        } else throw new IllegalArgumentException();
    }


    protected static float getParam(TsonMap map, String key, float def){
        TsonObj res = map.get(key);
        if(res==null)return def;
        if(res.isNumber())return res.getFloat();
        if(res.isString()){
            String s = res.getStr();
            switch (s.charAt(0)){
                case '+': return def + Float.parseFloat(s.substring(1));
                case '-': return def - Float.parseFloat(s.substring(1));
            }
            return Float.parseFloat(s);
        }
        throw new IllegalArgumentException();
    }
}