package net.minecraft.pathfinding;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import plus.fserver.world.PntMatrix;


public class PathNavigateGround extends PathNavigate {
    private boolean shouldAvoidSun;

    public PathNavigateGround(EntityLiving entitylivingIn, World worldIn) {
        super(entitylivingIn, worldIn);
    }


    @Override
    protected PathFinder getPathFinder() {
        this.nodeProcessor = new WalkNodeProcessor();
        this.nodeProcessor.setCanEnterDoors(true);
        return new PathFinder(this.nodeProcessor);
    }


    @Override
    protected boolean canNavigate() {
        return this.entity.onGround || this.getCanSwim() && this.isInLiquid() || this.entity.isRiding();
    }


    @Override
    protected Vec3d getEntityPosition() {
        return new Vec3d(this.entity.posX, this.getPathablePosY(), this.entity.posZ);
    }


    @Override
    public Path getPathToPos(BlockPos pos) {
        Chunk chunk = world.getChunk(pos);
        if(pos.getY()<0||pos.getY()>255)return null;
        if (chunk.getBlockState(pos).getMaterial() == Material.AIR) {
            BlockPos blockpos;

            for (blockpos = pos.down(); blockpos.getY() > 0 && chunk.getBlockState(blockpos).getMaterial() == Material.AIR; blockpos = blockpos.down()) {
                ;
            }
            if (blockpos.getY() > 0) {
                return super.getPathToPos(blockpos.up());
            }
            while (blockpos.getY() < this.world.getHeight() && chunk.getBlockState(blockpos).getMaterial() == Material.AIR)
            {
                blockpos = blockpos.up();
            }
            pos = blockpos;
        }
        if (!chunk.getBlockState(pos).getMaterial().isSolid()) {
            return super.getPathToPos(pos);
        } else {
            BlockPos blockpos1;
            for (blockpos1 = pos.up(); blockpos1.getY() < this.world.getHeight() && chunk.getBlockState(blockpos1).getMaterial().isSolid(); blockpos1 = blockpos1.up()) {
                ;
            }
            return super.getPathToPos(blockpos1);
        }
    }


    @Override
    public Path getPathToEntityLiving(Entity entityIn) {
        return this.getPathToPos(new BlockPos(entityIn));
    }


    private int getPathablePosY() {
        if (this.entity.isInWater() && this.getCanSwim()) {
            int i = (int)this.entity.getEntityBoundingBox().minY;
            Block block = this.world.getBlockState(new BlockPos(MathHelper.floor(this.entity.posX), i, MathHelper.floor(this.entity.posZ))).getBlock();
            int j = 0;

            while (block == Blocks.FLOWING_WATER || block == Blocks.WATER) {
                ++i;
                block = this.world.getBlockState(new BlockPos(MathHelper.floor(this.entity.posX), i, MathHelper.floor(this.entity.posZ))).getBlock();
                ++j;

                if (j > 16) {
                    return (int)this.entity.getEntityBoundingBox().minY;
                }
            }
            return i;
        } else {
            return (int)(this.entity.getEntityBoundingBox().minY + 0.5D);
        }
    }


    @Override
    protected void removeSunnyPath() {
        super.removeSunnyPath();

        if (this.shouldAvoidSun) {
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(
                    MathHelper.floor(this.entity.posX),
                    (int)(this.entity.getEntityBoundingBox().minY + 0.5D),
                    MathHelper.floor(this.entity.posZ));

            if (this.world.canSeeSky(pos)) return;
            PathPoint pathpoint;
            for (int i = 0; i < this.currentPath.getCurrentPathLength(); ++i) {
                pathpoint = this.currentPath.getPathPointFromIndex(i);

                if (this.world.canSeeSky(pos.setPos(pathpoint.x, pathpoint.y, pathpoint.z))) {
                    this.currentPath.setCurrentPathLength(i - 1);
                    return;
                }
            }
        }
    }


