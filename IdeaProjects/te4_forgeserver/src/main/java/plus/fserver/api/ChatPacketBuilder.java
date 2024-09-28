package plus.fserver.api;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import plus.fserver.ElcaCV2;
import plus.fserver.api.placeholders.PlayerVarProvider;
import plus.fserver.server.network.packet.SRawPacketChat;
import plus.tson.format.IVarGetter;
import plus.tson.format.TextFormatter;


public final class ChatPacketBuilder {
    private final IVarGetter<EntityPlayerMP> format;
    private boolean canSendNoPlayer = true;
    private boolean asyncRender = false;

    private ChatPacketBuilder(TextFormatter<EntityPlayerMP> format) {
        this.format = format.simplify();
    }


    public static ChatPacketBuilder of(String text){
        return of(new TextComponentAuto(text));
    }


    public static ChatPacketBuilder of(ITextComponent comp){
        return new ChatPacketBuilder(TextFormatter.compile(
                ITextComponent.Serializer.componentToJson(comp),
                PlayerVarProvider.PROVIDER)
        );
    }


    public static String toJsonMsg(String text){
        return ITextComponent.Serializer.componentToJson(new TextComponentAuto(text));
    }


    public ChatPacketBuilder asyncRender(boolean b){
        asyncRender = b;
        return this;
    }


    public Packet<INetHandlerPlayClient> build(EntityPlayerMP player){
        if(ElcaCV2.LIBBUILD){
            ITextComponent comp = ITextComponent.Serializer.jsonToComponent(format.get(player));
            if(comp == null)comp = new TextComponentString("");
            return new SPacketChat(comp);
        }
        return new SRawPacketChat(format.get(player));
    }


    public String buildJson(EntityPlayerMP player){
        return format.get(player);
    }


    public void send(EntityPlayerMP player){
        if(ElcaCV2.LIBBUILD){
            player.connection.sendPacket(build(player));
            return;
        }
        if(asyncRender){
            player.connection.netManager.exec(
                    () -> player.connection.netManager.sendPacketSync(build(player))
            );
        } else {
            player.connection.netManager.sendPacketE(build(player));
        }
    }


    public void send(ICommandSender sender){
        if(sender instanceof EntityPlayerMP){
            send((EntityPlayerMP) sender);
        } else {
            if(canSendNoPlayer) try {
                send(null);
            } catch (Exception e) {
                e.printStackTrace();
                canSendNoPlayer = false;
            }
        }
    }
}