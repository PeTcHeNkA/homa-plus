package plus.fserver.mixins.pf;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.fserver.utls.ChunkSelector;
import java.util.List;


@Mixin(EntityItem.class)
public abstract class EntityItemPatch extends Entity {
    @Shadow public abstract ItemStack getItem();
    @Shadow protected abstract boolean combineItems(EntityItem other);
    @Shadow private int age;

    public EntityItemPatch(World worldIn) {
        super(worldIn);
    }

    /**
     * @author Te4hno
     * @reason boost
     */
    @Overwrite
    private void searchForOtherItemsNearby() {
        if((age>>2 & 3) != 3)return;
        final ItemStack stack = this.getItem();
        if(stack.getCount()>=stack.getMaxStackSize())return;
        for (EntityItem entityitem : this.world.getEntitiesWithinAABB(EntityItem.class,
                this.getEntityBoundingBox().grow(0.73D, 0.0D, 0.73D))) {
            this.combineItems(entityitem);
        }
    }


    @Inject(method = "combineItems(Lnet/minecraft/entity/item/EntityItem;)Z", at = @At("HEAD"), cancellable = true)
    private void combineItems(EntityItem other, CallbackInfoReturnable<Boolean> info) {
        final ItemStack stack = this.getItem();
        if (stack.getCount() >= stack.getMaxStackSize()) {
            info.setReturnValue(false);
        }
    }


