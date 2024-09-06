package net.minecraft.entity.passive;

import net.minecraft.entity.player.*;
import com.google.common.base.*;
import net.minecraft.entity.ai.*;
import net.minecraft.util.datafix.*;
import net.minecraft.nbt.*;
import javax.annotation.*;
import net.minecraft.world.storage.loot.*;
import net.minecraft.util.*;
import net.minecraftforge.event.*;
import net.minecraft.item.*;
import net.minecraft.util.math.*;
import net.minecraft.init.*;
import net.minecraft.block.state.*;
import net.minecraft.block.*;
import net.minecraft.util.text.translation.*;
import net.minecraft.world.*;
import net.minecraft.entity.*;
import net.minecraft.network.datasync.*;
import plus.fserver.ElcaMobs;
import plus.fserver.entity.ai.EntityAIWatchClosestPlayer;


public class EntityOcelot extends EntityTameable {
    private static final DataParameter<Integer> OCELOT_VARIANT;
    private EntityAIAvoidEntity<EntityPlayer> avoidEntity;
    private EntityAITempt aiTempt;

    public EntityOcelot(final World worldIn) {
        super(worldIn);
        this.setSize(0.6f, 0.7f);
    }


    @Override
    protected void initEntityAI() {
        EntityAITasks tasks = this.tasks;

        tasks.addTask(1 , new EntityAISwimming(this));
        tasks.addTask(2 , aiSit = new EntityAISit(this));
        tasks.addTask(3 , aiTempt = new EntityAITempt(this, 0.6, Items.FISH, true));
        tasks.addTask(5 , new EntityAIFollowOwner(this, 1.0, 10.0f, 5.0f));
        tasks.addTask(6 , new EntityAIOcelotSit(this, 0.8));
        tasks.addTask(7 , new EntityAILeapAtTarget(this, 0.3f));
        tasks.addTask(8 , new EntityAIOcelotAttack(this));
        tasks.addTask(9 , new EntityAIMate(this, 0.8));
        tasks.addTask(10, new EntityAIWanderAvoidWater(this, 0.8, 1.0000001E-5f));
        tasks.addTask(11, new EntityAIWatchClosestPlayer(this, 10.0f));

        this.targetTasks.addTask(1, new EntityAITargetNonTamed<>(this, EntityChicken.class, false, (Predicate)null));
    }


