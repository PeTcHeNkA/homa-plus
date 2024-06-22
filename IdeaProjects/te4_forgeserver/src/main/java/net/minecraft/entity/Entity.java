package net.minecraft.entity;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Mirror;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IDataWalker;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import plus.fserver.ElcaConfig;
import plus.fserver.ElcaMobs;
import plus.fserver.api.IBlockEAccess;
import plus.fserver.ElcaCV;
import plus.fserver.server.SStrings;
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.utls.collections.ArrayListE6;
import plus.tson.utl.Te4HashSet;
import static plus.fserver.ElcaCV.SERVER_COMPILE;


public abstract class Entity implements ICommandSender, net.minecraftforge.common.capabilities.ICapabilitySerializable<NBTTagCompound> {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final List<ItemStack> EMPTY_EQUIPMENT = Collections.emptyList();
    private static final AxisAlignedBB ZERO_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
    private static double renderDistanceWeight = 1.0D;
    private static int nextEntityID;
    private int entityId;
    public boolean preventEntitySpawning;
    private final List<Entity> riddenByEntities;
    protected byte rideCooldown; //todo check mini error
    private Entity ridingEntity;
    public boolean forceSpawn;
    public World world;
    public double prevPosX;
    public double prevPosY;
    public double prevPosZ;
    public double posX;
    public double posY;
    public double posZ;
    public double motionX;
    public double motionY;
    public double motionZ;
    public float rotationYaw;
    public float rotationPitch;
    public float prevRotationYaw;
    public float prevRotationPitch;
    private AxisAlignedBB boundingBox;
    public boolean onGround;
    public boolean collidedHorizontally;
    public boolean collidedVertically;
    public boolean collided;
    public boolean velocityChanged;
    protected boolean isInWeb;
    //todo maybe error
    //private boolean isOutsideBorder;
    public boolean isDead;
    public float width;
    public float height;
    public float prevDistanceWalkedModified;
    public float distanceWalkedModified;
    public float distanceWalkedOnStepModified;
    public float fallDistance;
    private int nextStepDistance;
    private float nextFlap;
    public double lastTickPosX;
    public double lastTickPosY;
    public double lastTickPosZ;
    public float stepHeight;
    public boolean noClip;
    public float entityCollisionReduction;
    protected Random rand;
    public int ticksExisted;
    private short fire; //todo check mini error
    protected boolean inWater;
    public int hurtResistantTime;
    protected boolean firstUpdate;
    protected boolean isImmuneToFire;
    protected EntityDataManager dataManager;
    protected static final DataParameter<Byte> FLAGS = EntityDataManager.createKey(Entity.class, DataSerializers.BYTE);
    static final DataParameter<Integer> AIR = EntityDataManager.createKey(Entity.class, DataSerializers.VARINT);
    static final DataParameter<String> CUSTOM_NAME = EntityDataManager.createKey(Entity.class, DataSerializers.STRING);
    static final DataParameter<Boolean> CUSTOM_NAME_VISIBLE = EntityDataManager.createKey(Entity.class, DataSerializers.BOOLEAN);
    static final DataParameter<Boolean> SILENT = EntityDataManager.createKey(Entity.class, DataSerializers.BOOLEAN);
    static final DataParameter<Boolean> NO_GRAVITY = EntityDataManager.createKey(Entity.class, DataSerializers.BOOLEAN);
    public boolean addedToChunk;
    public int chunkCoordX;
    public int chunkCoordY;
    public int chunkCoordZ;
    @SideOnly(Side.CLIENT)
    public long serverPosX;
    @SideOnly(Side.CLIENT)
    public long serverPosY;
    @SideOnly(Side.CLIENT)
    public long serverPosZ;
    public boolean ignoreFrustumCheck;
    public boolean isAirBorne;
    public int timeUntilPortal;
    protected boolean inPortal;
    protected byte portalCounter; //todo check mini error
    public int dimension;
    protected BlockPos lastPortalPos;
    protected Vec3d lastPortalVec;
    protected EnumFacing teleportDirection;
    private boolean invulnerable;
    protected UUID entityUniqueID;
    protected String cachedUniqueIdString;
    @Nullable
    private CommandResultStats cmdResultStats;
    protected boolean glowing;
    private final Set<String> tags;
    private boolean isPositionDirty;
    private final float[] pistonDeltas; //todo check mini error
    private long pistonDeltasGameTime;

    public EntityTrackerEntry trackerEntry;
    public double elcaMinRange;
    //1 = track flag
    //public byte lodLvl = 0;

    //public boolean isTracked(){return (lodLvl & 1) == 1;}

    /**
     * Setting this to true will prevent the world from calling {@link #onUpdate()} for this entity.
     */
    public boolean updateBlocked;


    public Entity(World worldIn) {
        this.entityId = nextEntityID++;
        this.riddenByEntities = new ArrayList<>();
        //use zero AABB is safe
        this.boundingBox = ZERO_AABB;
        this.width = 0.6F;
        this.height = 1.8F;
        this.nextStepDistance = 1;
        this.nextFlap = 1.0F;

        //todo use local or thread - bad idea
        //this.rand = ThreadLocalRandom.current();
        if (worldIn != null) {
            this.dimension = worldIn.provider.getDimension();
            this.rand = worldIn.commonWorldEntityRandom;
        } else {
            this.rand = new Random();
        }

        this.fire = (short) -this.getFireImmuneTicks();
        this.firstUpdate = true;
        this.entityUniqueID = MathHelper.getRandomUUID(this.rand);
        this.cachedUniqueIdString = this.entityUniqueID.toString();
        //this.cmdResultStats = new CommandResultStats();
        this.tags = new Te4HashSet<>(4);
        this.pistonDeltas = new float[] {0.0f, 0.0f, 0.0f};
        this.world = worldIn;
        //replace zero AABB to self
        this.setPosition(0.0D, 0.0D, 0.0D);

        if(ElcaCV.DEBUG_NO_SERVER)return;

        EntityDataManager manager = this.dataManager = new EntityDataManager(this);
        manager.register(FLAGS, (byte) 0);
        manager.register(AIR, 300);
        manager.register(CUSTOM_NAME_VISIBLE, Boolean.FALSE);
        manager.register(CUSTOM_NAME, "");
        manager.register(SILENT, Boolean.FALSE);
        manager.register(NO_GRAVITY, Boolean.FALSE);

        this.entityInit();

        MinecraftForge.EVENT_BUS.post(
                new EntityEvent.EntityConstructing(this)
        );
        capabilities = ForgeEventFactory.gatherCapabilities(this);
        onCliInit();
    }


    protected void onCliInit(){}

    /** Forge: Used to store custom data for each entity. */
    private NBTTagCompound customEntityData;
    public boolean captureDrops = false;
    public java.util.ArrayList<EntityItem> capturedDrops = new java.util.ArrayList<>();
    private net.minecraftforge.common.capabilities.CapabilityDispatcher capabilities;


    public int getEntityId() {
        return this.entityId;
    }


    public void setEntityId(int id) {
        this.entityId = id;
    }


    public Set<String> getTags() {
        return this.tags;
    }


    public boolean addTag(String tag) {
        if (this.tags.size() >= 1024) return false;
        else {
            this.tags.add(tag);
            return true;
        }
    }


    public boolean removeTag(String tag) {
        return this.tags.remove(tag);
    }


    public void onKillCommand() {
        this.setDead();
    }


    protected abstract void entityInit();


    public EntityDataManager getDataManager() {
        return this.dataManager;
    }


    public boolean equals(Object p_equals_1_) {
        if (p_equals_1_ instanceof Entity) {
            return ((Entity)p_equals_1_).entityId == this.entityId;
        } else {
            return false;
        }
    }


    public int hashCode() {
        return this.entityId;
    }


    @SideOnly(Side.CLIENT)
    protected void preparePlayerToSpawn() {
        if (this.world != null) {
            while (this.posY > 0.0D && this.posY < 256.0D) {
                this.setPosition(this.posX, this.posY, this.posZ);

                if (this.world.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty()) {
                    break;
                }

                ++this.posY;
            }
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
            this.rotationPitch = 0.0F;
        }
    }


    public void setDead() {
        this.isDead = true;
    }


    public void setDropItemsWhenDead(boolean dropWhenDead) {}


    protected void setSize(float width, float height) {
        if (width != this.width || height != this.height) {
            float f = this.width;
            this.width = width;
            this.height = height;

            if (this.width < f) {
                double d0 = (double)width / 2.0D;
                this.setEntityBoundingBox(new AxisAlignedBB(this.posX - d0, this.posY, this.posZ - d0, this.posX + d0, this.posY + (double)this.height, this.posZ + d0));
                return;
            }

            AxisAlignedBB box = this.getEntityBoundingBox();
            box.init(
                    box.minX, box.minY, box.minZ, box.minX + (double)this.width,
                    box.minY + (double)this.height, box.minZ + (double)this.width
            );
            this.setEntityBoundingBox(box);

            if (this.width > f && !this.firstUpdate && !this.world.isRemote)
                this.move(MoverType.SELF, f - this.width, 0.0D, f - this.width);
        }
    }


    protected void setRotation(float yaw, float pitch) {
        this.rotationYaw = yaw % 360.0F;
        this.rotationPitch = pitch % 360.0F;
    }


    public void setPosition(double x, double y, double z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        // Forge - Process chunk registration after moving.
        if (this.isAddedToWorld() && !this.world.isRemote)
            this.world.updateEntityWithOptionalForce(this, false);
        float f = this.width / 2.0F;
        float f1 = this.height;
        //NOT EDIT THIS! CRITICAL ERROR
        this.setEntityBoundingBox(new AxisAlignedBB(x - (double)f, y, z - (double)f, x + (double)f, y + (double)f1, z + (double)f));
    }


    public void mutPosition(double x, double y, double z){
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        // Forge - Process chunk registration after moving.
        if (this.isAddedToWorld() && !this.world.isRemote)
            this.world.updateEntityWithOptionalForce(this, false);
        float f = this.width / 2.0F;
        float f1 = this.height;

        AxisAlignedBB box = this.getEntityBoundingBox();
        box.init(x - (double)f, y, z - (double)f, x + (double)f, y + (double)f1, z + (double)f);
        this.setEntityBoundingBox(box);
    }


    @SideOnly(Side.CLIENT)
    public void turn(float yaw, float pitch) {
        float f = this.rotationPitch;
        float f1 = this.rotationYaw;
        this.rotationYaw = (float)((double)this.rotationYaw + (double)yaw * 0.15D);
        this.rotationPitch = (float)((double)this.rotationPitch - (double)pitch * 0.15D);
        this.rotationPitch = MathHelper.clamp(this.rotationPitch, -90.0F, 90.0F);
        this.prevRotationPitch += this.rotationPitch - f;
        this.prevRotationYaw += this.rotationYaw - f1;

        if (this.ridingEntity != null) {
            this.ridingEntity.applyOrientationToEntity(this);
        }
    }


    public void onUpdate() {
        onUpdateE();
    }


    protected final void onUpdateE(){
        if (!this.world.isRemote || SERVER_COMPILE) this.setFlag(6, this.isGlowing());
        this.onEntityUpdate();
    }


