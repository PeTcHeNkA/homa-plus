package plus.common.mashine.block;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import plus.common.block.SpBasicTE;
import plus.common.mashine.block.scan.BlockScanner;
import plus.common.mashine.block.scan.BlockWay;
import plus.common.mashine.inv.InvTileEntity;
import plus.common.mashine.oxy.SpStationOxyChargerTileEntity;
import javax.annotation.Nullable;


public class SpStationOxy extends SpBasicTE {



    protected SpStationOxy() {
        super("sp_sta_oxy");
    }


    @Nullable
    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new SpStationOxyChargerTileEntity();
    }


    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            TileEntity tileentity = world.getTileEntity(pos);
            if (tileentity instanceof InvTileEntity) {
                NonNullList<ItemStack> items = ((InvTileEntity) tileentity).stacks;
                for(int i=0;i<items.size()-1;i++){
                    spawnAsEntity(world, pos, items.get(i));
                }
            }
            if(tileentity instanceof SpStationOxyChargerTileEntity) {
                int liq = ((SpStationOxyChargerTileEntity) tileentity).getLiquidLvl();
                if(liq > 1000) {
                    IBlockState wState;
                    world.setBlockState(pos, wState = Blocks.WATER.getDefaultState());
                    wState.neighborChanged(world, pos, Blocks.WATER, pos);
                }
            }
            world.removeTileEntity(pos);
            scanOnBreak(world, pos);
        }
    }
}