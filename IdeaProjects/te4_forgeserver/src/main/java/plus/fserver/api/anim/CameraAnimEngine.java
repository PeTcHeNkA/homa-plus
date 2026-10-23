package plus.fserver.api.anim;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Rotations;
import net.minecraft.world.WorldServer;
import plus.fserver.server.network.packet.SNodePacketPlayBase;
import plus.fserver.utls.McTsonUtils;
import plus.fserver.utls.collections.ArrayListE6;
import plus.tson.TsonFunc;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import static plus.fserver.utls.math.MathUtls.F_256_T_360;
import static plus.tson.ext.TsonNumUtils.*;
import static plus.tson.ext.TsonBoolUtils.*;
import static plus.tson.ext.TsonAccessUtils.*;


public class CameraAnimEngine {
    public static SPacketEntityMetadata createInvisible(int id){
        List<EntityDataManager.DataEntry<?>> inv = Collections.singletonList(
                    new EntityDataManager.DataEntry<>(EntityPlayerMP.FLAGS, (byte)32)
            );
        return new SPacketEntityMetadata(id, inv);
    }


    public static SPacketEntityMetadata createName(int id, String name){
        List<EntityDataManager.DataEntry<?>> inv = Lists.newArrayList(
                new EntityDataManager.DataEntry<>(EntityPlayerMP.CUSTOM_NAME, name),
                new EntityDataManager.DataEntry<>(EntityPlayerMP.CUSTOM_NAME_VISIBLE, true)
        );
        return new SPacketEntityMetadata(id, inv);
    }


    public static SPacketEntityMetadata rotateHead(int id, float x, float y, float z){
        List<EntityDataManager.DataEntry<?>> inv = Collections.singletonList(
                new EntityDataManager.DataEntry<>(EntityArmorStand.HEAD_ROTATION, new Rotations(x,y,z))
        );
        return new SPacketEntityMetadata(id, inv);
    }


    public static SPacketEntityEquipment createHeadEquip(int id, ItemStack stack){
        return new SPacketEntityEquipment(id, EntityEquipmentSlot.HEAD, stack);
    }


    public static class MultiEditBuilder extends Long2ObjectOpenHashMap<GroupEditEntry> {
        public void add(int x, int y, int z, IBlockState state){
            long key = ChunkPos.asLong(x>>4,z>>4);
            GroupEditEntry list = super.get(key);
            if(list == null){
                super.put(key, list = new GroupEditEntry());
            }
            list.addF(new BlockEditEntry(x,y,z,state));
        }
        public MultiPacketSender build(){
            MultiPacketSender list = new MultiPacketSender(super.size());
            for (GroupEditEntry entry:super.values()){
                list.addF(entry.build());
            }
            return list;
        }
    }


    public static class MultiPacketSender extends ArrayListE6<SNodePacketPlayBase>{
        public MultiPacketSender(){}
        public MultiPacketSender(int size){
            super(size);
        }


        public void sendSync(EntityPlayerMP player){
            Object[] data = super.data;
            NetworkManager nmgr = player.connection.netManager;
            for (int i = 0, l = data.length; i < l; i++){
                SNodePacketPlayBase packet = (SNodePacketPlayBase) data[i];
                nmgr.sendPacketSync(packet);
            }
        }


        public void sendAsync(EntityPlayerMP player){
            Object[] data = super.data;
            NetworkManager nmgr = player.connection.netManager;
            for (int i = 0, l = data.length-1; i < l; i++){
                SNodePacketPlayBase packet = (SNodePacketPlayBase) data[i];
                nmgr.addToQueueN(packet);
            }
            nmgr.sendPacketE((SNodePacketPlayBase) data[data.length-1]);
        }
    }


    private static class GroupEditEntry extends ArrayListE6<BlockEditEntry>{
        public SNodePacketPlayBase build(){
            if(size() == 1){
                BlockEditEntry etr = super.get(0);
                return new SPacketBlockChange(new BlockPos(etr.x, etr.y, etr.z), etr.state);
            } else {
                SPacketMultiBlockChange packet = new SPacketMultiBlockChange();
                SPacketMultiBlockChange.BlockUpdateData[] data = new SPacketMultiBlockChange.BlockUpdateData[size()];

                for (int i = 0; i < data.length; i++){
                    BlockEditEntry upd = super.get(i);
                    data[i] = packet.data(upd.offset(), upd.state);
                }
                BlockEditEntry upd = super.get(0);

                packet.init(upd.x>>4, upd.z>>4, data);
                return packet;
            }
        }
    }


    public static class BlockEditEntry{
        public final int x, y, z;
        public final IBlockState state;

