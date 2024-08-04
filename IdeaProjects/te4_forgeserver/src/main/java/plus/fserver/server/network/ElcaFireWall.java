package plus.fserver.server.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.ScheduledFuture;
import net.minecraft.entity.player.EntityPlayerMP;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.fserver.ElcaConfig;
import plus.fserver.api.FireWall;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import static plus.fserver.ElcaConfig.DEFAULT_SOFT_TIME;


public abstract class ElcaFireWall<T extends Channel> extends ChannelInitializer<T> implements FireWall {
    private static final Logger LOGGER = LogManager.getLogger("FIREWALL");
    private final ConcurrentHashMap<Key, UserData> map = new ConcurrentHashMap<>();
    private boolean breakUnknown = false;
    private boolean dirty = false;
    private ScheduledFuture<?> future;
    private boolean logHistory = true;
    private volatile long lastStamp = System.currentTimeMillis();
    private int connections = 0;

    public ElcaFireWall(EventLoopGroup group){
        group.execute(() -> {
            readBanList();
            future = group.scheduleWithFixedDelay(ElcaFireWall.this::recheckBanList, 1, 1, TimeUnit.MINUTES);
        });
    }


    @Override
    public void initChannel(T channel) throws Exception {
        InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
        Key key;
        UserData item = map.get(key = new Key(address.getAddress().getAddress()));
        long cur = System.currentTimeMillis();
        if(item == null || item.timeStamp < cur || item.unknown){
            if(breakUnknown) {
                channel.close();
                preInit(channel, key, item,  false);
                return;
            }
            if(++connections > ElcaConfig.MAX_CONNECTIONS_PER){
                channel.close();
                return;
            } else {
                if(cur > lastStamp){
                    connections = 0;
                    lastStamp = cur + ElcaConfig.MAX_CONNECTIONS_TIME;
                }
            }
        } else {
            if(!item.allowConnect){
                item.warn += 1;
                channel.close();
                preInit(channel, key, item,  false);
                return;
            }
        }
        preInit(channel, key, item,  true);
        postInit(channel);
    }


    protected abstract void postInit(Channel channel) throws Exception;


    private void preInit(Channel channel, Key key, UserData item, boolean allowConnect){
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


    public boolean softSet(EntityPlayerMP player, long time, boolean allow){
        if(player.connection != null){
            return softSet(player.connection.netManager.channel(), time, allow);
        }
        return false;
    }


    public boolean softSet(Channel channel, long time, boolean allow){
        InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
        softSet(new Key(address), time, allow);
        return true;
    }


    public boolean hasRule(Channel channel, Boolean allowConnect){
        InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
        UserData item = map.get(new Key(address.getAddress().getAddress()));
        if(item == null)return false;
        if(allowConnect == null)return true;
        return item.allowConnect == allowConnect.booleanValue();
    }


    public boolean softWarn(Channel channel, int amount){
        InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
        Key key;
        UserData item = map.get(key = new Key(address.getAddress().getAddress()));

        if(item == null){
            item = new UserData();
            map.put(key, item);
        }

        if(item.unknown){
            if(amount > 0) {
                if ((item.warn += amount) > ElcaConfig.MAX_CONNECTION_WARN) {
                    item.allowConnect = false;
                    long max = Math.max(item.timeStamp, System.currentTimeMillis());
                    max += item.warn;
                    item.warn = 0;
                    item.timeStamp = max;
                    return true;
                }
            } else {
                item.warn += amount;
                if(item.warn < 0)item.warn = 0;
            }
        }
        return breakUnknown && item.unknown;
    }


    @Override
    public void softSet(InetAddress address, long time, boolean allow){
        softSet(new Key(address), time, allow);
    }


    @Override
    public void softSet(InetAddress address, boolean allow){
        softSet(new Key(address), DEFAULT_SOFT_TIME, allow);
    }


    public void writeTo(DataOutputStream out) {
        long time = System.currentTimeMillis();
        map.forEach((key, item) -> {
            try {
                if(item.timeStamp > time && !item.unknown){
                    out.writeByte(key.bytes.length);
                    out.write(key.bytes);

                    out.writeLong(item.timeStamp);
                    out.writeBoolean(item.allowConnect);
                }
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
                UserData item = new UserData();
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
            Iterator<Map.Entry<Key, UserData>> itr = map.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry<Key, UserData> entry = itr.next();
                UserData data = entry.getValue();

                if(data.warn > 5000){
                    data.warn -= 5000;
                } else {
                    data.warn = 0;
                }

                if (data.timeStamp < curTime) {
                    itr.remove();
                    dirty = true;
                }
            }
        }
        saveBanList();
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
        UserData item = map.get(key);
        if(item == null){
            item = new UserData();
        }
        item.timeStamp = System.currentTimeMillis() + time;
        item.allowConnect = allow;
        item.unknown      = false;
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


    private static final class UserData {
        private long timeStamp;
        private boolean allowConnect = true;
        private boolean unknown      = true;
        private int warn = 0;

        public long timeStamp(){
            return timeStamp;
        }

        public boolean allowConnect(){
            return allowConnect;
        }

        public void set(boolean allowConnect, long time){
            this.allowConnect = allowConnect;
            this.timeStamp = time;
        }

        public void set(boolean allowConnect){
            this.allowConnect = allowConnect;
        }
    }
}
