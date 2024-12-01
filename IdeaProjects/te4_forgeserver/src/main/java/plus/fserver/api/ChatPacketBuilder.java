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
import plus.tson.TsonObj;
import plus.tson.format.IVarGetter;
import plus.tson.format.TextFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public final class ChatPacketBuilder {
    private final IVarGetter<EntityPlayerMP> format;
    private boolean canSendNoPlayer = true;
    private boolean asyncRender = false;

    private ChatPacketBuilder(TextFormatter<EntityPlayerMP> format) {
        this.format = format.simplify();
    }


    public static ChatPacketBuilder of(TsonObj obj){
        ChatPacketBuilder builder = of(TextComponentAuto.compFromTson(obj));
        if(obj.isMap()){
            if(obj.getMap().getOrDefaultBool("async", false))
                builder.asyncRender(true);
        }
        return builder;
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


    public void send(List<EntityPlayerMP> players){
        send(players, true);
    }


    public void send(List<EntityPlayerMP> players, boolean copy){
        int size = players.size();
        if(size == 0)return;
        if(size == 1){
            send(players.get(0));
            return;
        }
        if(ElcaCV2.LIBBUILD){
            players.forEach(this::send);
            return;
        }
        if(asyncRender){
            if(copy)players = new ArrayList<>(players);
            List<EntityPlayerMP> finalPlayers = players;

            players.get(0).connection.netManager.exec(() -> {
                for (EntityPlayerMP player: finalPlayers){
                    player.connection.netManager.sendPacketE(build(player));
                }
            });
        } else {
            for (EntityPlayerMP player:players){
                player.connection.netManager.sendPacketE(build(player));
            }
        }
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
                send((EntityPlayerMP)null);
            } catch (Exception e) {
                e.printStackTrace();
                canSendNoPlayer = false;
            }
        }
    }
}