    @Override
    public boolean allowSprint() {
        return true;
    }


    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(EntityOcelot.OCELOT_VARIANT, 0);
    }


    @Override
    public void updateAITasks() {
        if (this.getMoveHelper().isUpdating()) {
            final double d0 = this.getMoveHelper().getSpeed();
            if (d0 == 0.6) {
                this.setSneaking(true);
                this.setSprinting(false);
            }
            else if (d0 == 1.33) {
                this.setSneaking(false);
                this.setSprinting(true);
            }
            else {
                this.setSneaking(false);
                this.setSprinting(false);
            }
        }
        else {
            this.setSneaking(false);
            this.setSprinting(false);
        }
    }


    @Override
    protected boolean canDespawn() {
        return !this.isTamed() && this.ticksExisted > 2400;
    }


    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.30000001192092896);
    }

    public void fall(final float distance, final float damageMultiplier) {}


    public static void registerFixesOcelot(final DataFixer fixer) {
        EntityLiving.registerFixesMob(fixer, EntityOcelot.class);
    }


    @Override
    public void writeEntityToNBT(final NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("CatType", this.getTameSkin());
    }


    @Override
    public void readEntityFromNBT(final NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setTameSkin(compound.getInteger("CatType"));
    }


    @Nullable
    protected SoundEvent getAmbientSound() {
        if (!this.isTamed()) {
            return null;
        }
        if (this.isInLove()) {
            return SoundEvents.ENTITY_CAT_PURR;
        }
        return (this.rand.nextInt(4) == 0) ? SoundEvents.ENTITY_CAT_PURREOW : SoundEvents.ENTITY_CAT_AMBIENT;
    }


    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_CAT_HURT;
    }


    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_CAT_DEATH;
    }


    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }


    @Override
    public boolean attackEntityAsMob(final Entity entityIn) {
        return entityIn.attackEntityFrom(DamageSource.causeMobDamage((EntityLivingBase)this), 3.0f);
    }


    @Override
    public boolean attackEntityFrom(final DamageSource source, final float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        }
        if (this.aiSit != null) {
            this.aiSit.setSitting(false);
        }
        return super.attackEntityFrom(source, amount);
    }


    @Nullable
    protected ResourceLocation getLootTable() {
        return LootTableList.ENTITIES_OCELOT;
    }


    @Override
    public boolean processInteract(final EntityPlayer player, final EnumHand hand) {
        final ItemStack itemstack = player.getHeldItem(hand);
        if (this.isTamed()) {
            if (this.isOwner(player) && !this.world.isRemote && !this.isBreedingItem(itemstack)) {
                this.aiSit.setSitting(!this.isSitting());
            }
        }
        else if ((this.aiTempt == null || this.aiTempt.isRunning()) && itemstack.getItem() == Items.FISH && player.getDistanceSq((Entity)this) < 9.0) {
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            if (!this.world.isRemote) {
                if (this.rand.nextInt(3) == 0 && !ForgeEventFactory.onAnimalTame(this, player)) {
                    this.setTamedBy(player);
                    this.setTameSkin(1 + this.world.rand.nextInt(3));
                    this.playTameEffect(true);
                    this.aiSit.setSitting(true);
                    this.world.setEntityState(this, (byte)7);
                }
                else {
                    this.playTameEffect(false);
                    this.world.setEntityState(this, (byte)6);
                }
            }
            return true;
        }
        return super.processInteract(player, hand);
    }


    @Override
    public EntityOcelot createChild(final EntityAgeable ageable) {
        final EntityOcelot entityocelot = new EntityOcelot(this.world);
        if (this.isTamed()) {
            entityocelot.setOwnerId(this.getOwnerId());
            entityocelot.setTamed(true);
            entityocelot.setTameSkin(this.getTameSkin());
        }
        return entityocelot;
    }


    @Override
    public boolean isBreedingItem(final ItemStack stack) {
        return stack.getItem() == Items.FISH;
    }


    @Override
    public boolean canMateWith(final EntityAnimal otherAnimal) {
        if (otherAnimal == this) {
            return false;
        }
        if (!this.isTamed()) {
            return false;
        }
        if (!(otherAnimal instanceof EntityOcelot)) {
            return false;
        }
        final EntityOcelot entityocelot = (EntityOcelot)otherAnimal;
        return entityocelot.isTamed() && this.isInLove() && entityocelot.isInLove();
    }


    public int getTameSkin() {
        return this.dataManager.get(EntityOcelot.OCELOT_VARIANT);
    }


    public void setTameSkin(final int skinId) {
        this.dataManager.set(EntityOcelot.OCELOT_VARIANT, skinId);
    }


    @Override
    public boolean getCanSpawnHere() {
        return this.world.rand.nextInt(3) != 0;
    }


    @Override
    public boolean isNotColliding() {
        if (this.world.checkNoEntityCollision(this.getEntityBoundingBox(), (Entity)this) && this.world.getCollisionBoxes((Entity)this, this.getEntityBoundingBox()).isEmpty() && !this.world.containsAnyLiquid(this.getEntityBoundingBox())) {
            final BlockPos blockpos = new BlockPos(this.posX, this.getEntityBoundingBox().minY, this.posZ);
            if (blockpos.getY() < this.world.getSeaLevel()) {
                return false;
            }
            final IBlockState iblockstate = this.world.getBlockState(blockpos.down());
            final Block block = iblockstate.getBlock();
            if (block == Blocks.GRASS || block.isLeaves(iblockstate, (IBlockAccess)this.world, blockpos.down())) {
                return true;
            }
        }
        return false;
    }


    @Override
    public String getName() {
        if (this.hasCustomName()) {
            return this.getCustomNameTag();
        }
        return this.isTamed() ? I18n.translateToLocal("entity.Cat.name") : super.getName();
    }


    @Override
    protected void setupTamedAI() {
        if (this.avoidEntity == null) {
            this.avoidEntity = new EntityAIAvoidEntity<>(this, EntityPlayer.class, 16.0f, 0.8, 1.33);
        }
        this.tasks.removeTask(this.avoidEntity);
        if (!this.isTamed()) {
            this.tasks.addTask(4, this.avoidEntity);
        }
    }


    @Nullable
    public IEntityLivingData onInitialSpawn(final DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        if (this.getTameSkin() == 0 && this.world.rand.nextInt(7) == 0) {
            for (int i = 0; i < 2; ++i) {
                final EntityOcelot entityocelot = new EntityOcelot(this.world);
                entityocelot.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0f);
                entityocelot.setGrowingAge(-24000);
                this.world.spawnEntity(entityocelot);
            }
        }
        return livingdata;
    }


    @Override
    public int lodLvl() {
        return ElcaMobs.OCELOT.lodLvl(elcaMinRange);
    }


    static {
        OCELOT_VARIANT = EntityDataManager.createKey(EntityOcelot.class, DataSerializers.VARINT);
    }
}