package net.minecraft.entity.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockIDIndexes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.ElcaMobs;
import plus.fserver.api.Keep;
import plus.fserver.server.elca.ChunkSelector;
import static plus.fserver.ElcaCV.SERVER_COMPILE;


public class EntityFallingBlock extends Entity {
    private IBlockState fallTile;
    public int fallTime;
    public boolean shouldDropItem;
    private boolean dontSetBlock;
    private boolean hurtEntities;
    private int fallHurtMax;
    private float fallHurtAmount;
    public NBTTagCompound tileEntityData;
    @Keep
    protected static final DataParameter<BlockPos> ORIGIN = EntityDataManager.createKey(EntityFallingBlock.class, DataSerializers.BLOCK_POS);

    public EntityFallingBlock(final World worldIn) {
        super(worldIn);
        this.shouldDropItem = true;
        this.fallHurtMax = 40;
        this.fallHurtAmount = 2.0f;
    }

    public EntityFallingBlock(final World worldIn, final double x, final double y, final double z, final IBlockState fallingBlockState) {
        super(worldIn);
        this.shouldDropItem = true;
        this.fallHurtMax = 40;
        this.fallHurtAmount = 2.0f;
        this.fallTile = fallingBlockState;
        this.preventEntitySpawning = true;
        this.setSize(0.98f, 0.98f);
        this.mutPosition(x, y + (1.0f - this.height) / 2.0f, z);
        this.motionX = 0.0;
        this.motionY = 0.0;
        this.motionZ = 0.0;
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
        this.setOrigin(new BlockPos(this));
    }


    @Override
    public boolean canBeAttackedWithItem() {
        return false;
    }


    public void setOrigin(BlockPos pos) {
        this.dataManager.set(ORIGIN, pos);
    }


    @SideOnly(Side.CLIENT)
    public BlockPos getOrigin() {
        return this.dataManager.get(ORIGIN);
    }


    @Override
    protected boolean canTriggerWalking() {
        return false;
    }


    @Override
    protected void entityInit() {
        this.dataManager.registerU(ORIGIN, BlockPos.ORIGIN);
    }


    @Override
    public boolean canBeCollidedWith() {
        return !this.isDead;
    }


    public void setIgnoreFirstCheck(){
        this.fallTime = 1;
    }


    @Override
    public void onUpdate() {
        if (this.fallTile.getMaterial() == Material.AIR) {
            this.setDead();
        } else {
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;

            final Block block = this.fallTile.getBlock();
            World world = this.world;

            if (this.fallTime++ == 0) {
                BlockPos blockpos = world.localTempPos.setPos(this); //safe

                if (world.access().getBlockState(blockpos).getBlock() == block) {
                    world.access().setBlockState(blockpos, BlockIDIndexes.AIR_STATE, 3);
                }
                else if (!world.isRemote) {
                    this.setDead();
                    return;
                }
            }

            if (!this.hasNoGravity()) {
                this.motionY -= 0.03999999910593033D;
            }

            this.moveNow(MoverType.SELF, this.motionX, this.motionY, this.motionZ);

            if (!world.isRemote || SERVER_COMPILE) {
                ChunkSelector access = world.localSelector;
                BlockPos pos2 = world.localTempPos.setPos(this); //safe
                final boolean flag = this.fallTile.getBlock() == Blocks.CONCRETE_POWDER;
                boolean flag2 = flag && access.getBlockState(pos2).getMaterial() == Material.WATER;

                final double d0 = this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ;

                if (flag && d0 > 1.0) {
                    final RayTraceResult raytraceresult = world.rayTraceBlocks(new Vec3d(this.prevPosX, this.prevPosY, this.prevPosZ), new Vec3d(this.posX, this.posY, this.posZ), true);
                    if (raytraceresult != null && access.getBlockState(raytraceresult.getBlockPos()).getMaterial() == Material.WATER) {
                        pos2 = raytraceresult.getBlockPos();
                        flag2 = true;
                    }
                }

                if (!this.onGround && !flag2) {
                    if ((this.fallTime >= 100 && !world.isRemote && (pos2.getY() < 1 || pos2.getY() > 256)) || this.fallTime > 600) {
                        if (this.shouldDropItem && world.getGameRules().doEntityDrops.getBoolean()) {
                            this.entityDropItem(new ItemStack(block, 1, block.damageDropped(this.fallTile)), 0.0f);
                        }
                        this.setDead();
                    }
                }
                else {
                    final IBlockState iblockstate = access.getBlockState(pos2);
                    BlockPos temp = world.localTempPos2.setPos(this.posX, this.posY - 0.009999999776482582, this.posZ); //safe
                    if (access.isAirBlock(temp) && !flag2 && BlockFalling.canFallThrough(access.getBlockState(temp))) {
                        this.onGround = false;
                        return;
                    }
                    this.motionX *= 0.699999988079071;
                    this.motionZ *= 0.699999988079071;
                    this.motionY *= -0.5;
                    if (iblockstate.getBlock() != Blocks.PISTON_EXTENSION) {
                        this.setDead();
                        if (!this.dontSetBlock) {
                            if (world.mayPlace(block, pos2, true, EnumFacing.UP, this)
                                    && (flag2 || !BlockFalling.canFallThrough(access.getBlockState(pos2.getX(),pos2.getY()-1, pos2.getZ()))) && access.setBlockState(pos2, this.fallTile, 3)) {

                                if (block instanceof BlockFalling) {
                                    ((BlockFalling)block).onEndFalling(world, pos2, this.fallTile, iblockstate);
                                }
                                if (this.tileEntityData != null && block.hasTileEntity(this.fallTile)) {
                                    final TileEntity tileentity = world.getTileEntity(pos2);
                                    if (tileentity != null) {
                                        final NBTTagCompound nbttagcompound = tileentity.writeToNBT(new NBTTagCompound());
                                        for (final String s : this.tileEntityData.getKeySet()) {
                                            final NBTBase nbtbase = this.tileEntityData.getTag(s);
                                            if (!"x".equals(s) && !"y".equals(s) && !"z".equals(s)) {
                                                nbttagcompound.setTag(s, nbtbase.copy());
                                            }
                                        }
                                        tileentity.readFromNBT(nbttagcompound);
                                        tileentity.markDirty();
                                    }
                                }
                            }
                            else if (this.shouldDropItem && world.getGameRules().doEntityDrops.getBoolean()) {
                                this.entityDropItem(new ItemStack(block, 1, block.damageDropped(this.fallTile)), 0.0f);
                            }
                        }
                        else if (block instanceof BlockFalling) {
                            ((BlockFalling)block).onBroken(world, pos2);
                        }
                    }
                }
            }

            this.motionX *= 0.9800000190734863D;
            this.motionY *= 0.9800000190734863D;
            this.motionZ *= 0.9800000190734863D;
        }
    }


