package plus.fserver.api.anim;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraft.util.EnumParticleTypes;
import plus.fserver.utls.McTsonUtils;
import plus.tson.TsonMap;
import static plus.tson.ext.TsonNumUtils.*;
import static plus.tson.ext.TsonBoolUtils.*;


public class ParticlesFunc implements IRelativeFunc{
    private final EnumParticleTypes particleType;
    private final float xCoord;
    private final float yCoord;
    private final float zCoord;
    private final float xOffset;
    private final float yOffset;
    private final float zOffset;
    private final float particleSpeed;
    private final int particleCount;
    private final boolean longDistance;
    private final int[] particleArguments;

    public ParticlesFunc(TsonMap map) {
        particleType = McTsonUtils.particleTypeOf(map.get("type"));
        xCoord = calc(map, "x", 0.0f);
        yCoord = calc(map, "y", 0.0f);
        zCoord = calc(map, "z", 0.0f);

        xOffset = calc(map, "xo", 0.0f);
        yOffset = calc(map, "yo", 0.0f);
        zOffset = calc(map, "zo", 0.0f);

        particleSpeed = calc(map, "speed", 0.5f);
        particleCount = calc(map, "count", 1);
        longDistance  = calc(map, "long", false);

        if(map.containsKey("args"))
            particleArguments = intArr(map.getList("args"));
        else
            particleArguments = new int[0];
    }


    @Override
    public void call(double x, double y, double z, EntityPlayerMP player) {
        player.connection.netManager.sendPacketE(new SPacketParticles(
                particleType, longDistance,
                (float) (xCoord+x), (float) (yCoord+y), (float) (zCoord+z),
                xOffset, yOffset, zOffset, particleSpeed, particleCount,
                particleArguments
        ));
    }
}