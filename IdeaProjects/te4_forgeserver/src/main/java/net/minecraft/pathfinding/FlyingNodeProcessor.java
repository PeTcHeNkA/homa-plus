package net.minecraft.pathfinding;

import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldServer;
import plus.fserver.api.IBlockEAccess;
import plus.fserver.world.NavigationCache;
import plus.fserver.world.PntMatrix;
import plus.tson.utl.Te4HashSet;


public class FlyingNodeProcessor extends WalkNodeProcessor {
    public void init(IBlockAccess access, EntityLiving mob) {
        super.init(access, mob);
        this.avoidsWater = mob.getPathPriority(PathNodeType.WATER);
    }


    @Override
    protected PntMatrix getBy(EntityLiving mob) {
        return ((WorldServer)blockaccess.getWorld()).navCache.getBy(NavigationCache.FLY_ID);
    }


    @Override
    public void postProcess() {
        this.entity.setPathPriority(PathNodeType.WATER, this.avoidsWater);
        super.postProcess();
    }


    @Override
    public PathPoint getStart() {
        int i;

        if (this.getCanSwim() && this.entity.isInWater()) {
            i = (int)this.entity.getEntityBoundingBox().minY;
            pooledPos.setPos(MathHelper.floor(this.entity.posX), i, MathHelper.floor(this.entity.posZ));

            for (Block block = this.blockaccess.getBlockState(pooledPos).getBlock(); block == Blocks.FLOWING_WATER || block == Blocks.WATER; block = this.blockaccess.getBlockState(pooledPos).getBlock()) {
                ++i;
                pooledPos.setPos(MathHelper.floor(this.entity.posX), i, MathHelper.floor(this.entity.posZ));
            }
        } else {
            i = MathHelper.floor(this.entity.getEntityBoundingBox().minY + 0.5D);
        }

        pooledPos.setPosB(this.entity);
        PntMatrix.Chunked chunked = matrix.getChunked(pooledPos.getX(), pooledPos.getZ());

        PathNodeType type0 = chunked.getCur(pooledPos.getX(), i, pooledPos.getZ());
        if(type0==null) {
            type0 = this.getPathNodeType(this.entity, pooledPos.getX(), i, pooledPos.getZ());
            chunked.setCur(pooledPos.getX(), i, pooledPos.getZ(), type0);
        }

        if (this.entity.getPathPriority(type0) < 0.0F) {
            Set<BlockPos> set = new Te4HashSet<>();
            set.add(new BlockPos(this.entity.getEntityBoundingBox().minX, i, this.entity.getEntityBoundingBox().minZ));
            set.add(new BlockPos(this.entity.getEntityBoundingBox().minX, i, this.entity.getEntityBoundingBox().maxZ));
            set.add(new BlockPos(this.entity.getEntityBoundingBox().maxX, i, this.entity.getEntityBoundingBox().minZ));
            set.add(new BlockPos(this.entity.getEntityBoundingBox().maxX, i, this.entity.getEntityBoundingBox().maxZ));

            PathNodeType type;
            for (BlockPos blockpos : set) {
                type = chunked.getType(blockpos.getX(), blockpos.getY(), blockpos.getZ());
                if(type==null) {
                    type = this.getPathNodeType(this.entity, blockpos);
                    chunked.setType(blockpos.getX(), blockpos.getY(), blockpos.getZ(), type);
                }
                if (this.entity.getPathPriority(type) >= 0.0F) {
                    return super.openPoint(blockpos.getX(), blockpos.getY(), blockpos.getZ());
                }
            }
        }
        return super.openPoint(pooledPos.getX(), i, pooledPos.getZ());
    }


