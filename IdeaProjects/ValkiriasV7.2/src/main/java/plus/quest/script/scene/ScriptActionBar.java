package plus.quest.script.scene;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import plus.util.cfg.Sector;


public class ScriptActionBar implements UniversalScript{

    private final ClientboundSetActionBarTextPacket packet;

    public ScriptActionBar(Sector s, String path) {
        packet = new ClientboundSetActionBarTextPacket((IChatBaseComponent) null);
        packet.adventure$text = LegacyComponentSerializer.legacyAmpersand().deserialize(
                        s.getOrSetString(path+".message", "action_bar", true)
        );
    }

    @Override
    public void run(Player pl) {
        CraftPlayer p = (CraftPlayer) pl;
        p.getHandle().b.sendPacket(packet);
    }
}