    @Override
    public void fall(float distance, float damageMultiplier) {
        Block block = this.fallTile.getBlock();

        if (this.hurtEntities) {
            int i = MathHelper.ceil(distance - 1.0F);

            if (i > 0) {
                List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox());
                boolean flag = block == Blocks.ANVIL;
                DamageSource src = flag ? DamageSource.ANVIL : DamageSource.FALLING_BLOCK;

                for (Entity entity : list) {
                    entity.attackEntityFrom(src, (float)Math.min(MathHelper.floor(i * this.fallHurtAmount), this.fallHurtMax));
                }

                if (flag && this.rand.nextFloat() < 0.05000000074505806 + i * 0.05) {
                    int j = this.fallTile.getValue(BlockAnvil.DAMAGE);
                    if (++j > 2) {
                        this.dontSetBlock = true;
                    } else {
                        this.fallTile = this.fallTile.withProperty(BlockAnvil.DAMAGE, j);
                    }
                }
            }
        }
    }


    @Keep
    public static void registerFixesFallingBlock(DataFixer fixer) {}


    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        Block block = this.fallTile != null ? this.fallTile.getBlock() : Blocks.AIR;
        ResourceLocation resourcelocation = Block.REGISTRY.getNameForObject(block);
        nbt.setString("Block", resourcelocation == null ? "" : resourcelocation.toString());
        nbt.setByteU("Data", (byte)block.getMetaFromState(this.fallTile));
        nbt.setIntegerU("Time", this.fallTime);
        nbt.setBoolean("DropItem", this.shouldDropItem);
        nbt.setBoolean("HurtEntities", this.hurtEntities);
        nbt.setFloat("FallHurtAmount", this.fallHurtAmount);
        nbt.setIntegerU("FallHurtMax", this.fallHurtMax);

        if (this.tileEntityData != null) {
            nbt.setTag("TileEntityData", this.tileEntityData);
        }
    }


    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        int i = nbt.getByte("Data") & 255;

        if (nbt.hasKey("Block", 8)) {
            this.fallTile = Block.getBlockFromName(nbt.getString("Block")).getStateFromMeta(i);
        } else if (nbt.hasKey("TileID", 99)) {
            this.fallTile = Block.getBlockById(nbt.getInteger("TileID")).getStateFromMeta(i);
        } else {
            this.fallTile = Block.getBlockById(nbt.getByte("Tile") & 255).getStateFromMeta(i);
        }

        this.fallTime = nbt.getInteger("Time");
        Block block = this.fallTile.getBlock();

        if (nbt.hasKey("HurtEntities", 99)) {
            this.hurtEntities = nbt.getBoolean("HurtEntities");
            this.fallHurtAmount = nbt.getFloat("FallHurtAmount");
            this.fallHurtMax = nbt.getInteger("FallHurtMax");
        } else if (block == Blocks.ANVIL) {
            this.hurtEntities = true;
        }

        if (nbt.hasKey("DropItem", 99)) {
            this.shouldDropItem = nbt.getBoolean("DropItem");
        }

        if (nbt.hasKey("TileEntityData", 10)) {
            this.tileEntityData = nbt.getCompoundTag("TileEntityData");
        }

        if (block == null || block.getDefaultState().getMaterial() == Material.AIR) {
            this.fallTile = Blocks.SAND.getDefaultState();
        }
    }


    public void setHurtEntities(boolean p_145806_1_) {
        this.hurtEntities = p_145806_1_;
    }


    @Override
    public void addEntityCrashInfo(CrashReportCategory category) {
        super.addEntityCrashInfo(category);

        if (this.fallTile != null) {
            Block block = this.fallTile.getBlock();
            category.addCrashSection("Immitating block ID",   Block.getIdFromBlock(block));
            category.addCrashSection("Immitating block data", block.getMetaFromState(this.fallTile));
        }
    }


    @SideOnly(Side.CLIENT)
    public World getWorldObj() {
        return this.world;
    }


    @SideOnly(Side.CLIENT)
    public boolean canRenderOnFire() {
        return false;
    }


    @Nullable
    public IBlockState getBlock() {
        return this.fallTile;
    }


    public boolean ignoreItemEntityData() {
        return true;
    }


    @Override
    public int lodLvl() {
        return ElcaMobs.FALLING.lodLvl(elcaMinRange);
    }
}
