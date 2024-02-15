package plus.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.common.player.PlayerSpship;
import plus.common.spship.SpShip;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Mixin(EntityPlayer.class)
public abstract class MixinPlayer extends EntityLivingBase implements PlayerSpship{
    private static final Random random = new Random();
    private static final DataParameter<Integer> SPSHIP_ID =
            EntityDataManager.createKey(EntityPlayer.class, DataSerializers.VARINT);
//    private static final DataParameter<Boolean> SPSHIP_ACTIVE =
//            EntityDataManager.createKey(EntityPlayer.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> SPSHIP_SHIELD =
            EntityDataManager.createKey(EntityPlayer.class, DataSerializers.FLOAT);

    //private static final DataParameter<Integer> test;

    @Shadow
    public PlayerCapabilities capabilities;
    @Shadow
    public InventoryPlayer inventory;


    @Shadow
    public abstract void takeStat(StatBase timeSinceDeath);
    @Shadow
    public abstract void addStat(StatBase deaths);
    @Shadow
    public abstract boolean isSpectator();
    @Shadow
    protected abstract void destroyVanishingCursedItems();
    @Shadow
    public abstract void wakeUpPlayer(boolean b, boolean b1, boolean b2);
    @Shadow
    protected abstract void spawnShoulderEntities();


//    @Shadow public abstract void onDeath(DamageSource cause);

    @Shadow protected int flyToggleTimer;

    @Shadow public float prevCameraYaw;

    @Shadow public float cameraYaw;

    @Shadow protected float speedInAir;

//    @Shadow public abstract void onLivingUpdate();

    @Shadow protected abstract void collideWithPlayer(Entity entityIn);

    @Shadow protected abstract void playShoulderEntityAmbientSound(@Nullable NBTTagCompound p_192028_1_);

    @Shadow public abstract NBTTagCompound getLeftShoulderEntity();

    @Shadow public abstract NBTTagCompound getRightShoulderEntity();

    @Shadow public abstract void addMovementStat(double p_71000_1_, double p_71000_3_, double p_71000_5_);

    public MixinPlayer(World worldIn) {
        super(worldIn);
    }


    private SpShip spShip;
    //protected boolean isSpship = false;
    private boolean updated = false;


    @Override
    public boolean isSpshipMode(){
        return this.getDataManager().get(SPSHIP_ID) >= 0;
        //return this.getDataManager().get(SPSHIP_ACTIVE);
    }


    @Override
    public SpShip getSp(){
        return spShip;
    }


    @Override
    public float getSpShield() {
        return this.getDataManager().get(SPSHIP_SHIELD);
    }


    public void setSpShield(float shield){
        this.getDataManager().set(SPSHIP_SHIELD, shield);
    }


    @Override
    public int getSpModelId(){
        return this.getDataManager().get(SPSHIP_ID);
    }


    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(World worldIn, GameProfile gameProfileIn, CallbackInfo ci){
        spShip = new SpShip(this);
    }


    @Inject(method = "entityInit", at = @At("HEAD"))
    protected void entityInit(CallbackInfo ci) {
        this.getDataManager().register(SPSHIP_ID, -1);
        this.getDataManager().register(SPSHIP_SHIELD, 100f);
    }


    @Override
    public void setSpMode(boolean mode){
        if(mode){
            if(!updated){
                spShip.calcParams();
                this.getDataManager().set(SPSHIP_ID, spShip.modelId);
                onEnableSp();
            }
            updated = true;
        } else {
            if(updated){
                this.getDataManager().set(SPSHIP_ID, -1);
                onDisableSp();
            }
            updated = false;
        }
    }


    @Override
    public void onEnableSp(){
        capabilities.allowFlying = true;
        capabilities.isFlying = true;
    }


    @Override
    public void onDisableSp(){
        capabilities.allowFlying = false;
        capabilities.isFlying = false;
    }


    @Override
    public void setSpHitbox(){
        setSize(2,2);
    }


