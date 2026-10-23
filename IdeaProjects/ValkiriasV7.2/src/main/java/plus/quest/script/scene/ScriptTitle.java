package plus.quest.script.scene;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import plus.util.cfg.Sector;


public class ScriptTitle implements UniversalScript {

    protected final ClientboundSetTitleTextPacket title;
    protected final ClientboundSetTitlesAnimationPacket times;

    public ScriptTitle(Sector s, String path){

        title = new ClientboundSetTitleTextPacket((IChatBaseComponent) null);
        title.adventure$text = LegacyComponentSerializer.legacyAmpersand().deserialize(
                s.getOrSetString(path+".title", "Hello world!", true)
        );

        String[] cs = s.getOrSetString(path+".times", "10;10;10").split(";");
        times = new ClientboundSetTitlesAnimationPacket(
                Integer.parseInt(cs[0]),
                Integer.parseInt(cs[1]),
                Integer.parseInt(cs[2])
        );
    }


    @Override
    public void run(Player pl) {
        CraftPlayer p = (CraftPlayer) pl;
        p.getHandle().b.sendPacket(times);
        p.getHandle().b.sendPacket(title);
    }
}