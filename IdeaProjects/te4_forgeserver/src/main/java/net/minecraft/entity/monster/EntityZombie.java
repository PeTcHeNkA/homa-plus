package net.minecraft.entity.monster;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.*;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.ElcaMobs;


public class EntityZombie extends EntityMob {
    protected static final IAttribute SPAWN_REINFORCEMENTS_CHANCE = (new RangedAttribute((IAttribute)null, "zombie.spawnReinforcements", 0.0D, 0.0D, 1.0D)).setDescription("Spawn Reinforcements Chance");
    private static final UUID BABY_SPEED_BOOST_ID = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
    private static final AttributeModifier BABY_SPEED_BOOST = new AttributeModifier(BABY_SPEED_BOOST_ID, "Baby speed boost", 0.5D, 1);
    private static final DataParameter<Boolean> IS_CHILD = EntityDataManager.createKey(EntityZombie.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> VILLAGER_TYPE = EntityDataManager.createKey(EntityZombie.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> ARMS_RAISED = EntityDataManager.createKey(EntityZombie.class, DataSerializers.BOOLEAN);
    private final EntityAIBreakDoor breakDoor = new EntityAIBreakDoor(this);
    private boolean isBreakDoorsTaskSet;
    private float zombieWidth = -1.0F;
    private float zombieHeight;

    public EntityZombie(World worldIn) {
        super(worldIn);
        this.setSize(0.6F, 1.95F);
    }


    @Override
    protected void initEntityAI() {
        EntityAITasks tasks = this.tasks;
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(2, new EntityAIZombieAttack(this, 1.0D, false));
        tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
        tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1.0D));
        tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(8, new EntityAILookIdle(this));
        this.applyEntityAI();
    }


