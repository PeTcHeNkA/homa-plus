package plus.fserver.mixins.pf;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;


@Mixin(Block.class)
public class BlockPatch {
    @Shadow protected static ThreadLocal<Boolean> captureDrops;
    @Shadow protected static ThreadLocal<NonNullList<ItemStack>> capturedDrops;


    /**
     * @author Te4hno
     * @reason Speedy
     */
    @Overwrite
    public static void spawnAsEntity(World worldIn, BlockPos pos, ItemStack stack) {
        // do not drop items while restoring blockstates, prevents item dupe
        if (!worldIn.isRemote && !stack.isEmpty() && worldIn.getGameRules().doTileDrops.getBoolean() && !worldIn.restoringBlockSnapshots) {
            if (captureDrops.get()) {
                capturedDrops.get().add(stack);
                return;
            }
            double d0 = (double)(worldIn.rand.nextFloat() * 0.5F) + 0.25D;
            double d1 = (double)(worldIn.rand.nextFloat() * 0.5F) + 0.25D;
            double d2 = (double)(worldIn.rand.nextFloat() * 0.5F) + 0.25D;
            EntityItem entityitem = new EntityItem(worldIn, (double)pos.getX() + d0, (double)pos.getY() + d1, (double)pos.getZ() + d2, stack);
            entityitem.setDefaultPickupDelay();
            worldIn.spawnEntity(entityitem);
        }
    }


    /**
     * @author Te4hno
     * @reason Speedy
     */
    @Overwrite
    public void dropXpOnBlockBreak(World worldIn, BlockPos pos, int amount) {
        if (!worldIn.isRemote && worldIn.getGameRules().doTileDrops.getBoolean()) {
            while (amount > 0) {
                int i = EntityXPOrb.getXPSplit(amount);
                amount -= i;
                worldIn.spawnEntity(new EntityXPOrb(worldIn, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, i));
            }
        }
    }
//
//
//    /**
//     * @author Te4hno
//     * @reason Speedy
//     * todo maybe unstabile
//     */
//    @Overwrite
//    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
//        return false;
//        //return !this.material.blocksMovement();
//    }
}