package net.minecraft.entity.item;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackData;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.fserver.ElcaConfig;
import plus.fserver.ElcaMobs;
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.utls.collections.ArrayListE6;

import java.util.List;


public class EntityItem extends Entity {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final DataParameter<ItemStack> ITEM = EntityDataManager.createKey(EntityItem.class, DataSerializers.ITEM_STACK);
    private int age;
    private int pickupDelay;
    private int health;
    private String thrower;
    private String owner;
    public float hoverStart;

    /**
     * The maximum age of this EntityItem.  The item is expired once this is reached.
     */
    public int lifespan = 6000;
    private EntityDataManager.DataEntry<ItemStack> item;


    public EntityItem(World worldIn, double x, double y, double z) {
        super(worldIn);
        this.health = 5;
        this.hoverStart = (float)(Math.random() * Math.PI * 2.0D);
        this.setSize(0.25F, 0.25F);
        this.setPosition(x, y, z);
        this.rotationYaw = (float)(Math.random() * 360.0D);
        this.motionX = (float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D);
        this.motionY = 0.20000000298023224D;
        this.motionZ = (float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D);
    }


    public EntityItem(World worldIn, double x, double y, double z, ItemStack stack) {
        this(worldIn, x, y, z);
        this.setItem(stack);
        this.lifespan = (stack.getItem() == null ? 6000 : stack.getItem().getEntityLifespan(stack, worldIn));
    }


    @Override
    protected boolean canTriggerWalking() {
        return false;
    }


    public EntityItem(World worldIn) {
        super(worldIn);
        this.health = 5;
        this.hoverStart = (float)(Math.random() * Math.PI * 2.0D);
        this.setSize(0.25F, 0.25F);
        this.setItem(ItemStack.EMPTY);
    }


    @Override
    protected void entityInit() {
        item = this.getDataManager().registerV(ITEM, ItemStack.EMPTY);
    }


