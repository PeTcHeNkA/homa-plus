package net.minecraft.entity.passive;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCarrot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.*;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDesert;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.ElcaMobs;
import plus.fserver.entity.ai.EntityAIHurtByTargetSlim;
import plus.fserver.entity.ai.EntityAINearestAttackableTargetPlayer;
import plus.fserver.entity.ai.EntityAIWatchClosestPlayer;

import static plus.fserver.ElcaCV.SERVER_COMPILE;


public class EntityRabbit extends EntityAnimal {
    private static final DataParameter<Integer> RABBIT_TYPE;
    private int jumpTicks;
    private int jumpDuration;
    private boolean wasOnGround;
    private int currentMoveTypeDuration;
    private int carrotTicks;

    public EntityRabbit(World world) {
        super(world);
        this.setSize(0.4F, 0.5F);
        this.jumpHelper = new RabbitJumpHelper(this);
        this.moveHelper = new RabbitMoveHelper(this);
        this.setMovementSpeed(0.0);
    }


    @Override
    protected void initEntityAI() {
        EntityAITasks tasks = this.tasks;
        tasks.addTask(1 , new EntityAISwimming(this));
        tasks.addTask(1 , new AIPanic(this, 2.2));
        tasks.addTask(2 , new EntityAIMate(this, 0.8));
        tasks.addTask(3 , new EntityAITempt(this, 1.0, Items.CARROT, false));
        tasks.addTask(3 , new EntityAITempt(this, 1.0, Items.GOLDEN_CARROT, false));
        tasks.addTask(3 , new EntityAITempt(this, 1.0, Item.getItemFromBlock(Blocks.YELLOW_FLOWER), false));
        tasks.addTask(4 , new AIAvoidEntity(this, EntityPlayer.class, 8.0F, 2.2, 2.2));
        tasks.addTask(4 , new AIAvoidEntity(this, EntityWolf.class, 10.0F, 2.2, 2.2));
        tasks.addTask(4 , new AIAvoidEntity(this, EntityMob.class, 4.0F, 2.2, 2.2));
        tasks.addTask(5 , new AIRaidFarm(this));
        tasks.addTask(6 , new EntityAIWanderAvoidWater(this, 0.6));
        tasks.addTask(11, new EntityAIWatchClosestPlayer(this, 10.0F));
    }


    @Override
    protected float getJumpUpwardsMotion() {
        if (this.collidedHorizontally || this.moveHelper.isUpdating() && !(this.moveHelper.getY() <= this.posY + 0.5)) {
            return 0.5F;
        } else {
            Path path = this.navigator.getPath();
            if (path != null && path.getCurrentPathIndex() < path.getCurrentPathLength()) {
                Vec3d vec3d = path.getPosition(this);
                if (vec3d.y > this.posY + 0.5) {
                    return 0.5F;
                }
            }
            return this.moveHelper.getSpeed() <= 0.6 ? 0.2F : 0.3F;
        }
    }


    @Override
    protected void jump() {
        super.jump();
        if (this.moveHelper.getSpeed() > 0.0) {
            if (motionX * motionX + motionZ * motionZ < 0.010000000000000002) {
                this.moveRelative(0.0F, 0.0F, 1.0F, 0.1F);
            }
        }
        if (!this.world.isRemote || SERVER_COMPILE) {
            this.world.setEntityState(this, (byte)1);
        }
    }


    @SideOnly(Side.CLIENT)
    public float getJumpCompletion(float f) {
        return this.jumpDuration == 0 ? 0.0F : ((float)this.jumpTicks + f) / (float)this.jumpDuration;
    }


    public void setMovementSpeed(double p_175515_1_) {
        this.getNavigator().setSpeed(p_175515_1_);
        this.moveHelper.setMoveTo(this.moveHelper.getX(), this.moveHelper.getY(), this.moveHelper.getZ(), p_175515_1_);
    }


