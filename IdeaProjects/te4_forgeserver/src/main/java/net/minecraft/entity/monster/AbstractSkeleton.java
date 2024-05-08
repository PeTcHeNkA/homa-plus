package net.minecraft.entity.monster;

import java.util.Calendar;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.ElcaMobs;
import static plus.fserver.ElcaCV.SERVER_COMPILE;


public abstract class AbstractSkeleton extends EntityMob implements IRangedAttackMob {
    private static final DataParameter<Boolean> SWINGING_ARMS = EntityDataManager.createKey(AbstractSkeleton.class, DataSerializers.BOOLEAN);
    private final EntityAIAttackRangedBow<AbstractSkeleton> aiArrowAttack = new EntityAIAttackRangedBow<>(this, 1.0D, 20, 15.0F);
    private final EntityAIAttackMelee aiAttackOnCollide = new EntityAIAttackMelee(this, 1.2D, false) {
        @Override
        public void resetTask() {
            super.resetTask();
            AbstractSkeleton.this.setSwingingArms(false);
        }
        public void startExecuting() {
            super.startExecuting();
            AbstractSkeleton.this.setSwingingArms(true);
        }
    };


    public AbstractSkeleton(World worldIn) {
        super(worldIn);
        this.setSize(0.6F, 1.99F);
        this.setCombatTask();
    }


    protected void initEntityAI() {
        EntityAITasks tasks = this.tasks;
        tasks.addTask(1, new EntityAISwimming(this));
        tasks.addTask(2, new EntityAIRestrictSun(this));
        tasks.addTask(3, new EntityAIFleeSun(this, 1.0D));
        tasks.addTask(3, new EntityAIAvoidEntity<>(this, EntityWolf.class, 6.0F, 1.0D, 1.2D));
        tasks.addTask(5, new EntityAIWanderAvoidWater(this, 1.0D));
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(6, new EntityAILookIdle(this));

        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntityIronGolem.class, true));
    }


    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }


    @Override
    protected void entityInit() {
        this.dataManager.register(SWINGING_ARMS, Boolean.FALSE);
        super.entityInit();
    }


    @Override
    protected void playStepSound(BlockPos pos, Block blockIn) {
        this.playSound(this.getStepSound(), 0.15F, 1.0F);
    }

    protected abstract SoundEvent getStepSound();


    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.UNDEAD;
    }


    @Override
    public void onLivingUpdateTick() {
        if (this.world.isDaytime() && (!this.world.isRemote || SERVER_COMPILE)) {
            float f = this.getBrightness();

            if (f > 0.5F && this.rand.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && this.getRidingEntity() instanceof EntityBoat ?
                    (this.world.canSeeSky(MathHelper.floor(this.posX),MathHelper.floor(this.posY+1),MathHelper.floor(this.posZ))):(this.world.canSeeSky(this))) {
                boolean flag = true;
                ItemStack stack = this.getItemStackFromSlot(EntityEquipmentSlot.HEAD);

                if (!stack.isEmpty()) {
                    if (stack.isItemStackDamageable()) {
                        stack.setItemDamage(stack.getItemDamage() + this.rand.nextInt(2));

                        if (stack.getItemDamage() >= stack.getMaxDamage()) {
                            this.renderBrokenItemStack(stack);
                            this.setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemStack.EMPTY);
                        }
                    }
                    flag = false;
                }
                if (flag) this.setFire(8);
            }
        }
        super.onLivingUpdateTick();
    }


    @Override
    public void updateRidden() {
        super.updateRidden();

        if (this.getRidingEntity() instanceof EntityCreature) {
            EntityCreature entitycreature = (EntityCreature)this.getRidingEntity();
            this.renderYawOffset = entitycreature.renderYawOffset;
        }
    }


    @Override
    protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
        super.setEquipmentBasedOnDifficulty(difficulty);
        this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
    }



    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        this.setEquipmentBasedOnDifficulty(difficulty);
        this.setEnchantmentBasedOnDifficulty(difficulty);
        this.setCombatTask();
        this.setCanPickUpLoot(this.rand.nextFloat() < 0.55F * difficulty.getClampedAdditionalDifficulty());

        if (this.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty()) {
            Calendar calendar = this.world.getCurrentDate();

            if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31 && this.rand.nextFloat() < 0.25F) {
                this.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(this.rand.nextFloat() < 0.1F ? Blocks.LIT_PUMPKIN : Blocks.PUMPKIN));
                this.inventoryArmorDropChances[EntityEquipmentSlot.HEAD.getIndex()] = 0.0F;
            }
        }
        return livingdata;
    }


    public void setCombatTask() {
        if (this.world != null && (!this.world.isRemote || SERVER_COMPILE)) {
            this.tasks.removeTask(this.aiAttackOnCollide);
            this.tasks.removeTask(this.aiArrowAttack);
            ItemStack itemstack = this.getHeldItemMainhand();

            if (itemstack.getItem() instanceof net.minecraft.item.ItemBow) {
                int i = 20;

                if (this.world.getDifficulty() != EnumDifficulty.HARD) {
                    i = 40;
                }

                this.aiArrowAttack.setAttackCooldown(i);
                this.tasks.addTask(4, this.aiArrowAttack);
            } else {
                this.tasks.addTask(4, this.aiAttackOnCollide);
            }
        }
    }


    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
        EntityArrow entityarrow = this.getArrow(distanceFactor);
        if (this.getHeldItemMainhand().getItem() instanceof net.minecraft.item.ItemBow)
            entityarrow = ((net.minecraft.item.ItemBow) this.getHeldItemMainhand().getItem()).customizeArrow(entityarrow);
        double d0 = target.posX - this.posX;
        double d1 = target.getEntityBoundingBox().minY + (double)(target.height / 3.0F) - entityarrow.posY;
        double d2 = target.posZ - this.posZ;
        double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
        entityarrow.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, (float)(14 - this.world.getDifficulty().getId() * 4));
        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
        this.world.spawnEntity(entityarrow);
    }



    protected EntityArrow getArrow(float f) {
        EntityTippedArrow arrow = new EntityTippedArrow(this.world, this);
        arrow.setEnchantmentEffectsFromEntity(this, f);
        return arrow;
    }


    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setCombatTask();
    }


    @Override
    public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack) {
        super.setItemStackToSlot(slotIn, stack);

        if ((!this.world.isRemote || SERVER_COMPILE) && slotIn == EntityEquipmentSlot.MAINHAND) {
            this.setCombatTask();
        }
    }


    @Override
    public float getEyeHeight() {
        return 1.74F;
    }


    @Override
    public double getYOffset() {
        return -0.6D;
    }


    @SideOnly(Side.CLIENT)
    public boolean isSwingingArms() {
        return this.dataManager.get(SWINGING_ARMS);
    }


    @Override
    public void setSwingingArms(boolean swingingArms) {
        this.dataManager.set(SWINGING_ARMS, swingingArms);
    }


    @Override
    public int lodLvl() {
        return ElcaMobs.SKELETON.lodLvl(elcaMinRange);
    }
}
