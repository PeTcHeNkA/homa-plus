package plus.gen.sch_v3;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import plus.gen.schem.ITSchem;
import plus.gen.settings.SettingsFolder;
import plus.tson.utl.Tuple;
import java.util.Random;


public class SchemFast extends Schem implements ITSchem {


    SchemFast(Tuple<BL<BL<StolbInfo>>[], EntityInfo[]> tuple) {
        super(tuple);
    }

    public static Schem read(NBTTagCompound nbt) {
        return new SchemReader().read(nbt).toSchem(SchemFast::new, Schem::valid);
    }


    //@Override
    public void build(World world, Chunk chunk, int x, int y, int z, SettingsFolder settingsFolder, Random rand) {

        int prewReqChunkX = Integer.MIN_VALUE;
        int prewReqChunkZ = Integer.MIN_VALUE;
        //Chunk chunk = null;

        for(BL<BL<StolbInfo>> xLine:lines){
            int bx = xLine.align + x;
            int reqX = bx>>4;
            boolean flag = reqX != prewReqChunkX;
            for(BL<StolbInfo> zLine: xLine.items){
                int bz = zLine.align + z;
                int reqZ = bz>>4;

                if(flag || reqZ != prewReqChunkZ){
                    prewReqChunkX = reqX;
                    prewReqChunkZ = reqZ;
                    chunk = world.getChunk(reqX, reqZ);
                    flag = false;
                }
                for (StolbInfo info: zLine.items){
                    int by = y + info.y;
                    if(by<255 && by>0){
                        chunk.setBlockState(new BlockPos(bx, by, bz), info.state);
                    }
                }
            }
        }
    }

    public void buildAtChunk(World world, Chunk chunk, int x, int y, int z, int shiftX, int shiftY){


    }


}