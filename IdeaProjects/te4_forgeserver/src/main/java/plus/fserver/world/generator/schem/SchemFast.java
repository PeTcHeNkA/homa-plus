package plus.fserver.world.generator.schem;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import plus.fserver.world.generator.settings.SettingsFolder;
import plus.fserver.world.generator.vr.MultiChunkCache;
import plus.tson.utl.Tuple;


public class SchemFast extends Schem implements ITSchem{
    SchemFast(Tuple<Stolb[], EntityInfo[]> tuple) {
        super(tuple);
    }


    public static ITSchem read(NBTTagCompound nbt) {
        return new SchemReader().read(nbt).toSchem(SchemFast::new, SchemNormal::valid);
    }


    @Override
    public void placeInGen(MultiChunkCache gen, int x, int y, int z, SettingsFolder settingsFolder){
        if(stolbs.length>0){
            for(Stolb stolb:stolbs){
                int bx = stolb.x - senX + x;
                int bz = stolb.z - senZ + z;
                ChunkPrimer primer = gen.primer(bx>>4, bz>>4);
                bx = bx & 15; bz = bz & 15;
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
            int prewReqChunkX = Integer.MIN_VALUE;
            int prewReqChunkZ = Integer.MIN_VALUE;

            Chunk chunk = null;
            BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

            for(Stolb stolb:stolbs){
				int bx = stolb.x - senX + x;
                int bz = stolb.z - senZ + z;
                int reqX = bx>>4;
                int reqZ = bz>>4;
                if(reqX != prewReqChunkX || reqZ != prewReqChunkZ){
                    prewReqChunkX = reqX;
                    prewReqChunkZ = reqZ;

                    chunk = world.getChunk(reqX, reqZ);
                }
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
        return "++"+super.toString();
    }
}