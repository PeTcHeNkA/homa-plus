package plus.motd;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import plus.main;

import java.util.UUID;


public class Pinger {

    public final ServerPing.Builder ping;


    public Pinger(String s, String lore){


        ping = new ServerPing(
                main.server.get


                main.server.getConfiguration().getFavicon().get()
        ).asBuilder();

        ServerPing.SamplePlayer player1 = new ServerPing.SamplePlayer("name", UUID.randomUUID());
    }





    @Subscribe
    public EventTask onProxyPingEvent(ProxyPingEvent e) {
        return EventTask.async(() -> build(e)
        );
    }


    public void build(ProxyPingEvent e){
        //final ServerPing.Builder ping = e.getPing().asBuilder(

        ping.samplePlayers(
                player1
        );


        e.setPing(ping.build());
    }



}