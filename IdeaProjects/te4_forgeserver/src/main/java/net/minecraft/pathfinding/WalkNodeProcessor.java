package net.minecraft.pathfinding;

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
import net.minecraft.world.WorldServer;
import plus.fserver.entity.NavChunkLSECache;
import plus.fserver.world.PntMatrix;
import plus.tson.utl.Te4HashSet;


public class WalkNodeProcessor extends NodeProcessor {
    protected float avoidsWater;
    protected PntMatrix matrix;

    public WalkNodeProcessor(){}


    public void init(EntityLiving mob) {
        super.init(mob);
        matrix = getBy(mob);
        this.avoidsWater = mob.getPathPriority(PathNodeType.WATER);
    }


    protected PntMatrix getBy(EntityLiving mob){
        return ((WorldServer)mob.world).navCache.getBy(mob);
    }


    @Override
    public void postProcess() {
        this.entity.setPathPriority(PathNodeType.WATER, this.avoidsWater);
        super.postProcess();
    }


    @Override
    public PathPoint getStart(NavChunkLSECache access) {
        int yFactor;
        EntityLiving entity = this.entity;
        BlockPos.MutableBlockPos pooledPos = access.pooledPos;

        if (this.getCanSwim() && entity.isInWater()) {
            yFactor = (int)entity.getEntityBoundingBox().minY;

            pooledPos.setPosB(MathHelper.floor(entity.posX), yFactor, MathHelper.floor(entity.posZ));

            for (Block block = access.getBlockState(pooledPos).getBlock(); block == Blocks.FLOWING_WATER || block == Blocks.WATER; block = access.getBlockState(pooledPos).getBlock()) {
                ++yFactor;
                pooledPos.setY(yFactor);
            }
        } else if (entity.onGround) {
            yFactor = MathHelper.floor(entity.getEntityBoundingBox().minY + 0.5D);
        } else {
            IBlockState state;

            for (pooledPos.setPosB(entity);
                 ((state = access.getBlockState(pooledPos)).getMaterial() ==
                         Material.AIR || state.getBlock()
                                 .isPassable(access, pooledPos))
                         && pooledPos.getY() > 0; pooledPos.setY(pooledPos.getY()-1));

            yFactor = pooledPos.getY()+1;
        }

        BlockPos blockpos2 = new BlockPos(entity);

        PathNodeType type;
        PntMatrix.Chunked chunked = matrix.getChunked(blockpos2.getX(), blockpos2.getZ());
        type = chunked.getCur(blockpos2.getX(), yFactor, blockpos2.getZ());

        if(type == null) {
            type = this.getPathNodeType(access, blockpos2.getX(), yFactor, blockpos2.getZ(), entity, this.entitySizeX & 255, this.entitySizeY & 255, this.entitySizeZ & 255, this.getCanOpenDoors(), this.getCanEnterDoors());
            chunked.setCur(blockpos2.getX(), yFactor, blockpos2.getZ(), type);
        }

        if (entity.getPathPriority(type) < 0.0F) {
            Te4HashSet<BlockPos> set = new Te4HashSet<>(8, 1f);
            AxisAlignedBB box = entity.getEntityBoundingBox();
            BlockPos pos;
            set.add(pos = new BlockPos(box.minX, yFactor, box.minZ));

            if (needOpen(access, chunked, pos))
                return this.openPoint(access, pos.getX(), pos.getY(), pos.getZ());

            if(set.add(pos = new BlockPos(box.minX, yFactor, box.maxZ)))
                if (needOpen(access, chunked, pos))
                    return this.openPoint(access, pos.getX(), pos.getY(), pos.getZ());

            if(set.add(pos =new BlockPos(box.maxX, yFactor, box.minZ)))
                if (needOpen(access, chunked, pos))
                    return this.openPoint(access, pos.getX(), pos.getY(), pos.getZ());

            if(set.add(pos = new BlockPos(box.minX, yFactor, box.minZ)))
                if (needOpen(access, chunked, pos))
                    return this.openPoint(access, pos.getX(), pos.getY(), pos.getZ());
        }
        return this.openPoint(access, blockpos2.getX(), yFactor, blockpos2.getZ());
    }


