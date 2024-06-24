package net.minecraft.entity.item;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWaterMob;
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
import net.minecraft.network.play.client.CPacketSteerBoat;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.ElcaMobs;
import plus.fserver.api.IBlockEAccess;
import plus.fserver.utls.EntityLodFunc;


//todo jump on boat
public class EntityBoat extends Entity {
    private static final DataParameter<Integer> TIME_SINCE_HIT = EntityDataManager.createKey(EntityBoat.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> FORWARD_DIRECTION = EntityDataManager.createKey(EntityBoat.class, DataSerializers.VARINT);
    private static final DataParameter<Float> DAMAGE_TAKEN = EntityDataManager.createKey(EntityBoat.class, DataSerializers.FLOAT);
    private static final DataParameter<Integer> BOAT_TYPE = EntityDataManager.createKey(EntityBoat.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean>[] DATA_ID_PADDLE = new DataParameter[] {EntityDataManager.createKey(EntityBoat.class, DataSerializers.BOOLEAN), EntityDataManager.createKey(EntityBoat.class, DataSerializers.BOOLEAN)};
    private final float[] paddlePositions;
    private float outOfControlTicks;
    private float deltaRotation;
    private int lerpSteps;
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private double lerpYaw;
    private double lerpPitch;
    private boolean leftInputDown;
    private boolean rightInputDown;
    private boolean forwardInputDown;
    private boolean backInputDown;
    private double waterLevel;
    private float boatGlide;
    private EntityBoat.Status status;
    private EntityBoat.Status previousStatus;
    private double lastYd;

    public EntityBoat(World worldIn) {
        super(worldIn);
        this.paddlePositions = new float[2];
        this.preventEntitySpawning = true;
        this.setSize(1.375F, 0.5625F);
    }


    public EntityBoat(World worldIn, double x, double y, double z) {
        this(worldIn);
        this.setPosition(x, y, z);
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
    }


    @Override
    protected boolean canTriggerWalking() {
        return false;
    }


    @Override
    protected void entityInit() {
        EntityDataManager manager = this.dataManager;
        manager.register(TIME_SINCE_HIT, 0);
        manager.register(FORWARD_DIRECTION, 1);
        manager.register(DAMAGE_TAKEN, 0.0F);
        manager.register(BOAT_TYPE, Type.OAK.ordinal());

        for (DataParameter<Boolean> dataparameter : DATA_ID_PADDLE) {
            manager.register(dataparameter, Boolean.FALSE);
        }
        manager.trim();
    }


    @Nullable
    public AxisAlignedBB getCollisionBox(Entity entityIn) {
        return entityIn.canBePushed() ? entityIn.getEntityBoundingBox() : null;
    }


    @Nullable
    public AxisAlignedBB getCollisionBoundingBox() {
        return this.getEntityBoundingBox();
    }


    @Override
    public boolean canBePushed() {
        return true;
    }


    @Override
    public double getMountedYOffset() {
        return -0.1D;
    }


    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        } else if (!this.world.isRemote && !this.isDead) {
            if (source instanceof EntityDamageSourceIndirect && source.getTrueSource() != null && this.isPassenger(source.getTrueSource())) {
                return false;
            } else {
                this.setForwardDirection(-this.getForwardDirection());
                this.setTimeSinceHit(10);
                this.setDamageTaken(this.getDamageTaken() + amount * 10.0F);
                this.markVelocityChanged();
                boolean flag = source.getTrueSource() instanceof EntityPlayer && ((EntityPlayer)source.getTrueSource()).capabilities.isCreativeMode;

                if (flag || this.getDamageTaken() > 40.0F) {
                    if (!flag && this.world.getGameRules().doEntityDrops.getBoolean()) {
                        this.dropItemWithOffset(this.getItemBoat(), 1, 0.0F);
                    }
                    this.setDead();
                }
                return true;
            }
        } else {
            return true;
        }
    }


    @Override
    public void applyEntityCollision(Entity entityIn) {
        if (entityIn instanceof EntityBoat) {
            if (entityIn.getEntityBoundingBox().minY < this.getEntityBoundingBox().maxY) {
                super.applyEntityCollision(entityIn);
            }
        } else if (entityIn.getEntityBoundingBox().minY <= this.getEntityBoundingBox().minY) {
            super.applyEntityCollision(entityIn);
        }
    }


