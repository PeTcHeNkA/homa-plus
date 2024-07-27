package plus.space.server.world;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;


public class SolarTeleporter implements ITeleporter {
    private final SolarWorld worldFrom;
    public SolarTeleporter(SolarWorld worldFrom) {
        this.worldFrom = worldFrom;
    }

    @Override
    public void placeEntity(World world, Entity entity, float yaw) {

    }
}