    public void onEntityUpdate() {
        if (this.isRiding() && this.getRidingEntity().isDead) this.dismountRidingEntity();
        if (this.rideCooldown > 0) --this.rideCooldown;

        this.prevDistanceWalkedModified = this.distanceWalkedModified;
        this.prevPosX                   = this.posX;
        this.prevPosY                   = this.posY;
        this.prevPosZ                   = this.posZ;
        this.prevRotationPitch          = this.rotationPitch;
        this.prevRotationYaw            = this.rotationYaw;

        if (!this.world.isRemote || SERVER_COMPILE) {
            if (this.inPortal) {
                MinecraftServer server = this.world.getMinecraftServer();

                if (server.getAllowNether()) {
                    if (!this.isRiding()) {
                        int i = this.getMaxInPortalTime();
                        if (this.portalCounter++ >= i) {
                            this.portalCounter = (byte) i;
                            this.timeUntilPortal = this.getPortalCooldown();
                            int j;

                            if (this.world.provider.getDimensionType().getId() == -1) j = 0;
                            else j = -1;

                            this.changeDimension(j);
                        }
                    }
                    this.inPortal = false;
                }
            } else {
                if (this.portalCounter > 0) this.portalCounter -= 4;
                if (this.portalCounter < 0) this.portalCounter  = 0;
            }
            this.decrementTimeUntilPortal();
        }

        this.spawnRunningParticles();
        this.handleWaterMovement();

        if (this.world.isRemote) this.extinguish();
        else if (this.fire > 0) {
            if (this.isImmuneToFire) {
                this.fire -= 4;

                if (this.fire < 0) this.extinguish();
            } else
                if (--this.fire % 20 == 0) this.attackEntityFrom(DamageSource.ON_FIRE, 1.0F);
        }
        if(!inWater && (ticksExisted & 1) == 1)
            if (this.isInLava()) {
                this.setOnFireFromLava();
                this.fallDistance *= 0.5F;
            }

        if (this.posY < -64.0D) this.outOfWorld();
        if (!this.world.isRemote || SERVER_COMPILE) this.setFlag(0, this.fire > 0);

        this.firstUpdate = false;
    }


    protected void decrementTimeUntilPortal() {
        if (this.timeUntilPortal > 0) {
            --this.timeUntilPortal;
        }
    }


    public int getMaxInPortalTime() {
        return 1;
    }


    protected void setOnFireFromLava() {
        if (!this.isImmuneToFire) {
            this.attackEntityFrom(DamageSource.LAVA, 4.0F);
            this.setFire(15);
        }
    }


    public void setFire(int seconds) {
        int i = seconds * 20;
        if (this instanceof EntityLivingBase) {
            i = EnchantmentProtection.getFireTimeForEntity((EntityLivingBase)this, i);
        }
        if (this.fire < i) {
            this.fire = (short) i;
        }
    }


    public void extinguish() {
        this.fire = 0;
    }


    protected void outOfWorld() {
        this.setDead();
    }


    public boolean isOffsetPositionInLiquid(double x, double y, double z) {
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox().offset(x, y, z);
        return this.isLiquidPresentInAABB(axisalignedbb);
    }


    private boolean isLiquidPresentInAABB(AxisAlignedBB bb) {
        return this.world.getCollisionBoxes(this, bb).isEmpty() && !this.world.containsAnyLiquid(bb);
    }