    public Item getItemBoat() {
        switch (this.getBoatType()) {
            case OAK:
            default:
                return Items.BOAT;
            case SPRUCE:
                return Items.SPRUCE_BOAT;
            case BIRCH:
                return Items.BIRCH_BOAT;
            case JUNGLE:
                return Items.JUNGLE_BOAT;
            case ACACIA:
                return Items.ACACIA_BOAT;
            case DARK_OAK:
                return Items.DARK_OAK_BOAT;
        }
    }


    @SideOnly(Side.CLIENT)
    public void performHurtAnimation() {
        this.setForwardDirection(-this.getForwardDirection());
        this.setTimeSinceHit(10);
        this.setDamageTaken(this.getDamageTaken() * 11.0F);
    }


    @Override
    public boolean canBeCollidedWith() {
        return !this.isDead;
    }


    @SideOnly(Side.CLIENT)
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        this.lerpX = x;
        this.lerpY = y;
        this.lerpZ = z;
        this.lerpYaw = yaw;
        this.lerpPitch = pitch;
        this.lerpSteps = 10;
    }


    @Override
    public EnumFacing getAdjustedHorizontalFacing() {
        return this.getHorizontalFacing().rotateY();
    }


    @Override
    public void onUpdate() {
        this.previousStatus = this.status;
        this.status = this.getBoatStatus();

        if (this.status != EntityBoat.Status.UNDER_WATER && this.status != EntityBoat.Status.UNDER_FLOWING_WATER) {
            this.outOfControlTicks = 0.0F;
        } else {
            ++this.outOfControlTicks;
        }

        if (!this.world.isRemote && this.outOfControlTicks >= 60.0F) {
            this.removePassengers();
        }
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        int lodLvl;
        float tempF;
        if ((lodLvl  = this.getTimeSinceHit()) > 0)   this.setTimeSinceHit(lodLvl - 1     );
        if ((tempF = this.getDamageTaken()) > 0.0F) this.setDamageTaken (tempF  - 1.0F);
        lodLvl = lodLvl();

        if((ticksExisted & lodLvl) == 0) super.onUpdateE();

        this.tickLerp();

        if (this.canPassengerSteer()) {
            if (this.getPassengers().isEmpty() || !(this.getPassengers().get(0) instanceof EntityPlayer)) {
                this.setPaddleState(false, false);
            }

            this.updateMotion();

            if (this.world.isRemote) {
                this.controlBoat();
                this.world.sendPacketToServer(new CPacketSteerBoat(this.getPaddleState(0), this.getPaddleState(1)));
            }

            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        } else {
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
        }

        if((ticksExisted & lodLvl) == lodLvl) {

            for (int i = 0; i <= 1; ++i) {
                if (this.getPaddleState(i)) {
                    if (!this.isSilent() && (double) (this.paddlePositions[i] % ((float) Math.PI * 2F)) <= (Math.PI / 4D) && ((double) this.paddlePositions[i] + 0.39269909262657166D) % (Math.PI * 2D) >= (Math.PI / 4D)) {
                        SoundEvent soundevent = this.getPaddleSound();

                        if (soundevent != null) {
                            Vec3d vec3d = this.getLook(1.0F);
                            double d0 = i == 1 ? -vec3d.z : vec3d.z;
                            double d1 = i == 1 ? vec3d.x : -vec3d.x;
                            this.world.playSound(null, this.posX + d0, this.posY, this.posZ + d1, soundevent, this.getSoundCategory(), 1.0F, 0.8F + 0.4F * this.rand.nextFloat());
                        }
                    }
                    this.paddlePositions[i] = (float) ((double) this.paddlePositions[i] + 0.39269909262657166D);
                } else this.paddlePositions[i] = 0.0F;
            }

            if (lodLvl > EntityLodFunc.LOW1_MASK) return;

            this.doBlockCollisions();
            AxisAlignedBB box = world.commonWorldEntityAABB;
            this.getEntityBoundingBox().growTo(0.20000000298023224D, -0.009999999776482582D, 0.20000000298023224D, box);
            List<Entity> list = this.world.getEntitiesInAABBexcluding(this, box, EntitySelectors.getTeamCollisionPredicate(this));

            if (!list.isEmpty()) {
                boolean flag = !this.world.isRemote && !(this.getControllingPassenger() instanceof EntityPlayer);

                for (int j = 0; j < list.size(); ++j) {
                    Entity entity = list.get(j);

                    if (!entity.isPassenger(this)) {
                        if (flag && this.getPassengers().size() < 2 && !entity.isRiding() && entity.width < this.width && entity instanceof EntityLivingBase && !(entity instanceof EntityWaterMob) && !(entity instanceof EntityPlayer)) {
                            entity.startRiding(this);
                        } else
                            this.applyEntityCollision(entity);
                    }
                }
            }
        }
    }


    @Nullable
    protected SoundEvent getPaddleSound() {
        switch (this.getBoatStatus()) {
            case IN_WATER:
            case UNDER_WATER:
            case UNDER_FLOWING_WATER:
                return SoundEvents.ENTITY_BOAT_PADDLE_WATER;
            case ON_LAND:
                return SoundEvents.ENTITY_BOAT_PADDLE_LAND;
            case IN_AIR:
            default:
                return null;
        }
    }


    private void tickLerp() {
        if (this.lerpSteps > 0 && !this.canPassengerSteer()) {
            double d0 = this.posX + (this.lerpX - this.posX) / (double)this.lerpSteps;
            double d1 = this.posY + (this.lerpY - this.posY) / (double)this.lerpSteps;
            double d2 = this.posZ + (this.lerpZ - this.posZ) / (double)this.lerpSteps;
            double d3 = MathHelper.wrapDegrees(this.lerpYaw - (double)this.rotationYaw);
            this.rotationYaw = (float)((double)this.rotationYaw + d3 / (double)this.lerpSteps);
            this.rotationPitch = (float)((double)this.rotationPitch + (this.lerpPitch - (double)this.rotationPitch) / (double)this.lerpSteps);
            --this.lerpSteps;
            //NOT EDIT THIS! Otherwise, the entity's AABB will end up in the pool and will be reused
            this.setPosition(d0, d1, d2);
            this.setRotation(this.rotationYaw, this.rotationPitch);
        } else {
            this.setEntityBoundingBox(getCollisionBoundingBox().copy());
        }
    }


    public void setPaddleState(boolean left, boolean right) {
        this.dataManager.set(DATA_ID_PADDLE[0], left);
        this.dataManager.set(DATA_ID_PADDLE[1], right);
    }


    @SideOnly(Side.CLIENT)
    public float getRowingTime(int side, float limbSwing) {
        return this.getPaddleState(side) ? (float)MathHelper.clampedLerp((double)this.paddlePositions[side] - 0.39269909262657166D, (double)this.paddlePositions[side], (double)limbSwing) : 0.0F;
    }


    private EntityBoat.Status getBoatStatus() {
        EntityBoat.Status status = this.getUnderwaterStatus();
        if (status != null) {
            this.waterLevel = this.getEntityBoundingBox().maxY;
            return status;
        } else if (this.checkInWater()) {
            return EntityBoat.Status.IN_WATER;
        } else {
            float f = this.getBoatGlide();
            if (f > 0.0F) {
                this.boatGlide = f;
                return EntityBoat.Status.ON_LAND;
            } else
                return EntityBoat.Status.IN_AIR;
        }
    }


    public float getWaterLevelAbove() {
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        int i = MathHelper.floor(axisalignedbb.minX);
        int j = MathHelper.ceil(axisalignedbb.maxX);
        int k = MathHelper.floor(axisalignedbb.maxY);
        int l = MathHelper.ceil(axisalignedbb.maxY - this.lastYd);
        int i1 = MathHelper.floor(axisalignedbb.minZ);
        int j1 = MathHelper.ceil(axisalignedbb.maxZ);

        IBlockEAccess access = world.access();
        BlockPos.MutableBlockPos pos = world.localTempPos;
        label108: for (int k1 = k; k1 < l; ++k1) {
            float f = 0.0F;
            int l1 = i;

            while (true) {
                if (l1 >= j) {
                    if (f < 1.0F) return (float)pos.getY() + f;
                    break;
                }

                for (int i2 = i1; i2 < j1; ++i2) {
                    pos.setPos(l1, k1, i2);
                    IBlockState iblockstate = access.getBlockState(pos);

                    Boolean result = iblockstate.getBlock().isAABBInsideMaterial(world, pos, new AxisAlignedBB(pos), Material.WATER);
                    if (result != null) {
                        if (!result) continue;
                        f = Math.max(f, iblockstate.getBlock().getBlockLiquidHeight(world, pos, iblockstate, Material.WATER) + pos.getY());
                    }

                    if (iblockstate.getMaterial() == Material.WATER) {
                        f = Math.max(f, BlockLiquid.getBlockLiquidHeight(iblockstate, this.world, pos));
                    }
                    if (f >= 1.0F) continue label108;
                }
                ++l1;
            }
        }
        return (float)(l + 1);
    }


    public float getBoatGlide() {
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        AxisAlignedBB axisalignedbb1 = new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY - 0.001D, axisalignedbb.minZ, axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ);
        int i = MathHelper.floor(axisalignedbb1.minX) - 1;
        int j = MathHelper.ceil(axisalignedbb1.maxX) + 1;
        int k = MathHelper.floor(axisalignedbb1.minY) - 1;
        int l = MathHelper.ceil(axisalignedbb1.maxY) + 1;
        int i1 = MathHelper.floor(axisalignedbb1.minZ) - 1;
        int j1 = MathHelper.ceil(axisalignedbb1.maxZ) + 1;
        List<AxisAlignedBB> list = Lists.newArrayList();
        float f = 0.0F;
        int k1 = 0;
        BlockPos.MutableBlockPos pos = world.localTempPos;
        IBlockEAccess access = world.access();

        for (int l1 = i; l1 < j; ++l1) {
            for (int i2 = i1; i2 < j1; ++i2) {
                int j2 = (l1 != i && l1 != j - 1 ? 0 : 1) + (i2 != i1 && i2 != j1 - 1 ? 0 : 1);

                if (j2 != 2) {
                    for (int k2 = k; k2 < l; ++k2) {
                        if (j2 <= 0 || k2 != k && k2 != l - 1) {
                            pos.setPos(l1, k2, i2);
                            IBlockState iblockstate = access.getBlockState(pos);
                            iblockstate.addCollisionBoxToList(this.world, pos, axisalignedbb1, list, this, false);

                            if (!list.isEmpty()) {
                                f += iblockstate.getBlock().getSlipperiness(iblockstate, access, pos, this);
                                ++k1;
                            }
                            list.clear();
                        }
                    }
                }
            }
        }
        return f / (float)k1;
    }


    private boolean checkInWater() {
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        int i = MathHelper.floor(axisalignedbb.minX);
        int j = MathHelper.ceil(axisalignedbb.maxX);
        int k = MathHelper.floor(axisalignedbb.minY);
        int l = MathHelper.ceil(axisalignedbb.minY + 0.001D);
        int i1 = MathHelper.floor(axisalignedbb.minZ);
        int j1 = MathHelper.ceil(axisalignedbb.maxZ);
        boolean flag = false;
        this.waterLevel = Double.MIN_VALUE;
        BlockPos.MutableBlockPos pos = world.localTempPos;
        IBlockEAccess access = world.access();

        for (int k1 = i; k1 < j; ++k1) {
            for (int l1 = k; l1 < l; ++l1) {
                for (int i2 = i1; i2 < j1; ++i2) {
                    pos.setPos(k1, l1, i2);
                    IBlockState iblockstate = access.getBlockState(pos);

                    Boolean result = iblockstate.getBlock().isAABBInsideMaterial(world, pos, axisalignedbb, Material.WATER);
                    if (result != null) {
                        if (!result) continue;

                        float f = iblockstate.getBlock().getBlockLiquidHeight(world, pos, iblockstate, Material.WATER) + pos.getY();
                        this.waterLevel = Math.max(f, this.waterLevel);
                        flag |= axisalignedbb.minY < (double)f;
                    }

                    if (iblockstate.getMaterial() == Material.WATER) {
                        float f = BlockLiquid.getLiquidHeight(iblockstate, this.world, pos);
                        this.waterLevel = Math.max(f, this.waterLevel);
                        flag |= axisalignedbb.minY < (double)f;
                    }
                }
            }
        }
        return flag;
    }


    @Nullable
    private EntityBoat.Status getUnderwaterStatus() {
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        double d0 = axisalignedbb.maxY + 0.001D;
        int i = MathHelper.floor(axisalignedbb.minX);
        int j = MathHelper.ceil(axisalignedbb.maxX);
        int k = MathHelper.floor(axisalignedbb.maxY);
        int l = MathHelper.ceil(d0);
        int i1 = MathHelper.floor(axisalignedbb.minZ);
        int j1 = MathHelper.ceil(axisalignedbb.maxZ);
        boolean flag = false;
        BlockPos.MutableBlockPos pos = world.localTempPos;
        IBlockEAccess access = world.access();

        for (int k1 = i; k1 < j; ++k1) {
            for (int l1 = k; l1 < l; ++l1) {
                for (int i2 = i1; i2 < j1; ++i2) {
                    pos.setPos(k1, l1, i2);
                    IBlockState iblockstate = access.getBlockState(pos);

                    Boolean result = iblockstate.getBlock().isAABBInsideMaterial(world, pos, axisalignedbb, Material.WATER);
                    if (result != null) {
                        if (!result) continue;

                        if(iblockstate.getBlock().getBlockLiquidHeight(world, pos, iblockstate, Material.WATER) > 0) {
                            return EntityBoat.Status.UNDER_FLOWING_WATER;
                        } else
                            continue;
                    }

                    if (iblockstate.getMaterial() == Material.WATER && d0 < (double)BlockLiquid.getLiquidHeight(iblockstate, this.world, pos)) {
                        if (iblockstate.getValue(BlockLiquid.LEVEL) != 0) {
                            return Status.UNDER_FLOWING_WATER;
                        }
                        flag = true;
                    }
                }
            }
        }
        return flag ? EntityBoat.Status.UNDER_WATER : null;
    }


    private void updateMotion() {
        double d1 = this.hasNoGravity() ? 0.0D : -0.03999999910593033D;
        double d2 = 0.0D;
        float momentum = 0.05F;

        if (this.previousStatus == EntityBoat.Status.IN_AIR && this.status != EntityBoat.Status.IN_AIR && this.status != EntityBoat.Status.ON_LAND) {
            this.waterLevel = this.getEntityBoundingBox().minY + (double)this.height;
            this.setPosition(this.posX, (double)(this.getWaterLevelAbove() - this.height) + 0.101D, this.posZ);
            this.motionY = 0.0D;
            this.lastYd = 0.0D;
            this.status = EntityBoat.Status.IN_WATER;
        } else {
            if (this.status == EntityBoat.Status.IN_WATER) {
                d2 = (this.waterLevel - this.getEntityBoundingBox().minY) / (double)this.height;
                momentum = 0.9F;
            } else if (this.status == EntityBoat.Status.UNDER_FLOWING_WATER) {
                d1 = -7.0E-4D;
                momentum = 0.9F;
            } else if (this.status == EntityBoat.Status.UNDER_WATER) {
                d2 = 0.009999999776482582D;
                momentum = 0.45F;
            } else if (this.status == EntityBoat.Status.IN_AIR) {
                momentum = 0.9F;
            } else if (this.status == EntityBoat.Status.ON_LAND) {
                momentum = this.boatGlide;

                if (this.getControllingPassenger() instanceof EntityPlayer) {
                    this.boatGlide /= 2.0F;
                }
            }
            this.motionX *= momentum;
            this.motionZ *= momentum;
            this.deltaRotation *= momentum;
            this.motionY += d1;

            if (d2 > 0.0D) {
                this.motionY += d2 * 0.06153846016296973D;
                this.motionY *= 0.75D;
            }
        }
    }


    private void controlBoat() {
        if (this.isBeingRidden()) {
            float f = 0.0F;

            if (this.leftInputDown) {
                this.deltaRotation += -1.0F;
            }

            if (this.rightInputDown) {
                ++this.deltaRotation;
            }

            if (this.rightInputDown != this.leftInputDown && !this.forwardInputDown && !this.backInputDown) {
                f += 0.005F;
            }

            this.rotationYaw += this.deltaRotation;

            if (this.forwardInputDown) {
                f += 0.04F;
            }

            if (this.backInputDown) {
                f -= 0.005F;
            }
            this.motionX += MathHelper.sin(-this.rotationYaw * 0.017453292F) * f;
            this.motionZ += MathHelper.cos(this.rotationYaw * 0.017453292F) * f;
            this.setPaddleState(this.rightInputDown && !this.leftInputDown || this.forwardInputDown, this.leftInputDown && !this.rightInputDown || this.forwardInputDown);
        }
    }


    @Override
    public void updatePassenger(Entity passenger) {
        if (this.isPassenger(passenger)) {
            float f = 0.0F;
            float f1 = (float)((this.isDead ? 0.009999999776482582D : this.getMountedYOffset()) + passenger.getYOffset());

            if (this.getPassengers().size() > 1) {
                int i = this.getPassengers().indexOf(passenger);

                if (i == 0) f = 0.2F;
                else f = -0.6F;

                if (passenger instanceof EntityAnimal) f = (float)((double)f + 0.2D);
            }

            Vec3d vec3d = (new Vec3d((double)f, 0.0D, 0.0D)).rotateYaw(-this.rotationYaw * 0.017453292F - ((float)Math.PI / 2F));
            passenger.setPosition(this.posX + vec3d.x, this.posY + (double)f1, this.posZ + vec3d.z);
            passenger.rotationYaw += this.deltaRotation;
            passenger.setRotationYawHead(passenger.getRotationYawHead() + this.deltaRotation);
            this.applyYawToEntity(passenger);

            if (passenger instanceof EntityAnimal && this.getPassengers().size() > 1) {
                int j = passenger.getEntityId() % 2 == 0 ? 90 : 270;
                passenger.setRenderYawOffset(((EntityAnimal)passenger).renderYawOffset + (float)j);
                passenger.setRotationYawHead(passenger.getRotationYawHead() + (float)j);
            }
        }
    }


    protected void applyYawToEntity(Entity entityToUpdate) {
        entityToUpdate.setRenderYawOffset(this.rotationYaw);
        float f = MathHelper.wrapDegrees(entityToUpdate.rotationYaw - this.rotationYaw);
        float f1 = MathHelper.clamp(f, -105.0F, 105.0F);
        entityToUpdate.prevRotationYaw += f1 - f;
        entityToUpdate.rotationYaw += f1 - f;
        entityToUpdate.setRotationYawHead(entityToUpdate.rotationYaw);
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void applyOrientationToEntity(Entity entityToUpdate) {
        this.applyYawToEntity(entityToUpdate);
    }


    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setString("Type", this.getBoatType().getName());
    }


    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("Type", 8)) {
            this.setBoatType(EntityBoat.Type.getTypeFromString(nbt.getString("Type")));
        }
    }


    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        if (player.isSneaking()) return false;
        else {
            if (!this.world.isRemote && this.outOfControlTicks < 60.0F)
                player.startRiding(this);
            return true;
        }
    }


    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
        this.lastYd = this.motionY;

        if (!this.isRiding()) {
            if (onGroundIn) {
                if (this.fallDistance > 3.0F) {
                    if (this.status != EntityBoat.Status.ON_LAND) {
                        this.fallDistance = 0.0F;
                        return;
                    }
                    this.fall(this.fallDistance, 1.0F);

                    if (!this.world.isRemote && !this.isDead) {
                        this.setDead();

                        if (this.world.getGameRules().doEntityDrops.getBoolean()) {
                            for (int i = 0; i < 3; ++i) {
                                this.entityDropItem(new ItemStack(Item.getItemFromBlock(Blocks.PLANKS), 1, this.getBoatType().getMetadata()), 0.0F);
                            }
                            for (int j = 0; j < 2; ++j) {
                                this.dropItemWithOffset(Items.STICK, 1, 0.0F);
                            }
                        }
                    }
                }
                this.fallDistance = 0.0F;
            } else if (this.world.getBlockState((new BlockPos(this))
                    .down()).getMaterial() != Material.WATER && y < 0.0D) {
                this.fallDistance = (float)((double)this.fallDistance - y);
            }
        }
    }


    public boolean getPaddleState(int side) {
        return this.dataManager.get(DATA_ID_PADDLE[side]) && this.getControllingPassenger() != null;
    }


    public void setDamageTaken(float damageTaken) {
        this.dataManager.set(DAMAGE_TAKEN, damageTaken);
    }


    public float getDamageTaken() {
        return this.dataManager.get(DAMAGE_TAKEN);
    }


    public void setTimeSinceHit(int timeSinceHit) {
        this.dataManager.set(TIME_SINCE_HIT, timeSinceHit);
    }


    public int getTimeSinceHit() {
        return this.dataManager.get(TIME_SINCE_HIT);
    }


    public void setForwardDirection(int forwardDirection) {
        this.dataManager.set(FORWARD_DIRECTION, forwardDirection);
    }


    public int getForwardDirection() {
        return this.dataManager.get(FORWARD_DIRECTION);
    }


    public void setBoatType(EntityBoat.Type boatType) {
        this.dataManager.set(BOAT_TYPE, boatType.ordinal());
    }


    public EntityBoat.Type getBoatType() {
        return EntityBoat.Type.byId(this.dataManager.get(BOAT_TYPE));
    }


    @Override
    protected boolean canFitPassenger(Entity passenger) {
        return this.getPassengers().size() < 2;
    }


    @Nullable
    public Entity getControllingPassenger() {
        List<Entity> list = this.getPassengers();
        return list.isEmpty() ? null : list.get(0);
    }


    @SideOnly(Side.CLIENT)
    public void updateInputs(boolean p_184442_1_, boolean p_184442_2_, boolean p_184442_3_, boolean p_184442_4_) {
        this.leftInputDown = p_184442_1_;
        this.rightInputDown = p_184442_2_;
        this.forwardInputDown = p_184442_3_;
        this.backInputDown = p_184442_4_;
    }


    public static enum Status {
        IN_WATER,
        UNDER_WATER,
        UNDER_FLOWING_WATER,
        ON_LAND,
        IN_AIR;
    }


    public static enum Type {
        OAK(BlockPlanks.EnumType.OAK.getMetadata(), "oak"),
        SPRUCE(BlockPlanks.EnumType.SPRUCE.getMetadata(), "spruce"),
        BIRCH(BlockPlanks.EnumType.BIRCH.getMetadata(), "birch"),
        JUNGLE(BlockPlanks.EnumType.JUNGLE.getMetadata(), "jungle"),
        ACACIA(BlockPlanks.EnumType.ACACIA.getMetadata(), "acacia"),
        DARK_OAK(BlockPlanks.EnumType.DARK_OAK.getMetadata(), "dark_oak");

        private final String name;
        private final int metadata;

        private Type(int metadataIn, String nameIn) {
            this.name = nameIn;
            this.metadata = metadataIn;
        }


        public String getName() {
            return this.name;
        }


        public int getMetadata() {
            return this.metadata;
        }


        public String toString() {
            return this.name;
        }


        public static EntityBoat.Type byId(int id) {
            if (id < 0 || id >= values().length) {
                id = 0;
            }
            return values()[id];
        }

        public static EntityBoat.Type getTypeFromString(String nameIn) {
            for (int i = 0; i < values().length; ++i) {
                if (values()[i].getName().equals(nameIn)) {
                    return values()[i];
                }
            }
            return values()[0];
        }
    }

    // Forge: Fix MC-119811 by instantly completing lerp on board
    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        if(this.canPassengerSteer() && this.lerpSteps > 0) {
            this.lerpSteps = 0;
            this.posX = this.lerpX;
            this.posY = this.lerpY;
            this.posZ = this.lerpZ;
            this.rotationYaw = (float)this.lerpYaw;
            this.rotationPitch = (float)this.lerpPitch;
        }
    }


    @Override
    public int lodLvl() {
        return ElcaMobs.BOAT.lodLvl(elcaMinRange);
    }
}