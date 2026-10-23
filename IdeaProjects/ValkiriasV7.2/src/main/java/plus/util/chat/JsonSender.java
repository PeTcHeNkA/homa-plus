package plus.util.chat;

import net.minecraft.SystemUtils;
import net.minecraft.network.chat.ChatMessageType;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class JsonSender {

    public static void send(Player pl, String message){
        send((CraftPlayer)pl, message);
    }
    public static void send(CraftPlayer pl, String message){
        pl.getHandle().b.sendPacket(
                new PacketPlayOutChat(
                        IChatBaseComponent.ChatSerializer.a(message),
                        ChatMessageType.b,
                        SystemUtils.b
                )
        );
    }

}
