package plus.common.mashine.block;

import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.common.block.SimpleIronBlock;

import static plus.common.block.SpBasicTE.SCANNER;
import static plus.common.block.SpBasicTE.scanOnBreak;


public class SpMehModule extends SimpleIronBlock {
    public static final SpCraftTable  SP_CRAFT_TABLE  = new SpCraftTable();
    public static final SpMehModule   SP_MEH_MODULE   = new SpMehModule();
    public static final SpFurnace     SP_FURNACE      = new SpFurnace();
    public static final SpReact       SP_REACT        = new SpReact();
    public static final SpPress       SP_PRESS        = new SpPress();
    public static final SpCapacitor   SP_CAPACITOR    = new SpCapacitor();


    protected SpMehModule() {
        super("sp_machine_module");
    }


    public EnumPushReaction getPushReaction(IBlockState state) {
        return EnumPushReaction.BLOCK;
    }


    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state,
                                EntityLivingBase placer, ItemStack stack) {
        if(!worldIn.isRemote) {
            SCANNER.doScan(worldIn, pos);
        }
    }


    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state);
        scanOnBreak(world, pos);
    }
}