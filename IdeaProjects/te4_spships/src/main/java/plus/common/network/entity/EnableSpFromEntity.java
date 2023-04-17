package plus.common.network.entity;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import plus.common.network.BasicPacket;
import plus.common.player.PlayerSpship;


public class EnableSpFromEntity extends BasicPacket {
    //private static final Minecraft mc = Minecraft.getMinecraft();
    public int spId;
    public int playerId;

    public EnableSpFromEntity(){}

    public EnableSpFromEntity(int spId, int playerId){
        this.spId = spId;
        this.playerId = playerId;
    }


    public static class ClientHandler implements IMessageHandler<EnableSpFromEntity, EnableSpFromEntity> {
        @Override
        public EnableSpFromEntity onMessage(EnableSpFromEntity msg, MessageContext ctx) {
            World world = Minecraft.getMinecraft().player.world;
            Entity entPlayer = world.getEntityByID(msg.playerId);
            if(entPlayer==null)return null;
            EntityPlayer player = (EntityPlayer) entPlayer;

            PlayerSpship sp = ((PlayerSpship) entPlayer);
            Entity entitySp = world.getEntityByID(msg.spId);

            if(entitySp != null){
                sp.onEnableSp();
                player.setPositionAndRotation(
                        entitySp.posX,
                        entitySp.posY,
                        entitySp.posZ,
                        entitySp.rotationYaw,
                        entitySp.rotationPitch
                );
                player.prevPosX = player.posX;
                player.prevPosY = player.posY;
                player.prevPosZ = player.posZ;

//                if(mc.player.getEntityId() == msg.playerId){
                entitySp.setDead();
                sp.setSpMode(true);
//                } else {
//                    new TaskShoulder.Task(10) {
//                        @Override
//                        public void run() {
//                            entitySp.setDead();
//                            sp.setSpMode(true);
//                        }
//                    }.addToQueue();
//                }
            }
            return null;
        }
    }


    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(spId);
        buf.writeInt(playerId);
    }


    @Override
    public void fromBytes(ByteBuf buf) {
        spId     = buf.readInt();
        playerId = buf.readInt();
    }
}