    @Override
    protected boolean isDirectPathBetweenPoints(Vec3d posVec31, Vec3d posVec32, int sizeX, int sizeY, int sizeZ) {
        double d0 = posVec32.x - posVec31.x;
        double d1 = posVec32.z - posVec31.z;
        double d2 = d0 * d0 + d1 * d1;

        if (d2 < 1.0E-8D) {
            return false;
        } else {
            int fX = MathHelper.floor(posVec31.x);
            int fZ = MathHelper.floor(posVec31.z);

            d2 = 1.0D / Math.sqrt(d2);
            d0 = d0 * d2;
            d1 = d1 * d2;

            //todo maybe non-threadsafe
            IBlockAccess access = ((WorldServer)world).localSelector;
            PntMatrix matrix = ((WalkNodeProcessor)nodeProcessor).matrix;
            //todo maybe this equal "if false"
            if(matrix==null){
                matrix = ((WorldServer)world).navCache.getBy(entity);
            }
            PntMatrix.Chunked chunked = matrix.getChunked(fX, fZ);

            if (!this.isSafeToStandAt(access, chunked, fX, (int)posVec31.y, fZ, sizeX + 2, sizeY, sizeZ - 2, posVec31, d0, d1)) {
                return false;
            } else {
                double d6 = (double)fX - posVec31.x,
                       d7 = (double)fZ - posVec31.z,
                       d4 = 1.0D / Math.abs(d0),
                       d5 = 1.0D / Math.abs(d1);

                if (d0 >= 0) ++d6;
                if (d1 >= 0) ++d7;

                d6 = d6 / d0;
                d7 = d7 / d1;

                int k = d0 < 0.0D ? -1 : 1,
                    l = d1 < 0.0D ? -1 : 1;

                int i1 = MathHelper.floor(posVec32.x);
                int j1 = MathHelper.floor(posVec32.z);
                int k1 = i1 - fX;
                int l1 = j1 - fZ;

                while (k1 * k > 0 || l1 * l > 0) {
                    if (d6 < d7) {
                        d6 += d4;
                        fX += k;
                        k1 = i1 - fX;
                    } else {
                        d7 += d5;
                        fZ += l;
                        l1 = j1 - fZ;
                    }
                    if (!this.isSafeToStandAt(access, chunked, fX, (int)posVec31.y, fZ, sizeX, sizeY, sizeZ, posVec31, d0, d1)) {
                        return false;
                    }
                }
                return true;
            }
        }
    }


    private boolean isSafeToStandAt(IBlockAccess access, PntMatrix.Chunked chunked, int x, int y, int z, int sizeX, int sizeY, int sizeZ, Vec3d vec31, double param, double param2) {
        int dX = x - sizeX / 2;
        int dZ = z - sizeZ / 2;

        if (!this.isPositionClear(access, dX, y, dZ, sizeX, sizeY, sizeZ, vec31, param, param2)) {
            return false;
        } else {
            PathNodeType type;
            for (int curX = dX; curX < dX + sizeX; ++curX) {
                for (int curZ = dZ; curZ < dZ + sizeZ; ++curZ) {
                    double d0 = (double)curX + 0.5D - vec31.x;
                    double d1 = (double)curZ + 0.5D - vec31.z;

                    if (d0 * param + d1 * param2 >= 0.0D) {
                        chunked = chunked.get(curX, curZ);
                        type = chunked.getCur(curX, y - 1, curZ);
                        if(type==null) {
                            type = this.nodeProcessor.getPathNodeType(access, curX, y - 1, curZ, this.entity, sizeX, sizeY, sizeZ, true, true);
                            chunked.setCur(curX, y - 1, curZ, type);
                        }
                        if (type == PathNodeType.WATER) return false;
                        if (type == PathNodeType.LAVA) return false;
                        if (type == PathNodeType.OPEN) return false;

                        type = chunked.getCur(curX, y, curZ);
                        if(type==null) {
                            type = this.nodeProcessor.getPathNodeType(access, curX, y, curZ, this.entity, sizeX, sizeY, sizeZ, true, true);
                            chunked.setCur(curX, y, curZ, type);
                        }

                        float priority = this.entity.getPathPriority(type);
                        if (priority < 0.0F || priority >= 8.0F) return false;

                        if (type == PathNodeType.DAMAGE_FIRE || type == PathNodeType.DANGER_FIRE || type == PathNodeType.DAMAGE_OTHER) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
    }


    private boolean isPositionClear(IBlockAccess access, int x, int y, int z, int sizeX, int sizeY, int sizeZ, Vec3d vec, double p1, double p2) {
        int fx = x + sizeX -1, fy = y + sizeY - 1, fz = z + sizeZ - 1;
        //todo move to pool
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (;x<fx;++x)
            for (;z<fz;++z)
                for (;y<fy;++y){
                    pos.setPos(x, y, z);
                    if ((pos.getX() + 0.5D - vec.x) * p1 + (pos.getZ() + 0.5D - vec.z) * p2 >= 0.0D
                        && access.getBlockState(pos).getBlock().isPassable(access, pos)
                    ) return false;
                }
        return true;
    }


    public void setBreakDoors(boolean canBreakDoors) {
        this.nodeProcessor.setCanOpenDoors(canBreakDoors);
    }


    public void setEnterDoors(boolean enterDoors) {
        this.nodeProcessor.setCanEnterDoors(enterDoors);
    }


    public boolean getEnterDoors() {
        return this.nodeProcessor.getCanEnterDoors();
    }


    public void setCanSwim(boolean canSwim) {
        this.nodeProcessor.setCanSwim(canSwim);
    }


    public boolean getCanSwim() {
        return this.nodeProcessor.getCanSwim();
    }


    public void setAvoidSun(boolean avoidSun) {
        this.shouldAvoidSun = avoidSun;
    }
}