package net.minecraft.entity.passive;

import java.util.Calendar;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import plus.fserver.ElcaMobs;


public class EntityBat extends EntityAmbientCreature {
    private static final DataParameter<Byte> HANGING = EntityDataManager.createKey(EntityBat.class, DataSerializers.BYTE);
    private BlockPos spawnPosition;

    public EntityBat(World worldIn) {
        super(worldIn);
        this.setSize(0.5F, 0.9F);
        this.setIsBatHanging(true);
    }


    @Override
    public boolean allowSprint(){
        return false;
    }


    @Override
    protected void entityInit() {
        this.dataManager.registerU(HANGING, (byte) 0);
        super.entityInit();
    }


    @Override
    protected float getSoundVolume() {
        return 0.1F;
    }


    @Override
    protected float getSoundPitch() {
        return super.getSoundPitch() * 0.95F;
    }


    @Nullable
    @Override
    public SoundEvent getAmbientSound() {
        return this.getIsBatHanging() && this.rand.nextInt(4) != 0 ? null : SoundEvents.ENTITY_BAT_AMBIENT;
    }


    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_BAT_HURT;
    }


    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_BAT_DEATH;
    }


    @Override
    public boolean canBePushed()
    {
        return false;
    }


    @Override
    protected void collideWithEntity(Entity entityIn) {}


    @Override
    public boolean useSelfCollisions() {
        return true;
    }


    @Override
    public boolean ignoreCollisions(){
        return true;
    }


    @Override
    protected void collideWithNearbyEntities() {}


    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(6.0D);
    }


    public boolean getIsBatHanging() {
        return (this.dataManager.get(HANGING) & 1) != 0;
    }


    public void setIsBatHanging(boolean isHanging) {
        byte b0 = this.dataManager.get(HANGING);
        if (isHanging) this.dataManager.set(HANGING, (byte) (b0 | 1));
        else this.dataManager.set(HANGING, (byte) (b0 & -2));
    }


    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.getIsBatHanging()) {
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
            this.posY = (double)MathHelper.floor(this.posY) + 1.0D - (double)this.height;
        } else {
            this.motionY *= 0.6000000238418579D;
        }
    }


    @Override
    protected void updateAITasks() {
        super.updateAITasks();
        BlockPos blockpos = new BlockPos(this);
        BlockPos blockpos1 = blockpos.up();

        if (this.getIsBatHanging()) {
            if (this.world.getBlockState(blockpos1).isNormalCube()) {
                if (this.rand.nextInt(200) == 0) {
                    this.rotationYawHead = (float)this.rand.nextInt(360);
                }

                if (this.world.getNearestPlayerNotCreative(this, 4.0D) != null) {
                    this.setIsBatHanging(false);
                    this.world.playEvent((EntityPlayer)null, 1025, blockpos, 0);
                }
            } else {
                this.setIsBatHanging(false);
                this.world.playEvent((EntityPlayer)null, 1025, blockpos, 0);
            }
        } else {
            if (this.spawnPosition != null && (!this.world.isAirBlock(this.spawnPosition) || this.spawnPosition.getY() < 1))
            {
                this.spawnPosition = null;
            }

            if (this.spawnPosition == null || this.rand.nextInt(30) == 0 ||
                    this.spawnPosition.distanceSq((double)((int)this.posX), (double)((int)this.posY), (double)((int)this.posZ)) < 4.0D) {
                this.spawnPosition = new BlockPos((int)this.posX + this.rand.nextInt(7) - this.rand.nextInt(7), (int)this.posY + this.rand.nextInt(6) - 2, (int)this.posZ + this.rand.nextInt(7) - this.rand.nextInt(7));
            }

            double d0 = (double)this.spawnPosition.getX() + 0.5D - this.posX;
            double d1 = (double)this.spawnPosition.getY() + 0.1D - this.posY;
            double d2 = (double)this.spawnPosition.getZ() + 0.5D - this.posZ;
            this.motionX += (Math.signum(d0) * 0.5D - this.motionX) * 0.10000000149011612D;
            this.motionY += (Math.signum(d1) * 0.699999988079071D - this.motionY) * 0.10000000149011612D;
            this.motionZ += (Math.signum(d2) * 0.5D - this.motionZ) * 0.10000000149011612D;
            float f = (float)(MathHelper.atan2(this.motionZ, this.motionX) * (180D / Math.PI)) - 90.0F;
            float f1 = MathHelper.wrapDegrees(f - this.rotationYaw);
            this.moveForward = 0.5F;
            this.rotationYaw += f1;

            if (this.rand.nextInt(100) == 0 && this.world.getBlockState(blockpos1).isNormalCube()) {
                this.setIsBatHanging(true);
            }
        }
    }


    @Override
    protected boolean canTriggerWalking() {
        return false;
    }


    @Override
    public void fall(float distance, float damageMultiplier) {}


    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {}


    @Override
    public boolean doesEntityNotTriggerPressurePlate() {
        return true;
    }


    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        } else {
            if (!this.world.isRemote && this.getIsBatHanging()) {
                this.setIsBatHanging(false);
            }
            return super.attackEntityFrom(source, amount);
        }
    }


    public static void registerFixesBat(DataFixer fixer) {
        EntityLiving.registerFixesMob(fixer, EntityBat.class);
    }


    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        this.dataManager.set(HANGING, Byte.valueOf(nbt.getByte("BatFlags")));
    }


    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        nbt.setByteU("BatFlags", this.dataManager.get(HANGING));
    }


    @Override
    public boolean getCanSpawnHere() {
        BlockPos blockpos = world.localTempPos.setPos(this.posX, this.getEntityBoundingBox().minY, this.posZ); //safe

        if (blockpos.getY() >= this.world.getSeaLevel()) {
            return false;
        } else {
            int i = this.world.getLightFromNeighbors(blockpos);
            int j = 4;

            if (this.isDateAroundHalloween(this.world.getCurrentDate())) {
                j = 7;
            } else if (this.rand.nextBoolean()) {
                return false;
            }
            return i > this.rand.nextInt(j) ? false : super.getCanSpawnHere();
        }
    }


    private boolean isDateAroundHalloween(Calendar calendar) {
        return calendar.get(2) + 1 == 10 && calendar.get(5) >= 20 || calendar.get(2) + 1 == 11 && calendar.get(5) <= 3;
    }


    @Override
    public float getEyeHeight() {
        return this.height * 0.5F;
    }


    @Nullable
    protected ResourceLocation getLootTable() {
        return LootTableList.ENTITIES_BAT;
    }


    @Override
    public int lodLvl() {
        return ElcaMobs.BAT.lodLvl(elcaMinRange);
    }
}