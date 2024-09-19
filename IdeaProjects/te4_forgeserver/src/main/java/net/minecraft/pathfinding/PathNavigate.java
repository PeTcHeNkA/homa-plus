package net.minecraft.pathfinding;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import plus.fserver.ElcaCV;
import plus.fserver.api.IBlockEAccess;
import plus.fserver.server.elca.ChunkLCache;
import plus.fserver.utls.math.MathUtls;


public abstract class PathNavigate {
    protected EntityLiving entity;
    protected World world;
    @Nullable
    protected Path currentPath;
    protected double speed;
    private final IAttributeInstance pathSearchRange;
    protected int totalTicks;
    private int ticksAtLastPos;
    double lastPosX, lastPosY, lastPosZ;
    private Vec3d timeoutCachedNode = Vec3d.ZERO;
    private long timeoutTimer;
    private long lastTimeoutCheck;
    private double timeoutLimit;
    protected float maxDistanceToWaypoint = 0.5F;
    protected boolean tryUpdatePath;
    private long lastTimeUpdated;
    protected NodeProcessor nodeProcessor;
    private BlockPos targetPos;
    private final PathFinder pathFinder;

    public PathNavigate(EntityLiving entityIn, World worldIn) {
        this.entity = entityIn;
        this.world = worldIn;
        this.pathSearchRange = entityIn.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
        this.pathFinder = this.getPathFinder();
    }


    protected abstract PathFinder getPathFinder();


    public void setSpeed(double speedIn) {
        this.speed = speedIn;
    }


    public float getPathSearchRange() {
        return (float)this.pathSearchRange.getAttributeValue();
    }


    public boolean canUpdatePathOnTimeout() {
        return this.tryUpdatePath;
    }


    public void updatePath() {
        if (this.world.getTotalWorldTime() - this.lastTimeUpdated > world.tryUpdateNavigationLimit) {
            if (this.targetPos != null) {
                this.currentPath = null;
                this.currentPath = this.getPathToPos(this.targetPos);
                this.lastTimeUpdated = this.world.getTotalWorldTime();
                this.tryUpdatePath = false;
            }
        } else {
            this.tryUpdatePath = true;
        }
    }


