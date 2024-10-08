package plus.fserver.world.generator.schem;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import plus.fserver.world.generator.settings.SettingsFolder;
import plus.fserver.world.generator.vr.MultiChunkCache;
import plus.tson.utl.Tuple;


public class SchemInchunk extends Schem{
    SchemInchunk(Tuple<Stolb[], EntityInfo[]> tuple) {
        super(tuple);
        if(super.senX >7 || super.senZ >7)throw new RuntimeException("Schem in chunk should me lower 16x16 blocks!");
    }


    public static ITSchem read(NBTTagCompound nbt) {
        return new SchemReader().read(nbt).toSchem(SchemInchunk::new, SchemNormal::valid);
    }


    @Override//TODO баг
    public void placeInGen(MultiChunkCache gen, int x, int y, int z, SettingsFolder settingsFolder){
        if(stolbs.length>0){
            ChunkPrimer primer = gen.primer(x>>4, z>>4);
            x = x & 15; z = z & 15;
            int summ;
            if((summ = x+ senX) > 15) x = 15-summ + x;
            if((summ = z+ senZ) > 15) z = 15-summ + z;

            for(Stolb stolb:stolbs){
                int bx = stolb.x + x;
                int bz = stolb.z + z;

                for(StolbInfo inf:stolb.blocks){
                    int by = y + inf.y;
                    if(by<255 && by>0){
                        primer.setBlockState(bx, by, bz, inf.state);
                    }
                }
            }
        }
    }


    @Override
    public void build(World world, int x, int y, int z, SettingsFolder settingsFolder) {
        if(stolbs.length>0){
            Chunk chunk = world.getChunk(x>>4, z>>4);
            x = x & 15; z = z & 15;
            int summ;
            if((summ = x+ (senX<<1)) > 15) x = 15-summ + x;
            if((summ = z+ (senZ<<1)) > 15) z = 15-summ + z;

            BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

            for(Stolb stolb:stolbs){
                int bx = stolb.x + x;
                int bz = stolb.z + z;

                for(StolbInfo inf:stolb.blocks){
                    int by = y + inf.y;
                    if(by<255 && by>0){
                        mut.setPosB(bx, by, bz);
                        chunk.setBlockState(mut, inf.state);
                    }
                }
            }
        }
    }


    @Override
    public String toString() {
        return "[]"+super.toString();
    }
}