package net.minecraft.world;

import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketExplosion;
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
import plus.fserver.ElcaCV;
import plus.fserver.ElcaConfig;
import plus.fserver.server.elca.ChunkLCache;
import plus.fserver.utls.collections.ArrayListE10;
import plus.fserver.utls.math.MathUtls;
import plus.tson.utl.uns.UnsafeUtils;


public class Explosion {
    private final boolean causesFire;
    private final boolean damagesTerrain;
    final World world;
    private final double x;
    private final double y;
    private final double z;
    private final Entity exploder;
    private final float size;
    private final List<BlockPos> affectedBlockPositions;
    private final Map<EntityPlayer, Vec3d> playerKnockbackMap;
    private final Vec3d position;
    private final ChunkLCache selector;
    private boolean async = false;

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
        this.affectedBlockPositions = new ArrayListE10<>();
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

        this.selector = world.bigLocalCache.getOrInitExecutor(MathHelper.floor(x)>>4, MathHelper.floor(z)>>4, 6);
    }

//
//    public static class Async extends Explosion{
//        public Async(World worldIn, Entity entityIn, double x, double y, double z, float size, boolean causesFire, boolean damagesTerrain) {
//            super(worldIn, entityIn, x, y, z, size, causesFire, damagesTerrain);
//        }
//
//        @Override
//        public void doExplosionA() {
//            super.doExplosionA();
//            ((WorldServer)world).addSyncTask(() -> completeExplosion(Async.this));
//        }
//    }


    private static long encodeWorldCoord(final int x, final int y, final int z) {
        return (long) y << 52 | x + 33554432L << 26 | z + 33554432L;
    }

    private static LongOpenHashSet _temp;
    private static final long tempOff = UnsafeUtils.offsetS(Explosion.class, "_temp");
    private static LongOpenHashSet tempSet(){
        LongOpenHashSet temp = _temp;
        if(temp != null && UnsafeUtils.compareAndSwap(Explosion.class, tempOff, temp, null)) {
            temp.clear();
            return temp;
        }
        return new LongOpenHashSet(24, 0.75f);
    }

    private static void decodeWorldCoord(final BlockPos.MutableBlockPos pos, final long longPos) {
        pos.setPosB(
                (int)(longPos >> 26 & 0x3FFFFFFL) - 33554432,
                (int)(longPos >> 52 & 0xFFL),
                (int)(longPos & 0x3FFFFFFL) - 33554432
        );
    }


    public void doExplosionA() {
        LongOpenHashSet set = tempSet();
        double xPos, yPos, zPos;
        float fSize;

        final Entity exploder = this.exploder;
        final World world = this.world;

        ChunkLCache selector = this.selector;
        final float size = this.size;
        final double x = this.x, y = this.y, z = this.z;

        BlockPos.MutableBlockPos blockpos = new BlockPos.MutableBlockPos();
        int seed = world.rand.nextInt();

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

                        seed = seed * 31 + 17;

                        fSize = size * (0.7F + ((seed & MathUtls.FR24_MASK)*MathUtls.D1_F24) * 0.6F);

                        int bx = MathHelper.floor(xPos = x);
                        int by = MathHelper.floor(yPos = y);
                        int bz = MathHelper.floor(zPos = z);

                        blockpos.setPosB(bx, by, bz);
                        selector = selector.getExecutor(bx>>4, bz>>4);
                        IBlockState state = selector.getChunk().getBlockState(bx, by, bz);

                        l1: for (; fSize > 0.0F; fSize -= 0.22500001F) {
                            if (state.getMaterial() != Material.AIR) {
                                float f2 = exploder != null ? exploder.getExplosionResistance(this, world, blockpos, state) : state.getBlock().getExplosionResistance(world, blockpos, null, this);
                                fSize -= (f2 + 0.3F) * 0.3F;
                            }
                            while (fSize > 0.0F && (exploder == null || exploder.canExplosionDestroyBlock(this, world, blockpos, state, fSize))) {
                                set.add(encodeWorldCoord(bx, by, bz));

                                xPos += dX * 0.30000001192092896D;
                                yPos += dY * 0.30000001192092896D;
                                zPos += dZ * 0.30000001192092896D;

                                int bx2 = MathHelper.floor(xPos);
                                int by2 = MathHelper.floor(yPos);
                                int bz2 = MathHelper.floor(zPos);

                                if(bx2 != bx || by2 != by || bz2 != bz){
                                    blockpos.setPosB(bx = bx2, by = by2, bz = bz2);
                                    state = selector.getBlockState(bx2, by2, bz2);
                                    continue l1;
                                } else {
                                    fSize -= 0.22500001F;
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }

        if(!set.isEmpty()){
            ArrayListE10<BlockPos> aff = (ArrayListE10<BlockPos>)this.affectedBlockPositions;
            aff.ensureCapacity(set.size());
            LongIterator itr = set.iterator();
            while (itr.hasNext()){
                BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
                decodeWorldCoord(mut, itr.nextLong());
                aff.addF(mut);
            }
        }
        _temp = set;

        fSize = this.size * 2.0F;
        xPos = fSize - 1;
        zPos = fSize + 1;

        List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(exploder,
                new AxisAlignedBB(
                        MathHelper.floor(x - xPos),
                        MathHelper.floor(y - xPos),
                        MathHelper.floor(z - xPos),

                        MathHelper.floor(x + zPos),
                        MathHelper.floor(y + zPos),
                        MathHelper.floor(z + zPos)
                )
        );
        if(ElcaCV.ENABLE_EXPLOSION_EVENT)
            ForgeEventFactory.onExplosionDetonate(world, this, list, fSize);

        if(list.size() < ElcaConfig.ASYNC_EXPLOSION_THRESHOLD) {
            pushMinEntites(list);
        } else {
            async = true;
            world.getMinecraftServer().ticker().runAsync(new AsyExplosionPushTask(list));
        }
    }


    private class AsyExplosionPushTask extends ArrayList<Entity> implements Runnable{
        AsyExplosionPushTask(List<Entity> list){
            super(list);
        }
        @Override
        public void run() {
            pushMinEntites(this);
            ((WorldServer)world).addSyncTask(() -> {
                async = false;
                completeExplosion(Explosion.this);
            });
        }
    }


    private void pushMinEntites(List<Entity> list){
        float fSize = this.size * 2.0F;
        final ChunkLCache selector = this.selector;
        World world = this.world;
        Vec3d position = this.position;

        for (int k2 = 0; k2 < list.size(); ++k2) {
            Entity entity = list.get(k2);
            if(entity.isDead)continue;

            if (!entity.isImmuneToExplosions()) {
                double d12 = entity.getDistance(x, y, z) / fSize;

                if (d12 <= 1.0D) {
                    double xPos = entity.posX - x;
                    double yPos = entity.posY + entity.getEyeHeight() - y;
                    double zPos = entity.posZ - z;
                    double d13 = MathHelper.sqrt(xPos * xPos + yPos * yPos + zPos * zPos);

                    if (d13 != 0.0D) {
                        xPos = xPos / d13;
                        yPos = yPos / d13;
                        zPos = zPos / d13;
                        double d14 = world.getBlockDensity(selector, position, entity.getEntityBoundingBox());
                        double d10 = (1.0D - d12) * d14;
                        entity.attackEntityFrom(DamageSource.causeExplosionDamage(this), (float)((int)((d10 * d10 + d10) / 2.0D * 7.0D * fSize + 1.0D)));
                        double d11 = d10;

                        if (entity.isLiving()) {
                            d11 = EnchantmentProtection.getBlastDamageReduction((EntityLivingBase)entity, d10);

                            if (entity instanceof EntityPlayer) {
                                EntityPlayer entityplayer = (EntityPlayer)entity;

                                if (!entityplayer.isSpectator() && (!entityplayer.isCreative() || !entityplayer.capabilities.isFlying)) {
                                    this.playerKnockbackMap.put(entityplayer, new Vec3d(xPos * d10, yPos * d10, zPos * d10));
                                }
                            }
                        }

                        entity.motionX += xPos * d11;
                        entity.motionY += yPos * d11;
                        entity.motionZ += zPos * d11;
                    }
                }
            }
        }
    }


    public void doExplosionB(boolean spawnParticles) {
        final World world = this.world;
        final Random rand = world.rand;
        world.playSound(null, this.x, this.y, this.z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.2F) * 0.7F);

        if (this.size >= 2.0F && this.damagesTerrain) {
            world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
        } else {
            world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
        }

        ChunkLCache selector = this.selector;

        if (this.damagesTerrain) {
            for (BlockPos blockpos : this.affectedBlockPositions) {
                IBlockState state = selector.getBlockState(blockpos);

                if (spawnParticles) {
                    double d0 = (float)blockpos.getX() + rand.nextFloat();
                    double d1 = (float)blockpos.getY() + rand.nextFloat();
                    double d2 = (float)blockpos.getZ() + rand.nextFloat();
                    double d3 = d0 - this.x;
                    double d4 = d1 - this.y;
                    double d5 = d2 - this.z;
                    double d6 = MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
                    d3 = d3 / d6;
                    d4 = d4 / d6;
                    d5 = d5 / d6;
                    double d7 = 0.5D / (d6 / (double)this.size + 0.1D);
                    d7 = d7 * (double)(rand.nextFloat() * rand.nextFloat() + 0.3F);
                    d3 = d3 * d7;
                    d4 = d4 * d7;
                    d5 = d5 * d7;
                    world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, (d0 + this.x) * 0.5D, (d1 + this.y) * 0.5D, (d2 + this.z) * 0.5D, d3, d4, d5);
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, d3, d4, d5);
                }

                if (state.getMaterial() != Material.AIR) {
                    Block block = state.getBlock();
                    if (block.canDropFromExplosion(this)) {
                        block.dropBlockAsItemWithChance(world, blockpos, state, 1.0F / this.size, 0);
                    }
                    block.onBlockExploded(world, blockpos, this);
                }
            }
        }
        if (this.causesFire) {
            for (BlockPos pos : this.affectedBlockPositions) {
                if (selector.getBlockState(pos).getMaterial() == Material.AIR
                        && selector.getBlockState(pos.getX(), pos.getY()-1, pos.getZ()).isFullBlock() && (rand.nextInt() & 3) == 0) {
                    selector.setBlockState(pos, Blocks.FIRE.getDefaultState());
                }
            }
        }
    }


    public static void completeExplosion(Explosion explosion){
        if(explosion.async)return;
        explosion.doExplosionB(false);

        if (!explosion.damagesTerrain) {
            explosion.clearAffectedBlockPositions();
        }

        SPacketExplosion packet = null;
        for (EntityPlayer entityplayer : explosion.world.curPlayerEntities) {
            if (entityplayer.getDistanceSq(explosion.x, explosion.y, explosion.z) < 4096.0D) {
                if(packet==null){
                    packet = new SPacketExplosion(explosion.x, explosion.y, explosion.z, explosion.size,
                            explosion.getAffectedBlockPositions(),
                            explosion.getPlayerKnockbackMap().get(entityplayer)
                    );
                }
                ((EntityPlayerMP)entityplayer).connection.netManager.sendPacketE(packet);
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
            return this.exploder.isLiving() ? (EntityLivingBase)this.exploder : null;
        }
    }


    public void clearAffectedBlockPositions() {
        this.affectedBlockPositions.clear();
        if(!world.isRemote) selector.clear();
    }


    public List<BlockPos> getAffectedBlockPositions() {
        return this.affectedBlockPositions;
    }


    public Vec3d getPosition(){
        return this.position;
    }
}