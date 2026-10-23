package plus.gen.schem;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import plus.gen.settings.SettingsFolder;
import plus.tson.utl.Tuple;
import java.util.Random;


public class SchemFast extends Schem implements ITSchem{
    SchemFast(Tuple<Stolb[], EntityInfo[]> tuple) {
        super(tuple);
    }


    public static Schem read(NBTTagCompound nbt) {
        return new SchemReader().read(nbt).toSchem(SchemFast::new, SchemNormal::valid);
    }


    @Override
    public void build(World world, int x, int y, int z, SettingsFolder settingsFolder, Random rand) {
        if(stolbs.length>0){
            int prewReqChunkX = Integer.MIN_VALUE;
            int prewReqChunkZ = Integer.MIN_VALUE;

            Chunk chunk = null;

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
                for(StolbInfo inf:stolb.blocks){
                    int by = y + inf.y;
                    if(by<255 && by>0){
                        chunk.setBlockState(new BlockPos(bx, by, bz), inf.state);
                    }
                }
            }
        }
    }
}