    //todo hot point
    public void move(MoverType type, double x, double y, double z) {
        if (this.noClip) {
            getEntityBoundingBox().offsetMut(x, y, z);
            this.resetPositionToBB();
        } else {
            int temp, num1;
            float fl1;
            final World world = this.world;
            if (type == MoverType.PISTON) {
                long i;
                if ((i = world.getTotalWorldTime()) != this.pistonDeltasGameTime) {
                    Arrays.fill(this.pistonDeltas, 0.0f);
                    this.pistonDeltasGameTime = i;
                }

                if (x != 0.0D) {
                    fl1 = MathHelper.clamp((float)x + this.pistonDeltas[temp = EnumFacing.Axis.X.ordinal()], -0.51f, 0.51f);
                    x = fl1 - this.pistonDeltas[temp];
                    this.pistonDeltas[temp] = fl1;

                    if (Math.abs(x) <= 9.999999747378752E-6D) return;

                } else if (y != 0.0D) {
                    fl1 = MathHelper.clamp((float)y + this.pistonDeltas[temp = EnumFacing.Axis.Y.ordinal()], -0.51f, 0.51f);
                    y = fl1 - this.pistonDeltas[temp];
                    this.pistonDeltas[temp] = fl1;

                    if (Math.abs(y) <= 9.999999747378752E-6D) return;

                } else {
                    if (z == 0.0D) return;

                    fl1 = MathHelper.clamp((float)z + this.pistonDeltas[temp = EnumFacing.Axis.Z.ordinal()], -0.51f, 0.51f);
                    z = fl1 - this.pistonDeltas[temp];
                    this.pistonDeltas[temp] = fl1;

                    if (Math.abs(z) <= 9.999999747378752E-6D) return;
                }
            }

            double dx0 = this.posX;
            double dy0 = this.posY;
            double dz0 = this.posZ;

            if (this.isInWeb) {
                this.isInWeb = false;
                x *= 0.25D;
                y *= 0.05000000074505806D;
                z *= 0.25D;
                this.motionX = 0.0D;
                this.motionY = 0.0D;
                this.motionZ = 0.0D;
            }

            double dx1 = x;
            double dy1 = y;
            double dz1 = z;

            AxisAlignedBB prev = this.getEntityBoundingBox();
            AxisAlignedBB axisalignedbb = world.commonWorldEntityAABB;
            //todo this only for players
            //todo maybe merge calls?
            if ((type == MoverType.SELF || type == MoverType.PLAYER)
                    && this.onGround && this.isSneaking() && this instanceof EntityPlayer) {

                fl1 = -this.stepHeight;
                ArrayListE6<AxisAlignedBB> list = world.allocAABBList();

                for (; x != 0.0D && z != 0.0D && world.noCollisionsBoxesNoCacheIn(list, this, prev.offsetToF(x, fl1, z, axisalignedbb)); dz1 = z) {
                    if      (x < 0.05D && x >= -0.05D) x =  0.0D;
                    else if (x > 0.0D)                 x -= 0.05D;
                    else                               x += 0.05D;

                    dx1 = x;

                    if      (z < 0.05D && z >= -0.05D) z = 0.0D;
                    else if (z > 0.0D)                 z -= 0.05D;
                    else                               z += 0.05D;
                }

                for (; x != 0.0D && world.noCollisionsBoxesNoCacheIn(list, this, prev.offsetToF(x, fl1, 0.0D, axisalignedbb)); dx1 = x) {
                    if      (x < 0.05D && x >= -0.05D) x =  0.0D ;
                    else if (x > 0.0D)                 x -= 0.05D;
                    else                               x += 0.05D;
                }

                for (; z != 0.0D && world.noCollisionsBoxesNoCacheIn(list, this, prev.offsetToF(0.0D, fl1, z, axisalignedbb)); dz1 = z) {
                    if      (z < 0.05D && z >= -0.05D) z = 0.0D  ;
                    else if (z > 0.0D)                 z -= 0.05D;
                    else                               z += 0.05D;
                }

                world.freeAABBList(list);
            }

            prev.expandTo(x, y, z, axisalignedbb);
            axisalignedbb.minY -= 0.125;

            List<AxisAlignedBB> boxes = world.getCollisionBoxesLimited(this,
                    axisalignedbb, ElcaConfig.MAX_ENTITY_COLLISIONS_MOVE
            );

            axisalignedbb = this.getEntityBoundingBox();
            boolean flag;
            if (flag = y != 0.0D) {
                temp = 0;
                for (num1 = boxes.size(); temp < num1; ++temp)
                    y = boxes.get(temp).calculateYOffset(axisalignedbb, y);

            }
            if (x != 0.0D) {
                temp = 0;
                for (num1 = boxes.size(); temp < num1; ++temp)
                    x = boxes.get(temp).calculateXOffset(axisalignedbb, x);

                flag = true;
            }

            if (z != 0.0D) {
                temp = 0;

                for (num1 = boxes.size(); temp < num1; ++temp)
                    z = boxes.get(temp).calculateZOffset(axisalignedbb, z);

                flag = true;
            }

            if(flag) this.getEntityBoundingBox().offsetMut(x, y, z);


            flag = this.onGround || dy1 != y && dy1 < 0.0D;

            double dx2, dy2, dz2;
            if (this.stepHeight > 0.0F && flag && (dx1 != x || dz1 != z)) {
                dx2 = x;
                dy2 = y;
                dz2 = z;
                prev = this.getEntityBoundingBox();
                this.setEntityBoundingBox(axisalignedbb);
                y = this.stepHeight;

                final AxisAlignedBB aab2 = this.getEntityBoundingBox();
                aab2.expandTo(dx1, 0.0D, dz1, axisalignedbb = world.commonWorldEntityAABB);
                double d8 = y;
                temp = 0;

                for (num1 = boxes.size(); temp < num1; ++temp)
                    d8 = boxes.get(temp).calculateYOffset(axisalignedbb, d8);

                aab2.offsetMut(0.0D, d8, 0.0D);
                double d18 = dx1;
                temp = 0;

                for (num1 = boxes.size(); temp < num1; ++temp)
                    d18 = boxes.get(temp).calculateXOffset(aab2, d18);

                aab2.offsetMut(d18, 0.0D, 0.0D);

                double d19 = dz1;
                temp = 0;

                for (num1 = boxes.size(); temp < num1; ++temp)
                    d19 = boxes.get(temp).calculateZOffset(aab2, d19);

                aab2.offsetMut(0.0D, 0.0D, d19);

                double d20 = y;
                temp = 0;

                axisalignedbb = boundingBox;
                for (num1 = boxes.size(); temp < num1; ++temp)
                    d20 = boxes.get(temp).calculateYOffset(axisalignedbb, d20);

                double d21 = dx1;
                temp = 0;

                for (num1 = boxes.size(); temp < num1; ++temp)
                    d21 = boxes.get(temp).calculateXOffset(axisalignedbb, d21);

                double d22 = dz1;
                temp = 0;

                for (num1 = boxes.size(); temp < num1; ++temp)
                    d22 = boxes.get(temp).calculateZOffset(axisalignedbb, d22);

                if (d18 * d18 + d19 * d19 > d21 * d21 + d22 * d22) {
                    x = d18;
                    z = d19;
                    y = -d8;
                    this.setEntityBoundingBox(aab2);
                } else
                    axisalignedbb.offsetMut(x = d21, y = -d20, z = d22);

                temp = 0;

                for (num1 = boxes.size(); temp < num1; ++temp)
                    y = boxes.get(temp).calculateYOffset(this.getEntityBoundingBox(), y);

                if (dx2 * dx2 + dz2 * dz2 >= x * x + z * z) {
                    x = dx2;
                    y = dy2;
                    z = dz2;
                    this.setEntityBoundingBox(prev);
                } else {
                    (axisalignedbb = this.getEntityBoundingBox()).offsetMut(0.0D, y, 0.0D);
                    this.setEntityBoundingBox(axisalignedbb);
                }
            }
            this.resetPositionToBB();

            this.onGround = (collidedVertically = dy1 != y) && dy1 < 0.0D;
            this.collided = (collidedHorizontally = dx1 != x || dz1 != z) || this.collidedVertically;

            BlockPos.MutableBlockPos pos;
            IBlockState state = world.access().getBlockState(
                    pos = world.localTempPos.setPos(this.posX, this.posY - 0.20000000298023224D, this.posZ)
            );

            if (state.getMaterial() == Material.AIR) {
                IBlockState state1 = world.access().getBlockState(pos.getX(), pos.getY()-1, pos.getZ());
                Block block1 = state1.getBlock();

                if (block1 instanceof BlockFence || block1 instanceof BlockWall || block1 instanceof BlockFenceGate) {
                    state = state1;
                    pos.setY(pos.getY()-1);
                }
            }

            this.updateFallState(y, this.onGround, state, pos);

            if (dx1 != x) this.motionX = 0.0D;
            if (dz1 != z) this.motionZ = 0.0D;

            Block block = state.getBlock();

            if (collidedVertically) block.onLanded(this.world, this);

            if (this.canTriggerWalking() && (!this.onGround || !this.isSneaking() || !(this instanceof EntityPlayer)) && !this.isRiding()) {
                dx2 = this.posX - dx0;
                dy2 = block == Blocks.LADDER ? this.posY - dy0 : 0;
                dz2 = this.posZ - dz0;

                if (this.onGround) block.onEntityWalk(world, pos, this);


                this.distanceWalkedModified = (float)((double)this.distanceWalkedModified + (double)MathHelper.sqrt(dx2 * dx2 + dz2 * dz2) * 0.6D);
                this.distanceWalkedOnStepModified = (float)((double)this.distanceWalkedOnStepModified + (double)MathHelper.sqrt(dx2 * dx2 + dy2 * dy2 + dz2 * dz2) * 0.6D);

                if (this.distanceWalkedOnStepModified > (float)this.nextStepDistance && state.getMaterial() != Material.AIR) {
                    this.nextStepDistance = (int)this.distanceWalkedOnStepModified + 1;

                    if (this.isInWater()) {
                        Entity entity = this.isBeingRidden() && this.getControllingPassenger() != null ? this.getControllingPassenger() : this;
                        fl1 = entity == this ? 0.35F : 0.4F;
                        float f1 = MathHelper.sqrt(entity.motionX * entity.motionX * 0.20000000298023224D + entity.motionY * entity.motionY + entity.motionZ * entity.motionZ * 0.20000000298023224D) * fl1;

                        if (f1 > 1.0F) f1 = 1.0F;

                        this.playSound(this.getSwimSound(), f1, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
                    } else this.playStepSound(pos, block);

                } else if (this.distanceWalkedOnStepModified > this.nextFlap && this.makeFlySound() && state.getMaterial() == Material.AIR)
                    this.nextFlap = this.playFlySound(this.distanceWalkedOnStepModified);
            }
            try {
                this.doBlockCollisions();
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Checking entity block collision");
                CrashReportCategory cat = crashreport.makeCategory("Entity being checked for collision");
                this.addEntityCrashInfo(cat);
                if(!world.isRemote)world.getMinecraftServer().ticker().onError((WorldServer) world, new ReportedException(crashreport));
                else throw new ReportedException(crashreport);
            }

            flag = this.isWet();

            getEntityBoundingBox().growTo(-0.001D,-0.001D,-0.001D, axisalignedbb = world.commonWorldEntityAABB);

            if (world.isFlammableWithin(axisalignedbb)) {
                this.dealFireDamage(1);
                if (!flag) {
                    ++this.fire;

                    if (this.fire == 0) this.setFire(8);
                }
            } else if (this.fire <= 0) this.fire = (short) -this.getFireImmuneTicks();

            if (flag && this.isBurning()) {
                this.playSound(SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.7F, 1.6F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
                this.fire = (short) -this.getFireImmuneTicks();
            }
        }
    }


    public void resetPositionToBB() {
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        this.posX = (axisalignedbb.minX + axisalignedbb.maxX) * 0.5d;
        this.posY = axisalignedbb.minY;
        this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) * 0.5d;
        if (this.isAddedToWorld() && (!this.world.isRemote || SERVER_COMPILE))
            this.world.updateEntityWithOptionalForce(this, false);
        // Forge - Process chunk registration after moving.
    }


    protected SoundEvent getSwimSound() {
        return SoundEvents.ENTITY_GENERIC_SWIM;
    }


    protected SoundEvent getSplashSound() {
        return SoundEvents.ENTITY_GENERIC_SPLASH;
    }


    protected void doBlockCollisions() {
        AxisAlignedBB aabb = this.getEntityBoundingBox();

        int  pos1X = MathHelper.floor(aabb.minX + 0.001D),
             pos1Y = MathHelper.floor(aabb.minY + 0.001D),
             pos1Z = MathHelper.floor(aabb.minZ + 0.001D),

             pos2X = MathHelper.floor(aabb.maxX - 0.001D),
             pos2Y = MathHelper.floor(aabb.maxY - 0.001D),
             pos2Z = MathHelper.floor(aabb.maxZ - 0.001D);


        if (this.world.isAreaLoaded(pos1X, pos1Y, pos1Z, pos2X, pos2Y, pos2Z, true)) {
            BlockPos.MutableBlockPos pos = world.localTempPos;
            for (Chunk chunk = null; pos1X <= pos2X; ++pos1X)
                for (; pos1Z <= pos2Z; ++pos1Z) {
                    if(chunk == null || chunk.x != pos1X>>4 || chunk.z != pos1Z>>4)
                        chunk = world.getChunk(pos1X>>4, pos1Z>>4);
                    for (; pos1Y <= pos2Y; ++pos1Y) {
                        IBlockState iblockstate = chunk.getBlockState(pos1X, pos1Y, pos1Z);

                        try {
                            iblockstate.getBlock().onEntityCollision(this.world, pos.setPos(pos1X, pos1Y, pos1Z), iblockstate, this);
                            this.onInsideBlock(iblockstate);
                        } catch (Throwable throwable) {
                            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Colliding entity with block");
                            CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being collided with");
                            CrashReportCategory.addBlockInfo(crashreportcategory, pos, iblockstate);
                            if(world instanceof WorldServer)
                                world.getMinecraftServer().ticker().onError((WorldServer) world, new ReportedException(crashreport));
                            else throw new ReportedException(crashreport);
                        }
                    }
                }
        }
    }


    protected void onInsideBlock(IBlockState state) {}


    protected void playStepSound(BlockPos pos, Block blockIn) {
        SoundType type = blockIn.getSoundType(world.getBlockState(pos), world, pos, this);

        if (this.world.access().getBlockState(pos.getX(), pos.getY()+1, pos.getZ()).getBlock() == Blocks.SNOW_LAYER) {
            type = Blocks.SNOW_LAYER.getSoundType();
            this.playSound(type.getStepSound(), type.getVolume() * 0.15F, type.getPitch());
        } else if (!blockIn.getDefaultState().getMaterial().isLiquid()) {
            this.playSound(type.getStepSound(), type.getVolume() * 0.15F, type.getPitch());
        }
    }


    protected float playFlySound(float f) {
        return 0.0F;
    }


    protected boolean makeFlySound() {
        return false;
    }


    public void playSound(SoundEvent soundIn, float volume, float pitch) {
        if (!this.isSilent()) this.world.playSound(null, this.posX, this.posY, this.posZ, soundIn, this.getSoundCategory(), volume, pitch);
    }


    public boolean isSilent() {
        return this.dataManager.get(SILENT);
    }


    public void setSilent(boolean isSilent) {
        this.dataManager.set(SILENT, isSilent);
    }


    public boolean hasNoGravity() {
        return this.dataManager.get(NO_GRAVITY);
    }


    public void setNoGravity(boolean noGravity) {
        this.dataManager.set(NO_GRAVITY, noGravity);
    }


    protected boolean canTriggerWalking() {
        return true;
    }


    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
        if (onGroundIn) {
            if (this.fallDistance > 0.0F)
                state.getBlock().onFallenUpon(this.world, pos, this, this.fallDistance);

            this.fallDistance = 0.0F;
        } else if (y < 0.0D)
            this.fallDistance = (float)((double)this.fallDistance - y);
    }


    @Nullable
    public AxisAlignedBB getCollisionBoundingBox() {
        return null;
    }


    protected void dealFireDamage(int amount) {
        if (!this.isImmuneToFire)
            this.attackEntityFrom(DamageSource.IN_FIRE, (float)amount);
    }


    public final boolean isImmuneToFire() {
        return this.isImmuneToFire;
    }


    public void fall(float distance, float damageMultiplier) {
        if (this.isBeingRidden())
            for (Entity entity : this.getPassengers()) entity.fall(distance, damageMultiplier);
    }


    //todo unchecked
    public boolean isWet() {
        if (this.inWater) return true;
        else {
            if(!world.isRaining())return false;

            int pX = MathHelper.floor(posX),
                pY = MathHelper.floor(posY+height),
                pZ = MathHelper.floor(posZ);
            Chunk chunk = world.getChunk(pX>>4, pZ>>4);

            if(!chunk.canSeeSky(pX, pY, pZ) || chunk.getPrecipitationHeight(pX, pZ).getY() > pY) return false;

            if(ElcaCV.ENTITY_FAST_WET)return true;
            return !chunk.getBiome(pX, pZ, world.getBiomeProvider()).getEnableSnow();
        }
    }


    public boolean isInWater() {
        return this.inWater;
    }


    public boolean isOverWater() {
        return this.world.handleMaterialAcceleration(
                this.getEntityBoundingBox().grow(0.0D, -20.0D, 0.0D).shrink(0.001D), Material.WATER, this);
    }


    public boolean handleWaterMovement() {
        if (this.getRidingEntity() instanceof EntityBoat) this.inWater = false;
        else {
            AxisAlignedBB box;
            getEntityBoundingBox().growTo(-0.001D, -0.4010000059604645D, -0.001D, box = world.commonWorldEntityAABB);
            if (world.handleMaterialAcceleration(box, Material.WATER, this)) {
                if (!this.inWater && !this.firstUpdate) this.doWaterSplashEffect();

                this.fallDistance = 0.0F;
                this.inWater = true;
                this.extinguish();
            } else this.inWater = false;
        }
        return this.inWater;
    }


