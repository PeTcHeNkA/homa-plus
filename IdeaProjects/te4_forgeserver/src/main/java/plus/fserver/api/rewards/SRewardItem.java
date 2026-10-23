package plus.fserver.api.rewards;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import plus.fserver.api.offline.ISafeTask;
import plus.fserver.api.offline.OfflinePlayerMPAccess;
import plus.fserver.api.offline.TaskType;
import plus.fserver.api.vanillagui.ItemGenFunc;


public class SRewardItem implements ISafeTask {
    private final ItemGenFunc func;

    public SRewardItem(ItemGenFunc func) {
        this.func = func;
    }


    @Override
    public TaskType type() {
        return TaskType.SMART;
    }


    @Override
    public void accept(EntityPlayerMP player) {
        ItemStack item = func.create(player);
        if(!player.addItemStackToInventory(item)){
            //todo dont drop item, use mail?
            if(OfflinePlayerMPAccess.isOffline(player)){
                WorldServer world = player.getServerWorld();
                double x = player.posX;
                double y = player.posY + 0.30000001192092896D + (double)player.getEyeHeight();
                double z = player.posZ;

                String name = player.getName();

                world.addSyncTask(() -> {
                    EntityItem eItem = new EntityItem(world, x, y, z, item);
                    eItem.setPickupDelay(40);
                    eItem.setThrower(name);
                    world.spawnEntity(eItem);
                });
            }
        }
    }
}