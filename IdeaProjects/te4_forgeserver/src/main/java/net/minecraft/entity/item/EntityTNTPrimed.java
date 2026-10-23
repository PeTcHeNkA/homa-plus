package net.minecraft.entity.item;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import plus.fserver.ElcaCV;

import static net.minecraft.entity.EMoveHelper.COMMON_FUTURE;


public class EntityTNTPrimed extends Entity {
    private static final DataParameter<Integer> FUSE = EntityDataManager.createKey(EntityTNTPrimed.class, DataSerializers.VARINT);
    @Nullable
    private EntityLivingBase tntPlacedBy;
    private int fuse;
    private float strength = 4f;

    public EntityTNTPrimed(World world) {
        super(world);
        this.fuse = 80;
        this.preventEntitySpawning = true;
        this.isImmuneToFire = true;
        this.setSize(0.98F, 0.98F);
    }


    public EntityTNTPrimed(World world, double x, double y, double z, EntityLivingBase cause) {
        this(world);
        this.mutPosition(x, y, z);
        float rNum = (float)(world.rand.nextDouble() * 6.2831854820251465);
        this.motionX = -((float)Math.sin(rNum)) * 0.02F;
        this.motionY = 0.20000000298023224;
        this.motionZ = -((float)Math.cos(rNum)) * 0.02F;
        this.setFuse(80);
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
        this.tntPlacedBy = cause;
    }


    @Override
    protected void entityInit() {
        this.dataManager.register(FUSE, 80);
    }


    @Override
    public boolean enableMoveAccelerator() {
        return true;
    }


    @Override
    protected boolean canTriggerWalking() {
        return false;
    }


    @Override
    public boolean canBeCollidedWith() {
        return !this.isDead;
    }


    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (!this.hasNoGravity()) {
            this.motionY -= 0.03999999910593033;
        }
        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ, COMMON_FUTURE);
        --this.fuse;
        if (this.fuse <= 0) {
            this.setDead();
            if (!this.world.isRemote || ElcaCV.SERVER_COMPILE) {
                this.explode();
            }
        }
    }


    @Override
    public void completeCommon(){
        this.motionX *= 0.9800000190734863;
        this.motionY *= 0.9800000190734863;
        this.motionZ *= 0.9800000190734863;
        if (this.onGround) {
            this.motionX *= 0.699999988079071;
            this.motionZ *= 0.699999988079071;
            this.motionY *= -0.5;
        }
        if(!isDead){
            this.handleWaterMovement();
            this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + 0.5, this.posZ, 0.0, 0.0, 0.0);
        }
    }


    protected void explode() {
//        if(!world.isRemote){
//            ((WorldServer)world).explosionAsy(this, this.posX, this.posY + (double)(this.height / 16.0F), this.posZ, strength, false,  true);
//        } else {
        this.world.createExplosion(this, this.posX, this.posY + (double)(this.height / 16.0F), this.posZ, strength, true);
//        }
    }


    public void setStrength(float strength){
        this.strength = strength;
    }


    public float getStrength(){
        return strength;
    }


    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setShort("Fuse", (short)this.getFuse());
        if(strength != 4f){
            nbt.setFloat("Strength", strength);
        }
    }


    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        this.setFuse(nbt.getShort("Fuse"));
        if(nbt.hasKey("Strength"))
            strength = nbt.getFloat("Strength");
    }


    @Nullable
    public EntityLivingBase getTntPlacedBy() {
        return this.tntPlacedBy;
    }


    @Override
    public float getEyeHeight() {
        return 0.0F;
    }


    public void setFuse(int fuse) {
        this.dataManager.set(FUSE, fuse);
        this.fuse = fuse;
    }


    public void notifyDataManagerChange(DataParameter<?> key) {
        if (FUSE.equals(key)) {
            this.fuse = this.getFuseDataManager();
        }
    }


    public int getFuseDataManager() {
        return this.dataManager.get(FUSE);
    }


    public int getFuse() {
        return this.fuse;
    }
}