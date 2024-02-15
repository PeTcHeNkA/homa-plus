package plus.fserver.mixins.md;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import javax.annotation.Nullable;


@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBasePatch extends Entity {
    @Shadow public abstract ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn);
    @Shadow protected int ticksElytraFlying;
    @Shadow protected int ticksSinceLastSwing;
    @Shadow public abstract boolean isServerWorld();
    @Shadow protected abstract SoundEvent getFallSound(int heightIn);
    @Shadow public float jumpMovementFactor;
    @Shadow public abstract float getAIMoveSpeed();
    @Shadow public abstract boolean isPotionActive(Potion potionIn);
    @Shadow @Nullable public abstract PotionEffect getActivePotionEffect(Potion potionIn);
    @Shadow protected abstract float getWaterSlowDown();

    public EntityLivingBasePatch(World worldIn) {
        super(worldIn);
    }

    /**
     * @author Te4hno
     * @reason speedy
     */
    @Overwrite
    public boolean isElytraFlying() {
        return false;
    }


    /**
     * @author Te4hno
     * @reason speedy
     */
    @Overwrite //todo check Elytra
    private void updateElytra() {
        boolean flag = isElytraFlying();
        if (flag && !this.onGround && !this.isRiding()) {
            ItemStack itemstack = this.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            if (itemstack.getItem() == Items.ELYTRA && ItemElytra.isUsable(itemstack)) {
                if (!this.world.isRemote && (this.ticksElytraFlying + 1) % 20 == 0) {
                    itemstack.damageItem(1, (EntityLivingBase)(Object)this);
                }
            } else {
                flag = false;
            }
            if (!this.world.isRemote) {
                this.setFlag(7, flag);
            }
        } else {
            if (!this.world.isRemote && ticksSinceLastSwing != 0) {
                this.setFlag(7, false);
            }
        }
    }


//    /**
//     * @author Te4hno
//     * @reason speedy
//     */
//    @Overwrite
//    public boolean isOnLadder() {
//        Object inst = this;
//        if (inst instanceof EntityPlayer && ((EntityPlayer)inst).isSpectator()) return false;
//        int i = MathHelper.floor(this.posX);
//        int j = MathHelper.floor(this.getEntityBoundingBox().minY);
//        int k = MathHelper.floor(this.posZ);
//
//        BlockPos pos = new BlockPos(i, j, k);
//        IBlockAccess access = world.isRemote?world:((WorldServer)world).localSelector;
//        IBlockState state = access.getBlockState(pos);
//
//        if (!ForgeModContainer.fullBoundingBoxLadders) {
//            return state.getBlock().isLadder(state, access, pos, (EntityLivingBase)inst);
//        } else {
//            AxisAlignedBB bb = this.getEntityBoundingBox();
//            int mX = MathHelper.floor(bb.minX);
//            int mY = MathHelper.floor(bb.minY);
//            int mZ = MathHelper.floor(bb.minZ);
//            for (int y2 = mY; y2 < bb.maxY; y2++) {
//                for (int x2 = mX; x2 < bb.maxX; x2++) {
//                    for (int z2 = mZ; z2 < bb.maxZ; z2++) {
//                        state = access.getBlockState(pos = new BlockPos(x2, y2, z2));
//                        if (state.getBlock().isLadder(state, access, pos, (EntityLivingBase)inst)) {
//                            return true;
//                        }
//                    }
//                }
//            }
//            return false;
//        }
//    }