    @Override
    public void move(MoverType type, double x, double y, double z) {
        if (this.noClip) {
            this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, y, z));
            this.resetPositionToBB();
        } else {
            if (this.isInWeb) {
                this.isInWeb = false;
                x *= 0.25D;
                y *= 0.05000000074505806D;
                z *= 0.25D;
                this.motionX = 0.0D;
                this.motionY = 0.0D;
                this.motionZ = 0.0D;
            }

            double oldX = x;
            double oldY = y;
            double oldZ = z;

            List<AxisAlignedBB> boxes = null;

            if (y != 0.0D) {
                int k = 0;
                boxes = this.world.getCollisionBoxes(null,
                                this.getEntityBoundingBox().expand(x, y, z)
                        );

                for (int l = boxes.size(); k < l; ++k) {
                    y = boxes.get(k).calculateYOffset(this.getEntityBoundingBox(), y);
                }
                this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, y, 0.0D));
            }

            if (x != 0.0D) {
                if(boxes == null) boxes = this.world.getCollisionBoxes(null,
                        this.getEntityBoundingBox().expand(x, y, z)
                );

                int j5 = 0;
                for (int l5 = boxes.size(); j5 < l5; ++j5) {
                    x = boxes.get(j5).calculateXOffset(this.getEntityBoundingBox(), x);
                }
                if (x != 0.0D) {
                    this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, 0.0D, 0.0D));
                }
            }

            if (z != 0.0D) {
                if(boxes == null) boxes = this.world.getCollisionBoxes(null,
                        this.getEntityBoundingBox().expand(x, y, z)
                );

                int k5 = 0;

                for (int i6 = boxes.size(); k5 < i6; ++k5) {
                    z = boxes.get(k5).calculateZOffset(this.getEntityBoundingBox(), z);
                }
                if (z != 0.0D) {
                    this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, 0.0D, z));
                }
            }
            if(boxes != null) this.resetPositionToBB();

            this.collidedHorizontally = oldX != x || oldZ != z;
            this.collidedVertically = oldY != y;
            this.onGround = this.collidedVertically && oldY < 0.0D;
            this.collided = this.collidedHorizontally || this.collidedVertically;

            int curX = MathHelper.floor(this.posX);
            int curY = MathHelper.floor(this.posY - 0.20000000298023224D);
            int curZ = MathHelper.floor(this.posZ);

            Chunk chunk = this.world.getChunk(curX >> 4, curZ >> 4);
            IBlockState state = chunk.getBlockState(curX, curY, curZ);

            if(onGround) {
                if(fallDistance>0) {
                    BlockPos blockpos;
                    if (state.getMaterial() == Material.AIR) {
                        IBlockState iblockstate1 = chunk.getBlockState(curX, curY - 1, curZ);
                        Block block1 = iblockstate1.getBlock();
                        if (block1 instanceof BlockFence || block1 instanceof BlockWall || block1 instanceof BlockFenceGate) {
                            state = iblockstate1;
                            blockpos = new BlockPos(curX, curY - 1, curZ);
                        } else {
                            blockpos = new BlockPos(curX, curY, curZ);
                        }
                    } else {
                        blockpos = new BlockPos(curX, curY, curZ);
                    }
                    state.getBlock().onFallenUpon(this.world, blockpos, this, fallDistance);
                }
                fallDistance = 0;
            } else {
                this.fallDistance = (float)((double)this.fallDistance - y);
            }

            if (oldX != x) {
                this.motionX = 0.0D;
            }

            if (oldZ != z) {
                this.motionZ = 0.0D;
            }

            Block block = state.getBlock();

            if (oldY != y) {
                block.onLanded(this.world, this);
            }

            try {
                this.doBlockCollisions();
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Checking entity block collision");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being checked for collision");
                this.addEntityCrashInfo(crashreportcategory);
                throw new ReportedException(crashreport);
            }

            if (this.world.isFlammableWithin(this.getEntityBoundingBox().shrink(0.001D))) {
                this.dealFireDamage(1);
                if (!this.isWet()) this.setFire(8);
            }
        }
    }


    @Override
    protected boolean pushOutOfBlocks(double x, double y, double z) {
        if(--rideCooldown<0)return false;

        if (!this.world.collidesWithAnyBlock(this.getEntityBoundingBox())) {
            rideCooldown = 5;
            return false;
        } else {
            motionY += 0.1;
            rideCooldown = 0;

            int bX = MathHelper.floor(x);
            int bY = MathHelper.floor(y);
            int bZ = MathHelper.floor(z);
            double dX = x - bX;
            double dY = y - bY;
            double dZ = z - bZ;

            double d3 = Double.MAX_VALUE;

            EnumFacing enumfacing = EnumFacing.UP;
            ChunkSelector selector = new ChunkSelector(world);
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(bX-1, bY, bZ);

            if (!selector.isBlockFullCube(pos) && dX < d3) {
                d3 = dX;
                enumfacing = EnumFacing.WEST;
            }

            if (!selector.isBlockFullCube(pos.setPos(bX+1, bY, bZ)) && 1.0D - dX < d3) {
                d3 = 1.0D - dX;
                enumfacing = EnumFacing.EAST;
            }

            if (!selector.isBlockFullCube(pos.setPos(bX, bY, bZ-1)) && dZ < d3) {
                d3 = dZ;
                enumfacing = EnumFacing.NORTH;
            }

            if (!selector.isBlockFullCube(pos.setPos(bX, bY, bZ+1)) && 1.0D - dZ < d3) {
                d3 = 1.0D - dZ;
                enumfacing = EnumFacing.SOUTH;
            }

            if (!selector.isBlockFullCube(pos.setPos(bX, bY+1, bZ)) && 1.0D - dY < d3) {
                enumfacing = EnumFacing.UP;
            }

            float f = this.rand.nextFloat() * 0.2F + 0.1F;
            float f1 = (float)enumfacing.getAxisDirection().getOffset();

            if (enumfacing.getAxis() == EnumFacing.Axis.X) {
                this.motionX = f1 * f;
                this.motionY *= 0.75D;
                this.motionZ *= 0.75D;
            } else if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
                this.motionX *= 0.75D;
                this.motionY = f1 * f;
                this.motionZ *= 0.75D;
            } else if (enumfacing.getAxis() == EnumFacing.Axis.Z) {
                this.motionX *= 0.75D;
                this.motionY *= 0.75D;
                this.motionZ = f1 * f;
            }
            return true;
        }
    }
}