package plus.fserver.world.addon;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;


public final class EmptyTeleporter implements ITeleporter {
    public static final EmptyTeleporter INSTANCE = new EmptyTeleporter();
    private EmptyTeleporter(){}

    @Override
    public void placeEntity(final World world, final Entity entity, final float yaw) {
        entity.rotationYaw = entity.prevRotationYaw = yaw;
    }
}