//    public void travel(float strafe, float vertical, float forward) {
//        if (this.isServerWorld() || this.canPassengerSteer()) {
//            EntityLivingBase inst = (EntityLivingBase) (Object)this;
//
//            boolean isFly = inst instanceof EntityPlayer && ((EntityPlayer)inst).capabilities.isFlying;
//
//            if (!this.isInWater() || isFly) {
//                if (!this.isInLava() || isFly) {
//                    if (this.isElytraFlying()) {
//                        if (this.motionY > -0.5D) {
//                            this.fallDistance = 1.0F;
//                        }
//
//                        Vec3d vec3d = this.getLookVec();
//                        float f = this.rotationPitch * 0.017453292F;
//                        double d6 = Math.sqrt(vec3d.x * vec3d.x + vec3d.z * vec3d.z);
//                        double d8 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
//                        double d1 = vec3d.length();
//                        float f4 = MathHelper.cos(f);
//                        f4 = (float)((double)f4 * (double)f4 * Math.min(1.0D, d1 / 0.4D));
//                        this.motionY += -0.08D + (double)f4 * 0.06D;
//
//                        if (this.motionY < 0.0D && d6 > 0.0D) {
//                            double d2 = this.motionY * -0.1D * (double)f4;
//                            this.motionY += d2;
//                            this.motionX += vec3d.x * d2 / d6;
//                            this.motionZ += vec3d.z * d2 / d6;
//                        }
//
//                        if (f < 0.0F) {
//                            double d10 = d8 * (double)(-MathHelper.sin(f)) * 0.04D;
//                            this.motionY += d10 * 3.2D;
//                            this.motionX -= vec3d.x * d10 / d6;
//                            this.motionZ -= vec3d.z * d10 / d6;
//                        }
//
//                        if (d6 > 0.0D) {
//                            this.motionX += (vec3d.x / d6 * d8 - this.motionX) * 0.1D;
//                            this.motionZ += (vec3d.z / d6 * d8 - this.motionZ) * 0.1D;
//                        }
//
//                        this.motionX *= 0.9900000095367432D;
//                        this.motionY *= 0.9800000190734863D;
//                        this.motionZ *= 0.9900000095367432D;
//                        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
//
//                        if (this.collidedHorizontally && !this.world.isRemote)
//                        {
//                            double d11 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
//                            double d3 = d8 - d11;
//                            float f5 = (float)(d3 * 10.0D - 3.0D);
//
//                            if (f5 > 0.0F)
//                            {
//                                this.playSound(this.getFallSound((int)f5), 1.0F, 1.0F);
//                                this.attackEntityFrom(DamageSource.FLY_INTO_WALL, f5);
//                            }
//                        }
//
//                        if (this.onGround && !this.world.isRemote)
//                        {
//                            this.setFlag(7, false);
//                        }
//                    }
//                    else {
//                        float f6 = 0.91F;
//                        BlockPos.PooledMutableBlockPos blockPos = BlockPos.PooledMutableBlockPos.retain(this.posX, this.getEntityBoundingBox().minY - 1.0D, this.posZ);
//
//
//                        if (this.onGround) {
//                            IBlockState underState = this.world.getBlockState(blockPos);
//                            f6 = underState.getBlock().getSlipperiness(underState, this.world, blockPos, this) * 0.91F;
//                        }
//
//                        float f7 = 0.16277136F / (f6 * f6 * f6);
//                        float f8;
//
//                        if (this.onGround) {
//                            f8 = this.getAIMoveSpeed() * f7;
//                        } else {
//                            f8 = this.jumpMovementFactor;
//                        }
//
//                        this.moveRelative(strafe, vertical, forward, f8);
//                        f6 = 0.91F;
//
//                        if (this.onGround) {
//                            IBlockState underState = this.world.getBlockState(blockPos.setPos(this.posX, this.getEntityBoundingBox().minY - 1.0D, this.posZ));
//                            f6 = underState.getBlock().getSlipperiness(underState, this.world, blockPos, this) * 0.91F;
//                        }
//
//                        if (this.isOnLadder()) {
//                            this.motionX = MathHelper.clamp(this.motionX, -0.15000000596046448D, 0.15000000596046448D);
//                            this.motionZ = MathHelper.clamp(this.motionZ, -0.15000000596046448D, 0.15000000596046448D);
//                            this.fallDistance = 0.0F;
//
//                            if (this.motionY < -0.15D) {
//                                this.motionY = -0.15D;
//                            }
//
//                            boolean flag = this.isSneaking() && inst instanceof EntityPlayer;
//                            if (flag && this.motionY < 0.0D) {
//                                this.motionY = 0.0D;
//                            }
//                        }
//
//                        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
//
//                        if (this.collidedHorizontally && this.isOnLadder()) {
//                            this.motionY = 0.2D;
//                        }
//
//                        if (this.isPotionActive(MobEffects.LEVITATION)) {
//                            this.motionY += (0.05D * (double)(this.getActivePotionEffect(MobEffects.LEVITATION).getAmplifier() + 1) - this.motionY) * 0.2D;
//                        } else {
//                            blockPos.setPos(this.posX, 0.0D, this.posZ);
//
//                            if (!this.world.isRemote || this.world.isBlockLoaded(blockPos) && this.world.getChunk(blockPos).isLoaded())
//                            {
//                                if (!this.hasNoGravity())
//                                {
//                                    this.motionY -= 0.08D;
//                                }
//                            }
//                            else if (this.posY > 0.0D)
//                            {
//                                this.motionY = -0.1D;
//                            }
//                            else
//                            {
//                                this.motionY = 0.0D;
//                            }
//                        }
//
//                        this.motionY *= 0.9800000190734863D;
//                        this.motionX *= f6;
//                        this.motionZ *= f6;
//                        blockPos.release();
//                    }
//                }
//                else
//                {
//                    double d4 = this.posY;
//                    this.moveRelative(strafe, vertical, forward, 0.02F);
//                    this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
//                    this.motionX *= 0.5D;
//                    this.motionY *= 0.5D;
//                    this.motionZ *= 0.5D;
//
//                    if (!this.hasNoGravity())
//                    {
//                        this.motionY -= 0.02D;
//                    }
//
//                    if (this.collidedHorizontally && this.isOffsetPositionInLiquid(this.motionX, this.motionY + 0.6000000238418579D - this.posY + d4, this.motionZ))
//                    {
//                        this.motionY = 0.30000001192092896D;
//                    }
//                }
//            }
//            else
//            {
//                double d0 = this.posY;
//                float f1 = this.getWaterSlowDown();
//                float f2 = 0.02F;
//                float f3 = (float) EnchantmentHelper.getDepthStriderModifier(this);
//
//                if (f3 > 3.0F)
//                {
//                    f3 = 3.0F;
//                }
//
//                if (!this.onGround)
//                {
//                    f3 *= 0.5F;
//                }
//
//                if (f3 > 0.0F)
//                {
//                    f1 += (0.54600006F - f1) * f3 / 3.0F;
//                    f2 += (this.getAIMoveSpeed() - f2) * f3 / 3.0F;
//                }
//
//                this.moveRelative(strafe, vertical, forward, f2);
//                this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
//                this.motionX *= f1;
//                this.motionY *= 0.800000011920929D;
//                this.motionZ *= f1;
//
//                if (!this.hasNoGravity())
//                {
//                    this.motionY -= 0.02D;
//                }
//
//                if (this.collidedHorizontally && this.isOffsetPositionInLiquid(this.motionX, this.motionY + 0.6000000238418579D - this.posY + d0, this.motionZ))
//                {
//                    this.motionY = 0.30000001192092896D;
//                }
//            }
//        }
//
//        this.prevLimbSwingAmount = this.limbSwingAmount;
//        double d5 = this.posX - this.prevPosX;
//        double d7 = this.posZ - this.prevPosZ;
//        double d9 = this instanceof net.minecraft.entity.passive.EntityFlying ? this.posY - this.prevPosY : 0.0D;
//        float f10 = MathHelper.sqrt(d5 * d5 + d9 * d9 + d7 * d7) * 4.0F;
//
//        if (f10 > 1.0F)
//        {
//            f10 = 1.0F;
//        }
//
//        this.limbSwingAmount += (f10 - this.limbSwingAmount) * 0.4F;
//        this.limbSwing += this.limbSwingAmount;
//    }

}