    private boolean needOpen(NavChunkLSECache access, PntMatrix.Chunked chunked, BlockPos pos) {
        PathNodeType type;
        if ((type = chunked.getType(pos.getX(), pos.getY(), pos.getZ())) == null) {
            type = this.getPathNodeType(access, this.entity, pos);
            chunked.setType(pos.getX(), pos.getY(), pos.getZ(), type);
        }
        return this.entity.getPathPriority(type) >= 0.0F;
    }


    @Override
    public PathPoint getPathPointToCoords(NavChunkLSECache access, double x, double y, double z) {
        return this.openPoint(access, MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
    }


    @Override
    public int findPathOptions(NavChunkLSECache access, PathPoint[] pathOptions, PathPoint cur, PathPoint target, float maxDistance) {
        int i = 0;
        int stepH = 0;

        PntMatrix.Chunked chunked = matrix.getChunked(cur.x, cur.z);
        PathNodeType type = chunked.getCur(cur.x, cur.y + 1, cur.z);

        EntityLiving entity = this.entity;
        if(type == null) {
            type = this.getPathNodeType(access, entity, cur.x, cur.y + 1, cur.z);
            chunked.setCur(cur.x, cur.y + 1, cur.z, type);
        }

        if (entity.getPathPriority(type) >= 0.0F)
            stepH = MathHelper.floor(Math.max(1.0F, entity.stepHeight));


        BlockPos.MutableBlockPos pooledPos;
        (pooledPos = access.pooledPos).setPosB(cur.x, cur.y-1, cur.z);

        double curY = (double)cur.y - (1.0D - access.getBlockState(pooledPos).getBoundingBox(access, pooledPos).maxY);
        PathPoint pathpoint =  this.getSafePoint(access, chunked, cur.x, cur.y, cur.z + 1, stepH, curY, EnumFacing.SOUTH);
        PathPoint pathpoint1 = this.getSafePoint(access, chunked, cur.x - 1, cur.y, cur.z, stepH, curY, EnumFacing.WEST);
        PathPoint pathpoint2 = this.getSafePoint(access, chunked, cur.x + 1, cur.y, cur.z, stepH, curY, EnumFacing.EAST);
        PathPoint pathpoint3 = this.getSafePoint(access, chunked, cur.x, cur.y, cur.z - 1, stepH, curY, EnumFacing.NORTH);

        double maxDistanceSq = maxDistance * maxDistance;

        if (pathpoint != null && !pathpoint.visited && pathpoint.distanceToSquaredInt(target) < maxDistanceSq)
            pathOptions[i++] = pathpoint;


        if (pathpoint1 != null && !pathpoint1.visited && pathpoint1.distanceToSquaredInt(target) < maxDistanceSq)
            pathOptions[i++] = pathpoint1;


        if (pathpoint2 != null && !pathpoint2.visited && pathpoint2.distanceToSquaredInt(target) < maxDistanceSq)
            pathOptions[i++] = pathpoint2;


        if (pathpoint3 != null && !pathpoint3.visited && pathpoint3.distanceToSquaredInt(target) < maxDistanceSq)
            pathOptions[i++] = pathpoint3;


        boolean flag  = pathpoint3 == null || pathpoint3.nodeType == PathNodeType.OPEN || pathpoint3.costMalus != 0.0F;
        boolean flag1 = pathpoint == null  || pathpoint.nodeType == PathNodeType.OPEN  || pathpoint.costMalus  != 0.0F;
        boolean flag2 = pathpoint2 == null || pathpoint2.nodeType == PathNodeType.OPEN || pathpoint2.costMalus != 0.0F;
        boolean flag3 = pathpoint1 == null || pathpoint1.nodeType == PathNodeType.OPEN || pathpoint1.costMalus != 0.0F;

        PathPoint temp;
        if (flag && flag3) {
            temp = this.getSafePoint(access, chunked, cur.x - 1, cur.y, cur.z - 1, stepH, curY, EnumFacing.NORTH);

            if (temp != null && !temp.visited && temp.distanceToSquaredInt(target) < maxDistanceSq)
                pathOptions[i++] = temp;

        }

        if (flag && flag2) {
            temp = this.getSafePoint(access, chunked, cur.x + 1, cur.y, cur.z - 1, stepH, curY, EnumFacing.NORTH);

            if (temp != null && !temp.visited && temp.distanceToSquaredInt(target) < maxDistanceSq)
                pathOptions[i++] = temp;

        }

        if (flag1 && flag3) {
            temp = this.getSafePoint(access, chunked, cur.x - 1, cur.y, cur.z + 1, stepH, curY, EnumFacing.SOUTH);

            if (temp != null && !temp.visited && temp.distanceToSquaredInt(target) < maxDistanceSq)
                pathOptions[i++] = temp;

        }

        if (flag1 && flag2) {
            temp = this.getSafePoint(access, chunked, cur.x + 1, cur.y, cur.z + 1, stepH, curY, EnumFacing.SOUTH);

            if (temp != null && !temp.visited && temp.distanceToSquaredInt(target) < maxDistanceSq)
                pathOptions[i++] = temp;

        }

        return i;
    }


    @Nullable
    private PathPoint getSafePoint(NavChunkLSECache access, PntMatrix.Chunked chunked, final int x, int y, final int z, final int stepH, final double curY, final EnumFacing facing) {
        PathPoint pathpoint = null;

        double num1 = (double)y - (1.0D -
                access.getBlockState(x, y-1, z).getBoundingBox(
                        access, access.pooledPos.setPos(x, y-1, z)).maxY
        );

        if (num1 - curY > 1.125D) return null;
        else {
            PathNodeType type;
            chunked = chunked.get(x, z);

            type = chunked.getCur(x, y, z);

            if(type == null) {
                type = this.getPathNodeType(access, x, y, z, this.entity, this.entitySizeX & 255, this.entitySizeY & 255, this.entitySizeZ & 255, this.getCanOpenDoors(), this.getCanEnterDoors());
                //type = this.getPathNodeType(this.entity, x, y, z);
                chunked.setCur(x, y, z, type);
            }

            EntityLiving entity;
            float priority = (entity = this.entity).getPathPriority(type);

            if (priority >= 0.0F) {
                //todo
                pathpoint = this.openPoint(access, x, y, z);
                pathpoint.nodeType = type;
                pathpoint.costMalus = Math.max(pathpoint.costMalus, priority);
            }

            if (type != PathNodeType.WALKABLE) {
                num1 = (double)entity.width * 0.5D;

                if (pathpoint == null && stepH > 0 && type != PathNodeType.FENCE && type != PathNodeType.TRAPDOOR) {
                    pathpoint = this.getSafePoint(access, chunked, x, y + 1, z, stepH - 1, curY, facing);

                    if (pathpoint != null && (pathpoint.nodeType == PathNodeType.OPEN || pathpoint.nodeType == PathNodeType.WALKABLE) && entity.width < 1.0F) {

                        double d2 = (double) (x - facing.getXOffset()) + 0.5D;
                        double d3 = (double) (z - facing.getZOffset()) + 0.5D;

                        AxisAlignedBB box = entity.world.commonWorldEntityAABB;
                        box.init(
                                d2 - num1, (double) y + 0.001D,
                                d3 - num1, d2 + num1,
                                (float) y + entity.height, d3 + num1
                        );

                        box.expandTo(0.0D,
                                access.getBlockState(x, y, z)
                                        .getBoundingBox(access, access.pooledPos.setPos(x, y, z)).maxY - 0.002D,
                                0.0D, box);

                        if (entity.world.collidesWithAnyBlock(box)) pathpoint = null;
                    }
                }

                if (type == PathNodeType.OPEN) {
                    AxisAlignedBB box = entity.world.commonWorldEntityAABB;
                    box.init(
                            (double) x - num1 + 0.5D, (double) y + 0.001D,
                            (double) z - num1 + 0.5D, (double) x + num1 + 0.5D,
                            (float) y + entity.height, (double) z + num1 + 0.5D
                    );

                    if (entity.world.collidesWithAnyBlock(box)) return null;

                    if (entity.width >= 1.0F) {
                        PathNodeType type2 = chunked.getCur(x, y - 1, z);
                        if(type2==null) {
                            type2 = this.getPathNodeType(access, x, y, z, entity, this.entitySizeX & 255, this.entitySizeY & 255, this.entitySizeZ & 255, this.getCanOpenDoors(), this.getCanEnterDoors());
                            chunked.setCur(x, y, z, type2);
                        }

                        if (type2 == PathNodeType.BLOCKED) {
                            pathpoint = this.openPoint(access, x, y, z);
                            pathpoint.nodeType = PathNodeType.WALKABLE;
                            pathpoint.costMalus = Math.max(pathpoint.costMalus, priority);
                            return pathpoint;
                        }
                    }
                    int i = 0;

                    while (y > 0 && type == PathNodeType.OPEN) {
                        --y;

                        if (i++ >= entity.getMaxFallHeight()) return null;

                        type = chunked.getType(x, y, z);
                        if(type==null){
                            type = this.getPathNodeType(access, entity, x, y, z);
                            chunked.setType(x, y, z, type);
                        }
                        priority = entity.getPathPriority(type);

                        if (type != PathNodeType.OPEN && priority >= 0.0f) {
                            pathpoint = this.openPoint(access, x, y, z);
                            pathpoint.nodeType = type;
                            pathpoint.costMalus = Math.max(pathpoint.costMalus, priority);
                            break;
                        }

                        if (priority < 0.0F) return null;
                    }
                }

            }
            return pathpoint;
        }
    }



    @Override
    public PathNodeType getPathNodeType(NavChunkLSECache access, int x, int y, int z, EntityLiving entity, int xSize, int ySize, int zSize, boolean canBreakDoorsIn, boolean canEnterDoorsIn) {
        PathNodeType.Set enumset = access.localSet();

        PathNodeType pathnodetype = PathNodeType.BLOCKED;
        access.rawData = entity;
        pathnodetype = this.getPathNodeType(access, x, y, z, xSize, ySize, zSize, canBreakDoorsIn, canEnterDoorsIn, enumset, pathnodetype, entity.world.localTempPos3.setPos(entity));
        access.rawData = null;

        if (enumset.contains(PathNodeType.FENCE)) {
            return PathNodeType.FENCE;
        } else {
            PathNodeType nodeType = PathNodeType.BLOCKED;
            float priority1 = -1;
            for (PathNodeType nodeType1 : enumset) {
                float priority2 = entity.getPathPriority(nodeType1);
                if (priority2 < 0.0F) return nodeType1;
                if (priority2 >= priority1) {
                    nodeType = nodeType1;
                    priority1 = priority2;
                }
            }
            if (pathnodetype == PathNodeType.OPEN && priority1 == 0.0F) {
                return PathNodeType.OPEN;
            } else {
                return nodeType;
            }
        }
    }


    public PathNodeType getPathNodeType(final NavChunkLSECache access, int x, int y, int z, int xSize, int ySize, int zSize, boolean canOpenDoorsIn, boolean canEnterDoorsIn, PathNodeType.Set types, PathNodeType type, BlockPos pos) {
        PathNodeType nodeType;
        int stepX = xSize>1?2:1;
        int stepZ = zSize>1?2:1;
        for (int dY = 0; dY < ySize; ++dY) {
            for (int dX = 0; dX < xSize; dX += stepX) {
                for (int dZ = 0; dZ < zSize; dZ += stepZ) {

                    nodeType = this.getPathNodeType(access, dX + x, dY + y, dZ + z);

                    if (nodeType == PathNodeType.DOOR_WOOD_CLOSED && canOpenDoorsIn && canEnterDoorsIn) nodeType = PathNodeType.WALKABLE;
                    if (nodeType == PathNodeType.DOOR_OPEN && !canEnterDoorsIn)                         nodeType = PathNodeType.BLOCKED;
                    if (nodeType == PathNodeType.RAIL && !(access.getBlockState(pos).getBlock() instanceof BlockRailBase)
                            && !(access.getBlockState(pos.getX(), pos.getY()-1, pos.getZ()).getBlock() instanceof BlockRailBase)) nodeType = PathNodeType.FENCE;

                    if (dX == 0 && dY == 0 && dZ == 0) type = nodeType;

                    types.add(nodeType);
                }
            }
        }
        return type;
    }


    private PathNodeType getPathNodeType(final NavChunkLSECache access, EntityLiving entity, BlockPos pos) {
        return this.getPathNodeType(access, entity, pos.getX(), pos.getY(), pos.getZ());
    }


    private PathNodeType getPathNodeType(final NavChunkLSECache access, EntityLiving entity, int x, int y, int z) {
        return this.getPathNodeType(access, x, y, z, entity, this.entitySizeX & 255, this.entitySizeY & 255, this.entitySizeZ & 255, this.getCanOpenDoors(), this.getCanEnterDoors());
    }


    @Override
    public PathNodeType getPathNodeType(final NavChunkLSECache access, int x, int y, int z) {
        IBlockState state = access.getBlockState(x, y, z);
        PathNodeType raw = getPathNodeTypeRaw(access, state);
        if (raw == PathNodeType.OPEN && y >= 1) {
            state = access.getBlockState(x, y - 1, z);
            PathNodeType raw2 = getPathNodeTypeRaw(access, state);
            raw = raw2 != PathNodeType.WALKABLE && raw2 != PathNodeType.OPEN && raw2 != PathNodeType.WATER && raw2 != PathNodeType.LAVA ? PathNodeType.WALKABLE : PathNodeType.OPEN;

            if (raw2 == PathNodeType.DAMAGE_FIRE || state.getBlock() == Blocks.MAGMA) raw = PathNodeType.DAMAGE_FIRE;
            if (raw2 == PathNodeType.DAMAGE_CACTUS)                                   raw = PathNodeType.DAMAGE_CACTUS;
            if (raw2 == PathNodeType.DAMAGE_OTHER)                                    raw = PathNodeType.DAMAGE_OTHER;
        }
        return this.checkNeighborBlocks(access, x, y, z, raw);
    }


    public PathNodeType checkNeighborBlocks(NavChunkLSECache access, int x, int y, int z, PathNodeType nodeType) {
        if (nodeType == PathNodeType.WALKABLE) {
            IBlockState state;
            Block block;
            PathNodeType type;
            final BlockPos.MutableBlockPos pooledPos = access.pooledPos;
            for (int i = -1; i <= 1; ++i) {
                for (int j = -1; j <= 1; ++j) {
                    if (i != 0 || j != 0) {
                        state = access.getBlockState(pooledPos.setPos(i + x, y, j + z));
                        type = (block = state.getBlock()).getAiPathNodeType(state, access, pooledPos, (EntityLiving)access.rawData);
                        if (block == Blocks.CACTUS || type == PathNodeType.DAMAGE_CACTUS)  nodeType = PathNodeType.DANGER_CACTUS;
                        else if (block == Blocks.FIRE || type == PathNodeType.DAMAGE_FIRE) nodeType = PathNodeType.DANGER_FIRE;
                        else if (type == PathNodeType.DAMAGE_OTHER)                        nodeType = PathNodeType.DANGER_OTHER;
                    }
                }
            }
        }
        return nodeType;
    }


    protected static PathNodeType getPathNodeTypeRaw(NavChunkLSECache access, IBlockState state) {
        Block block = state.getBlock();
        PathNodeType type = block.getAiPathNodeType(state, access, access.pooledPos, (EntityLiving)access.rawData);
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
                    return block.isPassable(access, access.pooledPos) ? PathNodeType.OPEN : PathNodeType.BLOCKED;
                }
            } else {
                return PathNodeType.FENCE;
            }
        } else {
            return PathNodeType.TRAPDOOR;
        }
    }
}