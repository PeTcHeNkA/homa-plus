package net.minecraft.world;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.api.IBlockEAccess;
import plus.fserver.ElcaCV;
import plus.fserver.utls.ChunkLCache;
import plus.tson.utl.Te4HashSet;


public class Explosion {
    private final boolean causesFire;
    private final boolean damagesTerrain;
    private final World world;
    private final double x;
    private final double y;
    private final double z;
    private final Entity exploder;
    private final float size;
    private final List<BlockPos> affectedBlockPositions;
    private final Map<EntityPlayer, Vec3d> playerKnockbackMap;
    private final Vec3d position;
    private final IBlockEAccess selector;

    @SideOnly(Side.CLIENT)
    public Explosion(World worldIn, Entity entityIn, double x, double y, double z, float size, List<BlockPos> affectedPositions) {
        this(worldIn, entityIn, x, y, z, size, false, true, affectedPositions);
    }


    @SideOnly(Side.CLIENT)
    public Explosion(World worldIn, Entity entityIn, double x, double y, double z, float size, boolean causesFire, boolean damagesTerrain, List<BlockPos> affectedPositions) {
        this(worldIn, entityIn, x, y, z, size, causesFire, damagesTerrain);
        this.affectedBlockPositions.addAll(affectedPositions);
    }


    public Explosion(World worldIn, Entity entityIn, double x, double y, double z, float size, boolean causesFire, boolean damagesTerrain) {
        this.affectedBlockPositions = Lists.newArrayList();
        this.playerKnockbackMap = Maps.newHashMap();
        this.world = worldIn;
        this.exploder = entityIn;
        this.size = size;
        this.x = x;
        this.y = y;
        this.z = z;
        this.causesFire = causesFire;
        this.damagesTerrain = damagesTerrain;
        this.position = new Vec3d(this.x, this.y, this.z);

        selector = new ChunkLCache(world, MathHelper.floor(x)>>4, MathHelper.floor(z)>>4);
        //selector = world.isRemote?world:new ChunkSelector(world);
    }


    public void doExplosionA() {
        Te4HashSet<BlockPos> set = new Te4HashSet<>();
        double xPos, yPos, zPos;
        float fSize;

        for (int aX = 0, aY, aZ; aX < 16; ++aX) {
            for (aZ = 0; aZ < 16; ++aZ) {
                for (aY = 0; aY < 16; ++aY) {
                    if (aX == 0 || aX == 15 || aY == 0 || aY == 15 || aZ == 0 || aZ == 15) {
                        float dX = aX * 0.1333333F - 1.0F;
                        float dY = aY * 0.1333333F - 1.0F;
                        float dZ = aZ * 0.1333333f - 1.0F;
                        //todo check
                        fSize = (float)Math.sqrt(dX * dX + dY * dY + dZ * dZ);
                        dX = dX / fSize;
                        dY = dY / fSize;
                        dZ = dZ / fSize;
                        fSize = this.size * (0.7F + this.world.rand.nextFloat() * 0.6F);
                        xPos = this.x;
                        yPos = this.y;
                        zPos = this.z;

                        for (; fSize > 0.0F; fSize -= 0.22500001F) {
                            BlockPos blockpos = new BlockPos(xPos, yPos, zPos);
                            IBlockState state = selector.getBlockState(blockpos);

                            if (state.getMaterial() != Material.AIR) {
                                float f2 = this.exploder != null ? this.exploder.getExplosionResistance(this, this.world, blockpos, state) : state.getBlock().getExplosionResistance(world, blockpos, null, this);
                                fSize -= (f2 + 0.3F) * 0.3F;
                            }

                            if (fSize > 0.0F && (this.exploder == null || this.exploder.canExplosionDestroyBlock(this, this.world, blockpos, state, fSize))) {
                                set.add(blockpos);
                            }
                            xPos += dX * 0.30000001192092896D;
                            yPos += dY * 0.30000001192092896D;
                            zPos += dZ * 0.30000001192092896D;
                        }
                    }
                }
            }
        }
        this.affectedBlockPositions.addAll(set);
        fSize = this.size * 2.0F;
        xPos = fSize - 1;
        zPos = fSize + 1;

        List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this.exploder,
                new AxisAlignedBB(
                        MathHelper.floor(this.x - xPos),
                        MathHelper.floor(this.y - xPos),
                        MathHelper.floor(this.z - xPos),

                        MathHelper.floor(this.x + zPos),
                        MathHelper.floor(this.y + zPos),
                        MathHelper.floor(this.z + zPos)
                )
        );
        if(ElcaCV.ENABLE_EXPLOSION_EVENT)
            ForgeEventFactory.onExplosionDetonate(this.world, this, list, fSize);

