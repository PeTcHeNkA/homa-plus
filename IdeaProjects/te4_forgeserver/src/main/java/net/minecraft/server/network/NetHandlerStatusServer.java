package net.minecraft.server.network;

import io.netty.channel.Channel;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.status.INetHandlerStatusServer;
import net.minecraft.network.status.client.CPacketPing;
import net.minecraft.network.status.client.CPacketServerQuery;
import net.minecraft.network.status.server.SPacketPong;
import net.minecraft.network.status.server.SPacketServerInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import plus.fserver.server.network.ElcaFireWall;


public class NetHandlerStatusServer implements INetHandlerStatusServer {
    private static final ITextComponent EXIT_MESSAGE = new TextComponentString("Status handled.");
    private final MinecraftServer server;
    private final NetworkManager networkManager;
    private boolean handled;

    public NetHandlerStatusServer(MinecraftServer server, NetworkManager manager) {
        this.server = server;
        this.networkManager = manager;
    }


    @Override
    public void onDisconnect(ITextComponent conmp) {}


    @Override
    public void processServerQuery(CPacketServerQuery query) {
        if (this.handled) {
            this.networkManager.closeChannel(EXIT_MESSAGE);
        } else {
            this.handled = true;
            this.networkManager.sendPacket(new SPacketServerInfo(this.server.getServerStatusResponse()));
        }
    }


    @Override
    public void processPing(CPacketPing ping) {
        ElcaFireWall<Channel> firewall = server.getNetworkSystem().fireWall();
        if(firewall == null || !firewall.softWarn(networkManager.channel(), 500)){
            this.networkManager.sendPacket(new SPacketPong(ping.getClientTime()));
        }
        this.networkManager.closeChannel(EXIT_MESSAGE);
    }
}