package net.minecraft.entity.projectile;

import net.minecraft.entity.player.*;
import net.minecraftforge.fml.relauncher.*;
import net.minecraft.block.material.*;
import net.minecraft.block.*;
import net.minecraft.entity.*;
import net.minecraft.block.state.*;
import java.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.util.*;
import net.minecraft.nbt.*;
import net.minecraft.world.storage.loot.*;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.common.*;
import net.minecraft.entity.item.*;
import net.minecraft.init.*;
import net.minecraft.stats.*;
import net.minecraft.item.*;
import net.minecraft.network.datasync.*;
import plus.fserver.server.elca.ChunkSelector;


public class EntityFishHook extends Entity {
    private static final DataParameter<Integer> DATA_HOOKED_ENTITY;
    private boolean inGround;
    private int ticksInGround;
    private EntityPlayer angler;
    private int ticksInAir;
    private int ticksCatchable;
    private int ticksCaughtDelay;
    private int ticksCatchableDelay;
    private float fishApproachAngle;
    public Entity caughtEntity;
    private State currentState;
    private int luck;
    private int lureSpeed;

    @SideOnly(Side.CLIENT)
    public EntityFishHook(final World worldIn, final EntityPlayer p_i47290_2_, final double x, final double y, final double z) {
        super(worldIn);
        this.currentState = State.FLYING;
        this.init(p_i47290_2_);
        this.setPosition(x, y, z);
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
    }

    public EntityFishHook(final World worldIn, final EntityPlayer fishingPlayer) {
        super(worldIn);
        this.currentState = State.FLYING;
        this.init(fishingPlayer);
        this.shoot();
    }

    private void init(final EntityPlayer p_190626_1_) {
        this.setSize(0.25f, 0.25f);
        this.ignoreFrustumCheck = true;
        this.angler = p_190626_1_;
        this.angler.fishEntity = this;
    }

    public void setLureSpeed(final int p_191516_1_) {
        this.lureSpeed = p_191516_1_;
    }

    public void setLuck(final int p_191517_1_) {
        this.luck = p_191517_1_;
    }

