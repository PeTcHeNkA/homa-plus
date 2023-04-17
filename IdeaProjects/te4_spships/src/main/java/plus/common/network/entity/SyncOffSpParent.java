package plus.common.network.entity;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import plus.client.camera.Camera;
import plus.common.spship.entity.OffSpShip;
import plus.proxy.ServerProxy;


public class SyncOffSpParent implements IMessage {
//    private static final float codeM = 360f/256f; rot = (buf.readByte()+128)*codeE;
//    private static final float codeE = 256f/360f; buf.writeByte((int) ((rot*codeM)-128));
//    float rot;

    public int parent;
    public int entity;

    public SyncOffSpParent(){}


    public static class Handler implements IMessageHandler<SyncOffSpParent, SyncOffSpParent> {
        @Override
        public SyncOffSpParent onMessage(SyncOffSpParent msg, MessageContext ctx) {
            Entity e = ctx.getServerHandler().player.world.getEntityByID(msg.entity);
            if(e==null)return null;
            OffSpShip offSp = (OffSpShip) e;

            offSp.setParent(msg.parent);

            if(Minecraft.getMinecraft().player.getEntityId() == msg.parent) {
                Camera.getSelfSp().setSpMode(false);
            }

            return null;
        }
    }


    public SyncOffSpParent(Entity entity, EntityPlayer parent){
        this.parent = parent.getEntityId();
        this.entity = entity.getEntityId();
    }


    @Override
    public void fromBytes(ByteBuf buf) {
        buf.writeInt(parent);
        buf.writeInt(entity);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        parent = buf.readInt();
        entity = buf.readInt();
    }

    public void send(EntityPlayerMP target){
        ServerProxy.network.sendTo(this, target);
    }
}