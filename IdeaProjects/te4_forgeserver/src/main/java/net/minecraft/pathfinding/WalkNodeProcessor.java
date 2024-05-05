package net.minecraft.pathfinding;

import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldServer;
import plus.fserver.world.PntMatrix;
import plus.tson.utl.Te4HashSet;


public class WalkNodeProcessor extends NodeProcessor {
    protected float avoidsWater;
    protected EntityLiving currentEntity;
    protected PntMatrix matrix;

    public void init(IBlockAccess access, EntityLiving mob) {
        super.init(access, mob);
        matrix = getBy(mob);
        this.avoidsWater = mob.getPathPriority(PathNodeType.WATER);
    }


    protected PntMatrix getBy(EntityLiving mob){
        return ((WorldServer)blockaccess.getWorld()).navCache.getBy(mob);
    }


    @Override
    public void postProcess() {
        this.entity.setPathPriority(PathNodeType.WATER, this.avoidsWater);
        super.postProcess();
    }


    @Override
    public PathPoint getStart() {
        int yFactor;
        if (this.getCanSwim() && this.entity.isInWater()) {
            yFactor = (int)this.entity.getEntityBoundingBox().minY;

            for (Block block = this.blockaccess.getBlockState(pooledPos).getBlock(); block == Blocks.FLOWING_WATER || block == Blocks.WATER; block = this.blockaccess.getBlockState(pooledPos).getBlock()) {
                ++yFactor;
                pooledPos.setPos(MathHelper.floor(this.entity.posX), yFactor, MathHelper.floor(this.entity.posZ));
            }
        } else if (this.entity.onGround) {
            yFactor = MathHelper.floor(this.entity.getEntityBoundingBox().minY + 0.5D);
        } else {
            IBlockState state;

            for (pooledPos.setPos(this.entity);
                 ((state = this.blockaccess.getBlockState(pooledPos)).getMaterial() ==
                         Material.AIR || state.getBlock()
                                 .isPassable(this.blockaccess, pooledPos))
                         && pooledPos.getY() > 0; pooledPos.setY(pooledPos.getY()-1)) {;}

            yFactor = pooledPos.getY()+1;
        }

        BlockPos blockpos2 = new BlockPos(this.entity);

        PathNodeType type;
        PntMatrix.Chunked chunked = matrix.getChunked(blockpos2.getX(), blockpos2.getZ());
        type = chunked.getCur(blockpos2.getX(), yFactor, blockpos2.getZ());
        if(type==null) {
            type = this.getPathNodeType(this.entity, blockpos2.getX(), yFactor, blockpos2.getZ());
            chunked.setCur(blockpos2.getX(), yFactor, blockpos2.getZ(), type);
        }

        if (this.entity.getPathPriority(type) < 0.0F) {
            Set<BlockPos> set = new Te4HashSet<>();
            set.add(new BlockPos(this.entity.getEntityBoundingBox().minX, yFactor, this.entity.getEntityBoundingBox().minZ));
            set.add(new BlockPos(this.entity.getEntityBoundingBox().minX, yFactor, this.entity.getEntityBoundingBox().maxZ));
            set.add(new BlockPos(this.entity.getEntityBoundingBox().maxX, yFactor, this.entity.getEntityBoundingBox().minZ));
            set.add(new BlockPos(this.entity.getEntityBoundingBox().maxX, yFactor, this.entity.getEntityBoundingBox().maxZ));

            for (BlockPos pos : set) {
                type = chunked.getType(pos.getX(), pos.getY(), pos.getZ());
                if(type==null) {
                    type = this.getPathNodeType(this.entity, pos);
                    chunked.setType(pos.getX(), pos.getY(), pos.getZ(), type);
                }
                if (this.entity.getPathPriority(type) >= 0.0F) {
                    return this.openPoint(pos.getX(), pos.getY(), pos.getZ());
                }
            }
        }
        return this.openPoint(blockpos2.getX(), yFactor, blockpos2.getZ());
    }


