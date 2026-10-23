package plus.gen;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import plus.gen.schem.Schem;
import plus.gen.settings.BiomeChecker;
import plus.gen.struct.*;
import plus.gen.utl.Buffer;
import plus.gen.utl.GenProxy;
import plus.tson.TsonFile;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.utl.Tuple;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;


@Mod.EventBusSubscriber
public class Te4WorldGen implements IWorldGenerator {
    protected static boolean updateOnLoadWorld = !plus.MainDungeons.PROD;
    //private static long lastActiveTime = System.currentTimeMillis();
    //public static final Buffer<SubMultiStructure.Args> buffer = new Buffer<>();
    public static final Buffer<Runnable> buffer = new Buffer<>();
    //public static final Stack<Runnable> buffer = new Stack<>();
    //private static GenThread[] threads = new GenThread[4];
    private static int startTrace = 250;
    private static GenProxy proxy;

    public Te4WorldGen(){
        init();
    }


    protected static void init(){
        //AbcStruct.clearCache();
        Schem.clearCache();
        TsonFile config = new TsonFile(
                new File(Loader.instance().getConfigDir(), "struct-gen-config.tson")).load();
        if(!config.isEmpty()) {
            config.ifContainsInt("start-trace",i -> startTrace = i);
            config.ifContainsInt("max-stack", i -> SubMultiStructure.maxUsages = i);
            config.ifContainsBool("vanilla-biome-check", BiomeChecker::setMode);
            config.ifContainsBool("update-on-init", b -> updateOnLoadWorld = b);
            //config.ifContainsInt("threads", i -> threads = new GenThread[i]);
        } else {
            config.put("start-trace", startTrace);
            config.put("max-stack", SubMultiStructure.maxUsages);
            config.put("vanilla-biome-check",true);
            config.put("update-on-init", updateOnLoadWorld);
            config.put("threads", 4);
            config.save();
        }

        Tuple<File, String> data = TsonFile.read(
            Loader.instance().getConfigDir(), "struct-gen",
        "{structures=[<(plus.gen.struct.Structure), {id=(1), chance=(0.5), biomes=[\"plains\"], " +
            "spawn-at=[\"grass\"], structure=\"test\", max-pow=(50), pos=[(0),(1),(0)], add=[(0)], " +
            "settings=[[\"rot-90\"], [\"struct-block\"], [\"entities\", \"m-left\", \"m-front\", \"ignore:air\"]]}>], " +
            "classes=[(plus.gen.struct.Structure), (plus.gen.struct.SubStructure)" +
            "(plus.gen.struct.MultiStructure), (plus.gen.struct.SubMultiStructure),(OneOfSeveralStructure)]}"
        );
        TsonMap map = new TsonMap(data.B);
        ArrayList<Structure> structures0 = new ArrayList<>();
        if(map.containsKey("structures")) {
            for (TsonObj o : map.getList("structures")) {
                Object obj = o.getField();
                if (obj instanceof Structure) {
                    structures0.add((Structure) obj);
                }
            }
        } else {
            throw new RuntimeException("Config hasnt structures! --> "+data.B);
        }
        Structure[] structures = structures0.toArray(new Structure[0]);
        proxy = (random, chunkX, chunkZ, world, chunkGenerator, chunkProvider) -> {
            //restartThreads();
            AbcStruct.init(world);
            proxy = (random1, chunkX1, chunkZ1, world1, chunkGenerator1, chunkProvider1) -> {
                if(world1.provider.getDimension()==0) {
                    for (Structure s : structures) {
                        gen(s, world1, random1, chunkX1, chunkZ1);
                    }
                }
            };
            proxy.gen(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
        };
    }


//    private static final Thread[] threads = new Thread[1];
//    private static void restartThreads(){
//        for(int i=0;i<threads.length;i++){
//            if(threads[i]!=null){
//                try {
//                    threads[i].stop();
//                }catch (Exception ignored){}
//            }
//            threads[i] = new GenThread(i);
//            threads[i].start();
//        }
//    }

//    public static class GenThread extends Thread {
//        public GenThread(int i) {
//            setName("AsyncGen-" + i);
//            setDaemon(true);
//        }
//        @Override
//        public void run(){
//            while (true){
//                SubMultiStructure.usages = 0;
//                while (SubMultiStructure.maxUsages > SubMultiStructure.usages &&
//                        lastTickTime+3000>System.currentTimeMillis()
//                        && buffer.hasNext()) {
//                    try {
//                        buffer.flush().run();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//                SubMultiStructure.usages = 0;
//                try {
//                    Thread.sleep(20);
//                }catch (Exception e){}
//            }
//        }
//    }

//
//    public static class GenThread extends Thread{
//        public final Buffer<Runnable> buffer = new Buffer<>();
//        public GenThread(int i){
//            setName("AsyncGen-"+i);
//            setDaemon(true);
//        }
//
//        @Override
//        public void start(){
//            super.start();
//        }
//
//        @Override
//        public void run() {
//
//        }
//    }
//
    //public static final Buffer<Runnable> buffer = new Buffer<>();
//    public static void tick(){
//        while (true) {
//            if (lastTickTime + 3000 > System.currentTimeMillis()
//                    && buffer.hasNext()) {
//                Runnable r = buffer.flush();
//                if (r != null) {
//                    try {
//                        r.run();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            } else {
//                try {Thread.sleep(20);
//                } catch (InterruptedException ignored) {}
//            }
//        }
//    }
    //private static int cur = 0;
//    public static  void addAsyncTask(Runnable r){
//        buffer.put(r);
//        //threads[cur].buffer.put(r);
//        //if(++cur>=threads.length)cur=0;
//    }


    private static int calculateGenerationHeight(World world, int x, int z, Block[] topBlock) {
        int y = startTrace;
        boolean foundGround = false;
        while(!foundGround && y-- >= 0){
            Block block = world.getBlockState(new BlockPos(x,y,z)).getBlock();
            foundGround = contains(block, topBlock);
        }
        return y;
    }


    private static int calculateGenerationHeight(World world, int x, int z) {
        int y = startTrace;
        boolean foundGround = false;
        while(!foundGround && y-- >= 0){
            foundGround = world.getBlockState(new BlockPos(x,y,z)).getBlock() != Blocks.AIR;
        }
        return y;
    }


    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world,
                         IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        //chunkProvider.
        proxy.gen(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
    }


    private static boolean contains(Object v, Object[] values){
        if(values==null)return true;
        for(Object o:values){
            if(o==v)return true;
        }
        return false;
    }


    public static void gen(Structure structure,
                           World world, Random rand, int chunkX, int chunkZ){
        if(structure.spawnChance>1){
            float locChance = structure.spawnChance;
            while (locChance>1){
                gen(structure, world, rand, chunkX, chunkZ,1);
                locChance-=1;
            }
            gen(structure,world,rand,chunkX,chunkZ,locChance);
        } else {
            gen(structure,world,rand,chunkX,chunkZ,structure.spawnChance);
        }
    }


    private static void gen(Structure structure,
                     World world, Random rand, int chunkX, int chunkZ, float chance){

        int x = (chunkX * 16) + rand.nextInt(15);
        int z = (chunkZ * 16) + rand.nextInt(15);

        if(world.getWorldType() == WorldType.FLAT) {
            if(world.getDifficulty() == EnumDifficulty.PEACEFUL)return;
            if (rand.nextFloat()<chance) {
                structure.generate(world, x, calculateGenerationHeight(world, x, z), z);
            }
        } else {
            int y = calculateGenerationHeight(world, x, z, structure.spawnAt);
            if(structure.underGroundSpawn){
                y = rand.nextInt(y-8)+4;
            }
            BlockPos pos = new BlockPos(x,y,z);

            if (structure.biomeChecker.check(world.provider.getBiomeForCoords(pos))) {
                if (rand.nextFloat()<chance) {
                    structure.generate(world, x, y, z);
                }
            }
        }
    }

    //private static long lastTickTime = System.currentTimeMillis();
    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent ev){
        //lastTickTime = System.currentTimeMillis();
        SubMultiStructure.usages = 0;
        while (SubMultiStructure.maxUsages > SubMultiStructure.usages
                && buffer.hasNext()) {
            try {
                buffer.flush().run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        SubMultiStructure.usages = 0;
    }
}