package net.minecraft.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;
import plus.fserver.ElcaConfig;
import plus.fserver.utls.collections.AABBList;
import plus.fserver.utls.collections.EntityList;
import plus.tson.utl.Te4HashSet;
import java.util.Arrays;


public class EMoveHelper {
    public static void move(Entity entity, MoverType type, double x, double y, double z) {
        if (entity.noClip) {
            entity.getEntityBoundingBox().offsetMut(x, y, z);
            entity.resetPositionToBB();
        } else {
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

            entity.moveTask = task.init(entity, x, y, z);
            world.collideTask.add(entity);
        }
    }


    public static class MoveTask extends AABBList{
        final AxisAlignedBB moveBox = new AxisAlignedBB(0,0,0,0,0,0);
        Entity entity;
        double x, y, z;

        public MoveTask init(Entity entity, double x, double y, double z){
            this.entity = entity;
            super.fclear();
            this.x = x; this.y = y; this.z = z;
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
            entity.moveTask = null;
            this.entity = null;
            WorldServer world;
            (world =((WorldServer)entity.world)).getCollisionBoxesFalse(entity, moveBox, this);
            completeMove(entity, this, x, y, z);
            world.freeMoveTask(this);
        }
    }


    public static class ComboCollideTask extends BlockPos.MutableBlockPos{
        private static final Entity[] EMPTY = new Entity[0];
        private final Te4HashSet<BlockPos> allUpdate = new Te4HashSet<>();
        private final WorldServer world;
        private Entity[] temp = EMPTY;

        public ComboCollideTask(WorldServer world) {
            this.world = world;
        }

        public void add(Entity entity){
            setPosB(entity.chunkCoordX, entity.chunkCoordY, entity.chunkCoordZ);
            if(!allUpdate.contains(this)){
                allUpdate.add(new BlockPos(this));
            }
        }


        public void complete(){
            if(allUpdate.isEmpty())return;
            ChunkProviderServer server = world.chunkProviderServer;
            for (BlockPos pos:allUpdate){
                completeMoveTasks(server.getLoadedChunk(pos.getX(), pos.getZ()).getEntityLists()[pos.getY()]);
            }
            allUpdate.clear();
            Arrays.fill(temp, null);
        }


        private Entity[] export(EntityList<Entity> src, int size){
            Entity[] raw = src.raw();
            Entity[] result = temp;
            if(result.length < size){
                result = temp = new Entity[size+2];
            }
            if (size >= 0) System.arraycopy(raw, 0, result, 0, size);
            return result;
        }