        public BlockEditEntry(TsonList list){
            this(
                    list.getInt(0), list.getInt(1), list.getInt(2),
                    McTsonUtils.blockOfTson(list.get(3))
            );
        }

        public BlockEditEntry(int x, int y, int z, IBlockState state) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.state = state;
        }
        short offset(){
            return (short) (x << 12 | z << 8 | y);
        }
    }


    public static Animation readAnim(TsonMap map){
        String name = map.getStrSafe("name");
        TsonList points = map.getList("points");
        KeyPoint[] kPoints = new KeyPoint[points.size()];

        KeyPoint last = KeyPoint.EMPTY;

        for (int i = 0; i < kPoints.length; i++){
            KeyPoint cur;
            kPoints[i] = cur = new KeyPoint(last, points.getMap(i));
            last = cur;
        }
        Animation anim = new Animation(name, kPoints, calc(map, "relative", false));

        if(map.containsKey("head")){
            anim.setHead(McTsonUtils.itemStackOfTson(map.get("head")));
        }
        return anim;
    }


    public static class WorldAnimPlayer implements Runnable{
        private final AnimPlayer anim;
        private EntityPlayerMP player;
        private WorldServer world;
        private volatile ScheduledFuture<?> future;
        private boolean hasFirstPacket = false;

        public WorldAnimPlayer(AnimPlayer anim, EntityPlayerMP player) {
            this.player = player;
            this.anim = anim;
        }


        public void start(){
            world = player.getServerWorld();
            future = world.getMinecraftServer().ticker().service.scheduleAtFixedRate(this, 0, 50, TimeUnit.MILLISECONDS);
        }


        public void stop(){
            synchronized (anim){
                if(future != null){
                    if(hasFirstPacket){
                        boolean sendDestroy = !player.hasDisconnected();
                        if (sendDestroy) {
                            sendDestroy = player.world == world;
                        }
                        if(sendDestroy){
                            anim.camera.destroyCamera(player);
                        }
                    }
                    future.cancel(false);
                    future = null;
                    world = null;
                    player = null;
                }
            }
        }


        @Override
        public void run() {
            if(future == null)return;
            try {
                EntityPlayerMP player = this.player;
                boolean stopAnim = player.hasDisconnected();
                if (!stopAnim) {
                    stopAnim = player.world != world;
                }

                if (!stopAnim) {
                    if(hasFirstPacket){
                        stopAnim = !anim.update(player);
                    } else {
                        anim.setup(player);
                        hasFirstPacket = true;
                    }
                }
                if (stopAnim) stop();
            } catch (Throwable e){
                e.printStackTrace();
            }
        }
    }


    public static class Animation{
        final String name;
        final KeyPoint[] points;
        final boolean relative;
        ItemStack head;

        public Animation(String name, KeyPoint[] points, boolean relative) {
            this.name = name;
            this.points = points;
            this.relative = relative;
        }


        public void setHead(ItemStack stack){
            if(stack.isEmpty())return;
            this.head = stack;
        }


        public String getName() {
            return name;
        }


        public KeyPoint get(int index){
            KeyPoint[] points = this.points;
            if(index >= points.length)return null;
            return points[index];
        }


        protected AnimPlayer createPlayer(int cameraId){
            return new AnimPlayer(cameraId, this);
        }


        public WorldAnimPlayer forPlayer(EntityPlayerMP player){
            AnimPlayer plr = createPlayer(player.world.nextEntityId());
            if(relative){
                plr.setShift(player.posX, player.posY, player.posZ);
            }
            return new WorldAnimPlayer(plr, player);
        }
    }


    public static class AnimPlayer{
        private final Animation animation;
        private final VrStand camera;
        private KeyPoint current, next;
        private int time = 1;
        private int nextPoint = 1;
        private double dX, dY, dZ;

        public AnimPlayer(int cameraId, Animation animation) {
            this.animation = animation;
            this.current = animation.get(0);
            this.next    = animation.get(1);
            this.camera  = new VrStand(cameraId);
        }


        public AnimPlayer setShift(double x, double y, double z){
            dX = x;
            dY = y;
            dZ = z;
            return this;
        }


        public void setup(EntityPlayerMP player){
            if(current != null) {
                camera.set(dX, dY, dZ,0,0);
                camera.setupCamera(player);
                if(animation.head != null){
                    camera.sendHead(player, animation.head);
                }
                runFunc(current.func, dX, dY, dZ, player);
            }
        }


        private static void runFunc(TsonFunc func, double dx, double dy, double dz, EntityPlayerMP player){
            if(func != null){
                if(func instanceof IRelativeFunc){
                    ((IRelativeFunc) func).call(dx, dy, dz, player);
                } else {
                    func.call(player);
                }
            }
        }


        public boolean update(EntityPlayerMP player){
            KeyPoint current, next;
            if((next = this.next) == null || (current = this.current) == null)return false;
            VrStand camera = this.camera;

            if(current.updateCamera(camera, time++, next)){
                this.current = next;
                try {
                    runFunc(next.func, dX, dY, dZ, player);
                } catch (Throwable e){
                    e.printStackTrace();
                }
                this.next = next = animation.get(++nextPoint);
                this.time = 1;
            }
            camera.shift(dX, dY, dZ);
            camera.sendMove(player);
            return next != null;
        }
    }


    public static class KeyPoint{
        static final KeyPoint EMPTY = new KeyPoint(0,0,0,0,0,50);
        final double posX, posY, posZ;
        final float yaw, pith;
        final int time;
        TsonFunc func;

        public KeyPoint(double posX, double posY, double posZ, float yaw, float pith, int time) {
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            this.yaw = yaw;
            this.pith = pith;
            this.time = time;
        }

        public KeyPoint(KeyPoint prev, TsonObj obj) {
            if(obj.isMap()) {
                TsonMap point = obj.getMap();
                this.posX = calc(point, "x", prev.posX);
                this.posY = calc(point, "y", prev.posX);
                this.posZ = calc(point, "z", prev.posX);
                this.time = calc(point, "time", prev.time);

                if (point.containsKey("look")) {
                    TsonObj lookRaw = point.get("look");
                    double[] newVec;

                    if(lookRaw.isMap()) {
                        TsonMap lookTo = lookRaw.getMap();
                        newVec = look(
                                posX, posY, posZ,
                                calc(lookTo, "x", posX),
                                calc(lookTo, "y", posY),
                                calc(lookTo, "z", posZ)
                        );
                    } else if(lookRaw.isList()){
                        TsonList lookTo = lookRaw.getList();
                        newVec = look(
                                posX, posY, posZ,
                                lookTo.getDouble(0),
                                lookTo.getDouble(1),
                                lookTo.getDouble(2)
                        );
                    } else {
                        throw new IllegalArgumentException("Unsupported type "+lookRaw);
                    }
                    this.yaw  = (float) newVec[1];
                    this.pith = (float) newVec[0];
                } else {
                    this.yaw  = calc(point, "yaw" , (float) prev.posX);
                    this.pith = calc(point, "pith", (float) prev.posX);
                }

                if (point.containsKey("func")) {
                    func = getFunc(point, "func");
                    int args;
                    if ((args = func.countArgs()) != 1 && args != -1)
                        throw new IllegalArgumentException("Should be 1 arg of EntityPlayerMP");
                }
            } else if(obj.isList()){
                TsonList list = obj.getList();
                posX = list.getDouble(0);
                posY = list.getDouble(1);
                posZ = list.getDouble(2);
                if(list.size() >= 4){
                    time = list.getInt(3);
                } else {
                    time = prev.time;
                }
                if(list.size() >= 6){
                    yaw  = list.getFloat(4);
                    pith = list.getFloat(5);
                } else {
                    yaw  = prev.yaw;
                    pith = prev.pith;
                }
            } else {
                throw new IllegalArgumentException("Unsupported type "+obj);
            }
        }


        public boolean updateCamera(VrStand camera, int cTime, KeyPoint next){
            int time = this.time;
            double x, y, z;
            float yaw, pith;
            if(time == 0 || time == cTime){
                KeyPoint point = time==cTime?next:this;
                x    = point.posX;
                y    = point.posY;
                z    = point.posZ;
                yaw  = point.yaw;
                pith = point.pith;
            } else {
                float coef = coef(cTime, time);
                x    = intrPos(posX, next.posX, coef);
                y    = intrPos(posY, next.posY, coef);
                z    = intrPos(posZ, next.posZ, coef);
                yaw  = intrRotation(next.yaw , this.yaw , coef);
                pith = intrRotation(next.pith, this.pith, coef);
            }
            camera.set(x, y, z, yaw, pith);
            return time == cTime;
        }
    }


    public static class VrStand {
        private double posX, posY, posZ;
        private long prevX, prevY, prevZ;
        private float yaw, pith;
        private final int id;
        private final UUID uuid = UUID.randomUUID();

        public VrStand(int id) {
            this.id = id;
        }


        public void set(double x, double y, double z, float yaw, float pith){
            posX = x;
            posY = y;
            posZ = z;
            this.yaw = yaw;
            this.pith = pith;
        }


        public void shift(double x, double y, double z){
            posX += x;
            posY += y;
            posZ += z;
        }


        public void destroyCamera(EntityPlayerMP player){
            player.connection.netManager.sendPacketE(
                    new SPacketCamera(player.getEntityId())
            );
            sendDespawn(player);
        }


        public void rotateHead(EntityPlayerMP player, float x, float y, float z){
            player.connection.netManager.sendPacketE(CameraAnimEngine.rotateHead(id, x, y, z));
        }


        public void sendHead(EntityPlayerMP player, ItemStack stack){
            player.connection.netManager.sendPacketE(createHeadEquip(id, stack));
        }


        public void sendName(EntityPlayerMP player, String name){
            player.connection.netManager.sendPacketE(createName(id, name));
        }


        public void setupCamera(EntityPlayerMP player){
            sendSpawn(player);
            player.connection.netManager.sendPacketE(
                    new SPacketCamera(id)
            );
        }


        public void sendDespawn(EntityPlayerMP player){
            player.connection.netManager.sendPacketE(
                    new SPacketDestroyEntities(id)
            );
        }


        public void sendSpawn(EntityPlayerMP player){
            int yaw  = MathHelper.floor(this.yaw  * F_256_T_360);
            int pith = MathHelper.floor(this.pith * F_256_T_360);

            prevX = EntityTracker.getPositionLong(posX);
            prevY = EntityTracker.getPositionLong(posY);
            prevZ = EntityTracker.getPositionLong(posZ);

            SPacketSpawnObject packet = new SPacketSpawnObject(
                    id, uuid, posX, posY, posZ, (byte) yaw, (byte) pith, 78, 0
            );
            player.connection.netManager.addToQueueN(packet);
            player.connection.netManager.sendPacketE(createInvisible(id));
        }


        public void sendTp(EntityPlayerMP player){
            int yaw  = MathHelper.floor(this.yaw  * F_256_T_360);
            int pith = MathHelper.floor(this.pith * F_256_T_360);

            long cX = EntityTracker.getPositionLong(posX);
            long cY = EntityTracker.getPositionLong(posY);
            long cZ = EntityTracker.getPositionLong(posZ);

            prevX = cX;
            prevY = cY;
            prevZ = cZ;

            SPacketEntityTeleport packet = new SPacketEntityTeleport(
                    id, posX, posY, posZ, (byte) yaw, (byte) pith, false
            );
            player.connection.netManager.sendPacketE(packet);
        }


        public void sendMove(EntityPlayerMP player){
            long cX = EntityTracker.getPositionLong(posX);
            long cY = EntityTracker.getPositionLong(posY);
            long cZ = EntityTracker.getPositionLong(posZ);

            long eX = cX - prevX;
            long eY = cY - prevY;
            long eZ = cZ - prevZ;

            prevX = cX;
            prevY = cY;
            prevZ = cZ;

            int yaw  = MathHelper.floor(this.yaw  * F_256_T_360);
            int pith = MathHelper.floor(this.pith * F_256_T_360);

            SPacketEntity.S17PacketEntityLookMove packet = new SPacketEntity.S17PacketEntityLookMove(
                    id, eX, eY, eZ, (byte) yaw, (byte) pith, false
            );
            player.connection.netManager.sendPacketE(packet);
        }
    }


    protected static double intrPos(double from, double to, float coef){
        return from + (to - from) * coef;
    }


    private static float coef(int time, int curTime){
        return ((float)time/curTime);
    }


    public static double[] look(double x1, double y1, double z1, double x2, double y2, double z2){
        double dd1 = Math.abs(Math.abs(x1)-Math.abs(x2));
        dd1 = dd1*dd1;
        double dd2 = Math.abs(Math.abs(z1)-Math.abs(z2));
        dd2 = dd2*dd2;

        return new double[]{
                ((Math.toDegrees(Math.atan2(  Math.sqrt(dd1+dd2), -(y1-y2)  ))-90)),
                ( Math.toDegrees(Math.atan2(  x1-x2, z2-z1  )) )
        };
    }


    protected static float intrRotation(final float cur, final float prev, final float coef) {
        float f;
        for (f = cur - prev; f < -180.0f; f += 360.0f) {}
        while (f >= 180.0f) {
            f -= 360.0f;
        }
        return prev + coef * f;
    }



    public static TsonFunc getFunc(TsonMap src, String path){
        TsonObj obj = getR(src, path);
        if(obj == null)return null;
        return getFunc(obj);
    }


    public static TsonFunc getFunc(TsonObj obj){
        if(obj.isFunc())return (TsonFunc) obj.getField();
        if(obj.isList()){
            TsonList list = obj.getList();
            TsonFunc[] funcs = new TsonFunc[list.size()];
            for (int i = 0; i < funcs.length; i++){
                funcs[i] = getFunc(list.get(i));
            }
            return IRelativeFunc.merge(funcs);
        }
        throw new IllegalArgumentException("Unsupported type: "+obj);
    }
}