    protected void applyEntityAI() {
        this.tasks.addTask(6, new EntityAIMoveThroughVillage(this, 1.0D, false));
        EntityAITasks targetTasks = this.targetTasks;
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, true, EntityPigZombie.class));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
        targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntityVillager.class, false));
        targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntityIronGolem.class, true));
    }


    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AbstractAttributeMap map = this.getAttributeMap();
        map.getAttributeInstance(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(35.0D);
        map.getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23000000417232513D);
        map.getAttributeInstance(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
        map.getAttributeInstance(SharedMonsterAttributes.ARMOR).setBaseValue(2.0D);
        map.registerAttribute(SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(
                this.rand.nextDouble() * ForgeModContainer.zombieSummonBaseChance);
    }


    @Override
    protected void entityInit() {
        EntityDataManager manager = getDataManager();
        manager.register(IS_CHILD, Boolean.FALSE);
        manager.register(VILLAGER_TYPE, 0);
        manager.register(ARMS_RAISED, Boolean.FALSE);
        super.entityInit();
    }


    public void setArmsRaised(boolean armsRaised) {
        this.getDataManager().set(ARMS_RAISED, armsRaised);
    }


    @SideOnly(Side.CLIENT)
    public boolean isArmsRaised() {
        return this.getDataManager().get(ARMS_RAISED);
    }


    public boolean isBreakDoorsTaskSet() {
        return this.isBreakDoorsTaskSet;
    }


    public void setBreakDoorsAItask(boolean enabled) {
        if (this.isBreakDoorsTaskSet != enabled) {
            this.isBreakDoorsTaskSet = enabled;
            ((PathNavigateGround)this.getNavigator()).setBreakDoors(enabled);
            if (enabled) this.tasks.addTask(1, this.breakDoor);
            else this.tasks.removeTask(this.breakDoor);
        }
    }


    @Override
    public boolean isChild() {
        return this.getDataManager().get(IS_CHILD);
    }


    @Override
    protected int getExperiencePoints(EntityPlayer player) {
        if (this.isChild()) this.experienceValue = (int)((float)this.experienceValue * 2.5F);
        return super.getExperiencePoints(player);
    }



    public void setChild(boolean childZombie) {
        this.getDataManager().set(IS_CHILD, childZombie);

        if (this.world != null && !this.world.isRemote) {
            IAttributeInstance iattributeinstance = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
            iattributeinstance.removeModifier(BABY_SPEED_BOOST);

            if (childZombie) iattributeinstance.applyModifier(BABY_SPEED_BOOST);
        }
        this.setChildSize(childZombie);
    }


    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        if (IS_CHILD.equals(key)) this.setChildSize(this.isChild());
        super.notifyDataManagerChange(key);
    }


    @Override
    public void onLivingUpdateTick() {
        if (this.world.isDaytime() && !this.world.isRemote && !this.isChild() && this.shouldBurnInDay()) {
            float f = this.getBrightness();

            if (f > 0.5F && this.rand.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && this.world.canSeeSky(this)) {
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


    protected boolean shouldBurnInDay() {
        return true;
    }


    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (super.attackEntityFrom(source, amount)) {
            EntityLivingBase entitylivingbase = this.getAttackTarget();

            if (entitylivingbase == null && source.getTrueSource() instanceof EntityLivingBase) {
                entitylivingbase = (EntityLivingBase)source.getTrueSource();
            }

            int i = MathHelper.floor(this.posX);
            int j = MathHelper.floor(this.posY);
            int k = MathHelper.floor(this.posZ);
            net.minecraftforge.event.entity.living.ZombieEvent.SummonAidEvent summonAid = net.minecraftforge.event.ForgeEventFactory.fireZombieSummonAid(this, world, i, j, k, entitylivingbase, this.getEntityAttribute(SPAWN_REINFORCEMENTS_CHANCE).getAttributeValue());
            if (summonAid.getResult() == net.minecraftforge.fml.common.eventhandler.Event.Result.DENY) return true;

            if (summonAid.getResult() == net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW  ||
                    entitylivingbase != null && this.world.getDifficulty() == EnumDifficulty.HARD && (double)this.rand.nextFloat() < this.getEntityAttribute(SPAWN_REINFORCEMENTS_CHANCE).getAttributeValue() && this.world.getGameRules().getBoolean("doMobSpawning")) {
                EntityZombie zombie;
                if (summonAid.getCustomSummonedAid() != null && summonAid.getResult() == net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW) {
                    zombie = summonAid.getCustomSummonedAid();
                } else {
                    zombie = new EntityZombie(this.world);
                }

                for (int l = 0; l < 50; ++l) {
                    int i1 = i + MathHelper.getInt(this.rand, 7, 40) * MathHelper.getInt(this.rand, -1, 1);
                    int j1 = j + MathHelper.getInt(this.rand, 7, 40) * MathHelper.getInt(this.rand, -1, 1);
                    int k1 = k + MathHelper.getInt(this.rand, 7, 40) * MathHelper.getInt(this.rand, -1, 1);

                    if (this.world.getBlockState(new BlockPos(i1, j1 - 1, k1)).isSideSolid(this.world,
                            new BlockPos(i1, j1 - 1, k1), EnumFacing.UP) && this.world.getLightFromNeighbors(new BlockPos(i1, j1, k1)) < 10) {
                        zombie.mutPosition(i1, j1, k1);

                        if (!this.world.isAnyPlayerWithinRangeAt(i1, j1, k1, 7.0D) && this.world.checkNoEntityCollision(zombie.getEntityBoundingBox(), zombie) && this.world.getCollisionBoxes(zombie, zombie.getEntityBoundingBox()).isEmpty() && !this.world.containsAnyLiquid(zombie.getEntityBoundingBox())) {
                            this.world.spawnEntity(zombie);
                            if (entitylivingbase != null) zombie.setAttackTarget(entitylivingbase);
                            zombie.onInitialSpawn(this.world.getDifficultyForLocation(new BlockPos(zombie)), (IEntityLivingData)null);
                            this.getEntityAttribute(SPAWN_REINFORCEMENTS_CHANCE).applyModifier(new AttributeModifier("Zombie reinforcement caller charge", -0.05000000074505806D, 0));
                            zombie.getEntityAttribute(SPAWN_REINFORCEMENTS_CHANCE).applyModifier(new AttributeModifier("Zombie reinforcement callee charge", -0.05000000074505806D, 0));
                            break;
                        }
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }


    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        boolean flag = super.attackEntityAsMob(entityIn);

        if (flag) {
            float f = this.world.getDifficultyForLocation(new BlockPos(this)).getAdditionalDifficulty();

            if (this.getHeldItemMainhand().isEmpty() && this.isBurning() && this.rand.nextFloat() < f * 0.3F) {
                entityIn.setFire(2 * (int)f);
            }
        }
        return flag;
    }


    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_ZOMBIE_AMBIENT;
    }


    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_ZOMBIE_HURT;
    }


    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ZOMBIE_DEATH;
    }

    protected SoundEvent getStepSound() {
        return SoundEvents.ENTITY_ZOMBIE_STEP;
    }


    @Override
    protected void playStepSound(BlockPos pos, Block blockIn) {
        this.playSound(this.getStepSound(), 0.15F, 1.0F);
    }


    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.UNDEAD;
    }


    @Nullable
    @Override
    protected ResourceLocation getLootTable() {
        return LootTableList.ENTITIES_ZOMBIE;
    }


    @Override
    protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
        super.setEquipmentBasedOnDifficulty(difficulty);

        if (this.rand.nextFloat() < (this.world.getDifficulty() == EnumDifficulty.HARD ? 0.05F : 0.01F)) {
            int i = this.rand.nextInt(3);

            if (i == 0) {
                this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            } else {
                this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SHOVEL));
            }
        }
    }


    public static void registerFixesZombie(DataFixer fixer) {
        EntityLiving.registerFixesMob(fixer, EntityZombie.class);
    }


    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);

        if (this.isChild()) {
            tag.setBoolean("IsBaby", true);
        }
        tag.setBoolean("CanBreakDoors", this.isBreakDoorsTaskSet());
    }


    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        if (tag.getBoolean("IsBaby")) {
            this.setChild(true);
        }
        this.setBreakDoorsAItask(tag.getBoolean("CanBreakDoors"));
    }


    @Override
    public void onKillEntity(EntityLivingBase entityLivingIn) {
        super.onKillEntity(entityLivingIn);

        if ((this.world.getDifficulty() == EnumDifficulty.NORMAL || this.world.getDifficulty() == EnumDifficulty.HARD) && entityLivingIn instanceof EntityVillager) {
            if (this.world.getDifficulty() != EnumDifficulty.HARD && this.rand.nextBoolean()) {
                return;
            }

            EntityVillager villager = (EntityVillager)entityLivingIn;
            EntityZombieVillager zombie = new EntityZombieVillager(this.world);
            zombie.copyLocationAndAnglesFrom(villager);
            this.world.removeEntity(villager);
            zombie.onInitialSpawn(this.world.getDifficultyForLocation(new BlockPos(zombie)), new EntityZombie.GroupData(false));
            zombie.setProfession(villager.getProfession());
            zombie.setChild(villager.isChild());
            zombie.setNoAI(villager.isAIDisabled());

            if (villager.hasCustomName()) {
                zombie.setCustomNameTag(villager.getCustomNameTag());
                zombie.setAlwaysRenderNameTag(villager.getAlwaysRenderNameTag());
            }

            this.world.spawnEntity(zombie);
            this.world.playEvent((EntityPlayer)null, 1026, new BlockPos(this), 0);
        }
    }


    @Override
    public float getEyeHeight() {
        float f = 1.74F;

        if (this.isChild()) {
            f = (float)((double)f - 0.81D);
        }
        return f;
    }


    @Override
    protected boolean canEquipItem(ItemStack stack) {
        return stack.getItem() == Items.EGG && this.isChild() && this.isRiding() ? false : super.canEquipItem(stack);
    }


    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        float f = difficulty.getClampedAdditionalDifficulty();
        this.setCanPickUpLoot(this.rand.nextFloat() < 0.55F * f);

        if (livingdata == null) {
            livingdata = new EntityZombie.GroupData(this.world.rand.nextFloat() < net.minecraftforge.common.ForgeModContainer.zombieBabyChance);
        }

        if (livingdata instanceof EntityZombie.GroupData) {
            EntityZombie.GroupData data = (EntityZombie.GroupData)livingdata;

            if (data.isChild) {
                this.setChild(true);

                if ((double)this.world.rand.nextFloat() < 0.05D) {
                    //todo
                    AxisAlignedBB box = world.commonWorldEntityAABB;
                    this.getEntityBoundingBox().growTo(5.0D, 3.0D, 5.0D, box);
                    List<EntityChicken> list = this.world.getEntitiesWithinAABBNoCache(EntityChicken.class, box, EntitySelectors.IS_STANDALONE);

                    if (!list.isEmpty()) {
                        EntityChicken entitychicken = list.get(0);
                        entitychicken.setChickenJockey(true);
                        this.startRiding(entitychicken);
                    }
                }
                else if ((double)this.world.rand.nextFloat() < 0.05D) {
                    EntityChicken entitychicken1 = new EntityChicken(this.world);
                    entitychicken1.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);
                    entitychicken1.onInitialSpawn(difficulty, (IEntityLivingData)null);
                    entitychicken1.setChickenJockey(true);
                    this.world.spawnEntity(entitychicken1);
                    this.startRiding(entitychicken1);
                }
            }
        }

        this.setBreakDoorsAItask(this.rand.nextFloat() < f * 0.1F);
        this.setEquipmentBasedOnDifficulty(difficulty);
        this.setEnchantmentBasedOnDifficulty(difficulty);

        if (this.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty()) {
            Calendar calendar = this.world.getCurrentDate();

            if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31 && this.rand.nextFloat() < 0.25F) {
                this.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(this.rand.nextFloat() < 0.1F ? Blocks.LIT_PUMPKIN : Blocks.PUMPKIN));
                this.inventoryArmorDropChances[EntityEquipmentSlot.HEAD.getIndex()] = 0.0F;
            }
        }

        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).applyModifier(new AttributeModifier("Random spawn bonus", this.rand.nextDouble() * 0.05000000074505806D, 0));
        double d0 = this.rand.nextDouble() * 1.5D * (double)f;

        if (d0 > 1.0D) {
            this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).applyModifier(new AttributeModifier("Random zombie-spawn bonus", d0, 2));
        }

        if (this.rand.nextFloat() < f * 0.05F) {
            this.getEntityAttribute(SPAWN_REINFORCEMENTS_CHANCE).applyModifier(new AttributeModifier("Leader zombie bonus", this.rand.nextDouble() * 0.25D + 0.5D, 0));
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(new AttributeModifier("Leader zombie bonus", this.rand.nextDouble() * 3.0D + 1.0D, 2));
            this.setBreakDoorsAItask(true);
        }

        return livingdata;
    }


    public void setChildSize(boolean isChild) {
        this.multiplySize(isChild ? 0.5F : 1.0F);
    }


    @Override
    protected final void setSize(float width, float height) {
        boolean flag = this.zombieWidth > 0.0F && this.zombieHeight > 0.0F;
        this.zombieWidth = width;
        this.zombieHeight = height;

        if (!flag) {
            this.multiplySize(1.0F);
        }
    }


    protected final void multiplySize(float size) {
        super.setSize(this.zombieWidth * size, this.zombieHeight * size);
    }


    @Override
    public double getYOffset() {
        return this.isChild() ? 0.0D : -0.45D;
    }


    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);

        if (cause.getTrueSource() instanceof EntityCreeper) {
            EntityCreeper entitycreeper = (EntityCreeper)cause.getTrueSource();

            if (entitycreeper.getPowered() && entitycreeper.ableToCauseSkullDrop()) {
                entitycreeper.incrementDroppedSkulls();
                ItemStack itemstack = this.getSkullDrop();

                if (!itemstack.isEmpty()) {
                    this.entityDropItem(itemstack, 0.0F);
                }
            }
        }
    }


    protected ItemStack getSkullDrop() {
        return new ItemStack(Items.SKULL, 1, 2);
    }


    static class GroupData implements IEntityLivingData {
        public boolean isChild;

        private GroupData(boolean isChild) {
            this.isChild = isChild;
        }
    }


    @Override
    public int lodLvl() {
        return ElcaMobs.ZOMBIE.lodLvl(elcaMinRange);
    }
}
