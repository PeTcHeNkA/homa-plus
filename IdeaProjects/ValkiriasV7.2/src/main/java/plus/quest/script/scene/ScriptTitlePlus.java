package plus.quest.script.scene;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import plus.util.cfg.Sector;


public class ScriptTitlePlus extends ScriptTitle{

    private final ClientboundSetSubtitleTextPacket subTitle;

    public ScriptTitlePlus(Sector s, String path){
        super(s, path);
        subTitle = new ClientboundSetSubtitleTextPacket((IChatBaseComponent) null);
        subTitle.adventure$text = LegacyComponentSerializer.legacyAmpersand().deserialize(
                s.getOrSetString(path+".sub_title", "Subtitle!", true)
        );
    }


    @Override
    public void run(Player pl) {
        CraftPlayer p = (CraftPlayer) pl;
        p.getHandle().b.sendPacket(times);
        p.getHandle().b.sendPacket(title);
        p.getHandle().b.sendPacket(subTitle);
    }
}

