package plus.pro;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class Events implements Listener {
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        event.getPlayer().sendMessage("" +
                "MORECODEMORECODEMORECO" +
                "MORECODEMORECODEMORECO"
                +"MORECODE1MORECODEMORECO" +
                "MORECODEMORE1DEMORECO" +
                "MORE1CODEMORE1DEMORECO" +
                "MORE1CODEMORE1DEMORECO" +
                "MORE1CODEMORE1DEMORECO"
                +"MORECODE1MORECODEMORECO" +
                "MORECODEMO1RECODEMORECO" +
                "DEMORECODEMORECODEMORECODE"
        );
    }
}