    @Override
    public PathPoint getPathPointToCoords(double x, double y, double z) {
        return this.openPoint(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
    }


    @Override
    public int findPathOptions(PathPoint[] pathOptions, PathPoint cur, PathPoint target, float maxDistance) {
        int i = 0;
        int stepH = 0;

        PntMatrix.Chunked chunked = matrix.getChunked(cur.x, cur.z);

        PathNodeType type = chunked.getCur(cur.x, cur.y + 1, cur.z);
        if(type==null) {
            type = this.getPathNodeType(this.entity, cur.x, cur.y + 1, cur.z);
            chunked.setCur(cur.x, cur.y + 1, cur.z, type);
        }

        if (this.entity.getPathPriority(type) >= 0.0F) {
            stepH = MathHelper.floor(Math.max(1.0F, this.entity.stepHeight));
        }

        pooledPos.setPos(cur.x, cur.y-1, cur.z);

        double curY = (double)cur.y - (1.0D - this.blockaccess.getBlockState(pooledPos).getBoundingBox(this.blockaccess, pooledPos).maxY);
        PathPoint pathpoint =  this.getSafePoint(chunked, cur.x, cur.y, cur.z + 1, stepH, curY, EnumFacing.SOUTH);
        PathPoint pathpoint1 = this.getSafePoint(chunked, cur.x - 1, cur.y, cur.z, stepH, curY, EnumFacing.WEST);
        PathPoint pathpoint2 = this.getSafePoint(chunked, cur.x + 1, cur.y, cur.z, stepH, curY, EnumFacing.EAST);
        PathPoint pathpoint3 = this.getSafePoint(chunked, cur.x, cur.y, cur.z - 1, stepH, curY, EnumFacing.NORTH);

        if (pathpoint != null && !pathpoint.visited && pathpoint.distanceTo(target) < maxDistance) {
            pathOptions[i++] = pathpoint;
        }

        if (pathpoint1 != null && !pathpoint1.visited && pathpoint1.distanceTo(target) < maxDistance) {
            pathOptions[i++] = pathpoint1;
        }

        if (pathpoint2 != null && !pathpoint2.visited && pathpoint2.distanceTo(target) < maxDistance) {
            pathOptions[i++] = pathpoint2;
        }

        if (pathpoint3 != null && !pathpoint3.visited && pathpoint3.distanceTo(target) < maxDistance) {
            pathOptions[i++] = pathpoint3;
        }

        boolean flag  = pathpoint3 == null || pathpoint3.nodeType == PathNodeType.OPEN || pathpoint3.costMalus != 0.0F;
        boolean flag1 = pathpoint == null  || pathpoint.nodeType == PathNodeType.OPEN  || pathpoint.costMalus  != 0.0F;
        boolean flag2 = pathpoint2 == null || pathpoint2.nodeType == PathNodeType.OPEN || pathpoint2.costMalus != 0.0F;
        boolean flag3 = pathpoint1 == null || pathpoint1.nodeType == PathNodeType.OPEN || pathpoint1.costMalus != 0.0F;

        if (flag && flag3) {
            PathPoint pathpoint4 = this.getSafePoint(chunked, cur.x - 1, cur.y, cur.z - 1, stepH, curY, EnumFacing.NORTH);

            if (pathpoint4 != null && !pathpoint4.visited && pathpoint4.distanceTo(target) < maxDistance) {
                pathOptions[i++] = pathpoint4;
            }
        }

        if (flag && flag2) {
            PathPoint pathpoint5 = this.getSafePoint(chunked, cur.x + 1, cur.y, cur.z - 1, stepH, curY, EnumFacing.NORTH);

            if (pathpoint5 != null && !pathpoint5.visited && pathpoint5.distanceTo(target) < maxDistance) {
                pathOptions[i++] = pathpoint5;
            }
        }

        if (flag1 && flag3) {
            PathPoint pathpoint6 = this.getSafePoint(chunked, cur.x - 1, cur.y, cur.z + 1, stepH, curY, EnumFacing.SOUTH);

            if (pathpoint6 != null && !pathpoint6.visited && pathpoint6.distanceTo(target) < maxDistance) {
                pathOptions[i++] = pathpoint6;
            }
        }

        if (flag1 && flag2) {
            PathPoint pathpoint7 = this.getSafePoint(chunked, cur.x + 1, cur.y, cur.z + 1, stepH, curY, EnumFacing.SOUTH);

            if (pathpoint7 != null && !pathpoint7.visited && pathpoint7.distanceTo(target) < maxDistance) {
                pathOptions[i++] = pathpoint7;
            }
        }

        return i;
    }


    @Nullable
    private PathPoint getSafePoint(PntMatrix.Chunked chunked, final int x, int y, final int z, final int stepH, final double curY, final EnumFacing facing) {
        PathPoint pathpoint = null;

        double num1 = (double)y - (1.0D -
                this.blockaccess.getBlockState(pooledPos.setPos(x, y-1, z)).getBoundingBox(
                        this.blockaccess, pooledPos).maxY
        );

        if (num1 - curY > 1.125D) {
            return null;
        } else {
            PathNodeType type;
            chunked = chunked.get(x, z);

            type = chunked.getCur(x, y, z);
            if(type==null) {
                type = this.getPathNodeType(this.entity, x, y, z);
                chunked.setCur(x, y, z, type);
            }

            float priority = this.entity.getPathPriority(type);

            if (priority >= 0.0F) {
                //todo
                pathpoint = this.openPoint(x, y, z);
                pathpoint.nodeType = type;
                pathpoint.costMalus = Math.max(pathpoint.costMalus, priority);
            }

            if (type != PathNodeType.WALKABLE) {
                num1 = (double)this.entity.width / 2.0D;

                if (pathpoint == null && stepH > 0 && type != PathNodeType.FENCE && type != PathNodeType.TRAPDOOR) {
                    pathpoint = this.getSafePoint(chunked, x, y + 1, z, stepH - 1, curY, facing);

                    if (pathpoint != null && (pathpoint.nodeType == PathNodeType.OPEN || pathpoint.nodeType == PathNodeType.WALKABLE) && this.entity.width < 1.0F) {

                        double d2 = (double) (x - facing.getXOffset()) + 0.5D;
                        double d3 = (double) (z - facing.getZOffset()) + 0.5D;

                        AxisAlignedBB box = this.entity.world.commonWorldEntityAABB;
                        box.init(
                                d2 - num1, (double) y + 0.001D,
                                d3 - num1, d2 + num1,
                                (float) y + this.entity.height, d3 + num1
                        );

                        box.expandTo(0.0D,
                                this.blockaccess.getBlockState(pooledPos.setPos(x, y, z))
                                        .getBoundingBox(this.blockaccess, pooledPos).maxY - 0.002D,
                                0.0D, box);

                        if (this.entity.world.collidesWithAnyBlock(box)) {
                            pathpoint = null;
                        }
                    }
                }

                if (type == PathNodeType.OPEN) {
                    AxisAlignedBB box = this.entity.world.commonWorldEntityAABB;
                    box.init(
                            (double) x - num1 + 0.5D, (double) y + 0.001D,
                            (double) z - num1 + 0.5D, (double) x + num1 + 0.5D,
                            (float) y + this.entity.height, (double) z + num1 + 0.5D
                    );

                    if (this.entity.world.collidesWithAnyBlock(box)) {
                        return null;
                    }

                    if (this.entity.width >= 1.0F) {
                        PathNodeType type2 = chunked.getCur(x, y - 1, z);
                        if(type2==null) {
                            type2 = this.getPathNodeType(this.entity, x, y, z);
                            chunked.setCur(x, y, z, type2);
                        }
                        //type2 = this.getPathNodeType(this.entity, x, y - 1, z);

                        if (type2 == PathNodeType.BLOCKED) {
                            pathpoint = this.openPoint(x, y, z);
                            pathpoint.nodeType = PathNodeType.WALKABLE;
                            pathpoint.costMalus = Math.max(pathpoint.costMalus, priority);
                            return pathpoint;
                        }
                    }

                    int i = 0;

                    while (y > 0 && type == PathNodeType.OPEN) {
                        --y;

                        if (i++ >= this.entity.getMaxFallHeight()) {
                            return null;
                        }

                        type = chunked.getType(x, y, z);
                        if(type==null){
                            type = this.getPathNodeType(this.entity, x, y, z);
                            chunked.setType(x, y, z, type);
                        }

                        //type = this.getPathNodeType(this.entity, x, y, z);

                        priority = this.entity.getPathPriority(type);

                        if (type != PathNodeType.OPEN && priority >= 0.0f) {
                            pathpoint = this.openPoint(x, y, z);
                            pathpoint.nodeType = type;
                            pathpoint.costMalus = Math.max(pathpoint.costMalus, priority);
                            break;
                        }

                        if (priority < 0.0F) {
                            return null;
                        }
                    }
                }

            }
            return pathpoint;
        }
    }


    @Override
    public PathNodeType getPathNodeType(final IBlockAccess access, int x, int y, int z, EntityLiving entity, int xSize, int ySize, int zSize, boolean canBreakDoorsIn, boolean canEnterDoorsIn) {
        PathNodeType.Set enumset = new PathNodeType.Set();
        //EnumSet<PathNodeType> enumset = EnumSet.noneOf(PathNodeType.class);
        PathNodeType pathnodetype = PathNodeType.BLOCKED;
        this.currentEntity = entity;
        pathnodetype = this.getPathNodeType(access, x, y, z, xSize, ySize, zSize, canBreakDoorsIn, canEnterDoorsIn, enumset, pathnodetype, new BlockPos(entity));
        this.currentEntity = null;

        if (enumset.contains(PathNodeType.FENCE)) {
            return PathNodeType.FENCE;
        } else {
            PathNodeType nodeType = PathNodeType.BLOCKED;

            for (PathNodeType nodeType1 : enumset) {
                if (entity.getPathPriority(nodeType1) < 0.0F) return nodeType1;

                if (entity.getPathPriority(nodeType1) >= entity.getPathPriority(nodeType))
                    nodeType = nodeType1;
            }

            if (pathnodetype == PathNodeType.OPEN && entity.getPathPriority(nodeType) == 0.0F) {
                return PathNodeType.OPEN;
            } else {
                return nodeType;
            }
        }
    }


    public PathNodeType getPathNodeType(final IBlockAccess access, int x, int y, int z, int xSize, int ySize, int zSize, boolean canOpenDoorsIn, boolean canEnterDoorsIn, PathNodeType.Set types, PathNodeType type, BlockPos pos) {
        PathNodeType nodeType;
        int stepX = xSize>1?2:1;
        int stepZ = zSize>1?2:1;
        for (int dY = 0; dY < ySize; ++dY) {
            for (int dX = 0; dX < xSize; dX+=stepX) {
                for (int dZ = 0; dZ < zSize; dZ+=stepZ) {

                    nodeType = this.getPathNodeType(access, dX + x, dY + y, dZ + z);

                    if (nodeType == PathNodeType.DOOR_WOOD_CLOSED && canOpenDoorsIn && canEnterDoorsIn) {
                        nodeType = PathNodeType.WALKABLE;
                    }

                    if (nodeType == PathNodeType.DOOR_OPEN && !canEnterDoorsIn) {
                        nodeType = PathNodeType.BLOCKED;
                    }

                    if (nodeType == PathNodeType.RAIL && !(access.getBlockState(pos).getBlock() instanceof BlockRailBase) && !(access.getBlockState(pos.down()).getBlock() instanceof BlockRailBase)) {
                        nodeType = PathNodeType.FENCE;
                    }

                    if (dX == 0 && dY == 0 && dZ == 0) {
                        type = nodeType;
                    }
                    types.add(nodeType);
                }
            }
        }
        return type;
    }


    private PathNodeType getPathNodeType(EntityLiving entity, BlockPos pos) {
        return this.getPathNodeType(entity, pos.getX(), pos.getY(), pos.getZ());
    }


    private PathNodeType getPathNodeType(EntityLiving entity, int x, int y, int z) {
        return this.getPathNodeType(this.blockaccess, x, y, z, entity, this.entitySizeX, this.entitySizeY, this.entitySizeZ, this.getCanOpenDoors(), this.getCanEnterDoors());
    }


    @Override
    public PathNodeType getPathNodeType(final IBlockAccess access, int x, int y, int z) {
        IBlockState state = access.getBlockState(pooledPos.setPos(x, y, z));
        PathNodeType raw = this.getPathNodeTypeRaw(access, state);
        if (raw == PathNodeType.OPEN && y >= 1) {
            state = access.getBlockState(pooledPos.setPos(x, y - 1, z));
            PathNodeType raw2 = this.getPathNodeTypeRaw(access, state);
            raw = raw2 != PathNodeType.WALKABLE && raw2 != PathNodeType.OPEN && raw2 != PathNodeType.WATER && raw2 != PathNodeType.LAVA ? PathNodeType.WALKABLE : PathNodeType.OPEN;

            if (raw2 == PathNodeType.DAMAGE_FIRE || state.getBlock() == Blocks.MAGMA) {
                raw = PathNodeType.DAMAGE_FIRE;
            }

            if (raw2 == PathNodeType.DAMAGE_CACTUS) {
                raw = PathNodeType.DAMAGE_CACTUS;
            }
            if (raw2 == PathNodeType.DAMAGE_OTHER) raw = PathNodeType.DAMAGE_OTHER;
        }
        return this.checkNeighborBlocks(access, x, y, z, raw);
    }


    public PathNodeType checkNeighborBlocks(IBlockAccess access, int x, int y, int z, PathNodeType nodeType) {
        if (nodeType == PathNodeType.WALKABLE) {
            IBlockState state;
            Block block;
            PathNodeType type;
            for (int i = -1; i <= 1; ++i) {
                for (int j = -1; j <= 1; ++j) {
                    if (i != 0 || j != 0) {
                        state = access.getBlockState(pooledPos.setPos(i + x, y, j + z));
                        type = (block = state.getBlock()).getAiPathNodeType(state, access, pooledPos, this.currentEntity);
                        if (block == Blocks.CACTUS || type == PathNodeType.DAMAGE_CACTUS) {
                            nodeType = PathNodeType.DANGER_CACTUS;
                        }
                        else if (block == Blocks.FIRE || type == PathNodeType.DAMAGE_FIRE) {
                            nodeType = PathNodeType.DANGER_FIRE;
                        }
                        else if (type == PathNodeType.DAMAGE_OTHER) nodeType = PathNodeType.DANGER_OTHER;
                    }
                }
            }
        }
        return nodeType;
    }


    protected PathNodeType getPathNodeTypeRaw(IBlockAccess access, IBlockState state) {
        Block block = state.getBlock();
        PathNodeType type = block.getAiPathNodeType(state, access, pooledPos, this.currentEntity);
        if (type != null) return type;
        Material material = state.getMaterial();

        if (material == Material.AIR) {
            return PathNodeType.OPEN;
        } else if (block != Blocks.TRAPDOOR && block != Blocks.IRON_TRAPDOOR && block != Blocks.WATERLILY) {
            if (block == Blocks.FIRE) {
                return PathNodeType.DAMAGE_FIRE;
            } else if (block == Blocks.CACTUS) {
                return PathNodeType.DAMAGE_CACTUS;
            } else if (block instanceof BlockDoor) {
                boolean isClose = !state.getValue(BlockDoor.OPEN);

                if (material == Material.WOOD && isClose) {
                    return PathNodeType.DOOR_WOOD_CLOSED;

                } else if (material == Material.IRON && isClose) {
                    return PathNodeType.DOOR_IRON_CLOSED;

                } else if (!isClose) {
                    return PathNodeType.DOOR_OPEN;

                }
                return PathNodeType.TRAPDOOR;
            } else if (block instanceof BlockRailBase) {
                return PathNodeType.RAIL;
            } else if (!(block instanceof BlockFence) && !(block instanceof BlockWall)
                    && (!(block instanceof BlockFenceGate) || state.getValue(BlockFenceGate.OPEN))) {
                if (material == Material.WATER) {
                    return PathNodeType.WATER;
                } else if (material == Material.LAVA) {
                    return PathNodeType.LAVA;
                } else {
                    return block.isPassable(access, pooledPos) ? PathNodeType.OPEN : PathNodeType.BLOCKED;
                }
            } else {
                return PathNodeType.FENCE;
            }
        } else {
            return PathNodeType.TRAPDOOR;
        }
    }
}