    @Override
    public PathPoint getPathPointToCoords(double x, double y, double z) {
        return super.openPoint(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
    }


    @Override
    public int findPathOptions(PathPoint[] pathOptions, PathPoint cur, PathPoint target, float maxDistance) {
        int i = 0;
        PathPoint pathpoint = this.openPoint(cur.x, cur.y, cur.z + 1);
        PathPoint pathpoint1 = this.openPoint(cur.x - 1, cur.y, cur.z);
        PathPoint pathpoint2 = this.openPoint(cur.x + 1, cur.y, cur.z);
        PathPoint pathpoint3 = this.openPoint(cur.x, cur.y, cur.z - 1);
        PathPoint pathpoint4 = this.openPoint(cur.x, cur.y + 1, cur.z);
        PathPoint pathpoint5 = this.openPoint(cur.x, cur.y - 1, cur.z);

        float maxDistanceSq = maxDistance * maxDistance;

        if (pathpoint  != null && !pathpoint .visited && pathpoint .distanceToSquared(target) < maxDistanceSq) {
            pathOptions[i++] = pathpoint;
        }

        if (pathpoint1 != null && !pathpoint1.visited && pathpoint1.distanceToSquared(target) < maxDistanceSq) {
            pathOptions[i++] = pathpoint1;
        }

        if (pathpoint2 != null && !pathpoint2.visited && pathpoint2.distanceToSquared(target) < maxDistanceSq) {
            pathOptions[i++] = pathpoint2;
        }

        if (pathpoint3 != null && !pathpoint3.visited && pathpoint3.distanceToSquared(target) < maxDistanceSq) {
            pathOptions[i++] = pathpoint3;
        }

        if (pathpoint4 != null && !pathpoint4.visited && pathpoint4.distanceToSquared(target) < maxDistanceSq) {
            pathOptions[i++] = pathpoint4;
        }

        if (pathpoint5 != null && !pathpoint5.visited && pathpoint5.distanceToSquared(target) < maxDistanceSq) {
            pathOptions[i++] = pathpoint5;
        }

        boolean flag = pathpoint3 == null || pathpoint3.costMalus != 0.0F;
        boolean flag1 = pathpoint == null || pathpoint.costMalus != 0.0F;
        boolean flag2 = pathpoint2 == null || pathpoint2.costMalus != 0.0F;
        boolean flag3 = pathpoint1 == null || pathpoint1.costMalus != 0.0F;
        boolean flag4 = pathpoint4 == null || pathpoint4.costMalus != 0.0F;
        boolean flag5 = pathpoint5 == null || pathpoint5.costMalus != 0.0F;

        if (flag && flag3) {
            PathPoint pathpoint6 = this.openPoint(cur.x - 1, cur.y, cur.z - 1);

            if (pathpoint6 != null && !pathpoint6.visited && pathpoint6.distanceToSquared(target) < maxDistanceSq) {
                pathOptions[i++] = pathpoint6;
            }
        }
        if (flag && flag2) {
            PathPoint pathpoint7 = this.openPoint(cur.x + 1, cur.y, cur.z - 1);

            if (pathpoint7 != null && !pathpoint7.visited && pathpoint7.distanceToSquared(target) < maxDistanceSq) {
                pathOptions[i++] = pathpoint7;
            }
        }
        if (flag1 && flag3) {
            PathPoint pathpoint8 = this.openPoint(cur.x - 1, cur.y, cur.z + 1);

            if (pathpoint8 != null && !pathpoint8.visited && pathpoint8.distanceToSquared(target) < maxDistanceSq) {
                pathOptions[i++] = pathpoint8;
            }
        }
        if (flag1 && flag2) {
            PathPoint pathpoint9 = this.openPoint(cur.x + 1, cur.y, cur.z + 1);

            if (pathpoint9 != null && !pathpoint9.visited && pathpoint9.distanceToSquared(target) < maxDistanceSq) {
                pathOptions[i++] = pathpoint9;
            }
        }
        if (flag && flag4) {
            PathPoint pathpoint10 = this.openPoint(cur.x, cur.y + 1, cur.z - 1);

            if (pathpoint10 != null && !pathpoint10.visited && pathpoint10.distanceToSquared(target) < maxDistanceSq) {
                pathOptions[i++] = pathpoint10;
            }
        }
        if (flag1 && flag4) {
            PathPoint pathpoint11 = this.openPoint(cur.x, cur.y + 1, cur.z + 1);

            if (pathpoint11 != null && !pathpoint11.visited && pathpoint11.distanceToSquared(target) < maxDistanceSq) {
                pathOptions[i++] = pathpoint11;
            }
        }
        if (flag2 && flag4) {
            PathPoint pathpoint12 = this.openPoint(cur.x + 1, cur.y + 1, cur.z);

            if (pathpoint12 != null && !pathpoint12.visited && pathpoint12.distanceToSquared(target) < maxDistanceSq) {
                pathOptions[i++] = pathpoint12;
            }
        }
        if (flag3 && flag4) {
            PathPoint pathpoint13 = this.openPoint(cur.x - 1, cur.y + 1, cur.z);

            if (pathpoint13 != null && !pathpoint13.visited && pathpoint13.distanceToSquared(target) < maxDistanceSq) {
                pathOptions[i++] = pathpoint13;
            }
        }
        if (flag && flag5) {
            PathPoint pathpoint14 = this.openPoint(cur.x, cur.y - 1, cur.z - 1);

            if (pathpoint14 != null && !pathpoint14.visited && pathpoint14.distanceToSquared(target) < maxDistanceSq) {
                pathOptions[i++] = pathpoint14;
            }
        }
        if (flag1 && flag5) {
            PathPoint pathpoint15 = this.openPoint(cur.x, cur.y - 1, cur.z + 1);

            if (pathpoint15 != null && !pathpoint15.visited && pathpoint15.distanceToSquared(target) < maxDistanceSq) {
                pathOptions[i++] = pathpoint15;
            }
        }
        if (flag2 && flag5) {
            PathPoint pathpoint16 = this.openPoint(cur.x + 1, cur.y - 1, cur.z);

            if (pathpoint16 != null && !pathpoint16.visited && pathpoint16.distanceToSquared(target) < maxDistanceSq) {
                pathOptions[i++] = pathpoint16;
            }
        }
        if (flag3 && flag5) {
            PathPoint pathpoint17 = this.openPoint(cur.x - 1, cur.y - 1, cur.z);

            if (pathpoint17 != null && !pathpoint17.visited && pathpoint17.distanceToSquared(target) < maxDistanceSq) {
                pathOptions[i++] = pathpoint17;
            }
        }
        return i;
    }


    @Nullable
    protected PathPoint openPoint(int x, int y, int z) {
        PathPoint pathpoint = null;
        PathNodeType pathnodetype = this.getPathNodeType(this.entity, x, y, z);
        float f = this.entity.getPathPriority(pathnodetype);

        if (f >= 0.0F) {
            pathpoint = super.openPoint(x, y, z);
            pathpoint.nodeType = pathnodetype;
            pathpoint.costMalus = Math.max(pathpoint.costMalus, f);

            if (pathnodetype == PathNodeType.WALKABLE) {
                ++pathpoint.costMalus;
            }
        }
        return pathnodetype != PathNodeType.OPEN && pathnodetype != PathNodeType.WALKABLE ? pathpoint : pathpoint;
    }


    @Override
    public PathNodeType getPathNodeType(IBlockEAccess blockaccessIn, int x, int y, int z, EntityLiving entity, int xSize, int ySize, int zSize, boolean canBreakDoorsIn, boolean canEnterDoorsIn) {
        PathNodeType.Set enumset;
        if(matrix != null){
            enumset = matrix.localSet();
        } else {
            enumset = new PathNodeType.Set();
        }
        //EnumSet<PathNodeType> enumset = EnumSet.noneOf(PathNodeType.class);

        PathNodeType pathnodetype = PathNodeType.BLOCKED;
        BlockPos blockpos = new BlockPos(entity);
        this.currentEntity = entity;
        pathnodetype = this.getPathNodeType(blockaccessIn, x, y, z, xSize, ySize, zSize, canBreakDoorsIn, canEnterDoorsIn, enumset, pathnodetype, blockpos);
        this.currentEntity = null;

        if (enumset.contains(PathNodeType.FENCE)) {
            return PathNodeType.FENCE;
        } else {
            PathNodeType nodeType = PathNodeType.BLOCKED;
            float nodePriority = -1;
            for (PathNodeType type : enumset) {
                float priority = entity.getPathPriority(type);
                if (priority < 0.0F) {
                    return type;
                }

                if (priority >= nodePriority) {
                    nodeType = type;
                    nodePriority = priority;
                }
            }

            if (pathnodetype == PathNodeType.OPEN && nodePriority == 0.0F) {
                return PathNodeType.OPEN;
            }
            else {
                return nodeType;
            }
        }
    }


    @Override
    public PathNodeType getPathNodeType(IBlockEAccess access, int x, int y, int z) {
        IBlockState state = access.getBlockState(x, y, z);
        PathNodeType pathnodetype = this.getPathNodeTypeRaw(access, state);

        if (pathnodetype == PathNodeType.OPEN && y >= 1) {
            state = access.getBlockState(x, y - 1, z);
            PathNodeType type = this.getPathNodeTypeRaw(access, state);

            if (type != PathNodeType.DAMAGE_FIRE && state.getBlock() != Blocks.MAGMA && type != PathNodeType.LAVA) {
                if (type == PathNodeType.DAMAGE_CACTUS) {
                    pathnodetype = PathNodeType.DAMAGE_CACTUS;
                }
                else if (type == PathNodeType.DAMAGE_OTHER) pathnodetype = PathNodeType.DAMAGE_OTHER;
                else {
                    pathnodetype = type != PathNodeType.WALKABLE && type != PathNodeType.OPEN && type != PathNodeType.WATER ? PathNodeType.WALKABLE : PathNodeType.OPEN;
                }
            } else {
                pathnodetype = PathNodeType.DAMAGE_FIRE;
            }
        }
        return checkNeighborBlocks(access, x, y, z, pathnodetype);
    }


    private PathNodeType getPathNodeType(EntityLiving entity, BlockPos pos) {
        return this.getPathNodeType(entity, pos.getX(), pos.getY(), pos.getZ());
    }


    private PathNodeType getPathNodeType(EntityLiving entity, int x, int y, int z) {
        return this.getPathNodeType(this.blockaccess, x, y, z, entity, this.entitySizeX, this.entitySizeY, this.entitySizeZ, this.getCanOpenDoors(), this.getCanEnterDoors());
    }
}