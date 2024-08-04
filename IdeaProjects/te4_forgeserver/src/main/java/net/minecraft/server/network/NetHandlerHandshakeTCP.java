package net.minecraft.server.network;

import io.netty.channel.Channel;
import net.minecraft.network.handshake.*;
import net.minecraft.server.*;
import net.minecraft.network.handshake.client.*;
import net.minecraftforge.fml.common.*;
import net.minecraft.network.login.server.*;
import net.minecraft.network.*;
import net.minecraft.util.text.*;
import plus.fserver.server.network.ElcaFireWall;


public class NetHandlerHandshakeTCP implements INetHandlerHandshakeServer {
    private final MinecraftServer server;
    private final NetworkManager networkManager;

    public NetHandlerHandshakeTCP(final MinecraftServer serverIn, final NetworkManager netManager) {
        this.server = serverIn;
        this.networkManager = netManager;
    }


    @Override
    public void processHandshake(final C00Handshake handshake) {
        if (!FMLCommonHandler.instance().handleServerHandshake(handshake, this.networkManager)) {
            try {
                server.getNetworkSystem().fireWall().softWarn(networkManager.channel(), 500);
            } catch (Exception e){
                e.printStackTrace();
            }
            return;
        }
        switch (handshake.getRequestedState()) {
            case LOGIN: {
                this.networkManager.setConnectionState(EnumConnectionState.LOGIN);
                if (handshake.getProtocolVersion() > 340) {
                    blockAllConnections(15_000);
                    final ITextComponent comp = new TextComponentTranslation("multiplayer.disconnect.outdated_server", new Object[] { "1.12.2" });
                    this.networkManager.sendPacket(new SPacketDisconnect(comp));
                    this.networkManager.closeChannel(comp);
                    break;
                }
                if (handshake.getProtocolVersion() < 328) {
                    blockAllConnections(15_000);
                    final ITextComponent comp = new TextComponentTranslation("multiplayer.disconnect.outdated_client", new Object[] { "1.12.2" });
                    this.networkManager.sendPacket(new SPacketDisconnect(comp));
                    this.networkManager.closeChannel(comp);
                    break;
                }
                this.networkManager.setNetHandler(new NetHandlerLoginServer(this.server, this.networkManager));
                break;
            }
            case STATUS: {
                this.networkManager.setConnectionState(EnumConnectionState.STATUS);
                this.networkManager.setNetHandler(new NetHandlerStatusServer(this.server, this.networkManager));
                break;
            }
            default: {
                this.networkManager.closeChannel(new TextComponentString("Invalid intention: "+handshake.getRequestedState()));
                blockAllConnections(15_000);
                break;
            }
        }
    }


    public void onDisconnect(final ITextComponent reason) {}


    public void blockAllConnections(long time){
        ElcaFireWall<Channel> firewall = server.getNetworkSystem().fireWall();
        if(firewall == null)return;
        if(!firewall.hasRule(networkManager.channel(), true)){
            firewall.softSet(networkManager.channel(), time, false);
        }
    }
}