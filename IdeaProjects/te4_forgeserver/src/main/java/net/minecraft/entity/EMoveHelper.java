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
import plus.fserver.ElcaConfig;
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
        MoveTask task = world.allocMoveTask();
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


    public static class MoveTask extends AABBList{
        final AxisAlignedBB moveBox = new AxisAlignedBB(0,0,0,0,0,0);
        Entity entity;
        double x, y, z;
        Runnable future;

        public MoveTask init(Entity entity, Runnable future, double x, double y, double z){
            this.entity = entity;
            super.fclear();
            this.x = x; this.y = y; this.z = z;
            this.future = future;
            return this;
        }


        public void addBox(AxisAlignedBB box){
            AxisAlignedBB prev = getSafe(size);
            if(prev == null) {
                prev = new AxisAlignedBB(
                        box.minX, box.minY, box.minZ,
                        box.maxX, box.maxY, box.maxZ
                );
                super.addF(prev);
            } else {
                prev.init(
                        box.minX, box.minY, box.minZ,
                        box.maxX, box.maxY, box.maxZ
                );
                ++size;
            }
        }


        public void complete(){
            Entity entity = this.entity;
            WorldServer world;
            (world =((WorldServer)entity.world)).getCollisionBoxesFalse(entity, moveBox, this);
            entity.completeMove(this, x, y, z);
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
            world.freeMoveTask(this);
        }
    }


    public static class ComboCollideTask extends BlockPos.MutableBlockPos{
        private final Te4HashSet<BlockPos.MutableBlockPos> allUpdate = new Te4HashSet<>();
        private final WorldServer world;
        private BlockPos.MutableBlockPos[] temp = new MutableBlockPos[12];
        private int tempSize = 0;

        public ComboCollideTask(WorldServer world) {
            this.world = world;
        }

        public void add(Entity entity){
            setPosB(entity.chunkCoordX, entity.chunkCoordY, entity.chunkCoordZ);
            if(!allUpdate.contains(this)) allUpdate.add(newPos());
        }


        private BlockPos.MutableBlockPos newPos(){
            int size = tempSize-1;
            if(size == -1)return new BlockPos.MutableBlockPos(this);
            BlockPos.MutableBlockPos result = temp[size];
            this.tempSize = size;
            return result;
        }


        public void complete(){
            if(allUpdate.isEmpty())return;
            ChunkProviderServer server = world.chunkProviderServer;

            BlockPos.MutableBlockPos[] temp = this.temp;
            int tempSize = this.tempSize;

            for (BlockPos.MutableBlockPos pos: allUpdate){
                Chunk chunk = server.getLoadedChunk(pos.getX(), pos.getZ());
                if(chunk != null) {
                    ClassInheritanceMultiMap<Entity> list = chunk.getEntityLists()[pos.getY()];
                    if(list != null) completeMoveTasks(list);
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


        private static void completeMoveTasks(ClassInheritanceMultiMap<Entity> list) {
            if ((list.sizeC()) < 2) return;
            Entity[] arr = list.raw();
            int size;
            for (int i = 0; i < (size = list.sizeC()); i++) {
                Entity cur;
                MoveTask curTask;
                if ((curTask = (cur = arr[i]).moveTask) == null) continue;

                int lim;
                l1: if(!cur.ignoreCollisions() && (lim = curTask.sizeC()) < ElcaConfig.MAX_ENTITY_COLLISIONS_MOVE) {
                    AxisAlignedBB aabb;
                    double x1 = (aabb = curTask.moveBox).minX, y1 = aabb.minY, z1 = aabb.minZ,
                           x2 =  aabb.maxX                   , y2 = aabb.maxY, z2 = aabb.maxZ;
                    for (int j = i + 1; j < size; j++) {
                        Entity check;
                        if(!(check = arr[j]).ignoreCollisions()) {
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
                curTask.complete();
            }
        }
    }
}