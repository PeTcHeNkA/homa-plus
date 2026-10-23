package net.minecraft.entity;

import net.minecraft.block.state.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public abstract class EntityFlying extends EntityLiving {
    public EntityFlying(final World worldIn) {
        super(worldIn);
    }

    @Override
    public void fall(final float distance, final float damageMultiplier) {}


    @Override
    protected void updateFallState(final double y, final boolean onGroundIn, final IBlockState state, final BlockPos pos) {}


    @Override
    public void travel(final float strafe, final float vertical, final float forward) {
        if (this.isInWater()) {
            this.moveRelative(strafe, vertical, forward, 0.02f);
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ, () -> {//thread-safe
                this.motionX *= 0.800000011920929;
                this.motionY *= 0.800000011920929;
                this.motionZ *= 0.800000011920929;
            });
        } else if (this.isInLava()) {
            this.moveRelative(strafe, vertical, forward, 0.02f);
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ, () -> {//thread-safe
                this.motionX *= 0.5;
                this.motionY *= 0.5;
                this.motionZ *= 0.5;
            });
        } else {
            World world = this.world;
            float f = 0.91f;
            if (this.onGround) {
                final BlockPos underPos = world.localTempPos.setPos(MathHelper.floor(this.posX), MathHelper.floor(this.getEntityBoundingBox().minY) - 1, MathHelper.floor(this.posZ)); //safe
                final IBlockState underState = world.access().getBlockState(underPos);
                f = underState.getBlock().getSlipperiness(underState, world.access(), underPos, this) * 0.91f;
            }
            final float f2 = 0.16277136f / (f * f * f);
            this.moveRelative(strafe, vertical, forward, this.onGround ? (0.1f * f2) : 0.02f);
            f = 0.91f;
            if (this.onGround) {
                final BlockPos underPos2 = world.localTempPos.setPos(MathHelper.floor(this.posX), MathHelper.floor(this.getEntityBoundingBox().minY) - 1, MathHelper.floor(this.posZ)); //safe
                final IBlockState underState2 = world.access().getBlockState(
                        underPos2
                );
                f = underState2.getBlock().getSlipperiness(underState2, world.access(), underPos2, this) * 0.91f;
            }
            float finalF = f;
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ, () -> {//thread-safe
                this.motionX *= finalF;
                this.motionY *= finalF;
                this.motionZ *= finalF;
            });

        }

    }


    @SideOnly(Side.CLIENT)
    protected void updateLimbSwing() {
        this.prevLimbSwingAmount = this.limbSwingAmount;
        final double d1 = this.posX - this.prevPosX;
        final double d2 = this.posZ - this.prevPosZ;
        float f3 = MathHelper.sqrt(d1 * d1 + d2 * d2) * 4.0f;
        if (f3 > 1.0f) {
            f3 = 1.0f;
        }
        this.limbSwingAmount += (f3 - this.limbSwingAmount) * 0.4f;
        this.limbSwing += this.limbSwingAmount;
    }


    @Override
    public boolean isOnLadder() {
        return false;
    }
}