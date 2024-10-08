package plus.fserver.world.generator.schem;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import plus.fserver.world.generator.settings.SettingsFolder;
import plus.fserver.world.generator.vr.MultiChunkCache;
import plus.fserver.utls.math.FastRandom;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;


public class SchemFrame implements ITSchem{
    public final Stolb[] stolbs;
    private final IBlockState[] states;


    public static ITSchem read(NBTTagCompound nbt) {
        return new SchemFrame(new SchemReader().read(nbt).toSchem(SchemFast::new, SchemNormal::valid));
    }


    public SchemFrame(Schem parent) {
        ArrayList<IBlockState> states0 = new ArrayList<>();
        this.stolbs = new Stolb[parent.stolbs.length];
        for(int i=0;i<stolbs.length;i++){
            stolbs[i] = new Stolb(parent.stolbs[i], states0);
        }
        this.states = states0.toArray(new IBlockState[0]);
    }


    @Override
    public void placeInGen(MultiChunkCache gen, int x, int y, int z, SettingsFolder settingsFolder){
        if(stolbs.length>0){
            for(Stolb stolb:stolbs){
                int bx = stolb.x + x;
                int bz = stolb.z + z;
                ChunkPrimer primer = gen.primer(bx>>4, bz>>4);
                for(int ys0 :stolb.ys){
                    int by = y + ys0;
                    if(by<255 && by>0){
                        primer.setBlockState(bx, by, bz, states[FastRandom.INST.nextInt(states.length)]);
                    }
                }
            }
        }
    }


    @Override
    public void build(World world, int x, int y, int z, SettingsFolder settingsFolder) {
        if(stolbs.length>0){
            int prewReqChunkX = Integer.MIN_VALUE;
            int prewReqChunkZ = Integer.MIN_VALUE;

            Chunk chunk = null;
            BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
            IBlockState[] states = this.states;
            Random rand = world.rand;

            for(Stolb stolb:stolbs){
                int bx = stolb.x + x;
                int bz = stolb.z + z;
                int reqX = bx>>4;
                int reqZ = bz>>4;
                if(reqX != prewReqChunkX || reqZ != prewReqChunkZ){
                    prewReqChunkX = reqX;
                    prewReqChunkZ = reqZ;

                    chunk = world.getChunk(reqX, reqZ);
                }
                for(int ys0 :stolb.ys){
                    int by = y + ys0;
                    if(by<255 && by>0){
                        mut.setPosB(bx, by, bz);
                        chunk.setBlockState(mut, states[rand.nextInt(states.length)]);
                    }
                }
            }
        }
    }


    private static class Stolb{
        public final int[] ys;
        public final int x;
        public final int z;
        Stolb(Schem.Stolb parent, ArrayList<IBlockState> states) {
            this.x = parent.x;
            this.z = parent.z;
            ys = new int[parent.blocks.length];
            for(int i=0;i<ys.length;i++){
                ys[i] = parent.blocks[i].y;
                states.add(parent.blocks[i].state);
            }
        }
    }


    @Override
    public String toString() {
        for(Map.Entry<String,ITSchem> entry:Schem.cache.entrySet()){
            if(entry.getValue() == this)return "##("+entry.getKey()+")";
        }
        int count = 0;
        for(Stolb s:stolbs) for(int ignored :s.ys) ++count;
        return "##(UNNAMED "+count+" BLOCKS)";
    }
}