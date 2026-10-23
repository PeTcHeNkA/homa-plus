package plus;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import net.minecraft.SystemUtils;
import net.minecraft.network.chat.ChatMessageType;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutChat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.*;
import plus.Motions.CatScene;
import plus.AChat.AChatMessage;
import java.util.ArrayList;
import java.util.List;


public class DebugCmd implements CommandExecutor {
    private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
    private final Camera c;

    public DebugCmd(Camera c){
        this.c = c;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String string, String[] args) {
        Player player = (Player) commandSender;

        if(args[0].equals("test2")){
            send((Player) commandSender, args[1]);

        } else if(args[0].equals("test3")){
            List<String> list = new ArrayList<>();
            list.add(args[1]);
            list.add("@Hxover!");
            list.add("@S/upsssss");

            AChatMessage m = new AChatMessage(list);

            System.out.println(m.toString());
            send((CraftPlayer) commandSender, m.toString());

        } else

        CatScene.runCatScene(args[0], player);



        return true;
    }

    public static void send(Player pl, String message){
        send((CraftPlayer)pl, message);
    }
    public static void send(CraftPlayer pl, String message){
        pl.spigot().sendMessage();
        pl.getHandle().b.sendPacket(
                new PacketPlayOutChat(
                        IChatBaseComponent.ChatSerializer.a(message),
                        ChatMessageType.b,
                        SystemUtils.b
                )
        );
    }


}
