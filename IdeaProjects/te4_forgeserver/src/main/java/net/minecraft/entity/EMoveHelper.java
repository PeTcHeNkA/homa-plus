package net.minecraft.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import plus.fserver.ElcaCV;
import plus.fserver.ElcaConfig;
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.utls.collections.AABBList;
import plus.tson.utl.Te4HashSet;
import java.util.Arrays;


public class EMoveHelper {
    public static final Runnable COMMON_FUTURE = () -> {};
    public static void move(Entity entity, MoverType type, double x, double y, double z, Runnable future) {
        int temp;
        float fl1;
        final WorldServer world = (WorldServer) entity.world;
        if (type == MoverType.PISTON) {
            float[] pistonDeltas = entity.pistonDeltas;
            long i;
            if ((i = world.getTotalWorldTime()) != entity.pistonDeltasGameTime) {
                Arrays.fill(pistonDeltas, 0.0f);
                entity.pistonDeltasGameTime = i;
            }

            if (x != 0.0D) {
                fl1 = MathHelper.clamp((float)x + pistonDeltas[temp = EnumFacing.Axis.X.ordinal()], -0.51f, 0.51f);
                x = fl1 - pistonDeltas[temp];
                pistonDeltas[temp] = fl1;

                if (Math.abs(x) <= 9.999999747378752E-6D) return;

            } else if (y != 0.0D) {
                fl1 = MathHelper.clamp((float)y + pistonDeltas[temp = EnumFacing.Axis.Y.ordinal()], -0.51f, 0.51f);
                y = fl1 - pistonDeltas[temp];
                pistonDeltas[temp] = fl1;

                if (Math.abs(y) <= 9.999999747378752E-6D) return;

            } else {
                if (z == 0.0D) return;

                fl1 = MathHelper.clamp((float)z + pistonDeltas[temp = EnumFacing.Axis.Z.ordinal()], -0.51f, 0.51f);
                z = fl1 - pistonDeltas[temp];
                pistonDeltas[temp] = fl1;

                if (Math.abs(z) <= 9.999999747378752E-6D) return;
            }
        }

        if (entity.isInWeb) {
            entity.isInWeb = false;
            x *= 0.25D;
            y *= 0.05000000074505806D;
            z *= 0.25D;
            entity.motionX = 0.0D;
            entity.motionY = 0.0D;
            entity.motionZ = 0.0D;
        }

        AxisAlignedBB prev = entity.getEntityBoundingBox();
        MoveTask task = world.collideTask.allocMoveTask();
        AxisAlignedBB axisalignedbb = task.moveBox;
        //AxisAlignedBB axisalignedbb = new AxisAlignedBB(0,0,0,0,0,0);
        //AxisAlignedBB axisalignedbb = world.commonWorldEntityAABB;
        //todo this only for players
        //todo maybe merge calls?
        if ((type == MoverType.SELF || type == MoverType.PLAYER)
                && entity.onGround && entity.isSneaking() && entity instanceof EntityPlayer) {

            fl1 = -entity.stepHeight;
            AABBList list = world.allocAABBList();

            while (x != 0.0D && z != 0.0D && world.noCollisionsBoxesNoCacheIn(list, entity, prev.offsetToF(x, fl1, z, axisalignedbb))) {
                if      (x < 0.05D && x >= -0.05D) x =  0.0D;
                else if (x > 0.0D)                 x -= 0.05D;
                else                               x += 0.05D;

                if      (z < 0.05D && z >= -0.05D) z = 0.0D;
                else if (z > 0.0D)                 z -= 0.05D;
                else                               z += 0.05D;
            }

            while (x != 0.0D && world.noCollisionsBoxesNoCacheIn(list, entity, prev.offsetToF(x, fl1, 0.0D, axisalignedbb))) {
                if      (x < 0.05D && x >= -0.05D) x =  0.0D ;
                else if (x > 0.0D)                 x -= 0.05D;
                else                               x += 0.05D;
            }

            while (z != 0.0D && world.noCollisionsBoxesNoCacheIn(list, entity, prev.offsetToF(0.0D, fl1, z, axisalignedbb))) {
                if      (z < 0.05D && z >= -0.05D) z = 0.0D  ;
                else if (z > 0.0D)                 z -= 0.05D;
                else                               z += 0.05D;
            }

            world.freeAABBList(list);
        }

        prev.expandTo(x, y, z, axisalignedbb);
        axisalignedbb.minY -= 0.125;

        entity.moveTask = task.init(entity, future, x, y, z);
        world.collideTask.add(entity);
    }


    public static final class MoveTask extends AABBList{
        final AxisAlignedBB moveBox = new AxisAlignedBB(0,0,0,0,0,0);
        Entity entity;
        double x, y, z;
        Runnable future;

        MoveTask init(Entity entity, Runnable future, double x, double y, double z){
            this.entity = entity;
            super.fclear();
            this.x = x; this.y = y; this.z = z;
            this.future = future;
            return this;
        }


        void addBox(AxisAlignedBB box){
            int size;
            AxisAlignedBB[] data;
            AxisAlignedBB prev;
            if((size = this.size) >= (data = this.data).length)
                prev = null;
            else
                prev = data[size];

            if(prev == null) {
                prev = new AxisAlignedBB(
                        box.minX, box.minY, box.minZ,
                        box.maxX, box.maxY, box.maxZ
                );
                if(data.length<=size)data = this.data = newArrayAuto();
                data[this.size++] = prev;
            } else {
                prev.init(
                        box.minX, box.minY, box.minZ,
                        box.maxX, box.maxY, box.maxZ
                );
                ++this.size;
            }
        }


