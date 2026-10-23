package plus.versions;

import net.minecraft.network.protocol.game.PacketPlayOutCustomSoundEffect;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import plus.quest.motion.BasicPos;
import plus.quest.script.scene.ScriptPlayMusic;


public class SoundPlayer1_17 extends SoundPlayer{
    @Override
    public void play(ScriptPlayMusic s, Player player, BasicPos pos) {

        PacketPlayOutCustomSoundEffect packet = new PacketPlayOutCustomSoundEffect(
                new MinecraftKey(s.Music),
                net.minecraft.sounds.SoundCategory.valueOf(s.Category), new Vec3D(pos.X, pos.Y, pos.Z),
                s.volume, s.pith
        );
        ((CraftPlayer) player).getHandle().b.sendPacket(packet);
    }
}
