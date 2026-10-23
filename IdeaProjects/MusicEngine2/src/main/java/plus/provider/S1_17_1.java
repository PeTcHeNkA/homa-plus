package plus.provider;

import net.minecraft.network.protocol.game.PacketPlayOutCustomSoundEffect;
import net.minecraft.network.protocol.game.PacketPlayOutStopSound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;


public class S1_17_1 extends SoundProvider{

    private final Vec3D loc = new Vec3D( 0, 1000000000000000d, 0);

    @Override
    public void playBgSound(Player p, String sound, String category, float pitch) {
        PacketPlayOutCustomSoundEffect packet = new PacketPlayOutCustomSoundEffect(
                new MinecraftKey(sound),
                net.minecraft.sounds.SoundCategory.valueOf(category), loc,
                1000000000000000000f, pitch
        );
        ((CraftPlayer) p).getHandle().b.sendPacket(packet);
    }


    @Override
    public void playNormalSound(Player p, Location loc, String sound, String category, float volume, float pitch) {
        PacketPlayOutCustomSoundEffect packet = new PacketPlayOutCustomSoundEffect(
                new MinecraftKey(sound), net.minecraft.sounds.SoundCategory.valueOf(category),
                new Vec3D( loc.getX(), loc.getY(), loc.getZ()
                ),  volume, pitch
        );
        ((CraftPlayer) p).getHandle().b.sendPacket(packet);
    }


    @Override
    public void StopSound(Player p, String sound, String category) {
        PacketPlayOutStopSound packet = new PacketPlayOutStopSound(
                new MinecraftKey(sound), net.minecraft.sounds.SoundCategory.valueOf(category)
        );


        ((CraftPlayer) p).getHandle().b.sendPacket(packet);
    }
}