    @Nullable
    public final Path getPathToXYZ(double x, double y, double z) {
        if (!this.canNavigate()) return null;
        else if (this.currentPath != null && !this.currentPath.isFinished() && targetPos != null && isNear(targetPos, x, y, z))
            return this.currentPath;
        else {
            BlockPos targetPos;
            if((targetPos = this.targetPos) == null) {
                this.targetPos = targetPos = new BlockPos(x, y, z);
            } else {
                BlockPos.unsafeEdit(targetPos, MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
            }
            EntityLiving entity;
            ChunkLCache bigLocalCache = (entity = this.entity).world.bigLocalCache;
            bigLocalCache.init(entity);
            return this.pathFinder.findPath(bigLocalCache, entity, targetPos, this.getPathSearchRange());
        }
    }


    @Nullable
    public Path getPathToPos(BlockPos pos) {
        if (!this.canNavigate()) return null;
        else if (this.currentPath != null && !this.currentPath.isFinished() && targetPos != null && isNear(pos, targetPos))
            return this.currentPath;
        else {
            this.targetPos = pos;
            EntityLiving entity;
            ChunkLCache bigLocalCache = (entity = this.entity).world.bigLocalCache;
            bigLocalCache.init(entity);
            return this.pathFinder.findPath(bigLocalCache, entity, pos, this.getPathSearchRange());
        }
    }


    private boolean isNear(BlockPos pos1, double pos2x, double pos2y, double pos2z){
        if(pos1.getY() != MathHelper.floor(pos2y))return false;
        pos2x = MathUtls.range(pos2x, pos1.getX());
        if(pos2x>ElcaCV.PATH_NEAR_RADIUS || pos2x<-ElcaCV.PATH_NEAR_RADIUS)return false;
        pos2x = MathUtls.range(pos2z, pos1.getZ());
        return !(pos2x>ElcaCV.PATH_NEAR_RADIUS || pos2x<-ElcaCV.PATH_NEAR_RADIUS);
    }


    private boolean isNear(BlockPos pos1, BlockPos pos2){
        if(pos1.getY() != pos2.getY())return false;
        if(!(pos1.getX() == pos2.getX() || pos1.getX()+1 == pos2.getX() || pos1.getX()-1 == pos2.getX()))return false;
        return pos1.getZ() == pos2.getZ() || pos1.getZ()+1 == pos2.getZ() || pos1.getZ()-1 == pos2.getZ();
    }


    @Nullable
    public Path getPathToEntityLiving(Entity entityIn) {
        if (!this.canNavigate()) return null;
        else {
            if (this.currentPath != null && !this.currentPath.isFinished() && isNear(targetPos, entityIn.posX, entityIn.posY, entityIn.posZ)) return this.currentPath;
            else {
                EntityLiving entity;
                ChunkLCache bigLocalCache = (entity = this.entity).world.bigLocalCache;
                bigLocalCache.init(entity);
                BlockPos targetPos;
                if((targetPos = this.targetPos) == null) {
                    this.targetPos = new BlockPos(entityIn);
                } else {
                    BlockPos.unsafeEdit(
                            targetPos,
                            MathHelper.floor(entity.posX),
                            MathHelper.floor(entity.posY),
                            MathHelper.floor(entity.posZ)
                    );
                }
                return this.pathFinder.findPath(bigLocalCache, entity, entityIn, this.getPathSearchRange());
            }
        }
    }


    public boolean tryMoveToXYZ(double x, double y, double z, double speedIn) {
        return this.setPath(this.getPathToXYZ(x, y, z), speedIn);
    }


    public boolean tryMoveToEntityLiving(Entity entityIn, double speedIn) {
        Path path;
        return (path = this.getPathToEntityLiving(entityIn)) != null && this.setPath(path, speedIn);
    }


    public boolean setPath(@Nullable Path pathentityIn, double speedIn) {
        if (pathentityIn == null) {
            this.currentPath = null;
            return false;
        } else {
            if (!pathentityIn.isSamePath(this.currentPath)) this.currentPath = pathentityIn;

            this.removeSunnyPath();

            if (this.currentPath.getCurrentPathLength() <= 0) return false;
            else {
                this.speed = speedIn;
                this.ticksAtLastPos = this.totalTicks;
                this.updateEntityPos();
                return true;
            }
        }
    }


    @Nullable
    public Path getPath() {
        return this.currentPath;
    }


    public void onUpdateNavigation() {
        ++this.totalTicks;

        if (this.tryUpdatePath) {
            this.updatePath();
        }

        if (!this.noPath()) {
            if (this.canNavigate()) {
                this.pathFollow();
            } else {
                Path currentPath;
                if ((currentPath = this.currentPath) != null && currentPath.getCurrentPathIndex() < currentPath.getCurrentPathLength()) {
                    Entity entity;
                    Vec3d vec3d = this.getEntityPosition();
                    Vec3d vec3d1 = currentPath.getVectorFromIndex(entity = this.entity, currentPath.getCurrentPathIndex());

                    if (vec3d.y > vec3d1.y && !entity.onGround && MathHelper.floor(vec3d.x) ==
                            MathHelper.floor(vec3d1.x) && MathHelper.floor(vec3d.z) == MathHelper.floor(vec3d1.z)) {
                        currentPath.setCurrentPathIndex(currentPath.getCurrentPathIndex() + 1);
                    }
                }
            }

            this.debugPathFinding();

            if (!this.noPath()) {
                PathPoint point = this.currentPath.getCurrentPoint();
                EntityLiving entity = this.entity;
                double vecX = (double)point.x + (double)((int)(entity.width + 1.0F)) * 0.5;
                double vecZ = (double)point.z + (double)((int)(entity.width + 1.0F)) * 0.5;
                World world;
                IBlockEAccess access;
                BlockPos blockpos = (world = this.world).localTempPos3.setPos(vecX, point.y-1, vecZ);
                AxisAlignedBB box = (access = world.access()).getBlockState(blockpos).getBoundingBox(access, blockpos);
                entity.getMoveHelper().setMoveTo(
                        vecX,
                        point.y + 1.1D - box.maxY,
                        vecZ,
                        this.speed
                );
            }
        }
    }


    protected void debugPathFinding() {}


    protected void pathFollow() {
        Vec3d vec3d = this.getEntityPosition();
        int i = this.currentPath.getCurrentPathLength();

        for (int j = this.currentPath.getCurrentPathIndex(); j < this.currentPath.getCurrentPathLength(); ++j) {
            if ((double)this.currentPath.getPathPointFromIndex(j).y != Math.floor(vec3d.y)) {
                i = j;
                break;
            }
        }

        this.maxDistanceToWaypoint = this.entity.width > 0.75F ? this.entity.width / 2.0F : 0.75F - this.entity.width / 2.0F;
        PathPoint point = this.currentPath.getCurrentPoint();

        if (MathHelper.abs((float)(this.entity.posX - (point.x + 0.5D))) < this.maxDistanceToWaypoint && MathHelper.abs((float)(this.entity.posZ - (point.z + 0.5D))) < this.maxDistanceToWaypoint && Math.abs(this.entity.posY - point.y) < 1.0D) {
            this.currentPath.setCurrentPathIndex(this.currentPath.getCurrentPathIndex() + 1);
        }

        int k = MathHelper.ceil(this.entity.width);
        int l = MathHelper.ceil(this.entity.height);

        for (int j1 = i - 1; j1 >= this.currentPath.getCurrentPathIndex(); --j1) {
            if (this.isDirectPathBetweenPoints(vec3d, this.currentPath.getVectorFromIndex(this.entity, j1), k, l, k)) {
                this.currentPath.setCurrentPathIndex(j1);
                break;
            }
        }
        this.checkForStuck(vec3d);
    }


    protected final void checkForStuck(Vec3d pos) {
        if (this.totalTicks - this.ticksAtLastPos > 100) {
            if (pos.squareDistanceTo(lastPosX, lastPosY, lastPosZ) < 2.25D) {
                this.clearPath();
            }
            this.ticksAtLastPos = this.totalTicks;
            lastPosX = pos.x;
            lastPosY = pos.y;
            lastPosZ = pos.z;
        }


        if (this.currentPath != null && !this.currentPath.isFinished()) {
            PathPoint point = this.currentPath.getCurrentPoint();
            Vec3d vec = this.timeoutCachedNode;

            if (vec.x == point.x && vec.y == point.y && vec.z == point.z) {
                this.timeoutTimer += System.currentTimeMillis() - this.lastTimeoutCheck;
            } else {
                this.timeoutCachedNode = new Vec3d(point.x, point.y, point.z);
                double d0 = pos.distanceTo(this.timeoutCachedNode);
                this.timeoutLimit = this.entity.getAIMoveSpeed() > 0.0F ? d0 / (double)this.entity.getAIMoveSpeed() * 1000.0D : 0.0D;
            }

            if (this.timeoutLimit > 0.0D && (double)this.timeoutTimer > this.timeoutLimit * 3.0D) {
                this.timeoutCachedNode = Vec3d.ZERO;
                this.timeoutTimer = 0L;
                this.timeoutLimit = 0.0D;
                this.clearPath();
            }
            this.lastTimeoutCheck = System.currentTimeMillis();
        }
    }


    protected final void checkForStuck(double vX, double vY, double vZ) {
        if (this.totalTicks - this.ticksAtLastPos > 100) {
            if (MathUtls.squareDistanceBetween(lastPosX, lastPosY, lastPosZ, vX, vY, vZ) < 2.25D) {
                this.clearPath();
            }
            this.ticksAtLastPos = this.totalTicks;
            lastPosX = vX;
            lastPosY = vY;
            lastPosZ = vZ;
        }

        if (this.currentPath != null && !this.currentPath.isFinished()) {
            PathPoint point = this.currentPath.getCurrentPoint();
            Vec3d vec = this.timeoutCachedNode;

            if (vec.x == point.x && vec.y == point.y && vec.z == point.z) {
                this.timeoutTimer += System.currentTimeMillis() - this.lastTimeoutCheck;
            } else {
                this.timeoutCachedNode = new Vec3d(point.x, point.y, point.z);
                double d0 = Math.sqrt(this.timeoutCachedNode.squareDistanceTo(vX, vY, vZ));
                this.timeoutLimit = this.entity.getAIMoveSpeed() > 0.0F ? d0 / (double)this.entity.getAIMoveSpeed() * 1000.0D : 0.0D;
            }

            if (this.timeoutLimit > 0.0D && (double)this.timeoutTimer > this.timeoutLimit * 3.0D) {
                this.timeoutCachedNode = Vec3d.ZERO;
                this.timeoutTimer = 0L;
                this.timeoutLimit = 0.0D;
                this.clearPath();
            }
            this.lastTimeoutCheck = System.currentTimeMillis();
        }
    }


    public boolean noPath() {
        return this.currentPath == null || this.currentPath.isFinished();
    }


    public void clearPath() {
        this.currentPath = null;
    }


    protected abstract Vec3d getEntityPosition();


    protected abstract void updateEntityPos();


    protected abstract boolean canNavigate();


    protected boolean isInLiquid() {
        return this.entity.isInWater() || this.entity.isInLava();
    }


    protected void removeSunnyPath() {
        if (this.currentPath != null) {
            Path cur = this.currentPath;
            IBlockEAccess access = world.access();
            for (int i = 0; i < cur.getCurrentPathLength(); ++i) {
                PathPoint curPoint = cur.getPathPointFromIndex(i);

                if (access.getBlockState(curPoint.x, curPoint.y, curPoint.z).getBlock() == Blocks.CAULDRON) {
                    cur.setPoint(i, curPoint.cloneMove(curPoint.x, curPoint.y + 1, curPoint.z));

                    PathPoint point2 = i + 1 < cur.getCurrentPathLength() ? cur.getPathPointFromIndex(i + 1) : null;

                    if (point2 != null && curPoint.y >= point2.y)
                        cur.setPoint(i + 1, point2.cloneMove(point2.x, curPoint.y + 1, point2.z));
                }
            }
        }
    }


    protected abstract boolean isDirectPathBetweenPoints(Vec3d posVec31, Vec3d posVec32, int sizeX, int sizeY, int sizeZ);


    public boolean canEntityStandOnPos(BlockPos pos) {
        return this.world.getBlockState(pos.down()).isFullBlock();
    }


    public NodeProcessor getNodeProcessor() {
        return this.nodeProcessor;
    }
}