    private void shoot() {
        final float f = this.angler.prevRotationPitch + (this.angler.rotationPitch - this.angler.prevRotationPitch);
        final float f2 = this.angler.prevRotationYaw + (this.angler.rotationYaw - this.angler.prevRotationYaw);
        final float f3 = MathHelper.cos(-f2 * 0.017453292f - 3.1415927f);
        final float f4 = MathHelper.sin(-f2 * 0.017453292f - 3.1415927f);
        final float f5 = -MathHelper.cos(-f * 0.017453292f);
        final float f6 = MathHelper.sin(-f * 0.017453292f);
        final double d0 = this.angler.prevPosX + (this.angler.posX - this.angler.prevPosX) - f4 * 0.3;
        final double d2 = this.angler.prevPosY + (this.angler.posY - this.angler.prevPosY) + this.angler.getEyeHeight();
        final double d3 = this.angler.prevPosZ + (this.angler.posZ - this.angler.prevPosZ) - f3 * 0.3;
        this.setLocationAndAngles(d0, d2, d3, f2, f);
        this.motionX = -f4;
        this.motionY = MathHelper.clamp(-(f6 / f5), -5.0f, 5.0f);
        this.motionZ = -f3;
        final float f7 = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
        this.motionX *= 0.6 / f7 + 0.5 + this.rand.nextGaussian() * 0.0045;
        this.motionY *= 0.6 / f7 + 0.5 + this.rand.nextGaussian() * 0.0045;
        this.motionZ *= 0.6 / f7 + 0.5 + this.rand.nextGaussian() * 0.0045;
        final float f8 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * 57.29577951308232);
        this.rotationPitch = (float)(MathHelper.atan2(this.motionY, f8) * 57.29577951308232);
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
    }


    @Override
    protected void entityInit() {
        this.getDataManager().registerU(EntityFishHook.DATA_HOOKED_ENTITY, 0);
    }


    @Override
    public void notifyDataManagerChange(final DataParameter<?> key) {
        if (EntityFishHook.DATA_HOOKED_ENTITY.equals(key)) {
            final int i = this.getDataManager().get(EntityFishHook.DATA_HOOKED_ENTITY);
            this.caughtEntity = ((i > 0) ? this.world.getEntityByID(i - 1) : null);
        }
        super.notifyDataManagerChange(key);
    }


    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(final double distance) {
        return distance < 4096.0;
    }


    @SideOnly(Side.CLIENT)
    public void setPositionAndRotationDirect(final double x, final double y, final double z, final float yaw, final float pitch, final int posRotationIncrements, final boolean teleport) {}


    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.angler == null) {
            this.setDead();
        }
        else if (this.world.isRemote || !this.shouldStopFishing()) {
            if (this.inGround) {
                ++this.ticksInGround;
                if (this.ticksInGround >= 1200) {
                    this.setDead();
                    return;
                }
            }
            float f = 0.0f;
            final BlockPos blockpos = new BlockPos(this);
            final IBlockState iblockstate = this.world.access().getBlockState(blockpos);
            if (iblockstate.getMaterial() == Material.WATER) {
                f = BlockLiquid.getBlockLiquidHeight(iblockstate, this.world.access(), blockpos);
            }
            if (this.currentState == State.FLYING) {
                if (this.caughtEntity != null) {
                    this.motionX = 0.0;
                    this.motionY = 0.0;
                    this.motionZ = 0.0;
                    this.currentState = State.HOOKED_IN_ENTITY;
                    return;
                }
                if (f > 0.0f) {
                    this.motionX *= 0.3;
                    this.motionY *= 0.2;
                    this.motionZ *= 0.3;
                    this.currentState = State.BOBBING;
                    return;
                }
                if (!this.world.isRemote) {
                    this.checkCollision();
                }
                if (!this.inGround && !this.onGround && !this.collidedHorizontally) {
                    ++this.ticksInAir;
                }
                else {
                    this.ticksInAir = 0;
                    this.motionX = 0.0;
                    this.motionY = 0.0;
                    this.motionZ = 0.0;
                }
            }
            else {
                if (this.currentState == State.HOOKED_IN_ENTITY) {
                    if (this.caughtEntity != null) {
                        if (this.caughtEntity.isDead) {
                            this.caughtEntity = null;
                            this.currentState = State.FLYING;
                        }
                        else {
                            this.posX = this.caughtEntity.posX;
                            final double d2 = this.caughtEntity.height;
                            this.posY = this.caughtEntity.getEntityBoundingBox().minY + d2 * 0.8;
                            this.posZ = this.caughtEntity.posZ;
                            this.mutPosition(this.posX, this.posY, this.posZ);
                        }
                    }
                    return;
                }
                if (this.currentState == State.BOBBING) {
                    this.motionX *= 0.9;
                    this.motionZ *= 0.9;
                    double d3 = this.posY + this.motionY - blockpos.getY() - f;
                    if (Math.abs(d3) < 0.01) {
                        d3 += Math.signum(d3) * 0.1;
                    }
                    this.motionY -= d3 * this.rand.nextFloat() * 0.2;
                    if (!this.world.isRemote && f > 0.0f) {
                        this.catchingFish(blockpos);
                    }
                }
            }
            if (iblockstate.getMaterial() != Material.WATER) {
                this.motionY -= 0.03;
            }
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ, this::completeMove);
        }
    }


    private void completeMove(){
        this.updateRotation();
        this.motionX *= 0.92;
        this.motionY *= 0.92;
        this.motionZ *= 0.92;
        this.mutPosition(this.posX, this.posY, this.posZ);
    }


    private boolean shouldStopFishing() {
        final ItemStack itemstack = this.angler.getHeldItemMainhand();
        final ItemStack itemstack2 = this.angler.getHeldItemOffhand();
        final boolean flag = itemstack.getItem() instanceof ItemFishingRod;
        final boolean flag2 = itemstack2.getItem() instanceof ItemFishingRod;
        if (!this.angler.isDead && this.angler.isEntityAlive() && (flag || flag2) && this.getDistanceSq((Entity)this.angler) <= 1024.0) {
            return false;
        }
        this.setDead();
        return true;
    }

    private void updateRotation() {
        final float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * 57.29577951308232);
        this.rotationPitch = (float)(MathHelper.atan2(this.motionY, f) * 57.29577951308232);
        while (this.rotationPitch - this.prevRotationPitch < -180.0f) {
            this.prevRotationPitch -= 360.0f;
        }
        while (this.rotationPitch - this.prevRotationPitch >= 180.0f) {
            this.prevRotationPitch += 360.0f;
        }
        while (this.rotationYaw - this.prevRotationYaw < -180.0f) {
            this.prevRotationYaw -= 360.0f;
        }
        while (this.rotationYaw - this.prevRotationYaw >= 180.0f) {
            this.prevRotationYaw += 360.0f;
        }
        this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2f;
        this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2f;
    }


    private void checkCollision() {
        Vec3d vec3d = new Vec3d(this.posX, this.posY, this.posZ);
        Vec3d vec3d2 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

        RayTraceResult rtxResult1 = this.world.rayTraceBlocks(vec3d, vec3d2, false, true, false);
        vec3d = new Vec3d(this.posX, this.posY, this.posZ);

        if (rtxResult1 != null) {
            vec3d2 = new Vec3d(rtxResult1.hitVec.x, rtxResult1.hitVec.y, rtxResult1.hitVec.z);
        } else {
            vec3d2 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
        }

        Entity entity = null;
        AxisAlignedBB aabb = this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ);
        aabb.growTo(1,1,1, aabb);

        final List<Entity> list =
                this.world.getEntitiesInAABBexcludingLimited(this,
                        aabb, EntitySelectors.NOT_SPECTATING, 4
                );

        double d0 = 0.0;
        for (final Entity entity2 : list) {
            if (this.canBeHooked(entity2) && (entity2 != this.angler || this.ticksInAir >= 5)) {

                entity2.getEntityBoundingBox().growTo(0.30000001192092896, 0.30000001192092896, 0.30000001192092896, aabb);

                final RayTraceResult rtxResult2 = aabb.calculateIntercept(vec3d, vec3d2);
                if (rtxResult2 == null) {
                    continue;
                }
                final double d2 = vec3d.squareDistanceTo(rtxResult2.hitVec);
                if (d2 >= d0 && d0 != 0.0) {
                    continue;
                }
                entity = entity2;
                d0 = d2;
            }
        }
        if (entity != null) {
            rtxResult1 = new RayTraceResult(entity);
        }
        if (rtxResult1 != null && rtxResult1.typeOfHit != RayTraceResult.Type.MISS) {
            if (rtxResult1.typeOfHit == RayTraceResult.Type.ENTITY) {
                this.caughtEntity = rtxResult1.entityHit;
                this.setHookedEntity();
            }
            else {
                this.inGround = true;
            }
        }
    }


    private void setHookedEntity() {
        this.getDataManager().set(EntityFishHook.DATA_HOOKED_ENTITY, this.caughtEntity.getEntityId() + 1);
    }


    private void catchingFish(final BlockPos pos) {
        final WorldServer world = (WorldServer)this.world;
        final ChunkSelector selector = world.localSelector; //safe
        int i = 1;
        final BlockPos blockpos = pos.up();
        if (this.rand.nextFloat() < 0.25f && world.isRainingAt(blockpos)) {
            ++i;
        }
        if (this.rand.nextFloat() < 0.5f && !selector.getChunk(blockpos).canSeeSky(blockpos)) {
            --i;
        }
        if (this.ticksCatchable > 0) {
            --this.ticksCatchable;
            if (this.ticksCatchable <= 0) {
                this.ticksCaughtDelay = 0;
                this.ticksCatchableDelay = 0;
            }
            else {
                this.motionY -= 0.2 * this.rand.nextFloat() * this.rand.nextFloat();
            }
        }
        else if (this.ticksCatchableDelay > 0) {
            this.ticksCatchableDelay -= i;
            if (this.ticksCatchableDelay > 0) {
                this.fishApproachAngle += (float)(this.rand.nextGaussian() * 4.0);
                final float f = this.fishApproachAngle * 0.017453292f;
                final float f2 = MathHelper.sin(f);
                final float f3 = MathHelper.cos(f);
                final double d0 = this.posX + f2 * this.ticksCatchableDelay * 0.1f;
                final double d2 = MathHelper.floor(this.getEntityBoundingBox().minY) + 1.0f;
                final double d3 = this.posZ + f3 * this.ticksCatchableDelay * 0.1f;

                final IBlockState state = selector.getBlockState(
                        MathHelper.floor(d0),
                        MathHelper.floor(d2 - 1.0),
                        MathHelper.floor(d3)
                );

                if (state.getMaterial() == Material.WATER) {
                    if (this.rand.nextFloat() < 0.15f) {
                        world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, d0, d2 - 0.10000000149011612, d3, 1, f2, 0.1, f3, 0.0, new int[0]);
                    }
                    final float f4 = f2 * 0.04f;
                    final float f5 = f3 * 0.04f;
                    world.spawnParticle(EnumParticleTypes.WATER_WAKE, d0, d2, d3, 0, f5, 0.01, -f4, 1.0, new int[0]);
                    world.spawnParticle(EnumParticleTypes.WATER_WAKE, d0, d2, d3, 0, -f5, 0.01, f4, 1.0, new int[0]);
                }
            }
            else {
                this.motionY = -0.4f * MathHelper.nextFloat(this.rand, 0.6f, 1.0f);
                this.playSound(SoundEvents.ENTITY_BOBBER_SPLASH, 0.25f, 1.0f + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4f);
                final double d4 = this.getEntityBoundingBox().minY + 0.5;
                world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX, d4, this.posZ, (int)(1.0f + this.width * 20.0f), this.width, 0.0, this.width, 0.20000000298023224, new int[0]);
                world.spawnParticle(EnumParticleTypes.WATER_WAKE, this.posX, d4, this.posZ, (int)(1.0f + this.width * 20.0f), this.width, 0.0, this.width, 0.20000000298023224, new int[0]);
                this.ticksCatchable = MathHelper.getInt(this.rand, 20, 40);
            }
        }
        else if (this.ticksCaughtDelay > 0) {
            this.ticksCaughtDelay -= i;
            float f6 = 0.15f;
            if (this.ticksCaughtDelay < 20) {
                f6 += (float)((20 - this.ticksCaughtDelay) * 0.05);
            }
            else if (this.ticksCaughtDelay < 40) {
                f6 += (float)((40 - this.ticksCaughtDelay) * 0.02);
            }
            else if (this.ticksCaughtDelay < 60) {
                f6 += (float)((60 - this.ticksCaughtDelay) * 0.01);
            }
            if (this.rand.nextFloat() < f6) {
                final float f7 = MathHelper.nextFloat(this.rand, 0.0f, 360.0f) * 0.017453292f;
                final float f8 = MathHelper.nextFloat(this.rand, 25.0f, 60.0f);
                final double d5 = this.posX + MathHelper.sin(f7) * f8 * 0.1f;
                final double d6 = MathHelper.floor(this.getEntityBoundingBox().minY) + 1.0f;
                final double d7 = this.posZ + MathHelper.cos(f7) * f8 * 0.1f;

                final IBlockState state = selector.getBlockState(
                        (int)d5, (int)d6 - 1, (int)d7
                );

                if (state.getMaterial() == Material.WATER) {
                    world.spawnParticle(EnumParticleTypes.WATER_SPLASH, d5, d6, d7, 2 + this.rand.nextInt(2), 0.10000000149011612, 0.0, 0.10000000149011612, 0.0, new int[0]);
                }
            }
            if (this.ticksCaughtDelay <= 0) {
                this.fishApproachAngle = MathHelper.nextFloat(this.rand, 0.0f, 360.0f);
                this.ticksCatchableDelay = MathHelper.getInt(this.rand, 20, 80);
            }
        }
        else {
            this.ticksCaughtDelay = MathHelper.getInt(this.rand, 100, 600);
            this.ticksCaughtDelay -= this.lureSpeed * 20 * 5;
        }
    }


    protected boolean canBeHooked(final Entity entity) {
        return entity.canBeCollidedWith() || entity instanceof EntityItem;
    }


    @Override
    public void writeEntityToNBT(final NBTTagCompound compound) {}


    @Override
    public void readEntityFromNBT(final NBTTagCompound compound) {}


    public int handleHookRetraction() {
        if (!this.world.isRemote && this.angler != null) {
            int i = 0;
            ItemFishedEvent event = null;
            if (this.caughtEntity != null) {
                this.bringInHookedEntity();
                this.world.setEntityState(this, (byte)31);
                i = ((this.caughtEntity instanceof EntityItem) ? 3 : 5);
            }
            else if (this.ticksCatchable > 0) {
                final LootContext.Builder lootBuilder = new LootContext.Builder((WorldServer)this.world);
                lootBuilder.withLuck(this.luck + this.angler.getLuck()).withPlayer(this.angler).withLootedEntity(this);
                final List<ItemStack> result = this.world.getLootTableManager().getLootTableFromLocation(LootTableList.GAMEPLAY_FISHING).generateLootForPools(this.rand, lootBuilder.build());
                event = new ItemFishedEvent(result, this.inGround ? 2 : 1, this);
                MinecraftForge.EVENT_BUS.post(event);
                if (event.isCanceled()) {
                    this.setDead();
                    return event.getRodDamage();
                }
                for (final ItemStack itemstack : result) {
                    final EntityItem entityitem = new EntityItem(this.world, this.posX, this.posY, this.posZ, itemstack);
                    final double d0 = this.angler.posX - this.posX;
                    final double d2 = this.angler.posY - this.posY;
                    final double d3 = this.angler.posZ - this.posZ;
                    final double d4 = MathHelper.sqrt(d0 * d0 + d2 * d2 + d3 * d3);
                    final double d5 = 0.1;
                    entityitem.motionX = d0 * 0.1;
                    entityitem.motionY = d2 * 0.1 + MathHelper.sqrt(d4) * 0.08;
                    entityitem.motionZ = d3 * 0.1;
                    this.world.spawnEntity(entityitem);
                    this.angler.world.spawnEntity(new EntityXPOrb(this.angler.world, this.angler.posX, this.angler.posY + 0.5, this.angler.posZ + 0.5, this.rand.nextInt(6) + 1));
                    final Item item = itemstack.getItem();
                    if (item == Items.FISH || item == Items.COOKED_FISH) {
                        this.angler.addStat(StatList.FISH_CAUGHT, 1);
                    }
                }
                i = 1;
            }
            if (this.inGround) {
                i = 2;
            }
            this.setDead();
            return (event == null) ? i : event.getRodDamage();
        }
        return 0;
    }


    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(final byte id) {
        if (id == 31 && this.world.isRemote && this.caughtEntity instanceof EntityPlayer && ((EntityPlayer)this.caughtEntity).isUser()) {
            this.bringInHookedEntity();
        }
        super.handleStatusUpdate(id);
    }


    protected void bringInHookedEntity() {
        if (this.angler != null) {
            final double d0 = this.angler.posX - this.posX;
            final double d2 = this.angler.posY - this.posY;
            final double d3 = this.angler.posZ - this.posZ;
            final Entity caughtEntity = this.caughtEntity;
            caughtEntity.motionX += d0 * 0.1;
            final Entity caughtEntity2 = this.caughtEntity;
            caughtEntity2.motionY += d2 * 0.1;
            final Entity caughtEntity3 = this.caughtEntity;
            caughtEntity3.motionZ += d3 * 0.1;
        }
    }


    @Override
    protected boolean canTriggerWalking() {
        return false;
    }


    @Override
    public void setDead() {
        super.setDead();
        if (this.angler != null) {
            this.angler.fishEntity = null;
        }
    }


    public EntityPlayer getAngler() {
        return this.angler;
    }


    @Override
    public int lodLvl() {
        return 0;
    }

    
    static {
        DATA_HOOKED_ENTITY = EntityDataManager.createKey(EntityFishHook.class, DataSerializers.VARINT);
    }


    enum State {
        FLYING,
        HOOKED_IN_ENTITY,
        BOBBING;
    }
}