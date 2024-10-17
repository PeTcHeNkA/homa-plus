package net.minecraft.entity.passive;

import java.util.*;
import net.minecraft.world.*;
import net.minecraft.entity.player.*;
import net.minecraft.entity.ai.*;
import javax.annotation.*;
import net.minecraft.util.datafix.*;
import net.minecraft.nbt.*;
import net.minecraft.init.*;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.world.storage.loot.*;
import net.minecraft.entity.effect.*;
import net.minecraft.entity.monster.*;
import net.minecraft.inventory.*;
import net.minecraft.util.math.*;
import net.minecraft.entity.*;
import net.minecraft.network.datasync.*;
import com.google.common.collect.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.ElcaMobs;


public class EntityPig extends EntityAnimal {
    private static final DataParameter<Boolean> SADDLED;
    private static final DataParameter<Integer> BOOST_TIME;
    private static final Set<Item> TEMPTATION_ITEMS;
    private boolean boosting;
    private int boostTime;
    private int totalBoostTime;

    public EntityPig(final World worldIn) {
        super(worldIn);
        this.setSize(0.9f, 0.9f);
    }


    @Override
    protected void initEntityAI() {
        EntityAITasks tasks = this.tasks;
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAIPanic(this, 1.25));
        tasks.addTask(3, new EntityAIMate(this, 1.0));
        tasks.addTask(4, new EntityAITempt(this, 1.2, Items.CARROT_ON_A_STICK, false));
        tasks.addTask(4, new EntityAITempt(this, 1.2, false, (Set)EntityPig.TEMPTATION_ITEMS));
        tasks.addTask(5, new EntityAIFollowParent(this, 1.1));
        tasks.addTask(6, new EntityAIWanderAvoidWater(this, 1.0));
        tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0f));
        tasks.addTask(8, new EntityAILookIdle(this));
    }


    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25);
    }


    @Nullable
    public Entity getControllingPassenger() {
        if (this.getPassengers().isEmpty()) {
            return null;
        }
        return this.getPassengers().get(0);
    }


    @Nullable
    public boolean canBeSteered() {
        final Entity lvt_1_1_ = this.getControllingPassenger();
        if (!(lvt_1_1_ instanceof EntityPlayer)) {
            return false;
        }
        final EntityPlayer lvt_2_1_ = (EntityPlayer)lvt_1_1_;
        return lvt_2_1_.getHeldItemMainhand().getItem() == Items.CARROT_ON_A_STICK || lvt_2_1_.getHeldItemOffhand().getItem() == Items.CARROT_ON_A_STICK;
    }

    public void notifyDataManagerChange(final DataParameter<?> key) {
        if (EntityPig.BOOST_TIME.equals(key) && this.world.isRemote) {
            this.boosting = true;
            this.boostTime = 0;
            this.totalBoostTime = this.dataManager.get(EntityPig.BOOST_TIME);
        }
        super.notifyDataManagerChange((DataParameter)key);
    }

    protected void entityInit() {
        super.entityInit();
        this.dataManager.registerU(EntityPig.SADDLED, false);
        this.dataManager.registerU(EntityPig.BOOST_TIME, 0);
    }

    public static void registerFixesPig(final DataFixer fixer) {
        EntityLiving.registerFixesMob(fixer, (Class)EntityPig.class);
    }

    public void writeEntityToNBT(final NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Saddle", this.getSaddled());
    }

    public void readEntityFromNBT(final NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setSaddled(compound.getBoolean("Saddle"));
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_PIG_AMBIENT;
    }

    protected SoundEvent getHurtSound(final DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_PIG_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_PIG_DEATH;
    }

    protected void playStepSound(final BlockPos pos, final Block blockIn) {
        this.playSound(SoundEvents.ENTITY_PIG_STEP, 0.15f, 1.0f);
    }

    public boolean processInteract(final EntityPlayer player, final EnumHand hand) {
        if (super.processInteract(player, hand)) {
            return true;
        }
        final ItemStack lvt_3_1_ = player.getHeldItem(hand);
        if (lvt_3_1_.getItem() == Items.NAME_TAG) {
            lvt_3_1_.interactWithEntity(player, (EntityLivingBase)this, hand);
            return true;
        }
        if (this.getSaddled() && !this.isBeingRidden()) {
            if (!this.world.isRemote) {
                player.startRiding((Entity)this);
            }
            return true;
        }
        if (lvt_3_1_.getItem() == Items.SADDLE) {
            lvt_3_1_.interactWithEntity(player, (EntityLivingBase)this, hand);
            return true;
        }
        return false;
    }

    public void onDeath(final DamageSource cause) {
        super.onDeath(cause);
        if (this.world.isRemote) {
            return;
        }
        if (this.getSaddled()) {
            this.dropItem(Items.SADDLE, 1);
        }
    }

    @Nullable
    protected ResourceLocation getLootTable() {
        return LootTableList.ENTITIES_PIG;
    }

    public boolean getSaddled() {
        return this.dataManager.get(EntityPig.SADDLED);
    }

    public void setSaddled(final boolean saddled) {
        if (saddled) {
            this.dataManager.set(EntityPig.SADDLED, true);
        }
        else {
            this.dataManager.set(EntityPig.SADDLED, false);
        }
    }

    public void onStruckByLightning(final EntityLightningBolt lightningBolt) {
        if (this.world.isRemote || this.isDead) {
            return;
        }
        final EntityPigZombie lvt_2_1_ = new EntityPigZombie(this.world);
        lvt_2_1_.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
        lvt_2_1_.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
        lvt_2_1_.setNoAI(this.isAIDisabled());
        if (this.hasCustomName()) {
            lvt_2_1_.setCustomNameTag(this.getCustomNameTag());
            lvt_2_1_.setAlwaysRenderNameTag(this.getAlwaysRenderNameTag());
        }
        this.world.spawnEntity((Entity)lvt_2_1_);
        this.setDead();
    }

    public void travel(final float strafe, final float vertical, final float forward) {
        final Entity lvt_4_1_ = this.getPassengers().isEmpty() ? null : this.getPassengers().get(0);
        if (!this.isBeingRidden() || !this.canBeSteered()) {
            this.stepHeight = 0.5f;
            this.jumpMovementFactor = 0.02f;
            super.travel(strafe, vertical, forward);
            return;
        }
        this.rotationYaw = lvt_4_1_.rotationYaw;
        this.prevRotationYaw = this.rotationYaw;
        this.rotationPitch = lvt_4_1_.rotationPitch * 0.5f;
        this.setRotation(this.rotationYaw, this.rotationPitch);
        this.renderYawOffset = this.rotationYaw;
        this.rotationYawHead = this.rotationYaw;
        this.stepHeight = 1.0f;
        this.jumpMovementFactor = this.getAIMoveSpeed() * 0.1f;
        if (this.boosting && this.boostTime++ > this.totalBoostTime) {
            this.boosting = false;
        }
        if (this.canPassengerSteer()) {
            float lvt_5_1_ = (float)this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() * 0.225f;
            if (this.boosting) {
                lvt_5_1_ += lvt_5_1_ * 1.15f * MathHelper.sin(this.boostTime / (float)this.totalBoostTime * 3.1415927f);
            }
            this.setAIMoveSpeed(lvt_5_1_);
            super.travel(0.0f, 0.0f, 1.0f);
        }
        else {
            this.motionX = 0.0;
            this.motionY = 0.0;
            this.motionZ = 0.0;
        }

    }


    @SideOnly(Side.CLIENT)
    protected void updateLimbSwing() {
        this.prevLimbSwingAmount = this.limbSwingAmount;
        final double dx = this.posX - this.prevPosX;
        final double dz = this.posZ - this.prevPosZ;
        float f = MathHelper.sqrt(dx * dx + dz * dz) * 4.0f;
        if (f > 1.0f) {
            f = 1.0f;
        }
        this.limbSwingAmount += (f - this.limbSwingAmount) * 0.4f;
        this.limbSwing += this.limbSwingAmount;
    }


    public boolean boost() {
        if (this.boosting) {
            return false;
        }
        this.boosting = true;
        this.boostTime = 0;
        this.totalBoostTime = this.getRNG().nextInt(841) + 140;
        this.getDataManager().set(EntityPig.BOOST_TIME, this.totalBoostTime);
        return true;
    }


    @Override
    public EntityPig createChild(final EntityAgeable ageable) {
        return new EntityPig(this.world);
    }


    @Override
    public boolean isBreedingItem(final ItemStack stack) {
        return EntityPig.TEMPTATION_ITEMS.contains(stack.getItem());
    }


    @Override
    public int lodLvl() {
        return ElcaMobs.PIG.lodLvl(elcaMinRange);
    }


    static {
        SADDLED = EntityDataManager.createKey(EntityPig.class, DataSerializers.BOOLEAN);
        BOOST_TIME = EntityDataManager.createKey(EntityPig.class, DataSerializers.VARINT);
        TEMPTATION_ITEMS = Sets.newHashSet(Items.CARROT, Items.POTATO, Items.BEETROOT);
    }
}