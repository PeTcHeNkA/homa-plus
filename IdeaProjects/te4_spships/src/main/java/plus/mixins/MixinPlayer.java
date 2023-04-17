package plus.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
//import net.minecraft.entity.player.InventoryPlayer;
//import net.minecraft.util.DamageSource;
//import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.common.player.PlayerSpship;
import plus.common.spship.SpShip;

import java.util.Random;


@Mixin(EntityPlayer.class)
public abstract class MixinPlayer extends EntityLivingBase implements PlayerSpship{
    private static final Random random = new Random();
    private static final DataParameter<Integer> SPSHIP_ID =
            EntityDataManager.createKey(EntityPlayer.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> SPSHIP_ACTIVE =
            EntityDataManager.createKey(EntityPlayer.class, DataSerializers.BOOLEAN);

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


    @Shadow public abstract void onDeath(DamageSource cause);

    public MixinPlayer(World worldIn) {
        super(worldIn);
    }


    private SpShip spShip;
    //protected boolean isSpship = false;
    private boolean updated = false;


    @Override
    public boolean isSpshipMode(){
        return this.getDataManager().get(SPSHIP_ACTIVE);
    }


    @Override
    public SpShip getSp(){
        return spShip;
    }


    @Override
    public int getSpModelId(){
        return this.getDataManager().get(SPSHIP_ID);
    }


    @Override
    public void setSpModelId(int id){
        this.getDataManager().set(SPSHIP_ID, id);
        //System.out.println(getSpModelId());
    }


    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(World worldIn, GameProfile gameProfileIn, CallbackInfo ci){
        spShip = new SpShip(this);
    }


    @Inject(method = "entityInit", at = @At("HEAD"))
    protected void entityInit(CallbackInfo ci) {
        this.getDataManager().register(SPSHIP_ID, -1);
        this.getDataManager().register(SPSHIP_ACTIVE, false);
    }


    @Override
    public void setSpMode(boolean mode){
        //isSpship = mode;
        if(mode){
            if(!updated){
                this.getDataManager().set(SPSHIP_ACTIVE, true);
                onEnableSp();
            }
            updated = true;
        } else {
            if(updated){
                this.getDataManager().set(SPSHIP_ACTIVE, false);
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


    @Override
    public void shoot(float yaw, float pith) {
        if(isSpshipMode()) {
            spShip.shoot(this, yaw, pith);
        }
    }


    @Inject(method = "attackEntityFrom", at = @At("HEAD"), cancellable = true)
    public void attackEntityFrom0(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(onAttack(source,amount));
    }


    public boolean onAttack(DamageSource source, float amount) {
//        if (!net.minecraftforge.common.ForgeHooks.onPlayerAttack(this, source, amount))
        if (!(amount < 0.0f) && !this.capabilities.disableDamage && !this.isEntityInvulnerable(source)) {
            this.idleTime = 0;
            if (this.getHealth() > 0.0F) {

                if(world.isRemote) {
                    return false;
                }
                if(isSpshipMode()){//может быть медленно
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

            //if (!this.checkTotemDeathProtection(source)) {
            SoundEvent soundevent = this.getDeathSound();

            if (flag1 && soundevent != null) {
                this.playSound(soundevent, this.getSoundVolume(), this.getSoundPitch());
            }

            this.onDeathM(source);
            //}
        } else if (flag1) {
            this.playHurtSound(source);
        }

        if (isPlayerMp(this)) {
            trigger(this, source, amount, flag);
        }

        return !flag;
    }

    //this.damageEntity
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


    @Inject(method = "updateSize", at = @At("HEAD"), cancellable = true)
    protected void updateSize(CallbackInfo ci) {
        ci.cancel();
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

                //setSize();
            }
        }
    }


    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    public void onDeath(DamageSource cause, CallbackInfo ci) {
        ci.cancel();
        onDeathM(cause);
    }


    public void onDeathM(DamageSource cause){
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