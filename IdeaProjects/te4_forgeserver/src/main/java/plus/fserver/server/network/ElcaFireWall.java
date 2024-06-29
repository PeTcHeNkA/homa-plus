package plus.fserver.server.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.ScheduledFuture;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import static plus.fserver.ElcaConfig.DEFAULT_SOFT_TIME;


public abstract class ElcaFireWall<T extends Channel> extends ChannelInitializer<T> {
    private static final Logger LOGGER = LogManager.getLogger("FIREWALL");
    private final ConcurrentHashMap<Key,Item> map = new ConcurrentHashMap<>();
    private boolean breakUnknown = false;
    private boolean dirty = false;
    private ScheduledFuture<?> future;
    private boolean logHistory = false;

    public ElcaFireWall(EventLoopGroup group){
        group.execute(() -> {
            readBanList();
            future = group.scheduleWithFixedDelay(ElcaFireWall.this::saveBanList, 1, 1, TimeUnit.MINUTES);
        });
    }


    @Override
    public void initChannel(T channel) throws Exception {
        InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
        Key key;
        Item item = map.get(key = new Key(address.getAddress().getAddress()));
        if(item == null){
            if(breakUnknown) {
                channel.close();
                preInit(channel, key, null,  false);
                return;
            }
        } else {
            if(!item.allowConnect){
                channel.close();
                preInit(channel, key, item,  false);
                return;
            }
        }
        preInit(channel, key, item,  true);
        postInit(channel);
    }


    protected abstract void postInit(Channel channel) throws Exception;


    private void preInit(Channel channel, Key key, Item item, boolean allowConnect){
        if(logHistory){
            if(allowConnect){
                LOGGER.info("ALLOW [{}]", channel.remoteAddress());
            } else {
                LOGGER.warn("BLOCK [{}]", channel.remoteAddress());
            }
        }
    }


    public void setLog(boolean b){
        this.logHistory = b;
    }


    public void softSetAll(MinecraftServer server, long time, boolean allow){
        ArrayList<EntityPlayerMP> list = new ArrayList<>(server.getPlayerList().getPlayers());
        for(EntityPlayerMP player : list){
            softSet(player, time, allow);
        }
    }


    public boolean softSet(EntityPlayerMP player, boolean allow){
        return softSet(player, DEFAULT_SOFT_TIME, allow);
    }


    public boolean softSet(EntityPlayerMP player, long time, boolean allow){
        if(player.connection != null){
            Channel channel = player.connection.netManager.channel();
            InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
            softSet(new Key(address), time, allow);
            return true;
        }
        return false;
    }


    public void softSet(InetAddress address, long time, boolean allow){
        softSet(new Key(address), time, allow);
    }


    public void softSet(InetAddress address, boolean allow){
        softSet(new Key(address), DEFAULT_SOFT_TIME, allow);
    }


    public void writeTo(DataOutputStream out) {
        map.forEach((key, item) -> {
            try {
                out.writeByte(key.bytes.length);
                out.write(key.bytes);

                out.writeLong(item.timeStamp);
                out.writeBoolean(item.allowConnect);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }


    public void readFrom(DataInputStream in){
        try {
            while (in.available() > 0) {
                int length = in.readByte();
                byte[] bytes = new byte[length];
                in.readFully(bytes);
                Key key = new Key(bytes);
                long timeStamp = in.readLong();
                boolean allowConnect = in.readBoolean();
                Item item = new Item();
                item.timeStamp = timeStamp;
                item.allowConnect = allowConnect;
                map.put(key, item);
            }
        } catch (IOException e) {}
    }


    public void destroy(){
        if(future != null) future.cancel(true);
        saveBanList();
    }


    public void readBanList(){
        File file = new File("elca_softbans.bin");
        if(file.exists()){
            FileInputStream is = null;
            try {
                is = new FileInputStream(file);

                DataInputStream dis = new DataInputStream(is);
                readFrom(dis);

                dis.close();
            } catch (IOException e) {
                if(is != null) {
                    try {
                        is.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
                e.printStackTrace();
            }
        }
    }


    private void recheckBanList(){
        synchronized (map) {
            long curTime = System.currentTimeMillis();
            Iterator<Map.Entry<Key, Item>> itr = map.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry<Key, Item> entry = itr.next();
                if (entry.getValue().timeStamp < curTime) {
                    itr.remove();
                    dirty = true;
                }
            }
        }
    }


    public void saveBanList(){
        if (!dirty) return;
        synchronized (map) {
            if (!dirty) return;
            dirty = false;
            File file = new File("elca_softbans.bin");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            FileOutputStream os = null;
            try {
                os = new FileOutputStream(file);
                DataOutputStream dos = new DataOutputStream(os);
                writeTo(dos);
                dos.close();
            } catch (IOException e) {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
                e.printStackTrace();
            }
        }
    }


    private void softSet(Key key, long time, boolean allow){
        Item item = map.get(key);
        if(item == null){
            item = new Item();
        }
        item.timeStamp = System.currentTimeMillis() + time;
        item.allowConnect = allow;
        dirty = true;
    }


    public boolean isDirty(){
        return dirty;
    }


    public void setDirty(boolean dirty){
        this.dirty = dirty;
    }


    public boolean isBreakUnknown(){
        return breakUnknown;
    }


    public void setBreakUnknown(boolean breakUnknown){
        this.breakUnknown = breakUnknown;
    }


    private static final class Key{
        private final byte[] bytes;

        private Key(InetSocketAddress address){
            this(address.getAddress().getAddress());
        }

        private Key(InetAddress address){
            this(address.getAddress());
        }

        private Key(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            Key key = (Key) o;
            return Arrays.equals(bytes, key.bytes);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(bytes);
        }

        @Override
        public String toString() {
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }


    private static final class Item{
        private long timeStamp;
        private boolean allowConnect = true;
    }
}
