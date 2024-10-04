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
import plus.fserver.api.IBlockEAccess;
import plus.fserver.world.PntMatrix;


public class PathNavigateGround extends PathNavigate {
    private boolean shouldAvoidSun;

    public PathNavigateGround(EntityLiving entitylivingIn, World worldIn) {
        super(entitylivingIn, worldIn);
    }


    @Override
    protected PathFinder getPathFinder() {
        NodeProcessor proc = this.nodeProcessor = new WalkNodeProcessor(entity.world);
        proc.setCanEnterDoors(true);
        return new PathFinder(proc);
    }


    @Override
    protected boolean canNavigate() {
        Entity entity;
        return (entity = this.entity).onGround || this.getCanSwim() && this.isInLiquid() || entity.isRiding();
    }


    @Override
    protected Vec3d getEntityPosition() {
        Entity entity;
        return new Vec3d((entity = this.entity).posX, this.getPathablePosY(), entity.posZ);
    }


    @Override
    protected void updateEntityPos() {
        Entity entity;
        lastPosX = (entity = this.entity).posX;
        lastPosY = this.getPathablePosY();
        lastPosZ = entity.posZ;
    }


    @Override
    public Path getPathToPos(BlockPos pos) {
        World world = entity.world;
        Chunk chunk = world.getChunk(pos);
        if(pos.getY()<0||pos.getY()>255)return null;
        if (chunk.getBlockState(pos).getMaterial() == Material.AIR) {
            BlockPos blockpos = pos.down();
            int lim = blockpos.getY();

            for (; blockpos.getY() > 0 && chunk.getBlockState(blockpos).getMaterial() == Material.AIR;
                 BlockPos.unsafeEdit(blockpos, blockpos.getX(), blockpos.getY()-1, blockpos.getZ())) ;

            if (blockpos.getY() > 0) {
                BlockPos.unsafeEdit(blockpos, pos.getX(), pos.getY()+1, pos.getZ());
                return super.getPathToPos(blockpos);
            }
            BlockPos.unsafeEdit(blockpos, blockpos.getX(), lim, blockpos.getZ());
            lim = Math.min(blockpos.getY()+32, world.getHeight());
            while (blockpos.getY() < lim && chunk.getBlockState(blockpos).getMaterial() == Material.AIR){
                BlockPos.unsafeEdit(blockpos, blockpos.getX(), blockpos.getY()+1, blockpos.getZ());
            }

            pos = blockpos;
        }
        if (!chunk.getBlockState(pos).getMaterial().isSolid()) {
            return super.getPathToPos(pos);
        } else {
            BlockPos blockpos1;
            for (blockpos1 = pos.up(); blockpos1.getY() < world.getHeight() && chunk.getBlockState(blockpos1).getMaterial().isSolid(); blockpos1 = blockpos1.up()) {
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
        Entity entity = this.entity;
        if (entity.isInWater() && this.getCanSwim()) {
            IBlockEAccess access = entity.world.access();
            int i = (int)entity.getEntityBoundingBox().minY, px, pz, j = 0;

            Block block = access.getBlockState(px = MathHelper.floor(entity.posX), i, pz = MathHelper.floor(entity.posZ)).getBlock();


            while (block == Blocks.FLOWING_WATER || block == Blocks.WATER) {
                ++i;
                block = access.getBlockState(px, i, pz).getBlock();
                ++j;
                if (j > 16) return (int)entity.getEntityBoundingBox().minY;
            }
            return i;
        } else return (int)(entity.getEntityBoundingBox().minY + 0.5D);
    }


    @Override
    protected void removeSunnyPath() {
        super.removeSunnyPath();

        if (this.shouldAvoidSun) {
            Entity entity = this.entity;
            World world = entity.world;

            int prevCX, prevCZ, bx = MathHelper.floor(entity.posX), bz = MathHelper.floor(entity.posZ);
            Chunk chunk = world.getChunk(prevCX = bx >>4, prevCZ = bx >> 4);

            if (chunk.canSeeSky(bx, (int)(entity.getEntityBoundingBox().minY + 0.5D), bz)) return;
            PathPoint pathpoint;
            Path currentPath = this.currentPath;

            for (int i = 0; i < currentPath.getCurrentPathLength(); ++i) {
                pathpoint = currentPath.getPathPointFromIndex(i);
                bx = pathpoint.x; bz = pathpoint.z;
                int cx = bx >> 4, cz = bz >> 4;
                if(prevCX != cx || cz != prevCZ){
                    chunk = world.getChunk(prevCX = cx, prevCZ = cz);
                }

                if (chunk.canSeeSky(bx, pathpoint.y, bz)) {
                    currentPath.setCurrentPathLength(i - 1);
                    return;
                }
            }
        }
    }


    @Override
    protected void pathFollow() {
        Entity entity = this.entity;
        double vX = entity.posX;
        double vY = this.getPathablePosY();
        double vZ = entity.posZ;

        Path currentPath = this.currentPath;
        int i = currentPath.getCurrentPathLength();

        for (int j = currentPath.getCurrentPathIndex(); j < currentPath.getCurrentPathLength(); ++j) {
            if (currentPath.getPathPointFromIndex(j).y != MathHelper.floor(vY)) {
                i = j;
                break;
            }
        }

        this.maxDistanceToWaypoint = entity.width > 0.75F ? entity.width / 2.0F : 0.75F - entity.width / 2.0F;
        PathPoint point = currentPath.getCurrentPoint();

        if (MathHelper.abs((float)(vX - (point.x + 0.5D))) < this.maxDistanceToWaypoint && MathHelper.abs((float)(vZ - (point.z + 0.5D))) < this.maxDistanceToWaypoint && Math.abs(entity.posY - point.y) < 1.0D) {
            currentPath.setCurrentPathIndex(currentPath.getCurrentPathIndex() + 1);
        }

        int k = MathHelper.ceil(entity.width);
        int l = MathHelper.ceil(entity.height);

        for (int index = i - 1; index >= currentPath.getCurrentPathIndex(); --index) {
            PathPoint point0 = currentPath.getPathPointFromIndex(index);
            double v2x = (double)point0.x + (double)((int)(entity.width + 1.0F)) * 0.5;
            double v2z = (double)point0.z + (double)((int)(entity.width + 1.0F)) * 0.5;

            if (this.isDirectPathBetweenPoints(vX, vY, vZ, v2x, v2z, k, l, k)) {
                currentPath.setCurrentPathIndex(index);
                break;
            }
        }
        this.checkForStuck(vX, vY, vZ);
    }


    @Override
    protected boolean isDirectPathBetweenPoints(Vec3d vec, Vec3d posVec32, int sizeX, int sizeY, int sizeZ) {
        return isDirectPathBetweenPoints(vec.x, vec.y, vec.z, posVec32.x, posVec32.z, sizeX, sizeY, sizeZ);
    }


    private boolean isDirectPathBetweenPoints(double vx, double vy, double vz, double v2x, double v2z, int sizeX, int sizeY, int sizeZ) {
        double d0 = v2x - vx;
        double d1 = v2z - vz;
        double d2 = d0 * d0 + d1 * d1;

        if (d2 < 1.0E-8D) {
            return false;
        } else {
            int fX = MathHelper.floor(vx);
            int fZ = MathHelper.floor(vz);

            d2 = 1.0D / Math.sqrt(d2);
            d0 = d0 * d2;
            d1 = d1 * d2;

            //todo maybe non-threadsafe
            IBlockEAccess access = entity.world.access();
            PntMatrix matrix = ((WalkNodeProcessor)nodeProcessor).matrix;
            //todo maybe this equal "if false"
            if(matrix == null){
                matrix = ((WorldServer)entity.world).navCache.getBy(entity);
            }
            PntMatrix.Chunked chunked = matrix.getChunked(fX, fZ);

            if (!this.isSafeToStandAt(access, chunked, fX, (int)vy, fZ, sizeX + 2, sizeY, sizeZ - 2, vx, vz, d0, d1)) {
                return false;
            } else {
                double d6 = (double)fX - vx,
                        d7 = (double)fZ - vz,
                        d4 = 1.0D / Math.abs(d0),
                        d5 = 1.0D / Math.abs(d1);

                if (d0 >= 0) ++d6;
                if (d1 >= 0) ++d7;

                d6 = d6 / d0;
                d7 = d7 / d1;

                int k = d0 < 0.0D ? -1 : 1,
                        l = d1 < 0.0D ? -1 : 1;

                int i1 = MathHelper.floor(v2x);
                int j1 = MathHelper.floor(v2z);
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
                    if (!this.isSafeToStandAt(access, chunked, fX, (int)vy, fZ, sizeX, sizeY, sizeZ, vx, vz, d0, d1)) {
                        return false;
                    }
                }
                return true;
            }
        }
    }


    private boolean isSafeToStandAt(IBlockEAccess access, PntMatrix.Chunked chunked, int x, int y, int z, int sizeX, int sizeY, int sizeZ, double vecX, double vecZ, double param, double param2) {
        int dX = x - (sizeX >> 1);
        int dZ = z - (sizeZ >> 1);

        if (!isPositionClear(access, dX, y, dZ, sizeX, sizeY, sizeZ, vecX, vecZ, param, param2)) {
            return false;
        } else {
            PathNodeType type;
            for (int curX = dX; curX < dX + sizeX; ++curX) {
                for (int curZ = dZ; curZ < dZ + sizeZ; ++curZ) {
                    double d0 = (double)curX + 0.5D - vecX;
                    double d1 = (double)curZ + 0.5D - vecZ;

                    if (d0 * param + d1 * param2 >= 0.0D) {
                        chunked = chunked.get(curX, curZ);
                        type = chunked.getCur(curX, y - 1, curZ);
                        if(type == null) {
                            type = this.nodeProcessor.getPathNodeType(access, curX, y - 1, curZ, this.entity, sizeX, sizeY, sizeZ, true, true);
                            chunked.setCur(curX, y - 1, curZ, type);
                        }
                        if (type == PathNodeType.WATER) return false;
                        if (type == PathNodeType.LAVA) return false;
                        if (type == PathNodeType.OPEN) return false;

                        type = chunked.getCur(curX, y, curZ);
                        if(type == null) {
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


    private static boolean isPositionClear(IBlockEAccess access, int x, int y, int z, int sizeX, int sizeY, int sizeZ, double vecX, double vecZ, double p1, double p2) {
        //todo move to pool
        BlockPos.MutableBlockPos pos = access.getWorld().localTempPos2;
        for (int fx = x + sizeX -1, fy = y + sizeY - 1, fz = z + sizeZ - 1;x<fx;++x)
            for (;z<fz;++z)
                for (;y<fy;++y){
                    pos.setPosB(x, y, z);
                    if ((x + 0.5D - vecX) * p1 + (z + 0.5D - vecZ) * p2 >= 0.0D
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