    @Override
    public void restoreSpHitbox(){
        setSize(0.6f, 1.8f);
    }

    private final ArrayList<float[]> shoots = new ArrayList<>(5);
    @Override
    public void shoot(float yaw, float pith) {
        if(isSpshipMode() && shoots.size()<5){
            shoots.add(new float[]{yaw, pith});
        }
    }


    private float spSpeed = 0.0f;
    @Override
    public float getSpSpeed() {
        return spSpeed;
    }


    /**
     * @author Te4hno
     * @reason add Sp
     */
    @Overwrite
    public void onLivingUpdate(){
        if (this.flyToggleTimer > 0) {
            --this.flyToggleTimer;
        }

        this.inventory.decrementAnimations();
        this.prevCameraYaw = this.cameraYaw;
        super.onLivingUpdate();
        IAttributeInstance iattributeinstance = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);

        if (!this.world.isRemote) {
            iattributeinstance.setBaseValue(this.capabilities.getWalkSpeed());
        }

        this.jumpMovementFactor = this.speedInAir;

        boolean isSprinting = this.isSprinting();
        if(isSpshipMode()){
            if(!shoots.isEmpty()) {
                float[] vec = shoots.remove(0);
                spShip.shoot(vec[0], vec[1]);
            }
            if(isSprinting){
                spSpeed = getSp().accelerate(spSpeed);
                capabilities.setFlySpeed(0.07f * spSpeed);
            } else {
                spSpeed = 1.0f;
                capabilities.setFlySpeed(0.07f);
            }
            spShip.onTick();
        } else {
            capabilities.setFlySpeed(0.05f);
            spSpeed = 1.0f;
        }

        if (isSprinting) {
            this.jumpMovementFactor = (float)((double)this.jumpMovementFactor
                    + (double)this.speedInAir * 0.3D);
        }

        this.setAIMoveSpeed((float)iattributeinstance.getAttributeValue());
        float f, f1;

        if(isDead){
            f = f1 = 0.0f;
        } else {
            if (this.onGround) {
                f1 = 0.0f;
                f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
                if (f > 0.1F) {
                    f = 0.1F;
                }
            } else {
                f = 0.0f;
                f1 = (float)(Math.atan(-this.motionY * 0.20000000298023224D) * 15.0D);
            }
        }

        this.cameraYaw += (f - this.cameraYaw) * 0.4F;
        this.cameraPitch += (f1 - this.cameraPitch) * 0.8F;

        if (!isDead && !this.isSpectator()) {
            AxisAlignedBB axisalignedbb;

            if (this.isRiding() && !this.getRidingEntity().isDead) {
                axisalignedbb = this.getEntityBoundingBox().union(this.getRidingEntity().getEntityBoundingBox()).grow(1.0D, 0.0D, 1.0D);
            } else {
                axisalignedbb = this.getEntityBoundingBox().grow(1.0D, 0.5D, 1.0D);
            }

            List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(
                    this, axisalignedbb);

            for (int i = 0; i < list.size(); ++i) {
                Entity entity = list.get(i);

                if (!entity.isDead) {
                    this.collideWithPlayer(entity);
                }
            }
        }

        this.playShoulderEntityAmbientSound(this.getLeftShoulderEntity());
        this.playShoulderEntityAmbientSound(this.getRightShoulderEntity());

        if (!this.world.isRemote && (this.fallDistance > 0.5F ||
                this.isInWater() || this.isRiding()) || this.capabilities.isFlying) {
            this.spawnShoulderEntities();
        }
    }


