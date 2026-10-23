package plus.elch.chat;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import plus.elch.ElegantMain;
import plus.elch.chat.group.ChatGroup;
import plus.elch.chat.group.ChatPage;
import plus.elch.event.ElEventListener;
import plus.elch.user.ElData;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;


public class ChatListener implements ElEventListener<PlayerChatEvent> {
    @Override
    public void handle(PlayerChatEvent event) {
        String msg = event.getMessage();
        ChatGroup group = ChatGroup.byChar(msg.charAt(0));
        if(group == null){
            group = ChatGroup.getDefault();
        } else {
            msg = msg.substring(1);
        }
        Player sender;
        ChatPage page = group.getPage(sender = event.getPlayer(), msg);
        if(page == null) return;

        event.setCancelled(true);

        Component cMsg = page.format(sender, msg,
                sender.hasPermission(ElegantMain.ALLOW_EVAL_PERM)
        );

        Iterator<? extends Player> iter = group.getSelector().selectBy(sender);
        ArrayList<UUID> targets = new ArrayList<>();

        while (iter.hasNext()) {
            Player player = iter.next();
            ElData data = ElData.byPlayer(player);
            if(page.canGetMessage(data)) continue;
            targets.add(player.getUniqueId());

            player.sendMessage(cMsg);
        }

        ElMessage elMsg = ElegantMain.BUS.create(sender, msg, targets.toArray(new UUID[0]));


    }
}