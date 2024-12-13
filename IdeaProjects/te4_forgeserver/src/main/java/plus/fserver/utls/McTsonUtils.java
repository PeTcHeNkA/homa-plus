package plus.fserver.utls;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraft.world.biome.Biome;
import plus.tson.*;
import plus.tson.security.ClassManager;

import java.util.function.BiConsumer;


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

                throw new IllegalArgumentException(clazz);
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


    public static ItemStack itemStackOfTson(TsonObj obj){
        if(obj.isMap()) {
            TsonMap map = obj.getMap();
            Item item = itemOfTson(map.get("id"));
            if (item == Items.AIR) return ItemStack.EMPTY;
            int count = map.getOrDefaultInt("count", 1);
            if (count == 0) return ItemStack.EMPTY;
            int damage = map.getOrDefaultInt("damage", 0);
            NBTBase caps = nbtOfTson(map.get("ForgeCaps"));
            NBTBase tag = nbtOfTson(map.get("tag"));

            ItemStack stack = new ItemStack(item, count, damage, (NBTTagCompound) caps);
            if (tag != null) stack.setTagCompound((NBTTagCompound) tag);
            if (damage != 0) stack.setItemDamage(damage);
            return stack;
        } else if (obj.isString() || obj.isNumber()) {
            Item item = itemOfTson(obj);
            return new ItemStack(item);
        }
        throw new IllegalArgumentException("Unsupported type: "+obj);
    }


    public static NBTBase nbtOfTson(TsonObj obj){
        if(obj == null)return null;
        TsonObj.Type type = obj.type();
        switch (type){
            case STRING:
                return new NBTTagString(obj.getStr());
            case DOUBLE:
                return new NBTTagDouble(obj.getDouble());
            case BOOL:
                return NbtUtils.create(obj.getBool());
            case INT:
                return NbtUtils.create(obj.getInt());
            case FLOAT:
                return new NBTTagFloat(obj.getFloat());
            case LONG:
                return new NBTTagLong(obj.getLong());
            case LIST:{
                TsonList tList = obj.getList();
                NBTTagList nList = new NBTTagList(tList.size());
                for (TsonObj obj0:tList){
                    nList.appendTag(nbtOfTson(obj0));
                }
                return nList;
            }
            case MAP:{
                TsonMap tMap = obj.getMap();
                NBTTagCompound nMap = new NBTTagCompound(tMap.size());
                tMap.forEach((name, obj1) -> nMap.setTag(name, nbtOfTson(obj1)));
            }
        }
        throw new IllegalArgumentException("Invalid type: "+obj);
    }


    public static TsonObj nbtToTson(NBTBase nbt){
        if(nbt == null)return null;
        switch (nbt.getId()){
            case 0:
                return null;
            case 1:
                return new TsonInt(((NBTPrimitive)nbt).getByte());
            case 2:
                return new TsonInt(((NBTPrimitive)nbt).getShort());
            case 3:
                return new TsonInt(((NBTPrimitive)nbt).getInt());
            case 4:
                return new TsonLong(((NBTPrimitive)nbt).getLong()); //new NBTTagLong();
            case 5:
                return new TsonFloat(((NBTPrimitive)nbt).getFloat());//new NBTTagFloat();
            case 6:
                return new TsonDouble(((NBTPrimitive)nbt).getFloat());//new NBTTagDouble();
            case 8:
                return new TsonStr(((NBTTagString)nbt).getString());
            case 9:{
                //return new NBTTagList();
                NBTTagList list = (NBTTagList) nbt;
                TsonList tList = new TsonList();
                for (int i = 0; i < list.tagCount(); i++){
                    TsonObj add = nbtToTson(list.get(i));
                    if(add != null) tList.add(add);
                }
                return tList;
            }
            case 10:{
                //return new NBTTagCompound();
                NBTTagCompound map = (NBTTagCompound) nbt;
                TsonMap tMap = new TsonMap();
                for (String key:map.getKeySet()){
                    TsonObj add = nbtToTson(map.getTag(key));
                    if(add != null)tMap.fput(key, add);
                }
                return tMap;
            }
            default:
                return null;
        }
    }


    public static TsonObj itemStackToTson(ItemStack stack){
        int count = stack.getCount();
        if(count < 1)return new TsonStr("air");
        int dmg = stack.getItemDamage();

        TsonObj id = itemToTson(stack.getItem());

        TsonObj forgeCaps = nbtToTson(stack.getCapsNbt());
        TsonObj nbt       = nbtToTson(stack.getTagCompound());

        if(dmg == 0 && count == 1 && forgeCaps == null && nbt == null){
            return id;
        }

        TsonMap map = new TsonMap();
        map.fput("id", id);
        if(dmg != 0)map.put("damage", dmg);
        if(count != 1)map.put("count", count);
        if(forgeCaps != null){
            map.fput("ForgeCaps", forgeCaps);
        }
        if(nbt != null){
            map.fput("tag", nbt);
        }
        return map;
    }


    public static TsonObj itemToTson(Item item){
        ResourceLocation resLoc = Item.REGISTRY.getNameForObject(item);
        if(resLoc == null)return new TsonStr("air");
        if(resLoc.getNamespace().equals("minecraft")){
            return new TsonStr(resLoc.getPath());
        } else {
            return new TsonStr(resLoc.toString());
        }
    }


    public static Item itemOfTson(TsonObj obj){
        if(obj == null)return Items.AIR;
        if(obj.isString()){
            return Item.getByNameOrId(obj.getStr());
        }
        if(obj.isNumber()){
            return Item.getItemById(obj.getInt());
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



    public static Vec3d getVec3d(TsonObj obj){
        if(obj.isList()){
            TsonList list = obj.getList();
            return new Vec3d(list.getDouble(0), list.getDouble(1), list.getDouble(2));
        }
        if(obj.isMap()){
            TsonMap map = obj.getMap();
            return new Vec3d(map.getDouble("x"), map.getDouble("y"), map.getDouble("z"));
        }
        throw new IllegalArgumentException("Invalid type: "+obj);
    }


    public static GameType getGameType(TsonObj obj){
        if(obj.isNumber()){
            return GameType.getByID(obj.getInt());
        }
        if(obj.isString()){
            return GameType.getByName(obj.getStr());
        }
        throw new IllegalArgumentException("Invalid type: "+obj);
    }


    public static EnumParticleTypes particleTypeOf(TsonObj obj){
        if(obj.isString())return EnumParticleTypes.getByName(obj.getStr());
        if(obj.isNumber())return EnumParticleTypes.getParticleFromId(obj.getInt());
        throw new IllegalArgumentException("Invalid type: "+obj);
    }
}