    protected void doWaterSplashEffect() {
        Entity entity = this.isBeingRidden() && this.getControllingPassenger() != null ? this.getControllingPassenger() : this;
        float f = entity == this ? 0.2F : 0.9F;
        float f1 = MathHelper.sqrt(entity.motionX * entity.motionX * 0.20000000298023224D + entity.motionY * entity.motionY + entity.motionZ * entity.motionZ * 0.20000000298023224D) * f;

        if (f1 > 1.0F) f1 = 1.0F;

        this.playSound(this.getSplashSound(), f1, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
        float f2 = (float)MathHelper.floor(this.getEntityBoundingBox().minY);

        for (int i = 0; (float)i < 1.0F + this.width * 20.0F; ++i) {
            float f3 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
            float f4 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
            this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX + (double)f3, (double)(f2 + 1.0F), this.posZ + (double)f4, this.motionX, this.motionY - (double)(this.rand.nextFloat() * 0.2F), this.motionZ);
        }

        for (int j = 0; (float)j < 1.0F + this.width * 20.0F; ++j) {
            float f5 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
            float f6 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
            this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX + (double)f5, (double)(f2 + 1.0F), this.posZ + (double)f6, this.motionX, this.motionY, this.motionZ);
        }
    }


    public void spawnRunningParticles() {
        if (this.isSprinting() && !this.isInWater()) this.createRunningParticles();
    }


    protected void createRunningParticles() {
        BlockPos blockpos = world.localTempPos2.setPos(
                MathHelper.floor(this.posX),
                MathHelper.floor(this.posY - 0.20000000298023224D),
                MathHelper.floor(this.posZ)
        );
        IBlockState state = this.world.getBlockState(blockpos);

        if(!state.getBlock().addRunningEffects(state, world, blockpos, this))
            if (state.getRenderType() != EnumBlockRenderType.INVISIBLE)
                this.world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX + ((double)this.rand.nextFloat() - 0.5D) * (double)this.width, this.getEntityBoundingBox().minY + 0.1D, this.posZ + ((double)this.rand.nextFloat() - 0.5D) * (double)this.width, -this.motionX * 4.0D, 1.5D, -this.motionZ * 4.0D, Block.getStateId(state));

    }


    public boolean isInsideOfMaterial(Material materialIn) {
        if (this.getRidingEntity() instanceof EntityBoat) return false;
        else {
            double d0 = this.posY + (double)this.getEyeHeight();
            BlockPos blockpos;

            IBlockState state = world.access().getBlockState(
                    blockpos = world.localTempPos2.setPos(this.posX, d0, this.posZ)
            );
            Boolean result = state.getBlock().isEntityInsideMaterial(world.access(), blockpos, state, this, d0, materialIn, true);
            if (result != null) return result;

            if (state.getMaterial() == materialIn) return ForgeHooks.isInsideOfMaterial(materialIn, this, blockpos);
            else return false;
        }
    }


    public boolean isInLava() {
        AxisAlignedBB box;
        getEntityBoundingBox().growTo(-0.10000000149011612D, -0.4000000059604645D, -0.10000000149011612D, box = world.commonWorldEntityAABB);
        return world.isMaterialInBB(box, Material.LAVA);
    }


    public void moveRelative(float strafe, float up, float forward, float friction) {
        float f = strafe * strafe + up * up + forward * forward;

        if (f >= 1.0E-4F) {
            f = MathHelper.sqrt(f);

            if (f < 1.0F) {
                f = 1.0F;
            }

            f = friction / f;
            strafe = strafe * f;
            up = up * f;
            forward = forward * f;
            float f1 = MathHelper.sin(this.rotationYaw * 0.017453292F);
            float f2 = MathHelper.cos(this.rotationYaw * 0.017453292F);
            this.motionX += strafe * f2 - forward * f1;
            this.motionY += up;
            this.motionZ += forward * f2 + strafe * f1;
        }
    }


    @SideOnly(Side.CLIENT)
    public int getBrightnessForRender() {
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(MathHelper.floor(this.posX), 0, MathHelper.floor(this.posZ));

        if (this.world.isBlockLoaded(blockPos)) {
            blockPos.setY(MathHelper.floor(this.posY + (double)this.getEyeHeight()));
            return this.world.getCombinedLight(blockPos, 0);
        } else {
            return 0;
        }
    }


    public float getBrightness() {
        BlockPos blockPos = world.localTempPos3.setPos(
                this.posX, this.posY + (double)this.getEyeHeight(), this.posZ
        );
        if (this.world.isBlockLoaded(blockPos))
            return this.world.getLightBrightness(blockPos);
        else return 0.0F;
    }


    public void setWorld(World worldIn) {
        this.world = worldIn;
    }


    public void setPositionAndRotation(double x, double y, double z, float yaw, float pitch) {
        this.posX = MathHelper.clamp(x, -3.0E7D, 3.0E7D);
        this.posY = y;
        this.posZ = MathHelper.clamp(z, -3.0E7D, 3.0E7D);
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        pitch = MathHelper.clamp(pitch, -90.0F, 90.0F);
        this.rotationYaw = yaw;
        this.rotationPitch = pitch;
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
        double d0 = this.prevRotationYaw - yaw;

        if (d0 < -180.0D) this.prevRotationYaw += 360.0F;

        if (d0 >= 180.0D) this.prevRotationYaw -= 360.0F;

        // Forge - ensure target chunk is loaded.
        if (!this.world.isRemote || SERVER_COMPILE) this.world.getChunk((int) Math.floor(this.posX) >> 4, (int) Math.floor(this.posZ) >> 4);

        this.setPosition(this.posX, this.posY, this.posZ);
        this.setRotation(yaw, pitch);
    }


    public void moveToBlockPosAndAngles(BlockPos pos, float rotationYawIn, float rotationPitchIn) {
        this.setLocationAndAngles((double)pos.getX() + 0.5D, pos.getY(), (double)pos.getZ() + 0.5D, rotationYawIn, rotationPitchIn);
    }


    public void setLocationAndAngles(double x, double y, double z, float yaw, float pitch) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.lastTickPosX = this.posX;
        this.lastTickPosY = this.posY;
        this.lastTickPosZ = this.posZ;
        this.rotationYaw = yaw;
        this.rotationPitch = pitch;
        this.setPosition(this.posX, this.posY, this.posZ);
    }


    public float getDistance(Entity entityIn) {
        float f = (float)(this.posX - entityIn.posX);
        float f1 = (float)(this.posY - entityIn.posY);
        float f2 = (float)(this.posZ - entityIn.posZ);
        return MathHelper.sqrt(f * f + f1 * f1 + f2 * f2);
    }


    public double getDistanceSq(double x, double y, double z) {
        double d0 = this.posX - x;
        double d1 = this.posY - y;
        double d2 = this.posZ - z;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }


    public double getDistanceSq(BlockPos pos) {
        return pos.distanceSq(this.posX, this.posY, this.posZ);
    }


    public double getDistanceSqToCenter(BlockPos pos) {
        return pos.distanceSqToCenter(this.posX, this.posY, this.posZ);
    }


    public double getDistance(double x, double y, double z) {
        double d0 = this.posX - x;
        double d1 = this.posY - y;
        double d2 = this.posZ - z;
        return MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }


    public double getDistanceSq(Entity entityIn) {
        double d0 = this.posX - entityIn.posX;
        double d1 = this.posY - entityIn.posY;
        double d2 = this.posZ - entityIn.posZ;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }


    public void onCollideWithPlayer(EntityPlayer entityIn) {}


    public void applyEntityCollision(Entity entityIn) {
        if (!this.isRidingSameEntity(entityIn)) {
            if (!entityIn.noClip && !this.noClip) {
                double d0 = entityIn.posX - this.posX;
                double d1 = entityIn.posZ - this.posZ;
                double d2 = MathHelper.absMax(d0, d1);

                if (d2 >= 0.009999999776482582D) {
                    d2 = MathHelper.sqrt(d2);
                    d0 = d0 / d2;
                    d1 = d1 / d2;
                    double d3 = 1.0D / d2;

                    if (d3 > 1.0D) {
                        d3 = 1.0D;
                    }
                    d0 = d0 * d3;
                    d1 = d1 * d3;
                    d0 = d0 * 0.05000000074505806D;
                    d1 = d1 * 0.05000000074505806D;
                    d0 = d0 * (double)(1.0F - this.entityCollisionReduction);
                    d1 = d1 * (double)(1.0F - this.entityCollisionReduction);

                    if (!this.isBeingRidden()) this.addVelocity(-d0, 0.0D, -d1);
                    if (!entityIn.isBeingRidden()) entityIn.addVelocity(d0, 0.0D, d1);
                }
            }
        }
    }


    public void addVelocity(double x, double y, double z) {
        this.motionX += x;
        this.motionY += y;
        this.motionZ += z;
        this.isAirBorne = true;
    }


    protected void markVelocityChanged() {
        this.velocityChanged = true;
    }


    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (!this.isEntityInvulnerable(source)) this.markVelocityChanged();
        return false;
    }


    public Vec3d getLook(float partialTicks) {
        if (partialTicks == 1.0F) return this.getVectorForRotation(this.rotationPitch, this.rotationYaw);
        else {
            float f = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * partialTicks;
            float f1 = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * partialTicks;
            return this.getVectorForRotation(f, f1);
        }
    }


    protected final Vec3d getVectorForRotation(float pitch, float yaw) {
        float f = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3d(f1 * f2, f3, f * f2);
    }


    public Vec3d getPositionEyes(float partialTicks) {
        if (partialTicks == 1.0F)
            return new Vec3d(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ);
        else {
            double d0 = this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks;
            double d1 = this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks + (double)this.getEyeHeight();
            double d2 = this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks;
            return new Vec3d(d0, d1, d2);
        }
    }


    @Nullable
    @SideOnly(Side.CLIENT)
    public RayTraceResult rayTrace(double blockReachDistance, float partialTicks) {
        Vec3d vec3d = this.getPositionEyes(partialTicks);
        Vec3d vec3d1 = this.getLook(partialTicks);
        Vec3d vec3d2 = vec3d.add(vec3d1.x * blockReachDistance, vec3d1.y * blockReachDistance, vec3d1.z * blockReachDistance);
        return this.world.rayTraceBlocks(vec3d, vec3d2, false, false, true);
    }


    public boolean canBeCollidedWith() {
        return false;
    }


    public boolean canBePushed() {
        return false;
    }


    public void awardKillScore(Entity entity, int num, DamageSource src) {
        if (entity instanceof EntityPlayerMP) {
            CriteriaTriggers.ENTITY_KILLED_PLAYER.trigger((EntityPlayerMP)entity, this, src);
        }
    }


    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRender3d(double x, double y, double z) {
        double d0 = this.posX - x;
        double d1 = this.posY - y;
        double d2 = this.posZ - z;
        double d3 = d0 * d0 + d1 * d1 + d2 * d2;
        return this.isInRangeToRenderDist(d3);
    }


    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        double d0 = this.getEntityBoundingBox().getAverageEdgeLength();

        if (Double.isNaN(d0)) {
            d0 = 1.0D;
        }

        d0 = d0 * 64.0D * renderDistanceWeight;
        return distance < d0 * d0;
    }


    public boolean writeToNBTAtomically(NBTTagCompound compound) {
        String s = this.getEntityString();

        if (!this.isDead && s != null) {
            compound.setString("id", s);
            this.writeToNBT(compound);
            return true;
        } else {
            return false;
        }
    }


    public boolean writeToNBTOptional(NBTTagCompound compound) {
        String s = this.getEntityString();

        if (!this.isDead && s != null && !this.isRiding()) {
            compound.setString("id", s);
            this.writeToNBT(compound);
            return true;
        } else {
            return false;
        }
    }


    public static void registerFixes(DataFixer fixer) {
        fixer.registerWalker(FixTypes.ENTITY, new IDataWalker() {
            public NBTTagCompound process(IDataFixer fixer, NBTTagCompound compound, int versionIn) {
                if (compound.hasKey(SStrings.Passengers, 9)) {
                    NBTTagList nbttaglist = compound.getTagList(SStrings.Passengers, 10);

                    for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                        nbttaglist.set(i, fixer.process(FixTypes.ENTITY, nbttaglist.getCompoundTagAt(i), versionIn));
                    }
                }
                return compound;
            }
        });
    }


    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        try {
            tag.setTag(SStrings.Pos,                this.newDoubleNBTList(this.posX, this.posY, this.posZ));
            tag.setTag(SStrings.Motion,             this.newDoubleNBTList(this.motionX, this.motionY, this.motionZ));
            tag.setTag(SStrings.Rotation,           this.newFloatNBTList(this.rotationYaw, this.rotationPitch));
            tag.setFloat(SStrings.FallDistance,     this.fallDistance);
            tag.setShort(SStrings.Fire, (short)     this.fire);
            tag.setShort(SStrings.Air, (short)      this.getAir());
            tag.setBoolean(SStrings.OnGround,       this.onGround);
            tag.setInteger(SStrings.Dimension,      this.dimension);
            tag.setBoolean(SStrings.Invulnerable,   this.invulnerable);
            tag.setInteger(SStrings.PortalCooldown, this.timeUntilPortal);
            tag.setUniqueId(SStrings.UUID,          this.getUniqueID());

            if (this.hasCustomName()) {
                tag.setString(SStrings.CustomName, this.getCustomNameTag());
            }

            if (this.getAlwaysRenderNameTag()) {
                tag.setBoolean(SStrings.CustomNameVisible, this.getAlwaysRenderNameTag());
            }

            if(cmdResultStats != null) this.cmdResultStats.writeStatsToNBT(tag);

            if (this.isSilent()) {
                tag.setBoolean(SStrings.Silent, this.isSilent());
            }

            if (this.hasNoGravity()) {
                tag.setBoolean(SStrings.NoGravity, this.hasNoGravity());
            }

            if (this.glowing) {
                tag.setBoolean(SStrings.Glowing, this.glowing);
            }
            tag.setBoolean(SStrings.UpdateBlocked, updateBlocked);

            if (!this.tags.isEmpty()) {
                NBTTagList nbttaglist = new NBTTagList();
                for (String s : this.tags) {
                    nbttaglist.appendTag(new NBTTagString(s));
                }
                tag.setTag(SStrings.Tags, nbttaglist);
            }

            if (customEntityData != null) tag.setTag(SStrings.ForgeData, customEntityData);
            if (this.capabilities != null) tag.setTag(SStrings.ForgeCaps, this.capabilities.serializeNBT());

            this.writeEntityToNBT(tag);

            if (this.isBeingRidden()) {
                NBTTagList nbttaglist1 = new NBTTagList();
                for (Entity entity : this.getPassengers()) {
                    NBTTagCompound nbttagcompound = new NBTTagCompound();

                    if (entity.writeToNBTAtomically(nbttagcompound)) {
                        nbttaglist1.appendTag(nbttagcompound);
                    }
                }
                if (!nbttaglist1.isEmpty()) {
                    tag.setTag(SStrings.Passengers, nbttaglist1);
                }
            }
            return tag;
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Saving entity NBT");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being saved");
            this.addEntityCrashInfo(crashreportcategory);
            throw new ReportedException(crashreport);
        }
    }


    public void readFromNBT(NBTTagCompound compound) {
        try {
            NBTTagList pos = compound.getTagList(SStrings.Pos, 6);
            NBTTagList motion = compound.getTagList(SStrings.Motion, 6);
            NBTTagList rotation = compound.getTagList(SStrings.Rotation, 5);
            this.motionX = motion.getDoubleAt(0);
            this.motionY = motion.getDoubleAt(1);
            this.motionZ = motion.getDoubleAt(2);

            if (Math.abs(this.motionX) > 10.0D) {
                this.motionX = 0.0D;
            }

            if (Math.abs(this.motionY) > 10.0D) {
                this.motionY = 0.0D;
            }

            if (Math.abs(this.motionZ) > 10.0D) {
                this.motionZ = 0.0D;
            }

            this.posX = pos.getDoubleAt(0);
            this.posY = pos.getDoubleAt(1);
            this.posZ = pos.getDoubleAt(2);
            this.lastTickPosX = this.posX;
            this.lastTickPosY = this.posY;
            this.lastTickPosZ = this.posZ;
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            this.rotationYaw = rotation.getFloatAt(0);
            this.rotationPitch = rotation.getFloatAt(1);
            this.prevRotationYaw = this.rotationYaw;
            this.prevRotationPitch = this.rotationPitch;
            this.setRotationYawHead(this.rotationYaw);
            this.setRenderYawOffset(this.rotationYaw);
            this.fallDistance = compound.getFloat(SStrings.FallDistance);
            this.fire = compound.getShort(SStrings.Fire);
            this.setAir(compound.getShort(SStrings.Air));
            this.onGround = compound.getBoolean(SStrings.OnGround);

            if (compound.hasKey(SStrings.Dimension)) {
                this.dimension = compound.getInteger(SStrings.Dimension);
            }

            this.invulnerable = compound.getBoolean(SStrings.Invulnerable);
            this.timeUntilPortal = compound.getInteger(SStrings.PortalCooldown);

            if (compound.hasUniqueId(SStrings.UUID)) {
                this.entityUniqueID = compound.getUniqueId(SStrings.UUID);
                this.cachedUniqueIdString = this.entityUniqueID.toString();
            }

            this.mutPosition(this.posX, this.posY, this.posZ);
            this.setRotation(this.rotationYaw, this.rotationPitch);

            if (compound.hasKey(SStrings.CustomName, 8)) {
                this.setCustomNameTag(compound.getString(SStrings.CustomName));
            }

            this.setAlwaysRenderNameTag(compound.getBoolean(SStrings.CustomNameVisible));

            if(compound.hasKey("CommandStats", 10)){
                if(cmdResultStats == null)this.cmdResultStats = new CommandResultStats();
                this.cmdResultStats.readStatsFromNBT(compound);
            }

            this.setSilent(compound.getBoolean(SStrings.Silent));
            this.setNoGravity(compound.getBoolean(SStrings.NoGravity));
            this.setGlowing(compound.getBoolean(SStrings.Glowing));
            updateBlocked = compound.getBoolean(SStrings.UpdateBlocked);

            if (compound.hasKey(SStrings.ForgeData)) customEntityData = compound.getCompoundTag(SStrings.ForgeData);
            if (this.capabilities != null && compound.hasKey(SStrings.ForgeCaps)) this.capabilities.deserializeNBT(compound.getCompoundTag("ForgeCaps"));

            if (compound.hasKey(SStrings.Tags, 9)) {
                this.tags.clear();
                NBTTagList nbttaglist1 = compound.getTagList(SStrings.Tags, 8);
                int i = Math.min(nbttaglist1.tagCount(), 1024);

                for (int j = 0; j < i; ++j) {
                    this.tags.add(nbttaglist1.getStringTagAt(j));
                }
            }

            this.readEntityFromNBT(compound);

            if (this.shouldSetPosAfterLoading()) {
                this.mutPosition(this.posX, this.posY, this.posZ);
            }
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Loading entity NBT");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being loaded");
            this.addEntityCrashInfo(crashreportcategory);
            throw new ReportedException(crashreport);
        }
    }


    protected boolean shouldSetPosAfterLoading() {
        return true;
    }


    @Nullable
    protected final String getEntityString() {
        ResourceLocation resourcelocation = EntityList.getKey(this);
        return resourcelocation == null ? null : resourcelocation.toString();
    }


    protected abstract void readEntityFromNBT(NBTTagCompound nbt);


    protected abstract void writeEntityToNBT(NBTTagCompound nbt);


    protected NBTTagList newDoubleNBTList(double... numbers) {
        NBTTagList nbttaglist = new NBTTagList(numbers.length);
        for (double d0 : numbers) {
            nbttaglist.appendTag(new NBTTagDouble(d0));
        }
        return nbttaglist;
    }


    protected NBTTagList newFloatNBTList(float... numbers) {
        NBTTagList nbttaglist = new NBTTagList(numbers.length);
        for (float f : numbers) {
            nbttaglist.appendTag(new NBTTagFloat(f));
        }
        return nbttaglist;
    }


    @Nullable
    public EntityItem dropItem(Item itemIn, int size) {
        return this.dropItemWithOffset(itemIn, size, 0.0F);
    }


    @Nullable
    public EntityItem dropItemWithOffset(Item itemIn, int size, float offsetY) {
        return this.entityDropItem(new ItemStack(itemIn, size, 0), offsetY);
    }


    @Nullable
    public EntityItem entityDropItem(ItemStack stack, float offsetY) {
        if (stack.isEmpty()) {
            return null;
        } else {
            EntityItem entityitem = new EntityItem(this.world, this.posX, this.posY + (double)offsetY, this.posZ, stack);
            entityitem.setDefaultPickupDelay();
            if (captureDrops)
                this.capturedDrops.add(entityitem);
            else
                this.world.spawnEntity(entityitem);
            return entityitem;
        }
    }


    public boolean isEntityAlive() {
        return !this.isDead;
    }


    //todo check
    public boolean isEntityInsideOpaqueBlock() {
        if (!this.noClip) {
            final IBlockEAccess access = world.access();
            final float w08 = this.width * 0.8f, yeeH = this.getEyeHeight();

            for (int i = 0, px = 0, py = -999999, pz = 0; i < 8; ++i) {
                int y = MathHelper.floor(this.posY +  (( ((i) & 1) - 0.5F) * 0.1F) + yeeH);
                int x = MathHelper.floor(this.posX +  (( ((i >> 1) & 1)    - 0.5F) * w08));
                int z = MathHelper.floor(this.posZ +  (( ((i >> 2) & 1)    - 0.5F) * w08));

                if (px != x || py != y || pz != z)
                    if (access.getBlockState(px = x, py = y, pz = z).causesSuffocation()) return true;
            }
        }
        return false;
    }


    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        return false;
    }


    @Nullable
    public AxisAlignedBB getCollisionBox(Entity entityIn) {
        return null;
    }


    public void updateRidden() {
        Entity entity = this.getRidingEntity();
        if (this.isRiding() && entity.isDead) {
            this.dismountRidingEntity();
        } else {
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
            if(!updateBlocked) this.onUpdate();
            if (this.isRiding()) {
                entity.updatePassenger(this);
            }
        }
    }


    public void updatePassenger(Entity passenger) {
        if (this.isPassenger(passenger)) {
            passenger.setPosition(this.posX, this.posY + this.getMountedYOffset() + passenger.getYOffset(), this.posZ);
        }
    }


    @SideOnly(Side.CLIENT)
    public void applyOrientationToEntity(Entity entityToUpdate) {}


    public double getYOffset() {
        return 0.0D;
    }


    public double getMountedYOffset() {
        return (double)this.height * 0.75D;
    }


    public boolean startRiding(Entity entityIn) {
        return this.startRiding(entityIn, false);
    }


    public boolean startRiding(Entity entityIn, boolean force) {
        for (Entity entity = entityIn; entity.ridingEntity != null; entity = entity.ridingEntity) {
            if (entity.ridingEntity == this) {
                return false;
            }
        }

        if (!net.minecraftforge.event.ForgeEventFactory.canMountEntity(this, entityIn, true)) return false;
        if (force || this.canBeRidden(entityIn) && entityIn.canFitPassenger(this)) {
            if (this.isRiding()) {
                this.dismountRidingEntity();
            }
            this.ridingEntity = entityIn;
            this.ridingEntity.addPassenger(this);
            return true;
        } else {
            return false;
        }
    }


    protected boolean canBeRidden(Entity entityIn) {
        return this.rideCooldown <= 0;
    }


    public void removePassengers() {
        for (int i = this.riddenByEntities.size() - 1; i >= 0; --i) {
            this.riddenByEntities.get(i).dismountRidingEntity();
        }
    }


    public void dismountRidingEntity() {
        if (this.ridingEntity != null) {
            Entity entity = this.ridingEntity;
            if (!net.minecraftforge.event.ForgeEventFactory.canMountEntity(this, entity, false)) return;
            this.ridingEntity = null;
            entity.removePassenger(this);
        }
    }


    protected void addPassenger(Entity passenger) {
        if (passenger.getRidingEntity() != this) {
            throw new IllegalStateException("Use x.startRiding(y), not y.addPassenger(x)");
        } else {
            if ((!this.world.isRemote || SERVER_COMPILE) && passenger instanceof EntityPlayer && !(this.getControllingPassenger() instanceof EntityPlayer)) {
                this.riddenByEntities.add(0, passenger);
            } else {
                this.riddenByEntities.add(passenger);
            }
        }
    }


    protected void removePassenger(Entity passenger) {
        if (passenger.getRidingEntity() == this) {
            throw new IllegalStateException("Use x.stopRiding(y), not y.removePassenger(x)");
        } else {
            this.riddenByEntities.remove(passenger);
            passenger.rideCooldown = 60;
        }
    }


    protected boolean canFitPassenger(Entity passenger) {
        return this.getPassengers().size() < 1;
    }


    @SideOnly(Side.CLIENT)
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        this.setPosition(x, y, z);
        this.setRotation(yaw, pitch);
    }


    public float getCollisionBorderSize() {
        return 0.0F;
    }


    public Vec3d getLookVec() {
        return this.getVectorForRotation(this.rotationPitch, this.rotationYaw);
    }


    @SideOnly(Side.CLIENT)
    public Vec2f getPitchYaw() {
        return new Vec2f(this.rotationPitch, this.rotationYaw);
    }


    @SideOnly(Side.CLIENT)
    public Vec3d getForward() {
        return Vec3d.fromPitchYaw(this.getPitchYaw());
    }


    public void setPortal(BlockPos pos) {
        if (this.timeUntilPortal > 0) {
            this.timeUntilPortal = this.getPortalCooldown();
        } else {
            if ((!this.world.isRemote || SERVER_COMPILE) && !pos.equals(this.lastPortalPos)) {
                this.lastPortalPos = new BlockPos(pos);
                BlockPattern.PatternHelper helper = Blocks.PORTAL.createPatternHelper(this.world, this.lastPortalPos);
                double d0 = helper.getForwards().getAxis() == EnumFacing.Axis.X ? (double)helper.getFrontTopLeft().getZ() : (double)helper.getFrontTopLeft().getX();
                double d1 = helper.getForwards().getAxis() == EnumFacing.Axis.X ? this.posZ : this.posX;
                d1 = Math.abs(MathHelper.pct(d1 - (double)(helper.getForwards().rotateY().getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE ? 1 : 0), d0, d0 - (double)helper.getWidth()));
                double d2 = MathHelper.pct(this.posY - 1.0D, (double)helper.getFrontTopLeft().getY(), (double)(helper.getFrontTopLeft().getY() - helper.getHeight()));
                this.lastPortalVec = new Vec3d(d1, d2, 0.0D);
                this.teleportDirection = helper.getForwards();
            }
            this.inPortal = true;
        }
    }


    public int getPortalCooldown() {
        return 300;
    }


    @SideOnly(Side.CLIENT)
    public void setVelocity(double x, double y, double z) {
        this.motionX = x;
        this.motionY = y;
        this.motionZ = z;
    }


    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id) {}


    @SideOnly(Side.CLIENT)
    public void performHurtAnimation() {}


    public Iterable<ItemStack> getHeldEquipment() {
        return EMPTY_EQUIPMENT;
    }


    public Iterable<ItemStack> getArmorInventoryList() {
        return EMPTY_EQUIPMENT;
    }


    public Iterable<ItemStack> getEquipmentAndArmor() {
        return Iterables.concat(this.getHeldEquipment(), this.getArmorInventoryList());
    }


    public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack) {}


    public boolean isBurning() {
        boolean flag = this.world != null && this.world.isRemote;
        return !this.isImmuneToFire && (this.fire > 0 || flag && this.getFlag(0));
    }


    public boolean isRiding() {
        return this.getRidingEntity() != null;
    }


    public boolean isBeingRidden() {
        return !this.riddenByEntities.isEmpty();
        //return !this.getPassengers().isEmpty();
    }


    public boolean isSneaking() {
        return this.getFlag(1);
    }


    public void setSneaking(boolean sneaking) {
        this.setFlag(1, sneaking);
    }


    public boolean isSprinting() {
        return this.getFlag(3);
    }


    public void setSprinting(boolean sprinting) {
        this.setFlag(3, sprinting);
    }


    public boolean isGlowing() {
        if(SERVER_COMPILE)return this.glowing;
        else return this.glowing || this.world.isRemote && this.getFlag(6);
    }


    public void setGlowing(boolean glowingIn) {
        this.glowing = glowingIn;
        if (!this.world.isRemote || SERVER_COMPILE) {
            this.setFlag(6, this.glowing);
        }
    }


    public boolean isInvisible() {
        return this.getFlag(5);
    }


    @SideOnly(Side.CLIENT)
    public boolean isInvisibleToPlayer(EntityPlayer player) {
        if (player.isSpectator()) {
            return false;
        } else {
            Team team = this.getTeam();
            return team != null && player != null && player.getTeam() == team && team.getSeeFriendlyInvisiblesEnabled() ? false : this.isInvisible();
        }
    }


    @Nullable
    public Team getTeam() {
        return this.world.getScoreboard().getPlayersTeam(this.getCachedUniqueIdString());
    }


    public boolean isOnSameTeam(Entity entityIn) {
        return this.isOnScoreboardTeam(entityIn.getTeam());
    }


    public boolean isOnScoreboardTeam(Team teamIn) {
        return this.getTeam() != null ? this.getTeam().isSameTeam(teamIn) : false;
    }


    public void setInvisible(boolean invisible) {
        this.setFlag(5, invisible);
    }


    protected boolean getFlag(int flag) {
        return (this.dataManager.get(FLAGS) & 1 << flag) != 0;
    }


    protected void setFlag(int flag, boolean set) {
        byte b0 = this.dataManager.get(FLAGS);
        if (set) {
            this.dataManager.set(FLAGS, (byte) (b0 | 1 << flag));
        } else {
            this.dataManager.set(FLAGS, (byte) (b0 & ~(1 << flag)));
        }
    }


    public int getAir() {
        return this.dataManager.get(AIR);
    }


    public void setAir(int air) {
        this.dataManager.set(AIR, air);
    }


    public void onStruckByLightning(EntityLightningBolt lightningBolt) {
        this.attackEntityFrom(DamageSource.LIGHTNING_BOLT, 5.0F);
        ++this.fire;
        if (this.fire == 0) {
            this.setFire(8);
        }
    }


    public void onKillEntity(EntityLivingBase entityLivingIn) {}


    protected boolean pushOutOfBlocks(double x, double y, double z) {
        if (!this.world.collidesWithAnyBlock(this.getEntityBoundingBox())) {
            return false;
        } else {
            int bX = MathHelper.floor(x);
            int bY = MathHelper.floor(y);
            int bZ = MathHelper.floor(z);
            double dX = x - bX;
            double dY = y - bY;
            double dZ = z - bZ;

            motionY += 0.1;

            EnumFacing enumfacing = EnumFacing.UP;
            double d3 = Double.MAX_VALUE;
            //is safe?
            ChunkSelector selector = (ChunkSelector) world.access();

            BlockPos.MutableBlockPos pos = world.localTempPos;

            if (!selector.isBlockFullCube(pos) && dX < d3) {
                d3 = dX;
                enumfacing = EnumFacing.WEST;
            }

            if (!selector.isBlockFullCube(pos.setPos(bX+1, bY, bZ)) && 1.0D - dX < d3) {
                d3 = 1.0D - dX;
                enumfacing = EnumFacing.EAST;
            }

            if (!selector.isBlockFullCube(pos.setPos(bX, bY, bZ-1)) && dZ < d3) {
                d3 = dZ;
                enumfacing = EnumFacing.NORTH;
            }

            if (!selector.isBlockFullCube(pos.setPos(bX, bY, bZ+1)) && 1.0D - dZ < d3) {
                d3 = 1.0D - dZ;
                enumfacing = EnumFacing.SOUTH;
            }

            if (!selector.isBlockFullCube(pos.setPos(bX, bY+1, bZ)) && 1.0D - dY < d3) {
                enumfacing = EnumFacing.UP;
            }

            float f = this.rand.nextFloat() * 0.2F + 0.1F;
            float f1 = (float)enumfacing.getAxisDirection().getOffset();

            if (enumfacing.getAxis() == EnumFacing.Axis.X) {
                this.motionX = f1 * f;
                this.motionY *= 0.75D;
                this.motionZ *= 0.75D;
            } else if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
                this.motionX *= 0.75D;
                this.motionY = f1 * f;
                this.motionZ *= 0.75D;
            } else if (enumfacing.getAxis() == EnumFacing.Axis.Z) {
                this.motionX *= 0.75D;
                this.motionY *= 0.75D;
                this.motionZ = f1 * f;
            }
            return true;
        }
    }


    public void setInWeb() {
        this.isInWeb = true;
        this.fallDistance = 0.0F;
    }


    public String getName() {
        if (this.hasCustomName()) {
            return this.getCustomNameTag();
        } else {
            String s = EntityList.getEntityString(this);
            if (s == null) {
                s = "generic";
            }
            return I18n.translateToLocal("entity." + s + ".name");
        }
    }


    @Nullable
    public Entity[] getParts() {
        return null;
    }


    public boolean isEntityEqual(Entity entityIn) {
        return this == entityIn;
    }


    public float getRotationYawHead() {
        return 0.0F;
    }


    public void setRotationYawHead(float rotation) {}


    public void setRenderYawOffset(float offset) {}


    public boolean canBeAttackedWithItem() {
        return true;
    }


    public boolean hitByEntity(Entity entityIn) {
        return false;
    }


    public String toString() {
        return String.format("%s['%s'/%d, l='%s', x=%.2f, y=%.2f, z=%.2f]", this.getClass().getSimpleName(), this.getName(), this.entityId, this.world == null ? "~NULL~" : this.world.getWorldInfo().getWorldName(), this.posX, this.posY, this.posZ);
    }


    public boolean isEntityInvulnerable(DamageSource source) {
        return this.invulnerable && source != DamageSource.OUT_OF_WORLD && !source.isCreativePlayer();
    }


    public boolean getIsInvulnerable() {
        return this.invulnerable;
    }


    public void setEntityInvulnerable(boolean isInvulnerable) {
        this.invulnerable = isInvulnerable;
    }


    public void copyLocationAndAnglesFrom(Entity entityIn) {
        this.setLocationAndAngles(entityIn.posX, entityIn.posY, entityIn.posZ, entityIn.rotationYaw, entityIn.rotationPitch);
    }


    private void copyDataFromOld(Entity entityIn) {
        NBTTagCompound nbttagcompound = entityIn.writeToNBT(new NBTTagCompound());
        nbttagcompound.removeTag("Dimension");
        this.readFromNBT(nbttagcompound);
        this.timeUntilPortal = entityIn.timeUntilPortal;
        this.lastPortalPos = entityIn.lastPortalPos;
        this.lastPortalVec = entityIn.lastPortalVec;
        this.teleportDirection = entityIn.teleportDirection;
    }


    @Nullable
    public Entity changeDimension(int dimensionIn) {
        if (this.world.isRemote || this.isDead) return null;
        return changeDimension(dimensionIn, this.getServer().getWorld(dimensionIn).getDefaultTeleporter());
    }


    @Nullable // Forge: Entities that require custom handling should override this method, not the other
    public Entity changeDimension(int dimensionIn, net.minecraftforge.common.util.ITeleporter teleporter) {
        if ((!this.world.isRemote || SERVER_COMPILE) && !this.isDead) {
            if (!ForgeHooks.onTravelToDimension(this, dimensionIn)) return null;
            MinecraftServer minecraftserver = this.getServer();
            int i = this.dimension;
            WorldServer worldserver = minecraftserver.getWorld(i);
            WorldServer worldserver1 = minecraftserver.getWorld(dimensionIn);
            this.dimension = dimensionIn;

            if (i == 1 && dimensionIn == 1 && teleporter.isVanilla())
            {
                worldserver1 = minecraftserver.getWorld(0);
                this.dimension = 0;
            }

            this.world.removeEntity(this);
            this.isDead = false;
            BlockPos blockpos;

            if (dimensionIn == 1 && teleporter.isVanilla()) {
                blockpos = worldserver1.getSpawnCoordinate();
            } else {
                double moveFactor = worldserver.provider.getMovementFactor() / worldserver1.provider.getMovementFactor();
                double d0 = MathHelper.clamp(this.posX * moveFactor, worldserver1.getWorldBorder().minX() + 16.0D, worldserver1.getWorldBorder().maxX() - 16.0D);
                double d1 = MathHelper.clamp(this.posZ * moveFactor, worldserver1.getWorldBorder().minZ() + 16.0D, worldserver1.getWorldBorder().maxZ() - 16.0D);

                d0 = MathHelper.clamp((int)d0, -29999872, 29999872);
                d1 = MathHelper.clamp((int)d1, -29999872, 29999872);
                float f = this.rotationYaw;
                this.setLocationAndAngles(d0, this.posY, d1, 90.0F, 0.0F);
                teleporter.placeEntity(worldserver1, this, f);
                blockpos = new BlockPos(this);
            }

            worldserver.updateEntityWithOptionalForce(this, false);
            Entity entity = EntityList.newEntity(this.getClass(), worldserver1);

            if (entity != null) {
                entity.copyDataFromOld(this);

                if (i == 1 && dimensionIn == 1 && teleporter.isVanilla()) {
                    BlockPos blockpos1 = worldserver1.getTopSolidOrLiquidBlock(worldserver1.getSpawnPoint());
                    entity.moveToBlockPosAndAngles(blockpos1, entity.rotationYaw, entity.rotationPitch);
                } else {
                    entity.moveToBlockPosAndAngles(blockpos, entity.rotationYaw, entity.rotationPitch);
                }

                boolean flag = entity.forceSpawn;
                entity.forceSpawn = true;
                worldserver1.spawnEntity(entity);
                entity.forceSpawn = flag;
                worldserver1.updateEntityWithOptionalForce(entity, false);
            }

            this.isDead = true;
            worldserver.resetUpdateEntityTick();
            worldserver1.resetUpdateEntityTick();
            return entity;
        } else {
            return null;
        }
    }


    public boolean isNonBoss() {
        return true;
    }


    public float getExplosionResistance(Explosion explosionIn, World worldIn, BlockPos pos, IBlockState blockStateIn) {
        return blockStateIn.getBlock().getExplosionResistance(worldIn, pos, this, explosionIn);
    }


    public boolean canExplosionDestroyBlock(Explosion explosionIn, World worldIn, BlockPos pos, IBlockState blockStateIn, float p_174816_5_) {
        return true;
    }


    public int getMaxFallHeight() {
        return 3;
    }


    public Vec3d getLastPortalVec() {
        return this.lastPortalVec;
    }


    public EnumFacing getTeleportDirection() {
        return this.teleportDirection;
    }

    public boolean doesEntityNotTriggerPressurePlate()
    {
        return false;
    }


    public void addEntityCrashInfo(CrashReportCategory category) {
        category.addDetail("Entity Type", new ICrashReportDetail<String>() {
            public String call() throws Exception {
                return EntityList.getKey(Entity.this) + " (" + Entity.this.getClass().getCanonicalName() + ")";
            }
        });
        category.addCrashSection("Entity ID", Integer.valueOf(this.entityId));
        category.addDetail("Entity Name", new ICrashReportDetail<String>() {
            public String call() throws Exception {
                return Entity.this.getName();
            }
        });
        category.addCrashSection("Entity's Exact location", String.format("%.2f, %.2f, %.2f", this.posX, this.posY, this.posZ));
        category.addCrashSection("Entity's Block location", CrashReportCategory.getCoordinateInfo(MathHelper.floor(this.posX), MathHelper.floor(this.posY), MathHelper.floor(this.posZ)));
        category.addCrashSection("Entity's Momentum", String.format("%.2f, %.2f, %.2f", this.motionX, this.motionY, this.motionZ));
        category.addDetail("Entity's Passengers", new ICrashReportDetail<String>()
        {
            public String call() throws Exception
            {
                return Entity.this.getPassengers().toString();
            }
        });
        category.addDetail("Entity's Vehicle", new ICrashReportDetail<String>()
        {
            public String call() throws Exception
            {
                return Entity.this.getRidingEntity().toString();
            }
        });
    }


    public void setUniqueId(UUID uniqueIdIn) {
        this.entityUniqueID = uniqueIdIn;
        this.cachedUniqueIdString = this.entityUniqueID.toString();
    }


    @SideOnly(Side.CLIENT)
    public boolean canRenderOnFire() {
        return this.isBurning();
    }


    public UUID getUniqueID() {
        return this.entityUniqueID;
    }


    public String getCachedUniqueIdString() {
        return this.cachedUniqueIdString;
    }


    public boolean isPushedByWater() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public static double getRenderDistanceWeight() {
        return renderDistanceWeight;
    }


    @SideOnly(Side.CLIENT)
    public static void setRenderDistanceWeight(double renderDistWeight) {
        renderDistanceWeight = renderDistWeight;
    }


    @Override
    public ITextComponent getDisplayName() {
        TextComponentString textcomponentstring = new TextComponentString(ScorePlayerTeam.formatPlayerName(this.getTeam(), this.getName()));
        textcomponentstring.getStyle().setHoverEvent(this.getHoverEvent());
        textcomponentstring.getStyle().setInsertion(this.getCachedUniqueIdString());
        return textcomponentstring;
    }


    public void setCustomNameTag(String name) {
        this.dataManager.set(CUSTOM_NAME, name);
    }


    public String getCustomNameTag() {
        return this.dataManager.get(CUSTOM_NAME);
    }


    public boolean hasCustomName() {
        return !this.dataManager.get(CUSTOM_NAME).isEmpty();
    }


    public void setAlwaysRenderNameTag(boolean alwaysRenderNameTag) {
        this.dataManager.set(CUSTOM_NAME_VISIBLE, Boolean.valueOf(alwaysRenderNameTag));
    }


    public boolean getAlwaysRenderNameTag() {
        return ((Boolean)this.dataManager.get(CUSTOM_NAME_VISIBLE)).booleanValue();
    }


    public void setPositionAndUpdate(double x, double y, double z) {
        this.isPositionDirty = true;
        this.setLocationAndAngles(x, y, z, this.rotationYaw, this.rotationPitch);
        this.world.updateEntityWithOptionalForce(this, false);
    }


    public void notifyDataManagerChange(DataParameter<?> key) {}


    @SideOnly(Side.CLIENT)
    public boolean getAlwaysRenderNameTagForRender() {
        return this.getAlwaysRenderNameTag();
    }


    public EnumFacing getHorizontalFacing() {
        return EnumFacing.byHorizontalIndex(MathHelper.floor((double)(this.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3);
    }


    public EnumFacing getAdjustedHorizontalFacing() {
        return this.getHorizontalFacing();
    }


    protected HoverEvent getHoverEvent() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        ResourceLocation resourcelocation = EntityList.getKey(this);
        nbttagcompound.setString("id", this.getCachedUniqueIdString());
        if (resourcelocation != null) {
            nbttagcompound.setString("type", resourcelocation.toString());
        }
        nbttagcompound.setString("name", this.getName());
        return new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new TextComponentString(nbttagcompound.toString()));
    }


    public boolean isSpectatedByPlayer(EntityPlayerMP player) {
        return true;
    }


    public AxisAlignedBB getEntityBoundingBox() {
        return this.boundingBox;
    }


    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return this.getEntityBoundingBox();
    }


    public void setEntityBoundingBox(AxisAlignedBB bb) {
        this.boundingBox = bb;
    }


    public float getEyeHeight() {
        return this.height * 0.85F;
    }


    //todo maybe error if ignored
    public boolean isOutsideBorder() {
        return false;
    }


    //todo maybe error if ignored
    public void setOutsideBorder(boolean outsideBorder) {
        //this.isOutsideBorder = outsideBorder;
    }


    public boolean replaceItemInInventory(int inventorySlot, ItemStack itemStackIn) {
        return false;
    }


    public void sendMessage(ITextComponent component) {}


    public boolean canUseCommand(int permLevel, String commandName) {
        return true;
    }


    public BlockPos getPosition() {
        return new BlockPos(this.posX, this.posY + 0.5D, this.posZ);
    }


    public double distanceSq(double toX, double toY, double toZ) {
        double d0 =  posX         - toX;
        double d1 = (posY + 0.5D) - toY;
        double d2 =  posZ         - toZ;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }


    public Vec3d getPositionVector() {
        return new Vec3d(this.posX, this.posY, this.posZ);
    }


    public World getEntityWorld() {
        return this.world;
    }


    public Entity getCommandSenderEntity() {
        return this;
    }

    public boolean sendCommandFeedback() {
        return false;
    }


    public void setCommandStat(CommandResultStats.Type type, int amount) {
        if (this.world != null && (!this.world.isRemote || SERVER_COMPILE)) {
            if(this.cmdResultStats == null)this.cmdResultStats = new CommandResultStats();
            this.cmdResultStats.setCommandStatForSender(this.world.getMinecraftServer(), this, type, amount);
        }
    }


    @Nullable
    public MinecraftServer getServer() {
        return this.world.getMinecraftServer();
    }


    public CommandResultStats getCommandStats() {
        if(cmdResultStats == null)return cmdResultStats = new CommandResultStats();
        return this.cmdResultStats;
    }


    public void setCommandStats(Entity entityIn) {
        if(cmdResultStats == null) cmdResultStats = new CommandResultStats();
        this.cmdResultStats.addAllStats(entityIn.getCommandStats());
    }


    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {
        return EnumActionResult.PASS;
    }


    public boolean isImmuneToExplosions() {
        return false;
    }


    protected void applyEnchantments(EntityLivingBase entityLivingBaseIn, Entity entityIn) {
        if (entityIn instanceof EntityLivingBase) {
            EnchantmentHelper.applyThornEnchantments((EntityLivingBase)entityIn, entityLivingBaseIn);
        }
        EnchantmentHelper.applyArthropodEnchantments(entityLivingBaseIn, entityIn);
    }

    /* ================================== Forge Start =====================================*/
    /**
     * Internal use for keeping track of entities that are tracked by a world, to
     * allow guarantees that entity position changes will force a chunk load, avoiding
     * potential issues with entity desyncing and bad chunk data.
     */
    private boolean isAddedToWorld;

    /**
     * Gets whether this entity has been added to a world (for tracking). Specifically
     * between the times when an entity is added to a world and the entity being removed
     * from the world's tracked lists. See {@link World#onEntityAdded(Entity)} and
     * {@link World#onEntityRemoved(Entity)}.
     *
     * @return True if this entity is being tracked by a world
     */
    public final boolean isAddedToWorld() { return this.isAddedToWorld; }

    /**
     * Called after the entity has been added to the world's
     * ticking list. Can be overriden, but needs to call super
     * to prevent MC-136995.
     */
    public void onAddedToWorld() {
        this.isAddedToWorld = true;
    }

    /**
     * Called after the entity has been removed to the world's
     * ticking list. Can be overriden, but needs to call super
     * to prevent MC-136995.
     */
    public void onRemovedFromWorld() {
        this.isAddedToWorld = false;
    }

    /**
     * Returns a NBTTagCompound that can be used to store custom data for this entity.
     * It will be written, and read from disc, so it persists over world saves.
     * @return A NBTTagCompound
     */
    public NBTTagCompound getEntityData() {
        if (customEntityData == null) {
            customEntityData = new NBTTagCompound();
        }
        return customEntityData;
    }

    /**
     * Used in model rendering to determine if the entity riding this entity should be in the 'sitting' position.
     * @return false to prevent an entity that is mounted to this entity from displaying the 'sitting' animation.
     */
    public boolean shouldRiderSit()
    {
        return true;
    }

    /**
     * Called when a user uses the creative pick block button on this entity.
     *
     * @param target The full target the player is looking at
     * @return A ItemStack to add to the player's inventory, empty ItemStack if nothing should be added.
     */
    public ItemStack getPickedResult(RayTraceResult target)
    {
        if (this instanceof net.minecraft.entity.item.EntityPainting)
        {
            return new ItemStack(net.minecraft.init.Items.PAINTING);
        }
        else if (this instanceof EntityLeashKnot)
        {
            return new ItemStack(net.minecraft.init.Items.LEAD);
        }
        else if (this instanceof net.minecraft.entity.item.EntityItemFrame)
        {
            ItemStack held = ((net.minecraft.entity.item.EntityItemFrame)this).getDisplayedItem();
            if (held.isEmpty())
            {
                return new ItemStack(net.minecraft.init.Items.ITEM_FRAME);
            }
            else
            {
                return held.copy();
            }
        }
        else if (this instanceof net.minecraft.entity.item.EntityMinecart)
        {
            return ((net.minecraft.entity.item.EntityMinecart)this).getCartItem();
        }
        else if (this instanceof net.minecraft.entity.item.EntityBoat)
        {
            return new ItemStack(((EntityBoat)this).getItemBoat());
        }
        else if (this instanceof net.minecraft.entity.item.EntityArmorStand)
        {
            return new ItemStack(net.minecraft.init.Items.ARMOR_STAND);
        }
        else if (this instanceof net.minecraft.entity.item.EntityEnderCrystal)
        {
            return new ItemStack(net.minecraft.init.Items.END_CRYSTAL);
        }
        else
        {
            ResourceLocation name = EntityList.getKey(this);
            if (name != null && EntityList.ENTITY_EGGS.containsKey(name))
            {
                ItemStack stack = new ItemStack(net.minecraft.init.Items.SPAWN_EGG);
                net.minecraft.item.ItemMonsterPlacer.applyEntityIdToItemStack(stack, name);
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public UUID getPersistentID()
    {
        return entityUniqueID;
    }

    /**
     * Reset the entity ID to a new value. Not to be used from Mod code
     */
    @Deprecated // TODO: remove (1.13?)
    public final void resetEntityId()
    {
        this.entityId = nextEntityID++;
    }

    public boolean shouldRenderInPass(int pass)
    {
        return pass == 0;
    }

    /**
     * Returns true if the entity is of the @link{EnumCreatureType} provided
     * @param type The EnumCreatureType type this entity is evaluating
     * @param forSpawnCount If this is being invoked to check spawn count caps.
     * @return If the creature is of the type provided
     */
//    public boolean isCreatureType(EnumCreatureType type, boolean forSpawnCount)
//    {
//        if (forSpawnCount && (this instanceof EntityLiving) && ((EntityLiving)this).isNoDespawnRequired()) return false;
//        return type.getCreatureClass().isAssignableFrom(this.getClass());
//    }


    public boolean isCreatureType(@NotNull EnumCreatureType type, boolean forSpawnCount) {
        return false;
    }

    /**
     * If a rider of this entity can interact with this entity. Should return true on the
     * ridden entity if so.
     *
     * @return if the entity can be interacted with from a rider
     */
    public boolean canRiderInteract() {
        return false;
    }

    /**
     * If the rider should be dismounted from the entity when the entity goes under water
     *
     * @param rider The entity that is riding
     * @return if the entity should be dismounted when under water
     */
    public boolean shouldDismountInWater(Entity rider) {
        return this instanceof EntityLivingBase;
    }


    @Override
    public boolean hasCapability(net.minecraftforge.common.capabilities.Capability<?> capability, @Nullable net.minecraft.util.EnumFacing facing) {
        return capabilities != null && capabilities.hasCapability(capability, facing);
    }


    @Override
    @Nullable
    public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @Nullable net.minecraft.util.EnumFacing facing) {
        return capabilities == null ? null : capabilities.getCapability(capability, facing);
    }


    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.readFromNBT(nbt);
    }


    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound ret = new NBTTagCompound();
        ret.setString("id", this.getEntityString());
        return this.writeToNBT(ret);
    }

    /**
     * Checks if this {@link Entity} can trample a {@link Block}.
     *
     * @param world The world in which the block will be trampled
     * @param block The block being tested
     * @param pos The block pos
     * @param fallDistance The fall distance
     * @return {@code true} if this entity can trample, {@code false} otherwise
     */
    public boolean canTrample(World world, Block block, BlockPos pos, float fallDistance) {
        return world.rand.nextFloat() < fallDistance - 0.5F
                && this instanceof EntityLivingBase
                && (this instanceof EntityPlayer || world.getMobGrifering(this))
                && this.width * this.width * this.height > 0.512F;
    }
    /* ================================== Forge End =====================================*/


    public void addTrackingPlayer(EntityPlayerMP player) {}


    public void removeTrackingPlayer(EntityPlayerMP player) {}


    public float getRotatedYaw(Rotation transformRotation) {
        float f = MathHelper.wrapDegrees(this.rotationYaw);

        switch (transformRotation) {
            case CLOCKWISE_180:
                return f + 180.0F;
            case COUNTERCLOCKWISE_90:
                return f + 270.0F;
            case CLOCKWISE_90:
                return f + 90.0F;
            default:
                return f;
        }
    }


    public float getMirroredYaw(Mirror transformMirror) {
        float f = MathHelper.wrapDegrees(this.rotationYaw);

        switch (transformMirror) {
            case LEFT_RIGHT:
                return -f;
            case FRONT_BACK:
                return 180.0F - f;
            default:
                return f;
        }
    }


    public boolean ignoreItemEntityData() {
        return false;
    }


    public boolean setPositionNonDirty() {
        boolean flag = this.isPositionDirty;
        this.isPositionDirty = false;
        return flag;
    }


    @Nullable
    public Entity getControllingPassenger() {
        return null;
    }


    public List<Entity> getPassengers() {
        return this.riddenByEntities.isEmpty() ? Collections.emptyList() : Lists.newArrayList(this.riddenByEntities);
    }


    public boolean isPassenger(Entity entityIn) {
        for (Entity entity : this.getPassengers())
            if (entity.equals(entityIn)) return true;
        return false;
    }


    public Collection<Entity> getRecursivePassengers() {
        Set<Entity> set = Sets.newHashSet();
        this.getRecursivePassengersByType(Entity.class, set);
        return set;
    }


    public <T extends Entity> Collection<T> getRecursivePassengersByType(Class<T> entityClass) {
        Set<T> set = new Te4HashSet<>();
        this.getRecursivePassengersByType(entityClass, set);
        return set;
    }


    private <T extends Entity> void getRecursivePassengersByType(Class<T> entityClass, Set<T> theSet) {
        for (Entity entity : this.getPassengers()) {
            if (entityClass.isAssignableFrom(entity.getClass())) theSet.add((T)entity);
            entity.getRecursivePassengersByType(entityClass, theSet);
        }
    }


    public Entity getLowestRidingEntity() {
        Entity entity;
        for (entity = this; entity.isRiding(); entity = entity.getRidingEntity());
        return entity;
    }


    public boolean isRidingSameEntity(Entity entityIn) {
        return this.getLowestRidingEntity() == entityIn.getLowestRidingEntity();
    }


    public boolean isRidingOrBeingRiddenBy(Entity entityIn) {
        for (Entity entity : this.getPassengers()) {
            if (entity.equals(entityIn)) return true;
            if (entity.isRidingOrBeingRiddenBy(entityIn)) return true;
        }
        return false;
    }


    public boolean canPassengerSteer() {
        Entity entity = this.getControllingPassenger();
        return (entity instanceof EntityPlayer)? ((EntityPlayer)entity).isUser() : (!this.world.isRemote || SERVER_COMPILE);
    }


    protected float updateDistance(float f1, float f2){
        return f2;
    }


    //mapped updateDistance
    protected float func_110146_f(float f1, float f2){
        return f2;
    }


    @Nullable
    public Entity getRidingEntity() {
        return this.ridingEntity;
    }


    public EnumPushReaction getPushReaction() {
        return EnumPushReaction.NORMAL;
    }


    public SoundCategory getSoundCategory() {
        return SoundCategory.NEUTRAL;
    }


    protected int getFireImmuneTicks() {
        return 1;
    }


    public int lodLvl(){
        return ElcaMobs.GLOBAL.lodLvl(elcaMinRange);
    }
}