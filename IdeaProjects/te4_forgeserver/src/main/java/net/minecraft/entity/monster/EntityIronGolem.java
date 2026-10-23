package net.minecraft.entity.monster;

import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.village.*;
import javax.annotation.*;
import net.minecraft.world.*;
import net.minecraft.entity.player.*;
import com.google.common.base.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.*;
import net.minecraft.util.math.*;
import net.minecraft.block.material.*;
import net.minecraft.block.*;
import net.minecraft.block.state.*;
import net.minecraft.util.datafix.*;
import net.minecraft.nbt.*;
import net.minecraft.init.*;
import net.minecraftforge.fml.relauncher.*;
import net.minecraft.util.*;
import net.minecraft.world.storage.loot.*;
import net.minecraft.network.datasync.*;
import plus.fserver.ElcaMobs;
import plus.fserver.entity.ai.EntityAIHurtByTargetSlim;
import plus.fserver.entity.ai.EntityAIWatchClosestPlayer;


public class EntityIronGolem extends EntityGolem {
    private static final Predicate<EntityLiving> GOLEM_TARGET_PREDICATE = entity -> entity != null && IMob.VISIBLE_MOB_SELECTOR.apply(entity) && !(entity instanceof EntityCreeper);
    protected static final DataParameter<Byte> PLAYER_CREATED;
    private int homeCheckTimer;
    @Nullable
    Village village;
    private int attackTimer;
    private int holdRoseTick;

    public EntityIronGolem(final World worldIn) {
        super(worldIn);
        this.setSize(1.4f, 2.7f);
    }


