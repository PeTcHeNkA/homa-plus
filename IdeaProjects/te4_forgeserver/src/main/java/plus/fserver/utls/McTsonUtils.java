package plus.fserver.utls;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.exception.NoSearchException;
import plus.tson.security.ClassManager;


public class McTsonUtils {


    public static ClassManager mobClassManager(){
        return new ClassManager() {
            @Override
            public Class<?> forName(String clazz) {
                switch (clazz){
                    case "bat": return EntityBat.class;
                    case "chicken": return EntityChicken.class;
                    case "cow": return EntityCow.class;
                    case "pig": return EntityPig.class;
                    case "rabbit": return EntityRabbit.class;
                    case "donkey": return EntityDonkey.class;
                    case "wolf": return EntityWolf.class;
                    case "villager": return EntityVillager.class;
                    case "lama": return EntityLlama.class;
                    case "horse": return EntityHorse.class;
                    case "iron_golem": return EntityIronGolem.class;

                    case "zombie": return EntityZombie.class;
                    case "blaze": return EntityBlaze.class;
                    case "skeleton": return EntitySkeleton.class;
                    case "enderman": return EntityEnderman.class;
                    case "creeper": return EntityCreeper.class;
                    case "slime": return EntitySlime.class;
                    case "husk": return EntityHusk.class;
                    case "spider": return EntitySpider.class;
                    case "cave_spider": return EntityCaveSpider.class;
                    case "silverfish": return EntitySilverfish.class;
                    case "stray": return EntityStray.class;
                    case "vex": return EntityVex.class;
                }

                String name = "Entity"+clazz.substring(0,1).toUpperCase()+clazz.substring(1);
                Class<?> result = forName0("net.minecraft.entity.passive."+name);
                if(result != null)return result;
                result = forName0("net.minecraft.entity.monster."+name);
                if(result != null)return result;
                result = forName0(clazz);
                if(result != null)return result;

                throw new NoSearchException(clazz);
            }
            private Class<?> forName0(String name){
                try {
                    return Class.forName(name);
                } catch (ClassNotFoundException e){return null;}
            }
        };
    }


    public static IBlockState blockOfTson(TsonObj obj){
        if(obj.isString()){
            String name = obj.getStr();
            int meta = 0;
            int sep = name.lastIndexOf(":");

            if(sep>0){
                try {
                    meta = Integer.parseInt(name.substring(sep+1));
                    name = name.substring(0,sep);
                } catch (Exception e){}
            }
            return Block.REGISTRY.getObject(new ResourceLocation(name)).getStateFromMeta(meta);
        } else if(obj.isNumber()){
            return Block.getBlockById(obj.getInt()).getDefaultState();
        }
        throw new RuntimeException();
    }


    public static int getParam(TsonMap map, String key, int def){
        TsonObj res = map.get(key);
        if(res==null)return def;
        if(res.isNumber())return res.getInt();
        if(res.isString()){
            String s = res.getStr();
            switch (s.charAt(0)){
                case '+': return def + Integer.parseInt(s.substring(1));
                case '-': return def - Integer.parseInt(s.substring(1));
                case '*': return def * Integer.parseInt(s.substring(1));
                case '/': return def / Integer.parseInt(s.substring(1));
            }
            return Integer.parseInt(s);
        }
        throw new IllegalArgumentException();
    }


    public static float getParam(TsonMap map, String key, float def){
        TsonObj res = map.get(key);
        if(res==null)return def;
        if(res.isNumber())return res.getFloat();
        if(res.isString()){
            String s = res.getStr();
            switch (s.charAt(0)){
                case '+': return def + Float.parseFloat(s.substring(1));
                case '-': return def - Float.parseFloat(s.substring(1));
                case '*': return def * Float.parseFloat(s.substring(1));
                case '/': return def / Float.parseFloat(s.substring(1));
            }
            return Float.parseFloat(s);
        }
        throw new IllegalArgumentException();
    }


    public static Biome.BiomeProperties readProperties(TsonMap map){
        Biome.BiomeProperties properties = new Biome.BiomeProperties(map.getStr("display"));
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


    public static int getColor(TsonMap map, String key, int def){
        TsonObj obj = map.get(key);
        if(obj==null)return def;
        if(obj.isNumber()){
            return obj.getInt();
        } else if(obj.isString()){
            return Integer.parseInt(obj.getStr(), 16);
        } else throw new IllegalArgumentException();
    }
}