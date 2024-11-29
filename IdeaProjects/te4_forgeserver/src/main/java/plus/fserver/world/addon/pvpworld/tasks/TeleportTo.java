package plus.fserver.world.addon.pvpworld.tasks;

import net.minecraft.entity.player.EntityPlayerMP;
import plus.fserver.world.addon.LobbyTeleporter;
import java.util.function.Consumer;


public class TeleportTo extends LobbyTeleporter implements Consumer<EntityPlayerMP> {
    private final Integer dimTo;

    public TeleportTo(Integer dimTo) {
        this.dimTo = dimTo;
    }

    @Override
    public void accept(EntityPlayerMP player) {
        super.placeEntity(player.world, player, player.rotationYaw);
        if(dimTo != null) {
            if(player.world.provider.getDimension() != dimTo.intValue()){
                player.changeDimension(dimTo.intValue());
            }
        }
    }
}