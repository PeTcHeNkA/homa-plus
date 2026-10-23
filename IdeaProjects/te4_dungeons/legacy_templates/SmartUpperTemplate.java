package plus.gen.legacy_templates;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import plus.tson.TsonMap;


public class SmartUpperTemplate extends SmartTemplate{
    private final int limit;
    public SmartUpperTemplate(NBTTagCompound nbt, TsonMap map) {
        super(nbt);
        final int[] lim = {10};
        map.ifContainsInt("limit", integer -> lim[0] = integer);
        limit = lim[0];
    }


    protected void onPlace(World world, BlockPos blockpos, VTemplate.BlockInfo blockInfo,
                           IBlockState state, int y, int curY){
        Chunk ch = world.getChunk(blockpos);
        ch.setBlockState(blockpos, state);

        if(blockInfo.y == size.getY()){
            int bx = blockpos.getX();
            int bz = blockpos.getZ();
            ++curY;
            int maxY = 0;
            while (maxY<limit && curY<255 && mayBeReplaced(ch.getBlockState(bx,curY,bz).getBlock())){
                BlockPos pos = new BlockPos(bx,curY,bz);
                ch.setBlockState(pos,state);
                ++curY;
                ++maxY;
            }
        }
    }
}