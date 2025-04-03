package plus.elch.user;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.entity.Player;
import plus.elch.utl.IoUtils;
import plus.tson.utl.Te4HashMap;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;


public class ElData{
    public static final Te4HashMap<UUID,ElData> DATA = new Te4HashMap<>();
    private long bits = 0;
    private Map<String,String> customData;
    private boolean dirty = false;

    public boolean getBit(int bit){
        return (bits & (1L << bit)) != 0;
    }


    public void setBit(int bit, boolean val){
        bits = (bits & ~(1L << bit)) | (val ? 1L << bit : 0);
        dirty = true;
    }


    public String getCustom(String key){
        if(customData == null)return null;
        return customData.get(key);
    }


    public void setCustom(String key, String val){
        if(customData == null)
            customData = new Object2ObjectOpenHashMap<>(4);
        customData.put(key, val);
        dirty = true;
    }


    public boolean isDirty(){
        return dirty;
    }


    public void read(InputStream in) throws Exception{
        bits = IoUtils.readLong(in);
        int cLen = IoUtils.readShort(in);
        if(cLen > 0){
            Te4HashMap<String,String> map = new Te4HashMap<>(cLen);
            customData = map;
            for(int i = 0; i < cLen; i++){
                String key = IoUtils.readShortString(in);
                String val = IoUtils.readShort2String(in);
                map.put(key, val);
            }
        }
    }


    public void write(OutputStream out) throws Exception{
        IoUtils.writeLong(out, bits);
        Map<String,String> map;
        if((map = customData) != null && !map.isEmpty()) {
            IoUtils.writeShort(out, map.size());
            map.forEach((s, s2) -> {
                try {
                    IoUtils.writeShortString(out, s);
                    IoUtils.writeShort2String(out, s2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }


    public static ElData byPlayer(Player player){
        return byUUID(player.getUniqueId());
    }


    public static ElData byUUID(UUID uuid){
        ElData data = DATA.get(uuid);
        if(data == null)
            DATA.fput(uuid, data = createOrReadData(uuid));
        return data;
    }


    public static ElData createOrReadData(UUID uuid){
        return new ElData();
    }
}