        for (int k2 = 0; k2 < list.size(); ++k2) {
            Entity entity = list.get(k2);

            if (!entity.isImmuneToExplosions()) {
                double d12 = entity.getDistance(this.x, this.y, this.z) / (double)fSize;

                if (d12 <= 1.0D) {
                    xPos = entity.posX - this.x;
                    yPos = entity.posY + entity.getEyeHeight() - this.y;
                    zPos = entity.posZ - this.z;
                    double d13 = MathHelper.sqrt(xPos * xPos + yPos * yPos + zPos * zPos);

                    if (d13 != 0.0D) {
                        xPos = xPos / d13;
                        yPos = yPos / d13;
                        zPos = zPos / d13;
                        double d14 = this.world.getBlockDensity(selector, position, entity.getEntityBoundingBox());
                        double d10 = (1.0D - d12) * d14;
                        entity.attackEntityFrom(DamageSource.causeExplosionDamage(this), (float)((int)((d10 * d10 + d10) / 2.0D * 7.0D * (double)fSize + 1.0D)));
                        double d11 = d10;

                        if (entity instanceof EntityLivingBase) {
                            d11 = EnchantmentProtection.getBlastDamageReduction((EntityLivingBase)entity, d10);
                        }

                        entity.motionX += xPos * d11;
                        entity.motionY += yPos * d11;
                        entity.motionZ += zPos * d11;

                        if (entity instanceof EntityPlayer) {
                            EntityPlayer entityplayer = (EntityPlayer)entity;

                            if (!entityplayer.isSpectator() && (!entityplayer.isCreative() || !entityplayer.capabilities.isFlying)) {
                                this.playerKnockbackMap.put(entityplayer, new Vec3d(xPos * d10, yPos * d10, zPos * d10));
                            }
                        }
                    }
                }
            }
        }
    }


    public void doExplosionB(boolean spawnParticles) {
        this.world.playSound(null, this.x, this.y, this.z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F);

        if (this.size >= 2.0F && this.damagesTerrain) {
            this.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
        } else {
            this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
        }

        if (this.damagesTerrain) {
            for (BlockPos blockpos : this.affectedBlockPositions) {
                IBlockState state = selector.getBlockState(blockpos);

                if (spawnParticles) {
                    double d0 = (float)blockpos.getX() + world.rand.nextFloat();
                    double d1 = (float)blockpos.getY() + world.rand.nextFloat();
                    double d2 = (float)blockpos.getZ() + world.rand.nextFloat();
                    double d3 = d0 - this.x;
                    double d4 = d1 - this.y;
                    double d5 = d2 - this.z;
                    double d6 = MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
                    d3 = d3 / d6;
                    d4 = d4 / d6;
                    d5 = d5 / d6;
                    double d7 = 0.5D / (d6 / (double)this.size + 0.1D);
                    d7 = d7 * (double)(this.world.rand.nextFloat() * this.world.rand.nextFloat() + 0.3F);
                    d3 = d3 * d7;
                    d4 = d4 * d7;
                    d5 = d5 * d7;
                    this.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, (d0 + this.x) * 0.5D, (d1 + this.y) * 0.5D, (d2 + this.z) * 0.5D, d3, d4, d5);
                    this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, d3, d4, d5);
                }

                if (state.getMaterial() != Material.AIR) {
                    Block block = state.getBlock();
                    if (block.canDropFromExplosion(this)) {
                        block.dropBlockAsItemWithChance(this.world, blockpos, state, 1.0F / this.size, 0);
                    }
                    block.onBlockExploded(this.world, blockpos, this);
                }
            }
        }
        if (this.causesFire) {
            for (BlockPos pos : this.affectedBlockPositions) {
                if (selector.getBlockState(pos).getMaterial() == Material.AIR
                        && this.world.getBlockState(pos.down()).isFullBlock() && (world.rand.nextInt() & 3) == 0) {
                    selector.setBlockState(pos, Blocks.FIRE.getDefaultState());
                }
            }
        }
    }


    public Map<EntityPlayer, Vec3d> getPlayerKnockbackMap() {
        return this.playerKnockbackMap;
    }


    @Nullable
    public EntityLivingBase getExplosivePlacedBy() {
        if (this.exploder == null) {
            return null;
        } else if (this.exploder instanceof EntityTNTPrimed) {
            return ((EntityTNTPrimed)this.exploder).getTntPlacedBy();
        } else {
            return this.exploder instanceof EntityLivingBase ? (EntityLivingBase)this.exploder : null;
        }
    }


    public void clearAffectedBlockPositions() {
        this.affectedBlockPositions.clear();
        if(!world.isRemote)((ChunkLCache)selector).clear();
    }


    public List<BlockPos> getAffectedBlockPositions() {
        return this.affectedBlockPositions;
    }


    public Vec3d getPosition(){
        return this.position;
    }
}