package plus.common.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import plus.proxy.ServerProxy;


public abstract class BasicPacket implements IMessage {
    public void sendToServer(){
        ServerProxy.network.sendToServer(this);
    }


    public void sendTo(EntityPlayer player){
        sendTo((EntityPlayerMP) player);
    }


    public void sendTo(EntityPlayerMP player){
        ServerProxy.network.sendTo(this, player);
    }
}