//    /**
//     * @author Te4hno
//     * @reason add Sp
//     */
//    @Overwrite
//    public void travel(float strafe, float vertical, float forward) {
//        double d0 = this.posX;
//        double d1 = this.posY;
//        double d2 = this.posZ;
//
//        if (this.capabilities.isFlying && !this.isRiding()) {
//            double d3 = this.motionY;
//            float f = this.jumpMovementFactor;
//            this.jumpMovementFactor = this.capabilities.getFlySpeed() * (float)(this.isSprinting() ? 2 : 1);
//
//            if(isSpshipMode()){
//                travel0(strafe, vertical, forward);
//            } else {
//                super.travel(strafe, vertical, forward);
//            }
//
//            this.motionY = d3 * 0.6D;
//            this.jumpMovementFactor = f;
//            this.fallDistance = 0.0F;
//            this.setFlag(7, false);
//        } else {
//            super.travel(strafe, vertical, forward);
//        }
//        this.addMovementStat(this.posX - d0, this.posY - d1, this.posZ - d2);
//
//    }
//
//    private void travel0(float strafe, float vertical, float forward) {
//        float f6 = 0.91F;
//        BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(this.posX, this.getEntityBoundingBox().minY - 1.0D, this.posZ);
//
//        if (this.onGround) {
//            IBlockState underState = this.world.getBlockState(pos);
//            f6 = underState.getBlock().getSlipperiness(underState, this.world, pos, this) * 0.91F;
//        }
//
//        float f7 = 0.16277136F / (f6 * f6 * f6);
//        float f8;
//
//        if (this.onGround) {
//            f8 = this.getAIMoveSpeed() * f7;
//        } else {
//            f8 = this.jumpMovementFactor;
//        }
//
//        this.moveRelative(strafe, vertical, forward, f8);
//        f6 = 0.91F;
//
//        if (this.onGround) {
//            IBlockState underState = this.world.getBlockState(pos.setPos(this.posX, this.getEntityBoundingBox().minY - 1.0D, this.posZ));
//            f6 = underState.getBlock().getSlipperiness(underState, this.world, pos, this) * 0.91F;
//        }
//
//        double prev = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
//
//        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
//
//        if (this.collidedHorizontally) {
//            double d11 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
//            System.out.println("NEN! w"+world.isRemote+";"+prev+"->"+d11);
//            double d3 = prev - d11;
//            float f5 = (float)(d3 * 10.0D - 3.0D);
//
//            if (f5 > 0.0F) {
//                this.playSound(this.getFallSound((int)f5), 1.0F, 1.0F);
//                this.attackEntityFrom(DamageSource.FLY_INTO_WALL, f5);
//            }
//        }
//
//        pos.setPos(this.posX, 0.0D, this.posZ);
//
//        if (!this.world.isRemote || this.world.isBlockLoaded(pos) && this.world.getChunk(pos).isLoaded()) {
//            if (!this.hasNoGravity()) {
//                this.motionY -= 0.08D;
//            }
//           } else if (this.posY > 0.0D) {
//            this.motionY = -0.1D;
//        } else {
//            this.motionY = 0.0D;
//        }
//
//        this.motionY *= 0.9800000190734863D;
//        this.motionX *= f6;
//        this.motionZ *= f6;
//        pos.release();
//    }
//

    /**
     * @author Te4hno
     * @reason add Sp
     */
    @Overwrite
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (!(amount < 0.0f) && !this.capabilities.disableDamage && !this.isEntityInvulnerable(source)) {
            this.idleTime = 0;
            if (this.getHealth() > 0.0F) {
                if(world.isRemote) {
                    return false;
                }
                if(isSpshipMode()){//TODO может быть медленно
                    spShip.onDamage(source, amount);
                    return false;
                }
                if (this.isPlayerSleeping() && !this.world.isRemote) {
                    this.wakeUpPlayer(true, true, false);
                }
                this.spawnShoulderEntities();
                if(source.isFireDamage() && this.isPotionActive(MobEffects.FIRE_RESISTANCE)){
                    return false;
                }
                return spAttackEntityFrom(source, amount);
            }
        }
        return false;
    }


    public boolean spAttackEntityFrom(DamageSource source, float amount) {
        boolean flag;

        if (this.canBlockDamageSource(source)) {
            this.damageShield(amount);
            amount = 0.0F;

            if (!source.isProjectile()) {
                Entity entity = source.getImmediateSource();

                if (entity instanceof EntityLivingBase) {
                    this.blockUsingShield((EntityLivingBase)entity);
                }
            }
            flag = true;
        } else {
            flag = false;
            ItemStack headItem = this.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
            if ((source == DamageSource.ANVIL || source == DamageSource.FALLING_BLOCK)
                    && !headItem.isEmpty()) {
                headItem.damageItem((int)(amount * 4.0F + this.rand.nextFloat() * amount * 2.0F), this);
                amount *= 0.75F;
            }
        }

        this.limbSwingAmount = 1.5F;
        boolean flag1;

        if (this.hurtResistantTime > this.maxHurtResistantTime / 2.0F) {
            if (amount <= this.lastDamage) {
                return false;
            }
            damagePl(source, amount - this.lastDamage);

            this.lastDamage = amount;
            flag1 = false;
        } else {
            flag1 = true;
            this.lastDamage = amount;
            this.hurtResistantTime = this.maxHurtResistantTime;
            damagePl(source, amount);
            this.maxHurtTime = 10;
            this.hurtTime = 10;
        }

        this.attackedAtYaw = 0.0F;
        Entity entity1 = source.getTrueSource();

        if (entity1 != null) {
            if (entity1 instanceof EntityLivingBase) {
                this.setRevengeTarget((EntityLivingBase)entity1);

                if (entity1 instanceof EntityPlayer) {
                    this.recentlyHit = 100;
                    this.attackingPlayer = (EntityPlayer)entity1;

                    if (entity1 instanceof EntityPlayerMP) {
                        CriteriaTriggers.PLAYER_HURT_ENTITY.trigger((EntityPlayerMP)entity1,
                                this, source, amount, amount, flag);
                    }
                } else if (entity1 instanceof EntityTameable) {
                    EntityTameable wolf = (EntityTameable)entity1;

                    if (wolf.isTamed()) {
                        this.recentlyHit = 100;
                        this.attackingPlayer = null;
                    }
                }
            }
        }

        if (flag1) {
            if (flag) {
                world.setEntityState(this, (byte)29);
            } else if (source instanceof EntityDamageSource &&
                    ((EntityDamageSource)source).getIsThornsDamage()) {
                world.setEntityState(this, (byte)33);
            } else {
                byte b0;

                if (source == DamageSource.DROWN) {
                    b0 = 36;
                } else if (source.isFireDamage()) {
                    b0 = 37;
                    markVelocityChanged();
                } else {
                    b0 = 2;
                    markVelocityChanged();
                }
                world.setEntityState(this, b0);
            }

            if (entity1 != null) {
                double d1 = entity1.posX - this.posX;
                double d0 = entity1.posZ - this.posZ;

                this.attackedAtYaw = (float)
                        (MathHelper.atan2(d0, d1) * (180d / Math.PI) - this.rotationYaw);
                this.knockBack(entity1, 0.4f, d1, d0);
            } else {
                this.attackedAtYaw = random.nextFloat() * 360;
            }
        }

        if (this.getHealth() <= 0.0F) {
            SoundEvent soundevent = this.getDeathSound();

            if (flag1 && soundevent != null) {
                this.playSound(soundevent, this.getSoundVolume(), this.getSoundPitch());
            }

            this.onDeath(source);
        } else if (flag1) {
            this.playHurtSound(source);
        }

        if (isPlayerMp(this)) {
            trigger(this, source, amount, flag);
        }

        return !flag;
    }


    private void damagePl(DamageSource src, float damage)    {
        damage = this.applyPotionDamageCalculations(
                src, this.applyArmorCalculations(src, damage)
        );
        float f = damage;
        float abs = this.getAbsorptionAmount();
        if(abs>0){
            damage = damage - abs;
            if(damage>0) {
                this.setAbsorptionAmount(abs - (f - damage));
                float f1 = this.getHealth();
                this.getCombatTracker().trackDamage(src, f1, damage);
                this.setHealth(f1 - damage);
                this.setAbsorptionAmount(this.getAbsorptionAmount() - damage);
            } else {
                this.setAbsorptionAmount(abs - f);
            }
        } else {
            float f1 = this.getHealth();
            this.getCombatTracker().trackDamage(src, f1, damage);
            this.setHealth(f1 - damage);
        }
    }


    private boolean canBlockDamageSource(DamageSource damageSourceIn) {
        if (!damageSourceIn.isUnblockable() && this.isActiveItemStackBlocking()) {
            Vec3d vec3d = damageSourceIn.getDamageLocation();
            if (vec3d != null) {
                Vec3d vec3d1 = this.getLook(1.0F);
                Vec3d vec3d2 = vec3d.subtractReverse(new Vec3d(this.posX, this.posY, this.posZ)).normalize();
                vec3d2 = new Vec3d(vec3d2.x, 0.0D, vec3d2.z);

                return vec3d2.dotProduct(vec3d1) < 0.0D;
            }
        }
        return false;
    }


    public boolean isPlayerMp(Entity e){
        return e instanceof EntityPlayerMP;
    }


    private void trigger(Entity player, DamageSource source, float amount, boolean wasBlocked){
        CriteriaTriggers.ENTITY_HURT_PLAYER.trigger((EntityPlayerMP)player, source, amount, amount, wasBlocked);
    }


    /**
     * @author Te4hno
     * @reason add Sp
     */
    @Overwrite
    protected void updateSize() {
        float f;
        float f1;

        if(isSpshipMode()){
            f = 2f;
            f1 = 2f;
        } else {
            if (this.isElytraFlying()) {
                f = 0.6F;
                f1 = 0.6F;
            } else if (this.isPlayerSleeping()) {
                f = 0.2F;
                f1 = 0.2F;
            } else if (this.isSneaking()) {
                f = 0.6F;
                f1 = 1.65F;
            } else {
                f = 0.6F;
                f1 = 1.8F;
            }
        }

        if (f != this.width || f1 != this.height) {
            AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
            axisalignedbb = new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ,
                    axisalignedbb.minX + (double)f,
                    axisalignedbb.minY + (double)f1,
                    axisalignedbb.minZ + (double)f);

            if (!this.world.collidesWithAnyBlock(axisalignedbb)) {
                this.setEntityBoundingBox(axisalignedbb);
                this.height = f1;

                if (this.width > f && !this.firstUpdate && !this.world.isRemote) {
                    this.move(MoverType.SELF, f - this.width, 0.0D, f - this.width);
                }
                this.width = f;
            }
        }
    }


    /**
     * @author Te4hno
     * @reason add Sp
     */
    @Overwrite
    public void onDeath(DamageSource cause){
        super.onDeath(cause);
        this.setSize(0.2F, 0.2F);
        this.setPosition(this.posX, this.posY, this.posZ);
        this.motionY = 0.1;

        captureDrops = true;
        capturedDrops.clear();

        if (!this.isSpectator()) {
            this.destroyVanishingCursedItems();
            this.inventory.dropAllItems();
        }

        captureDrops = false;

        if (cause != null) {
            this.motionX = -MathHelper.cos((this.attackedAtYaw + this.rotationYaw) * 0.017453292F) * 0.1F;
            this.motionZ = -MathHelper.sin((this.attackedAtYaw + this.rotationYaw) * 0.017453292F) * 0.1F;
        } else {
            this.motionX = 0.0D;
            this.motionZ = 0.0D;
        }

        this.addStat(StatList.DEATHS);
        this.takeStat(StatList.TIME_SINCE_DEATH);
        this.extinguish();
        this.setFlag(0, false);
        setSpMode(false);
    }
}