    @Override
    public void onUpdate() {
        ItemStack item = this.getItem();
        if (item.getItem().onEntityItemUpdate(this)) return;
        if (item.isEmpty()) {
            this.setDead();
        } else {
            if (this.pickupDelay > 0 && this.pickupDelay != 32767) {
                --this.pickupDelay;
            }
            int lodLvl;
            if(((lodLvl = lodLvl()) & ticksExisted) != lodLvl)return;
            onUpdateE();

            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            double d0 = this.motionX;
            double d1 = this.motionY;
            double d2 = this.motionZ;

            if (lodLvl == 0 && !this.hasNoGravity() || (age & 1) == 1) {
                this.motionY -= 0.03999999910593033D;
            }

            if (!this.world.isRemote && lodLvl == 0) {
                this.noClip = this.pushOutOfBlocks(this.posX, (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D, this.posZ);
            } else {
                this.noClip = false;
            }

            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);

            if (((int)this.prevPosX != (int)this.posX || (int)this.prevPosY != (int)this.posY || (int)this.prevPosZ != (int)this.posZ) || this.ticksExisted % 25 == 0) {
                BlockPos blockpos = world.localTempPos3.setPos(this);
                if (this.world.access().getBlockState(blockpos).getMaterial() == Material.LAVA) {
                    this.motionY = 0.20000000298023224D;
                    this.motionX = (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F;
                    this.motionZ = (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F;
                    this.playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.4F, 2.0F + this.rand.nextFloat() * 0.4F);
                }

                if (!this.world.isRemote) {
                    this.searchForOtherItemsNearby();
                }
            }

            float f = 0.98F;

            if (this.onGround) {
                BlockPos underPos = world.localTempPos3.setPos(
                        MathHelper.floor(this.posX), MathHelper.floor(this.getEntityBoundingBox().minY) - 1, MathHelper.floor(this.posZ)
                );
                IBlockState underState = this.world.access().getBlockState(underPos);
                f = underState.getBlock().getSlipperiness(underState, this.world, underPos, this) * 0.98F;
            }

            this.motionX *= f;
            this.motionY *= 0.9800000190734863D;
            this.motionZ *= f;

            if (this.onGround) {
                this.motionY *= -0.5D;
            }

            if (this.age != -32768) {
                ++this.age;
            }

            //this.handleWaterMovement();

            if (!this.world.isRemote) {
                double d3 = this.motionX - d0;
                double d4 = this.motionY - d1;
                double d5 = this.motionZ - d2;
                double d6 = d3 * d3 + d4 * d4 + d5 * d5;

                if (d6 > 0.01D) {
                    this.isAirBorne = true;
                }
            }

            if (!this.world.isRemote && this.age >= lifespan) {
                int hook = ForgeEventFactory.onItemExpire(this, item);
                if (hook < 0) this.setDead();
                else          this.lifespan += hook;
            }
            //if (item.isEmpty()) this.setDead();
        }
    }


    private void searchForOtherItemsNearby() {
        if((age>>2 & 3) != 3)return;
        final ItemStack stack = this.getItem();
        if(stack.getCount() >= stack.getMaxStackSize())return;

        for (EntityItem entityitem : this.world.getEntitiesWithinAABB(EntityItem.class,
                this.getEntityBoundingBox().grow(0.73D, 0.0D, 0.73D))) {
            this.combineItems(entityitem);
        }
    }


    private boolean combineItems(EntityItem other) {
        if (other == this) {
            return false;
        } else if (other.isEntityAlive() && this.isEntityAlive()) {
            final ItemStack stack = this.getItem();
            if (stack.getCount() >= stack.getMaxStackSize()) return false;

            ItemStack oStack = other.getItem();

            if (this.pickupDelay != 32767 && other.pickupDelay != 32767) {
                if (this.age != -32768 && other.age != -32768) {
                    if (oStack.getItem() != stack.getItem()) {
                        return false;
                    } else if (oStack.hasTagCompound() ^ stack.hasTagCompound()) {
                        return false;
                    } else if (oStack.hasTagCompound() && !oStack.getTagCompound().equals(stack.getTagCompound())) {
                        return false;
                    } else if (oStack.getItem() == null) {
                        return false;
                    } else if (oStack.getItem().getHasSubtypes() && oStack.getMetadata() != stack.getMetadata()) {
                        return false;
                    } else if (oStack.getCount() < stack.getCount()) {
                        return other.combineItems(this);
                    } else if (oStack.getCount() + stack.getCount() > oStack.getMaxStackSize()) {
                        return false;
                    } else if (!stack.areCapsCompatible(oStack)) {
                        return false;
                    } else {
                        oStack.grow(stack.getCount());
                        other.pickupDelay = Math.max(other.pickupDelay, this.pickupDelay);
                        other.age = Math.min(other.age, this.age);
                        other.setItem(oStack);
                        this.setDead();
                        return true;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


    public void setAgeToCreativeDespawnTime() {
        this.age = 4800;
    }


    @Override
    public boolean handleWaterMovement() {
        if (this.world.handleMaterialAcceleration(this.getEntityBoundingBox(), Material.WATER, this)) {
            if (!this.inWater && !this.firstUpdate) {
                this.doWaterSplashEffect();
            }
            this.inWater = true;
        } else {
            this.inWater = false;
        }
        return this.inWater;
    }


    @Override
    protected void dealFireDamage(int amount) {
        this.attackEntityFrom(DamageSource.IN_FIRE, (float)amount);
    }


    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.world.isRemote || this.isDead) return false; //Forge: Fixes MC-53850
        if (this.isEntityInvulnerable(source)) {
            return false;
        }
        else if (!this.getItem().isEmpty() && this.getItem().getItem() == Items.NETHER_STAR && source.isExplosion()) {
            return false;
        } else {
            this.markVelocityChanged();
            this.health = (int)((float)this.health - amount);

            if (this.health <= 0)
            {
                this.setDead();
            }
            return false;
        }
    }


    public static void registerFixesItem(DataFixer fixer) {
        fixer.registerWalker(FixTypes.ENTITY, new ItemStackData(EntityItem.class, "Item"));
    }


    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setShort("Health", (short)this.health);
        nbt.setShort("Age", (short)this.age);
        nbt.setShort("PickupDelay", (short)this.pickupDelay);
        nbt.setInteger("Lifespan", lifespan);

        if (this.getThrower() != null) {
            nbt.setString("Thrower", this.thrower);
        }

        if (this.getOwner() != null) {
            nbt.setString("Owner", this.owner);
        }

        if (!this.getItem().isEmpty()) {
            nbt.setTag("Item", this.getItem().writeToNBT(new NBTTagCompound()));
        }
    }


    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        this.health = nbt.getShort("Health");
        this.age = nbt.getShort("Age");

        if (nbt.hasKey("PickupDelay")) {
            this.pickupDelay = nbt.getShort("PickupDelay");
        }

        if (nbt.hasKey("Owner")) {
            this.owner = nbt.getString("Owner");
        }

        if (nbt.hasKey("Thrower")) {
            this.thrower = nbt.getString("Thrower");
        }

        NBTTagCompound nbttagcompound = nbt.getCompoundTag("Item");
        this.setItem(new ItemStack(nbttagcompound));

        if (this.getItem().isEmpty()) {
            this.setDead();
        }
        if (nbt.hasKey("Lifespan")) lifespan = nbt.getInteger("Lifespan");
    }


    @Override
    public void onCollideWithPlayer(EntityPlayer entityIn) {
        if (!this.world.isRemote) {
            if (this.pickupDelay > 0) return;
            ItemStack itemstack = this.getItem();
            Item item = itemstack.getItem();
            int i = itemstack.getCount();

            int hook = ForgeEventFactory.onItemPickup(this, entityIn);
            if (hook < 0) return;
            ItemStack clone = itemstack.copy();

            if (this.pickupDelay <= 0 && (this.owner == null || lifespan - this.age <= 200 || this.owner.equals(entityIn.getName())) && (hook == 1 || i <= 0 || entityIn.inventory.addItemStackToInventory(itemstack) || clone.getCount() > this.getItem().getCount())) {
                clone.setCount(clone.getCount() - this.getItem().getCount());
                net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerItemPickupEvent(entityIn, this, clone);

                if (itemstack.isEmpty()) {
                    entityIn.onItemPickup(this, i);
                    this.setDead();
                    itemstack.setCount(i);
                }
                entityIn.addStat(StatList.getObjectsPickedUpStats(item), i);
            }
        }
    }


    @Override
    public String getName() {
        return this.hasCustomName() ? this.getCustomNameTag() : I18n.translateToLocal("item." + this.getItem().getTranslationKey());
    }


    @Override
    public boolean canBeAttackedWithItem() {
        return false;
    }


    @Nullable
    public Entity changeDimension(int dimensionIn, net.minecraftforge.common.util.ITeleporter teleporter) {
        Entity entity = super.changeDimension(dimensionIn, teleporter);

        if (!this.world.isRemote && entity instanceof EntityItem) {
            ((EntityItem)entity).searchForOtherItemsNearby();
        }
        return entity;
    }


    public ItemStack getItem() {
        return item.getValue();
    }


    public void setItem(ItemStack stack) {
        item.setValue(stack);
        item.setDirty(true);
    }


    public String getOwner() {
        return this.owner;
    }


    public void setOwner(String owner) {
        this.owner = owner;
    }


    public String getThrower() {
        return this.thrower;
    }


    public void setThrower(String thrower) {
        this.thrower = thrower;
    }


    @SideOnly(Side.CLIENT)
    public int getAge() {
        return this.age;
    }


    public void setDefaultPickupDelay() {
        this.pickupDelay = 10;
    }

    public void setNoPickupDelay() {
        this.pickupDelay = 0;
    }


    public void setInfinitePickupDelay() {
        this.pickupDelay = 32767;
    }


    public void setPickupDelay(int ticks) {
        this.pickupDelay = ticks;
    }


    public boolean cannotPickup() {
        return this.pickupDelay > 0;
    }

    public void setNoDespawn() {
        this.age = -6000;
    }


    public void makeFakeItem() {
        this.setInfinitePickupDelay();
        this.age = getItem().getItem().getEntityLifespan(getItem(), world) - 1;
    }


    private ArrayListE6<AxisAlignedBB> defBoxes(double x, double y, double z){
        AxisAlignedBB box = world.commonWorldEntityAABB;
        getEntityBoundingBox().expandTo(x, y, z, box);
        return this.world.getCollisionBoxesLimitedC(null, box, ElcaConfig.MAX_ENTITY_COLLISIONS_MOVE);
    }


    @Override
    public void move(MoverType type, double x, double y, double z) {
        if (this.noClip) {
            this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, y, z));
            this.resetPositionToBB();
        } else {
            if (this.isInWeb) {
                this.isInWeb = false;
                x *= 0.25D;
                y *= 0.05000000074505806D;
                z *= 0.25D;
                this.motionX = 0.0D;
                this.motionY = 0.0D;
                this.motionZ = 0.0D;
            }

            double oldX = x;
            double oldY = y;
            double oldZ = z;

            ArrayListE6<AxisAlignedBB> boxes = null;
            AxisAlignedBB curBox = this.getEntityBoundingBox();
            int temp, curX, curY, curZ;
            if (y != 0.0D) {
                boxes = defBoxes(x, y, z);
                temp = 0;
                for (curY = boxes.sizeC(); temp < curY; ++temp)
                    y = boxes.getC(temp).calculateYOffset(curBox, y);
            }
            if (x != 0.0D) {
                temp = 0;
                if(boxes == null)boxes = defBoxes(x, y, z);

                for (curX = boxes.sizeC(); temp < curX; ++temp)
                    x = boxes.getC(temp).calculateXOffset(curBox, x);

            }

            if (z != 0.0D) {
                temp = 0;
                if(boxes == null)boxes = defBoxes(x, y, z);

                for (curZ = boxes.sizeC(); temp < curZ; ++temp)
                    z = boxes.getC(temp).calculateZOffset(curBox, z);

            }
            if(boxes != null) {
                curBox.offsetMut(x, y, z);
                this.resetPositionToBB();
            }

            this.collided = (collidedVertically = oldY != y) || (collidedHorizontally = oldX != x || oldZ != z);

            Chunk chunk;
            IBlockState state = (chunk = this.world.getChunk(
                    (curX = MathHelper.floor(this.posX)) >> 4,
                    (curZ = MathHelper.floor(this.posZ)) >> 4
            )).getBlockState(
                    curX,
                    (curY = MathHelper.floor(this.posY - 0.20000000298023224D)),
                    curZ
            );

            if(onGround = collidedVertically && oldY < 0.0D) {
                if(fallDistance>0) {
                    BlockPos blockpos;
                    if (state.getMaterial() == Material.AIR) {
                        IBlockState iblockstate1 = chunk.getBlockState(curX, curY - 1, curZ);
                        Block block1 = iblockstate1.getBlock();
                        if (block1 instanceof BlockFence || block1 instanceof BlockWall || block1 instanceof BlockFenceGate) {
                            state = iblockstate1;
                            blockpos = new BlockPos(curX, curY - 1, curZ);
                        } else {
                            blockpos = new BlockPos(curX, curY, curZ);
                        }
                    } else
                        blockpos = new BlockPos(curX, curY, curZ);

                    state.getBlock().onFallenUpon(this.world, blockpos, this, fallDistance);
                }
                fallDistance = 0;
            } else
                this.fallDistance = (float)((double)this.fallDistance - y);

            if (oldX != x) this.motionX = 0.0D;
            if (oldZ != z) this.motionZ = 0.0D;
            if (oldY != y) state.getBlock().onLanded(this.world, this);

            try {
                this.doBlockCollisions();
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Checking entity block collision");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being checked for collision");
                this.addEntityCrashInfo(crashreportcategory);
                world.getMinecraftServer().ticker().onError((WorldServer) world, new ReportedException(crashreport));
            }

            AxisAlignedBB res;
            curBox.growTo(-0.01D, -0.01D, -0.01D, res = world.commonWorldEntityAABB);

            if (this.world.isFlammableWithin(res)) {
                this.dealFireDamage(1);
                if (!this.isWet()) this.setFire(8);
            }
        }
    }


    @Override
    protected boolean pushOutOfBlocks(double x, double y, double z) {
        if(--rideCooldown<0)return false;

        if (!this.world.collidesWithAnyBlock(this.getEntityBoundingBox())) {
            rideCooldown = 5;
            return false;
        } else {
            motionY += 0.1;
            rideCooldown = 0;

            int bX = MathHelper.floor(x);
            int bY = MathHelper.floor(y);
            int bZ = MathHelper.floor(z);
            double dX = x - bX;
            double dY = y - bY;
            double dZ = z - bZ;

            double d3 = Double.MAX_VALUE;

            EnumFacing facing = EnumFacing.UP;
            ChunkSelector selector = world.localSelector;
            BlockPos.MutableBlockPos pos = world.localTempPos3.setPos(bX-1, bY, bZ);

            if (!selector.isBlockFullCube(pos) && dX < d3) {
                d3 = dX;
                facing = EnumFacing.WEST;
            }

            if (!selector.isBlockFullCube(pos.setPos(bX+1, bY, bZ)) && 1.0D - dX < d3) {
                d3 = 1.0D - dX;
                facing = EnumFacing.EAST;
            }

            if (!selector.isBlockFullCube(pos.setPos(bX, bY, bZ-1)) && dZ < d3) {
                d3 = dZ;
                facing = EnumFacing.NORTH;
            }

            if (!selector.isBlockFullCube(pos.setPos(bX, bY, bZ+1)) && 1.0D - dZ < d3) {
                d3 = 1.0D - dZ;
                facing = EnumFacing.SOUTH;
            }

            if (!selector.isBlockFullCube(pos.setPos(bX, bY+1, bZ)) && 1.0D - dY < d3) {
                facing = EnumFacing.UP;
            }

            float f = this.rand.nextFloat() * 0.2F + 0.1F;
            float f1 = (float)facing.getAxisDirection().getOffset();

            if (facing.getAxis() == EnumFacing.Axis.X) {
                this.motionX = f1 * f;
                this.motionY *= 0.75D;
                this.motionZ *= 0.75D;
            } else if (facing.getAxis() == EnumFacing.Axis.Y) {
                this.motionX *= 0.75D;
                this.motionY = f1 * f;
                this.motionZ *= 0.75D;
            } else if (facing.getAxis() == EnumFacing.Axis.Z) {
                this.motionX *= 0.75D;
                this.motionY *= 0.75D;
                this.motionZ = f1 * f;
            }
            return true;
        }
    }


    @Override
    public int lodLvl() {
        return ElcaMobs.ITEM.lodLvl(elcaMinRange);
    }
}
