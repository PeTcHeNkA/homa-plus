package plus.space.server.biome;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import plus.fserver.utls.TsonThreeRegistry;
import plus.space.common.world.biome.TsonBiome;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.exception.NoSearchException;
import plus.tson.security.ClassManager;
import java.util.List;
import java.util.Random;


public class TsonBiomeServer extends TsonBiome {
    public final TsonThreeRegistry.Generator threeGen;

    protected TsonBiomeServer(TsonMap map) {
        super(map);
        map.ifContains("block_top", obj -> topBlock = blockOfTson(obj));
        map.ifContains("block_fill", obj -> fillerBlock = blockOfTson(obj));

        map.ifContainsMap("spawn",(spawn)->{
            spawn.ifContainsList("clear",(lst)->{
                if(lst.contains("normal")) spawnableCreatureList.clear();
                if(lst.contains("haves")) spawnableCaveCreatureList.clear();
                if(lst.contains("water")) spawnableWaterCreatureList.clear();
                if(lst.contains("monster")) spawnableMonsterList.clear();
            });
            spawn.ifContainsMap("add",(add)->{
                ClassManager mcm = mobClassManager();
                add.ifContainsList("normal",(lst)-> addToSpawn(lst, mcm, spawnableCreatureList));
                add.ifContainsList("haves",(lst)-> addToSpawn(lst, mcm, spawnableCaveCreatureList));
                add.ifContainsList("water",(lst)-> addToSpawn(lst, mcm, spawnableWaterCreatureList));
                add.ifContainsList("monster",(lst)-> addToSpawn(lst, mcm, spawnableMonsterList));
            });
        });

        map.ifContainsMap("decorator",(dec)->{
            decorator.bigMushroomsPerChunk = getParam(dec,"big_mushrooms", decorator.bigMushroomsPerChunk);
            decorator.mushroomsPerChunk = getParam(dec,"mushrooms", decorator.mushroomsPerChunk);
            decorator.grassPerChunk = getParam(dec,"grass", decorator.grassPerChunk);
            decorator.flowersPerChunk = getParam(dec,"flowers", decorator.flowersPerChunk);
            decorator.treesPerChunk = getParam(dec,"threes", decorator.treesPerChunk);
            decorator.extraTreeChance = dec.getOrDefaultFloat("extra_three_chance", decorator.extraTreeChance);
            decorator.generateFalls = dec.getOrDefaultBool("falls", decorator.generateFalls);
            decorator.clayPerChunk = getParam(dec,"clay", decorator.clayPerChunk);
            decorator.sandPatchesPerChunk = getParam(dec,"sand_patches", decorator.sandPatchesPerChunk);
            decorator.gravelPatchesPerChunk = getParam(dec,"gravel_patches", decorator.gravelPatchesPerChunk);
            decorator.reedsPerChunk = getParam(dec,"reeds", decorator.reedsPerChunk);
            decorator.deadBushPerChunk = getParam(dec,"dead_bush", decorator.deadBushPerChunk);
            decorator.cactiPerChunk = getParam(dec,"cacti", decorator.cactiPerChunk);
        });

        if(map.containsKey("threes")){
            threeGen = new TsonThreeRegistry.Generator(map.getList("threes"));
        } else {
            threeGen = null;
        }
    }


    protected static IBlockState blockOfTson(TsonObj obj){
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


    protected static int getParam(TsonMap map, String key, int def){
        TsonObj res = map.get(key);
        if(res==null)return def;
        if(res.isNumber())return res.getInt();
        if(res.isString()){
            String s = res.getStr();
            switch (s.charAt(0)){
                case '+': return def + Integer.parseInt(s.substring(1));
                case '-': return def - Integer.parseInt(s.substring(1));
            }
            return Integer.parseInt(s);
        }
        throw new IllegalArgumentException();
    }


    public WorldGenAbstractTree getRandomTreeFeature(Random rand) {
        if(threeGen != null)return threeGen.get(rand);
        return super.getRandomTreeFeature(rand);
    }


    private static ClassManager mobClassManager(){
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


    private static void addToSpawn(TsonList items, ClassManager mcm, List<SpawnListEntry> to){
        for(TsonObj obj:items) to.add(entryOfTson(obj, mcm));
    }


    private static SpawnListEntry entryOfTson(TsonObj obj, ClassManager mcm){
        if(obj.isMap()){
            TsonMap map = obj.getMap();
            return new SpawnListEntry(
                    (Class<EntityLiving>) mcm.forName(map.getStr("class")),
                    map.getOrDefaultInt("weight",50),
                    map.getOrDefaultInt("min",1),
                    map.getOrDefaultInt("max",2)
            );
        } else if(obj.isList()){
            TsonList list = obj.getList();
            return new SpawnListEntry(
                    (Class<EntityLiving>) mcm.forName(list.getStr(0)),
                    list.getInt(1),
                    list.getInt(2),
                    list.getInt(3)
            );
        }
        throw new IllegalArgumentException();
    }
}