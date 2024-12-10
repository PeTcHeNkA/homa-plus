package plus.fserver.api;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import plus.fserver.ElcaCV2;
import plus.fserver.api.placeholders.PlayerVarProvider;
import plus.fserver.server.network.packet.SRawPacketChat;
import plus.tson.TsonObj;
import plus.tson.format.IVarGetter;
import plus.tson.format.TextFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public final class ChatPacketBuilder {
    private final IVarGetter<IPlayerDataModel> format;
    private boolean canSendNoPlayer = true;
    private boolean asyncRender = false;

    private ChatPacketBuilder(TextFormatter<IPlayerDataModel> format) {
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


    public Packet<INetHandlerPlayClient> build(IPlayerDataModel model){
        if(ElcaCV2.LIBBUILD){
            ITextComponent comp = ITextComponent.Serializer.jsonToComponent(format.get(model));
            if(comp == null)comp = new TextComponentString("");
            return new SPacketChat(comp);
        }
        return new SRawPacketChat(format.get(model));
    }


    public Packet<INetHandlerPlayClient> build(EntityPlayerMP player, String key, String value){
        return build(PlayerVarProvider.model(player, key, value));
    }


    public Packet<INetHandlerPlayClient> build(EntityPlayerMP player, String key1, String value1, String key2, String value2){
        return build(PlayerVarProvider.model(player, key1, value1, key2, value2));
    }


    public Packet<INetHandlerPlayClient> build(EntityPlayerMP player, Map<String,String> model){
        return build(PlayerVarProvider.model(player, model));
    }


    public String buildJson(IPlayerDataModel player){
        return format.get(player);
    }


    public String buildJson(EntityPlayerMP player, String key, String value){
        return buildJson(PlayerVarProvider.model(player, key, value));
    }


    public String buildJson(EntityPlayerMP player, String key1, String value1, String key2, String value2){
        return buildJson(PlayerVarProvider.model(player, key1, value1, key2, value2));
    }


    public String buildJson(EntityPlayerMP player, Map<String,String> model){
        return buildJson(PlayerVarProvider.model(player, model));
    }


    public void send(List<EntityPlayerMP> players){
        send(null, players, true);
    }


    public void send(IPlayerDataModel model, List<EntityPlayerMP> players){
        send(model, players, true);
    }


    public void send(List<EntityPlayerMP> players, boolean copy){
        send(null, players, copy);
    }


    public void send(IPlayerDataModel model, List<EntityPlayerMP> players, boolean copy){
        int size = players.size();
        if(size == 0)return;
        if(size == 1){
            send(players.get(0));
            return;
        }
        if(ElcaCV2.LIBBUILD){
            if(model == null) players.forEach(this::send);
            else {
                Packet<INetHandlerPlayClient> msg = build(model);
                players.forEach(player -> player.connection.sendPacket(msg));
            }
            return;
        }
        if(asyncRender){
            if(copy) players = new ArrayList<>(players);
            List<EntityPlayerMP> finalPlayers = players;

            if(model == null) {
                players.get(0).connection.netManager.exec(() -> {
                    for (EntityPlayerMP player : finalPlayers)
                        player.connection.netManager.sendPacketE(build(player));
                });
            } else {
                players.get(0).connection.netManager.exec(() -> {
                    Packet<INetHandlerPlayClient> msg = build(model);
                    for (EntityPlayerMP player : finalPlayers)
                        player.connection.netManager.sendPacketE(msg);
                });
            }
        } else {
            if(model == null) {
                for (EntityPlayerMP player : players)
                    player.connection.netManager.sendPacketE(build(player));
            } else {
                Packet<INetHandlerPlayClient> msg = build(model);
                if(copy) players = new ArrayList<>(players);
                List<EntityPlayerMP> finalPlayers1 = players;
                players.get(0).connection.netManager.exec(() -> {
                    for (EntityPlayerMP player : finalPlayers1)
                        player.connection.netManager.sendPacketE(msg);
                });
            }
        }
    }


    public static void send(EntityPlayerMP player, Packet<?> packet){
        if(ElcaCV2.LIBBUILD){
            player.connection.sendPacket(packet);
        } else {
            player.connection.netManager.sendPacketE(packet);
        }
    }


    public void send(IPlayerDataModel model){
        EntityPlayerMP player = model.getSelfPlayer();
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


    public void sendToTeam(IPlayerDataModel model){
        EntityPlayerMP player = model.getSelfPlayer();
        Team team = player.getTeam();
        if(team == null){
            send(model);
        } else {
            if(asyncRender){
                player.connection.netManager.exec(() -> {
                    Packet<INetHandlerPlayClient> msg = build(model);
                    sendToTeam(player.world, team, msg);
                });
            } else {
                Packet<INetHandlerPlayClient> msg = build(model);
                player.connection.netManager.exec(() -> sendToTeam(player.world, team, msg));
            }
        }
    }


    private static void sendToTeam(World world, Team team, Packet<INetHandlerPlayClient> msg) {
        for (EntityPlayer player : world.curPlayerEntities){
            if(player.getTeam() == team && player instanceof EntityPlayerMP)
                ((EntityPlayerMP) player).connection.netManager.sendPacketSync(msg);
        }
    }
}