package plus.gen.legacy_templates;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.gen.schem.ITSchem;
import plus.gen.settings.SettingsFolder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class FastTemplate extends McTemplate implements ITSchem {

    public FastTemplate(NBTTagCompound nbt) {
        super(nbt);
        onPostInit(this);
    }


    private static void onPostInit(FastTemplate t){
        List<BlockInfo> list = new ArrayList<>(Arrays.asList(t.blocks));
        for(int i=0;i<list.size();i++){
            BlockInfo cur = list.get(i);
            Block block = list.get(i).blockState.getBlock();
            if(block == Blocks.AIR || block == Blocks.STRUCTURE_BLOCK){
                list.remove(cur);
            }
        }
        if(list.size()!=t.blocks.length){
            t.blocks = list.toArray(new BlockInfo[0]);
        }
    }


    @Override
    public void build(World world, int x, int y, int z, SettingsFolder settings, Random rand) {
        for (VTemplate.BlockInfo blockInfo : this.blocks) {
            int by = y + blockInfo.y;
            if(by<255 && by>0){
                BlockPos blockpos = new BlockPos(x+blockInfo.x, by, z+blockInfo.z);
                world.getChunk(blockpos).setBlockState(blockpos, blockInfo.blockState);
            }
        }
    }
}