    @Override
    public void setJumping(boolean b) {
        super.setJumping(b);
        if (b) {
            this.playSound(this.getJumpSound(), this.getSoundVolume(), ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) * 0.8F);
        }
    }


    public void startJumping() {
        this.setJumping(true);
        this.jumpDuration = 10;
        this.jumpTicks = 0;
    }


    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(RABBIT_TYPE, 0);
    }


    @Override
    public void updateAITasks() {
        if (this.currentMoveTypeDuration > 0) {
            --this.currentMoveTypeDuration;
        }

        if (this.carrotTicks > 0) {
            this.carrotTicks -= this.rand.nextInt(3);
            if (this.carrotTicks < 0) {
                this.carrotTicks = 0;
            }
        }

        if (this.onGround) {
            if (!this.wasOnGround) {
                this.setJumping(false);
                this.checkLandingDelay();
            }

            if (this.getRabbitType() == 99 && this.currentMoveTypeDuration == 0) {
                EntityLivingBase entitylivingbase = this.getAttackTarget();
                if (entitylivingbase != null && this.getDistanceSq(entitylivingbase) < 16.0) {
                    this.calculateRotationYaw(entitylivingbase.posX, entitylivingbase.posZ);
                    this.moveHelper.setMoveTo(entitylivingbase.posX, entitylivingbase.posY, entitylivingbase.posZ, this.moveHelper.getSpeed());
                    this.startJumping();
                    this.wasOnGround = true;
                }
            }

            RabbitJumpHelper entityrabbit$rabbitjumphelper = (RabbitJumpHelper)this.jumpHelper;
            if (!entityrabbit$rabbitjumphelper.getIsJumping()) {
                if (this.moveHelper.isUpdating() && this.currentMoveTypeDuration == 0) {
                    Path path = this.navigator.getPath();
                    Vec3d vec3d = new Vec3d(this.moveHelper.getX(), this.moveHelper.getY(), this.moveHelper.getZ());
                    if (path != null && path.getCurrentPathIndex() < path.getCurrentPathLength()) {
                        vec3d = path.getPosition(this);
                    }

                    this.calculateRotationYaw(vec3d.x, vec3d.z);
                    this.startJumping();
                }
            } else if (!entityrabbit$rabbitjumphelper.canJump()) {
                this.enableJumpControl();
            }
        }
        this.wasOnGround = this.onGround;
    }


    @Override
    public void spawnRunningParticles() {}


    private void calculateRotationYaw(double d1, double d2) {
        this.rotationYaw = (float)(MathHelper.atan2(d2 - this.posZ, d1 - this.posX) * 57.29577951308232) - 90.0F;
    }


    private void enableJumpControl() {
        ((RabbitJumpHelper)this.jumpHelper).setCanJump(true);
    }


    private void disableJumpControl() {
        ((RabbitJumpHelper)this.jumpHelper).setCanJump(false);
    }


    private void updateMoveTypeDuration() {
        if (this.moveHelper.getSpeed() < 2.2) {
            this.currentMoveTypeDuration = 10;
        } else {
            this.currentMoveTypeDuration = 1;
        }
    }

    private void checkLandingDelay() {
        this.updateMoveTypeDuration();
        this.disableJumpControl();
    }


    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.jumpTicks != this.jumpDuration) {
            ++this.jumpTicks;
        } else if (this.jumpDuration != 0) {
            this.jumpTicks = 0;
            this.jumpDuration = 0;
            this.setJumping(false);
        }
    }


    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(3.0);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.30000001192092896);
    }


    public static void registerFixesRabbit(DataFixer fixer) {
        EntityLiving.registerFixesMob(fixer, EntityRabbit.class);
    }


    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        nbt.setIntegerU("RabbitType", this.getRabbitType());
        nbt.setIntegerU("MoreCarrotTicks", this.carrotTicks);
    }


    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        this.setRabbitType(nbt.getInteger("RabbitType"));
        this.carrotTicks = nbt.getInteger("MoreCarrotTicks");
    }


    protected SoundEvent getJumpSound() {
        return SoundEvents.ENTITY_RABBIT_JUMP;
    }


    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_RABBIT_AMBIENT;
    }


    @Override
    protected SoundEvent getHurtSound(DamageSource p_184601_1_) {
        return SoundEvents.ENTITY_RABBIT_HURT;
    }


    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_RABBIT_DEATH;
    }


    @Override
    public boolean attackEntityAsMob(Entity entity) {
        if (this.getRabbitType() == 99) {
            this.playSound(SoundEvents.ENTITY_RABBIT_ATTACK, 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
            return entity.attackEntityFrom(DamageSource.causeMobDamage(this), 8.0F);
        } else {
            return entity.attackEntityFrom(DamageSource.causeMobDamage(this), 3.0F);
        }
    }


    @Override
    public SoundCategory getSoundCategory() {
        return this.getRabbitType() == 99 ? SoundCategory.HOSTILE : SoundCategory.NEUTRAL;
    }


    @Override
    public boolean attackEntityFrom(DamageSource src, float amount) {
        return this.isEntityInvulnerable(src) ? false : super.attackEntityFrom(src, amount);
    }


    @Override
    protected ResourceLocation getLootTable() {
        return LootTableList.ENTITIES_RABBIT;
    }


    private boolean isRabbitBreedingItem(Item item) {
        return item == Items.CARROT || item == Items.GOLDEN_CARROT || item == Item.getItemFromBlock(Blocks.YELLOW_FLOWER);
    }


    @Override
    public EntityRabbit createChild(EntityAgeable entity) {
        EntityRabbit entityrabbit = new EntityRabbit(this.world);
        int i = this.getRandomRabbitType();
        if (this.rand.nextInt(20) != 0) {
            if (entity instanceof EntityRabbit && this.rand.nextBoolean()) {
                i = ((EntityRabbit)entity).getRabbitType();
            } else {
                i = this.getRabbitType();
            }
        }
        entityrabbit.setRabbitType(i);
        return entityrabbit;
    }


    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return this.isRabbitBreedingItem(stack.getItem());
    }


    public int getRabbitType() {
        return this.dataManager.get(RABBIT_TYPE);
    }


    public void setRabbitType(int type) {
        if (type == 99) {
            this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(8.0);
            this.tasks.addTask(4, new AIEvilAttack(this));
            this.targetTasks.addTask(1, EntityAIHurtByTargetSlim.create(this, false));
            this.targetTasks.addTask(2, new EntityAINearestAttackableTargetPlayer(this, true));
            this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityWolf.class, true));
            if (!this.hasCustomName()) {
                this.setCustomNameTag(I18n.translateToLocal("entity.KillerBunny.name"));
            }
        }
        this.dataManager.set(RABBIT_TYPE, type);
    }


    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance inst, @Nullable IEntityLivingData data) {
        data = super.onInitialSpawn(inst, data);
        int i = this.getRandomRabbitType();
        boolean flag = false;
        if (data instanceof RabbitTypeData) {
            i = ((RabbitTypeData)data).typeData;
            flag = true;
        } else {
            data = new RabbitTypeData(i);
        }

        this.setRabbitType(i);
        if (flag) {
            this.setGrowingAge(-24000);
        }
        return data;
    }


    private int getRandomRabbitType() {
        Biome biome = this.world.getBiome(new BlockPos(this));
        int i = this.rand.nextInt(100);
        if (biome.isSnowyBiome()) {
            return i < 80 ? 1 : 3;
        } else if (biome instanceof BiomeDesert) {
            return 4;
        } else {
            return i < 50 ? 0 : (i < 90 ? 5 : 2);
        }
    }


    private boolean isCarrotEaten() {
        return this.carrotTicks == 0;
    }


    protected void createEatingParticles() {
        BlockCarrot carrot = (BlockCarrot)Blocks.CARROTS;
        IBlockState state = carrot.withAge(carrot.getMaxAge());
        this.world.spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + 0.5 + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, 0.0, 0.0, 0.0, Block.getStateId(state));
        this.carrotTicks = 40;
    }


    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte p_70103_1_) {
        if (p_70103_1_ == 1) {
            this.createRunningParticles();
            this.jumpDuration = 10;
            this.jumpTicks = 0;
        } else {
            super.handleStatusUpdate(p_70103_1_);
        }
    }


    static {
        RABBIT_TYPE = EntityDataManager.createKey(EntityRabbit.class, DataSerializers.VARINT);
    }


    public static class RabbitTypeData implements IEntityLivingData {
        public int typeData;

        public RabbitTypeData(int type) {
            this.typeData = type;
        }
    }


    static class RabbitMoveHelper extends EntityMoveHelper {
        private final EntityRabbit rabbit;
        private double nextJumpSpeed;

        public RabbitMoveHelper(EntityRabbit p_i45862_1_) {
            super(p_i45862_1_);
            this.rabbit = p_i45862_1_;
        }


        @Override
        public void onUpdateMoveHelper() {
            if (this.rabbit.onGround && !this.rabbit.isJumping && !((RabbitJumpHelper)this.rabbit.jumpHelper).getIsJumping()) {
                this.rabbit.setMovementSpeed(0.0);
            } else if (this.isUpdating()) {
                this.rabbit.setMovementSpeed(this.nextJumpSpeed);
            }
            super.onUpdateMoveHelper();
        }


        @Override
        public void setMoveTo(double x, double y, double z, double speed) {
            if (this.rabbit.isInWater()) {
                speed = 1.5;
            }
            super.setMoveTo(x, y, z, speed);
            if (speed > 0.0) {
                this.nextJumpSpeed = speed;
            }
        }
    }


    public static class RabbitJumpHelper extends EntityJumpHelper {
        private final EntityRabbit rabbit;
        private boolean canJump;

        public RabbitJumpHelper(EntityRabbit rabbit) {
            super(rabbit);
            this.rabbit = rabbit;
        }


        public boolean getIsJumping() {
            return this.isJumping;
        }


        public boolean canJump() {
            return this.canJump;
        }


        public void setCanJump(boolean b) {
            this.canJump = b;
        }


        @Override
        public void doJump() {
            if (this.isJumping) {
                this.rabbit.startJumping();
                this.isJumping = false;
            }
        }
    }

    static class AIRaidFarm extends EntityAIMoveToBlock {
        private final EntityRabbit rabbit;
        private boolean wantsToRaid;
        private boolean canRaid;

        public AIRaidFarm(EntityRabbit p_i45860_1_) {
            super(p_i45860_1_, 0.699999988079071, 16);
            this.rabbit = p_i45860_1_;
        }


        @Override
        public boolean shouldExecute() {
            if (this.runDelay <= 0) {
                if (!rabbit.world.getMobGrifering(rabbit)) return false;

                this.canRaid = false;
                //this.wantsToRaid = this.rabbit.isCarrotEaten();
                this.wantsToRaid = true;
            }
            return super.shouldExecute();
        }


        @Override
        public boolean shouldContinueExecuting() {
            return this.canRaid && super.shouldContinueExecuting();
        }


        @Override
        public void updateTask() {
            super.updateTask();
            this.rabbit.getLookHelper().setLookPosition((double)this.destinationBlock.getX() + 0.5, (double)(this.destinationBlock.getY() + 1), (double)this.destinationBlock.getZ() + 0.5, 10.0F, (float)this.rabbit.getVerticalFaceSpeed());
            if (this.getIsAboveDestination()) {
                World world = this.rabbit.world;
                BlockPos pos = this.destinationBlock.up();
                IBlockState state = world.access().getBlockState(pos);
                Block block = state.getBlock();
                if (this.canRaid && block instanceof BlockCarrot) {
                    Integer integer = state.getValue(BlockCarrot.AGE);
                    if (integer == 0) {
                        world.access().setBlockState(pos, BlockIDIndexes.AIR_STATE, 2);
                        world.destroyBlock(pos, true);
                    } else {
                        world.access().setBlockState(pos, state.withProperty(BlockCarrot.AGE, integer - 1), 2);
                        world.playEvent(2001, pos, Block.getStateId(state));
                    }
                    this.rabbit.createEatingParticles();
                }
                this.canRaid = false;
                this.runDelay = 10;
            }
        }


        @Override
        protected boolean shouldMoveTo(World world, BlockPos pos) {
            Block block = world.access().getBlockState(pos).getBlock();
            if (block == Blocks.FARMLAND && this.wantsToRaid && !this.canRaid) {
                IBlockState state = world.access().getBlockState(pos.getX(), pos.getY()-1, pos.getZ());
                block = state.getBlock();
                if (block instanceof BlockCarrot && ((BlockCarrot)block).isMaxAge(state)) {
                    this.canRaid = true;
                    return true;
                }
            }
            return false;
        }
    }


    static class AIPanic extends EntityAIPanic {
        private final EntityRabbit rabbit;

        public AIPanic(EntityRabbit rabbit, double speed) {
            super(rabbit, speed);
            this.rabbit = rabbit;
        }


        @Override
        public void updateTask() {
            super.updateTask();
            this.rabbit.setMovementSpeed(this.speed);
        }
    }


    static class AIEvilAttack extends EntityAIAttackMelee {
        public AIEvilAttack(EntityRabbit rabbit) {
            super(rabbit, 1.4, true);
        }


        @Override
        protected double getAttackReachSqr(EntityLivingBase entity) {
            return 4.0F + entity.width;
        }
    }


    static class AIAvoidEntity<T extends Entity> extends EntityAIAvoidEntity<T> {
        private final EntityRabbit rabbit;

        public AIAvoidEntity(EntityRabbit rabbit, Class<T> clazz, float distance, double farSpeed, double nearSpeed) {
            super(rabbit, clazz, distance, farSpeed, nearSpeed);
            this.rabbit = rabbit;
        }


        @Override
        public boolean shouldExecute() {
            return this.rabbit.getRabbitType() != 99 && super.shouldExecute();
        }
    }


    @Override
    public int lodLvl() {
        return ElcaMobs.RABBIT.lodLvl(elcaMinRange);
    }
}