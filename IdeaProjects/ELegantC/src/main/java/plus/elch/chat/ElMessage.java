package plus.elch.chat;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import plus.elch.utl.FastExitException;
import plus.elch.utl.IoUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class ElMessage {
    private UUID sender;
    private int dim, posX, posY, posZ;
    private byte yaw, pith;
    private Object targets; //UUID or UUID[]
    private String message;
    private long selfId;
    private long time;

    public ElMessage(){}


    public ElMessage(ElMessageBus bus, Player player, String message){
        selfId = bus.nextId();
        sender = player.getUniqueId();
        time = System.currentTimeMillis();

        dim = player.getWorld().getEnvironment().getId();

        Location pos = player.getLocation();
        posX = (int)pos.getX();
        posY = (int)pos.getY();
        posZ = (int)pos.getZ();

        yaw =  (byte)(pos.getYaw() / 1.40625f);
        pith = (byte)(pos.getPitch() / 1.40625f);

        this.message = message;
    }


    public ElMessage setTargets(UUID... targets){
        if(targets.length == 0){
            this.targets = null;
            return this;
        }
        if(targets.length == 1){
            this.targets = targets[0];
            return this;
        }
        this.targets = targets;
        return this;
    }


    public ElMessage setTargets(UUID target){
        this.targets = target;
        return this;
    }


    public ElMessage setMessage(String message){
        this.message = message;
        return this;
    }


    public ElMessage setId(long id){
        selfId = id;
        return this;
    }


    public UUID sender(){
        return sender;
    }


    public int getX(){
        return posX;
    }


    public int getY(){
        return posY;
    }


    public int getZ(){
        return posZ;
    }


    public int getDim(){
        return dim;
    }


    public float getYaw(){
        return yaw * 1.40625f;
    }


    public float getPitch(){
        return pith * 1.40625f;
    }


    public String getMessage() {
        return message;
    }


    public long getTime() {
        return time;
    }


    public long getId() {
        return selfId;
    }


    public int countTargets(){
        Object targets;
        if((targets = this.targets) == null)return 0;
        if(targets instanceof UUID)return 1;
        return ((UUID[])targets).length;
    }


    public void read(InputStream stream) throws FastExitException, IOException {
        sender = IoUtils.readUUID(stream);
        time   = IoUtils.readLong(stream);
        dim    = IoUtils.readInt(stream);
        posX   = IoUtils.readInt(stream);
        posY   = IoUtils.readInt(stream);
        posZ   = IoUtils.readInt(stream);

        yaw    = (byte) IoUtils.readByte(stream);
        pith   = (byte) IoUtils.readByte(stream);

        int countTar = IoUtils.readShort(stream);
        if(countTar > 0) {
            if (countTar == 1) {
                targets = IoUtils.readUUID(stream);
            } else {
                UUID[] ids = new UUID[countTar];
                for (int i = 0; i < ids.length; i++)
                    ids[i] = IoUtils.readUUID(stream);
                targets = ids;
            }
        }

        message = IoUtils.readShort2String(stream);
    }


    public void write(OutputStream stream) throws IOException {
        IoUtils.writeUUID(stream, sender);
        IoUtils.writeLong(stream, time);
        IoUtils.writeInt(stream , dim);
        IoUtils.writeInt(stream , posX);
        IoUtils.writeInt(stream , posY);
        IoUtils.writeInt(stream , posZ);

        IoUtils.writeByte(stream, yaw);
        IoUtils.writeByte(stream, pith);

        int countTar = countTargets();
        IoUtils.writeShort(stream, countTar);
        if(countTar > 0) {
            if (countTar == 1) {
                IoUtils.writeUUID(stream, (UUID)targets);
            } else {
                UUID[] ids = (UUID[])targets;
                for (UUID id : ids)
                    IoUtils.writeUUID(stream, id);
            }
        }

        IoUtils.writeShort2String(stream, message);
    }


    public int calcSize(){
        //uuid          = 8 + 8
        //time          = 8
        //dim           = 4
        //posX          = 4
        //posY          = 4
        //posZ          = 4
        //yaw           = 1
        //pith          = 1
        //countTar      = 2
        //message(head) = 2
        //message(body) = message.length()
        return 46 + message.length();
    }
}