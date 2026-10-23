package plus.space.common.net;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class CallBackMsg implements IMessage {
    private int id;
    private int mtd;
    private String data;

    public CallBackMsg() {
    }

    public CallBackMsg(int id, int mtd, String data) {
        this.id = id;
        this.mtd = mtd;
        this.data = data;
    }


    @Override
    public void fromBytes(ByteBuf buf) {
        mtd = buf.readByte();
        id = buf.readInt();
        data = ByteBufUtils.readUTF8String(buf);
    }


    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(mtd);
        buf.writeInt(id);
        ByteBufUtils.writeUTF8String(buf, data==null?"":data);
    }

    public int getId() {
        return id;
    }

    public int getMtd() {
        return mtd;
    }

    public String getData() {
        return data;
    }

    public CallBackMsg reply(String data) {
        this.data = data;
        return this;
    }
}