        void complete(ComboCollideTask task, WorldServer world, ChunkSelector selector, AxisAlignedBB tempAABB){
            Entity entity;
            world.getCollisionBoxesFast(selector, task, entity = this.entity, moveBox, this);
            entity.completeMove(selector, task, tempAABB, this, x, y, z);
            Runnable future;
            if((future = this.future) != null){
                if(future == COMMON_FUTURE)entity.completeCommon();
                else {
                    future.run();
                    this.future = null;
                }
            }
            this.entity = null;
            entity.moveTask = null;
        }
    }


    public static class ComboCollideTask extends BlockPos.MutableBlockPos{
        private final Te4HashSet<BlockPos.MutableBlockPos> allUpdate = new Te4HashSet<>();
        private BlockPos.MutableBlockPos[] temp = new MutableBlockPos[12];
        private int tempSize = 0;
        private EMoveHelper.MoveTask[] allTasks = new EMoveHelper.MoveTask[128];
        private int realSize = 0;

        public void add(Entity entity){
            editRoot(entity.chunkCoordX, entity.chunkCoordY, entity.chunkCoordZ);
            Te4HashSet<BlockPos.MutableBlockPos> allUpdate;
            if(!(allUpdate = this.allUpdate).contains(this)) allUpdate.add(newPos());
        }


        private BlockPos.MutableBlockPos newPos(){
            int size = tempSize-1;
            if(size == -1)return new BlockPos.MutableBlockPos(this);
            BlockPos.MutableBlockPos result = temp[size];
            this.tempSize = size;
            return result;
        }


        public EMoveHelper.MoveTask allocMoveTask(){
            if (realSize == 0) return new MoveTask();
            else return allTasks[--realSize];
        }


        private void freeMoveTask(EMoveHelper.MoveTask task) {
            EMoveHelper.MoveTask[] allTasks;
            if ((allTasks = this.allTasks).length == realSize) {
                EMoveHelper.MoveTask[] newTasks = new EMoveHelper.MoveTask[allTasks.length + 128];
                System.arraycopy(allTasks, 0, newTasks, 0, allTasks.length);
                allTasks = this.allTasks = newTasks;
            }
            allTasks[realSize++] = task;
        }


        public void complete(WorldServer world, ChunkSelector selector, AxisAlignedBB tempAABB){
            if(allUpdate.isEmpty())return;
            ChunkProviderServer provider = selector.provider();

            BlockPos.MutableBlockPos[] temp = this.temp;
            int tempSize = this.tempSize;

            for (BlockPos.MutableBlockPos pos: allUpdate){
                Chunk chunk = provider.getLoadedChunk(pos.getX(), pos.getZ());
                if(chunk != null) {
                    ClassInheritanceMultiMap<Entity> list = chunk.getEntityLists()[pos.getY()];
                    if(list != null) {
                        selector.setChunk(chunk);
                        completeMoveTasks(world, selector, tempAABB, list);
                    }
                }
                if(tempSize == temp.length){
                    BlockPos.MutableBlockPos[] newTemp = new MutableBlockPos[temp.length+4];
                    System.arraycopy(temp, 0, newTemp, 0, tempSize);
                    temp = this.temp = newTemp;
                }
                temp[tempSize++] = pos;
            }
            this.tempSize = tempSize;
            allUpdate.clear();
        }


        private void completeMoveTasks(WorldServer world, ChunkSelector selector, AxisAlignedBB tempAABB, ClassInheritanceMultiMap<Entity> list) {
            int size;
            if ((size = list.sizeC()) < 2) return;
            Entity[] arr = list.raw();

            //AxisAlignedBB tempAABB = this.tempAABB;
            //fix out of index exception when list modified in other thread
            size = Math.min(size, arr.length-1);
            for (int i = 0; i < size; i++) {
                MoveTask curTask;
                Entity cur;
                //fix null pointer exception when list modified in other thread
                if((cur = arr[i]) == null || (curTask = cur.moveTask) == null)continue;

                int lim;
                l1: if(!cur.ignoreCollisions() && (lim = curTask.sizeC()) < ElcaConfig.MAX_ENTITY_COLLISIONS_MOVE) {
                    AxisAlignedBB aabb;
                    double x1 = (aabb = curTask.moveBox).minX, y1 = aabb.minY, z1 = aabb.minZ,
                           x2 =  aabb.maxX                   , y2 = aabb.maxY, z2 = aabb.maxZ;
                    for (int j = i + 1; j < size; j++) {
                        Entity check;
                        if((check = arr[j]) != null && !check.ignoreCollisions()) {
                            MoveTask checkTask;
                            if ((checkTask = check.moveTask) == null) {
                                AxisAlignedBB checkBox;
                                if ((checkBox = check.getEntityBoundingBox())
                                        .intersectsXZY(x1, y1, z1, x2, y2, z2)) {
                                    curTask.addBox(checkBox);
                                    if (++lim >= ElcaConfig.MAX_ENTITY_COLLISIONS_MOVE) break l1;
                                }
                            } else {
                                AxisAlignedBB checkBox;
                                if ((checkBox = checkTask.moveBox).intersectsXZY(x1, y1, z1, x2, y2, z2)) {
                                    curTask.addBox(checkBox);
                                    checkTask.addBox(checkBox);
                                    if (++lim >= ElcaConfig.MAX_ENTITY_COLLISIONS_MOVE) break l1;
                                }
                            }
                        }
                    }
                }
                curTask.complete(this, world, selector, tempAABB);
                this.freeMoveTask(curTask);
            }
        }
    }
}