    @Override
    protected void initEntityAI() {
        EntityAITasks tasks = this.tasks;
        tasks.addTask(1, new EntityAIAttackMelee(this, 1.0, true));
        tasks.addTask(2, new EntityAIMoveTowardsTarget(this, 0.9, 32.0f));
        tasks.addTask(3, new EntityAIMoveThroughVillage(this, 0.6, true));
        tasks.addTask(4, new EntityAIMoveTowardsRestriction(this, 1.0));
        tasks.addTask(5, new EntityAILookAtVillager(this));
        tasks.addTask(6, new EntityAIWanderAvoidWater(this, 0.6));
        tasks.addTask(7, new EntityAIWatchClosestPlayer(this, 6.0f));
        tasks.addTask(8, new EntityAILookIdle(this));

        tasks = this.targetTasks;
        tasks.addTask(1, new EntityAIDefendVillage(this));
        tasks.addTask(2, EntityAIHurtByTargetSlim.create(this, false));
        tasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntityLiving.class, 10, false, true, GOLEM_TARGET_PREDICATE));
    }


    @Override
    public boolean allowSprint() {
        return false;
    }


    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.registerU(EntityIronGolem.PLAYER_CREATED, (byte)0);
    }


    @Override
    protected void updateAITasks() {
        final int homeCheckTimer = this.homeCheckTimer - 1;
        this.homeCheckTimer = homeCheckTimer;
        if (homeCheckTimer <= 0) {
            this.homeCheckTimer = 70 + this.rand.nextInt(50);
            this.village = this.world.getVillageCollection().getNearestVillage(new BlockPos(this), 32);
            if (this.village == null) {
                this.detachHome();
            }
            else {
                final BlockPos blockpos = this.village.getCenter();
                this.setHomePosAndDistance(blockpos, (int)(this.village.getVillageRadius() * 0.6f));
            }
        }
        super.updateAITasks();
    }


    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AbstractAttributeMap map = this.getAttributeMap();
        map.get(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(100.0);
        map.get(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25);
        map.get(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0);
    }


    @Override
    protected int decreaseAirSupply(final int air) {
        return air;
    }


    @Override
    protected void collideWithEntity(final Entity entityIn) {
        if (entityIn instanceof IMob && !(entityIn instanceof EntityCreeper) && this.getRNG().nextInt(20) == 0) {
            this.setAttackTarget((EntityLivingBase)entityIn);
        }
        super.collideWithEntity(entityIn);
    }


    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.attackTimer > 0) {
            --this.attackTimer;
        }
        if (this.holdRoseTick > 0) {
            --this.holdRoseTick;
        }
        if (this.motionX * this.motionX + this.motionZ * this.motionZ > 2.500000277905201E-7 && this.rand.nextInt(5) == 0) {
            final int i = MathHelper.floor(this.posX);
            final int j = MathHelper.floor(this.posY - 0.20000000298023224);
            final int k = MathHelper.floor(this.posZ);
            final IBlockState iblockstate = this.world.getBlockState(new BlockPos(i, j, k));
            if (iblockstate.getMaterial() != Material.AIR) {
                this.world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX + (this.rand.nextFloat() - 0.5) * this.width, this.getEntityBoundingBox().minY + 0.1, this.posZ + (this.rand.nextFloat() - 0.5) * this.width, 4.0 * (this.rand.nextFloat() - 0.5), 0.5, (this.rand.nextFloat() - 0.5) * 4.0, Block.getStateId(iblockstate));
            }
        }
    }


    @Override
    public boolean canAttackClass(final Class<? extends EntityLivingBase> cls) {
        return (!this.isPlayerCreated() || !EntityPlayer.class.isAssignableFrom(cls)) && cls != EntityCreeper.class && super.canAttackClass((Class)cls);
    }


    public static void registerFixesIronGolem(final DataFixer fixer) {
        EntityLiving.registerFixesMob(fixer, EntityIronGolem.class);
    }


    @Override
    public void writeEntityToNBT(final NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("PlayerCreated", this.isPlayerCreated());
    }


    @Override
    public void readEntityFromNBT(final NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setPlayerCreated(compound.getBoolean("PlayerCreated"));
    }


    @Override
    public boolean attackEntityAsMob(final Entity entityIn) {
        this.attackTimer = 10;
        this.world.setEntityState(this, (byte)4);
        final boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float)(7 + this.rand.nextInt(15)));
        if (flag) {
            entityIn.motionY += 0.4000000059604645;
            this.applyEnchantments(this, entityIn);
        }
        this.playSound(SoundEvents.ENTITY_IRONGOLEM_ATTACK, 1.0f, 1.0f);
        return flag;
    }


    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(final byte id) {
        if (id == 4) {
            this.attackTimer = 10;
            this.playSound(SoundEvents.ENTITY_IRONGOLEM_ATTACK, 1.0f, 1.0f);
        }
        else if (id == 11) {
            this.holdRoseTick = 400;
        }
        else if (id == 34) {
            this.holdRoseTick = 0;
        }
        else {
            super.handleStatusUpdate(id);
        }
    }


    public Village getVillage() {
        return this.village;
    }


    @SideOnly(Side.CLIENT)
    public int getAttackTimer() {
        return this.attackTimer;
    }


    public void setHoldingRose(final boolean flag) {
        if (flag) {
            this.holdRoseTick = 400;
            this.world.setEntityState(this, (byte)11);
        }
        else {
            this.holdRoseTick = 0;
            this.world.setEntityState(this, (byte)34);
        }
    }


    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_IRONGOLEM_HURT;
    }


    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_IRONGOLEM_DEATH;
    }


    @Override
    protected void playStepSound(final BlockPos pos, final Block blockIn) {
        this.playSound(SoundEvents.ENTITY_IRONGOLEM_STEP, 1.0f, 1.0f);
    }


    @Nullable
    protected ResourceLocation getLootTable() {
        return LootTableList.ENTITIES_IRON_GOLEM;
    }


    public int getHoldRoseTick() {
        return this.holdRoseTick;
    }


    public boolean isPlayerCreated() {
        return (this.dataManager.get(EntityIronGolem.PLAYER_CREATED) & 0x1) != 0x0;
    }


    public void setPlayerCreated(final boolean playerCreated) {
        final byte b0 = this.dataManager.get(EntityIronGolem.PLAYER_CREATED);
        if (playerCreated) {
            this.dataManager.set(EntityIronGolem.PLAYER_CREATED, (byte)(b0 | 0x1));
        }
        else {
            this.dataManager.set(EntityIronGolem.PLAYER_CREATED, (byte)(b0 & 0xFFFFFFFE));
        }
    }


    @Override
    public int lodLvl() {
        return ElcaMobs.IRON_GOLEM.lodLvl(elcaMinRange);
    }


    @Override
    public void onDeath(final DamageSource cause) {
        if (!this.isPlayerCreated() && this.attackingPlayer != null && this.village != null) {
            this.village.modifyPlayerReputation(this.attackingPlayer.getUniqueID(), -5);
        }
        super.onDeath(cause);
    }


    static {
        PLAYER_CREATED = EntityDataManager.createKey(EntityIronGolem.class, DataSerializers.BYTE);
    }
}