        public void completeMoveTasks(ClassInheritanceMultiMap<Entity> part) {
            int size;
            EntityList<Entity> list;
            if ((size = (list = part.rawValues()).sizeC()) < 2) return;

            Entity[] arr = export(list, size);

            for (int i = 0; i < size; i++) {
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
                                AxisAlignedBB checkBox = check.getEntityBoundingBox();
                                if (checkBox.intersectsXZY(x1, y1, z1, x2, y2, z2)) {
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


    public static void completeMove(Entity entity, AABBList boxes, double x, double y, double z){
        World world = entity.world;
        final int bSize = boxes.sizeC();
        final Object[] rawBoxes = boxes.raw();

        double dx0 = entity.posX;
        double dy0 = entity.posY;
        double dz0 = entity.posZ;

        double dx1 = x;
        double dy1 = y;
        double dz1 = z;

        AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox();
        boolean flag;
        int temp, num1;
        if (flag = y != 0.0D) {
            temp = 0;
            for (num1 = bSize; temp < num1; ++temp)
                y = ((AxisAlignedBB)rawBoxes[temp]).calculateYOffset(axisalignedbb, y);

        }
        if (x != 0.0D) {
            temp = 0;
            for (num1 = bSize; temp < num1; ++temp)
                x = ((AxisAlignedBB)rawBoxes[temp]).calculateXOffset(axisalignedbb, x);

            flag = true;
        }

        if (z != 0.0D) {
            temp = 0;

            for (num1 = bSize; temp < num1; ++temp)
                z = ((AxisAlignedBB)rawBoxes[temp]).calculateZOffset(axisalignedbb, z);

            flag = true;
        }

        AxisAlignedBB prev = world.commonWorldEntityAABB;
        prev.initSafe(
                axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ,
                axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ
        );
        if(flag) {
            axisalignedbb.offsetMut(x, y, z);
        }

        flag = entity.onGround || dy1 != y && dy1 < 0.0D;

        double dx2, dy2, dz2;
        if (entity.stepHeight > 0.0F && flag && (dx1 != x || dz1 != z)) {
            dx2 = x;
            dy2 = y;
            dz2 = z;

            //entity.setEntityBoundingBox(axisalignedbb);
            y = entity.stepHeight;

            final AxisAlignedBB aab2 = entity.getEntityBoundingBox();
            aab2.expandTo(dx1, 0.0D, dz1, axisalignedbb = world.commonWorldEntityAABB2);
            double d8 = y;
            temp = 0;

            for (num1 = bSize; temp < num1; ++temp)
                d8 = ((AxisAlignedBB)rawBoxes[temp]).calculateYOffset(axisalignedbb, d8);

            aab2.offsetMut(0.0D, d8, 0.0D);
            double d18 = dx1;
            temp = 0;

            for (num1 = bSize; temp < num1; ++temp)
                d18 = ((AxisAlignedBB)rawBoxes[temp]).calculateXOffset(aab2, d18);

            aab2.offsetMut(d18, 0.0D, 0.0D);

            double d19 = dz1;
            temp = 0;

            for (num1 = bSize; temp < num1; ++temp)
                d19 = ((AxisAlignedBB)rawBoxes[temp]).calculateZOffset(aab2, d19);

            aab2.offsetMut(0.0D, 0.0D, d19);

            double d20 = y;
            temp = 0;

            axisalignedbb = entity.getEntityBoundingBox();
            for (num1 = bSize; temp < num1; ++temp)
                d20 = ((AxisAlignedBB)rawBoxes[temp]).calculateYOffset(axisalignedbb, d20);

            double d21 = dx1;
            temp = 0;

            for (num1 = bSize; temp < num1; ++temp)
                d21 = ((AxisAlignedBB)rawBoxes[temp]).calculateXOffset(axisalignedbb, d21);

            double d22 = dz1;
            temp = 0;

            for (num1 = bSize; temp < num1; ++temp)
                d22 = ((AxisAlignedBB)rawBoxes[temp]).calculateZOffset(axisalignedbb, d22);

            if (d18 * d18 + d19 * d19 > d21 * d21 + d22 * d22) {
                x = d18;
                z = d19;
                y = -d8;
                entity.setEntityBoundingBox(aab2);
            } else
                axisalignedbb.offsetMut(x = d21, y = -d20, z = d22);

            temp = 0;

            for (num1 = bSize; temp < num1; ++temp)
                y = ((AxisAlignedBB)rawBoxes[temp]).calculateYOffset(axisalignedbb, y);

            if (dx2 * dx2 + dz2 * dz2 >= x * x + z * z) {
                x = dx2;
                y = dy2;
                z = dz2;

                entity.getEntityBoundingBox().initSafe(
                        prev.minX, prev.minY, prev.minZ,
                        prev.maxX, prev.maxY, prev.maxZ
                );
            } else {
                entity.getEntityBoundingBox().offsetMut(0.0D, y, 0.0D);
            }
        }
        entity.resetPositionToBB();

        entity.onGround = (entity.collidedVertically = dy1 != y) && dy1 < 0.0D;
        entity.collided = (entity.collidedHorizontally = dx1 != x || dz1 != z) || entity.collidedVertically;

        BlockPos.MutableBlockPos pos;
        IBlockState state = world.localSelector.getState(
                pos = world.localTempPos.setPos(entity.posX, entity.posY - 0.20000000298023224D, entity.posZ)
        );

        if (state.getMaterial() == Material.AIR) {
            IBlockState state1 = world.localSelector.getBlockState(pos.getX(), pos.getY()-1, pos.getZ());
            Block block1 = state1.getBlock();

            if (block1 instanceof BlockFence || block1 instanceof BlockWall || block1 instanceof BlockFenceGate) {
                state = state1;
                pos.setY(pos.getY()-1);
            }
        }

        entity.updateFallState(y, entity.onGround, state, pos);

        if (dx1 != x) entity.motionX = 0.0D;
        if (dz1 != z) entity.motionZ = 0.0D;

        Block block = state.getBlock();

        if (entity.collidedVertically) block.onLanded(entity.world, entity);

        if (entity.canTriggerWalking() && (!entity.onGround || !entity.isSneaking() || !(entity instanceof EntityPlayer)) && !entity.isRiding()) {
            dx2 = entity.posX - dx0;
            dy2 = block == Blocks.LADDER ? entity.posY - dy0 : 0;
            dz2 = entity.posZ - dz0;

            if (entity.onGround) block.onEntityWalk(world, pos, entity);


            entity.distanceWalkedModified = (float)((double)entity.distanceWalkedModified + (double)MathHelper.sqrt(dx2 * dx2 + dz2 * dz2) * 0.6D);
            entity.distanceWalkedOnStepModified = (float)((double)entity.distanceWalkedOnStepModified + (double)MathHelper.sqrt(dx2 * dx2 + dy2 * dy2 + dz2 * dz2) * 0.6D);

            if (entity.distanceWalkedOnStepModified > (float)entity.nextStepDistance && state.getMaterial() != Material.AIR) {
                entity.nextStepDistance = (int)entity.distanceWalkedOnStepModified + 1;

                if (entity.isInWater()) {
                    Entity entity1 = entity.isBeingRidden() && entity.getControllingPassenger() != null ? entity.getControllingPassenger() : entity;
                    float fl1 = entity1 == entity ? 0.35F : 0.4F;
                    float f1 = MathHelper.sqrt(entity1.motionX * entity1.motionX * 0.20000000298023224D + entity1.motionY * entity1.motionY + entity1.motionZ * entity1.motionZ * 0.20000000298023224D) * fl1;

                    if (f1 > 1.0F) f1 = 1.0F;

                    entity.playSound(entity1.getSwimSound(), f1, 1.0F + (entity.rand.nextFloat() - entity.rand.nextFloat()) * 0.4F);
                } else entity.playStepSound(pos, block);

            } else if (entity.distanceWalkedOnStepModified > entity.nextFlap && entity.makeFlySound() && state.getMaterial() == Material.AIR)
                entity.nextFlap = entity.playFlySound(entity.distanceWalkedOnStepModified);
        }
        try {
            entity.doBlockCollisions();
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Checking entity block collision");
            CrashReportCategory cat = crashreport.makeCategory("Entity being checked for collision");
            entity.addEntityCrashInfo(cat);
            world.getMinecraftServer().ticker().onError((WorldServer) world, new ReportedException(crashreport));
        }

        flag = entity.isWet();

        entity.getEntityBoundingBox().growTo(-0.001D,-0.001D,-0.001D, axisalignedbb = world.commonWorldEntityAABB);

        if (world.isFlammableWithin(axisalignedbb)) {
            entity.dealFireDamage(1);
            if (!flag) {
                ++entity.fire;

                if (entity.fire == 0) entity.setFire(8);
            }
        } else if (entity.fire <= 0) entity.fire = (short) -entity.getFireImmuneTicks();

        if (flag && entity.isBurning()) {
            entity.playSound(SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.7F, 1.6F + (entity.rand.nextFloat() - entity.rand.nextFloat()) * 0.4F);
            entity.fire = (short) -entity.getFireImmuneTicks();
        }
    }
}
