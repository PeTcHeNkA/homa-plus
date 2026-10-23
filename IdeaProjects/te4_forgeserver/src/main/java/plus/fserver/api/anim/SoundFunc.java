package plus.fserver.api.anim;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketCustomSound;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import plus.fserver.utls.McTsonUtils;
import plus.tson.TsonMap;
import static plus.tson.ext.TsonNumUtils.*;
import static plus.tson.ext.TsonBoolUtils.*;


public class SoundFunc implements IRelativeFunc{
    private final String soundName;
    private final SoundCategory category;
    private final double x;
    private final double y;
    private final double z;
    private final float volume;
    private final float pitch;
    private final boolean custom;

    public SoundFunc(TsonMap map){
        soundName = map.getStr("name");
        category  = McTsonUtils.soundCatOf(map.get("category"));
        x = calc(map, "x", 0d);
        y = calc(map, "y", 10000d);
        z = calc(map, "z", 0d);

        volume = calc(map, "volume", 1f);
        pitch  = calc(map, "pith", 1f);
        custom = calc(map, "custom", false);
    }


    @Override
    public void call(double x, double y, double z, EntityPlayerMP player) {
        double fx = this.x + x, fy = this.y + y, fz = this.z + z;
        SoundEvent se = null;
        if(!custom){
            se = SoundEvent.REGISTRY.getObject(new ResourceLocation(soundName));
        }
        if(se == null){
            player.connection.netManager.sendPacketE(new SPacketCustomSound(
                    soundName, category, fx, fy, fz, volume, pitch
            ));
        } else {
            player.connection.netManager.sendPacketE(new SPacketSoundEffect(
                    se, category, fx, fy, fz, volume, pitch
            ));
        }
    }
}