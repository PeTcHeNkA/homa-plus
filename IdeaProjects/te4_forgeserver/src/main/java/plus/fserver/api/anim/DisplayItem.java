package plus.fserver.api.anim;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import plus.fserver.utls.McTsonUtils;
import plus.tson.TsonMap;
import java.util.concurrent.TimeUnit;
import static plus.tson.ext.TsonNumUtils.*;


public class DisplayItem implements IRelativeFunc{
    private final ItemStack item;
    private final String name;
    private final double x, y, z;
    private final float rx, ry, rz;
    private final int ticks;

    public DisplayItem(TsonMap map){
        x = calc(map, "x", 0d);
        y = calc(map, "y", 0d);
        z = calc(map, "z", 0d);

        rx = calc(map, "rx", 0f);
        ry = calc(map, "ry", 0f);
        rz = calc(map, "rz", 0f);
        ticks = calc(map, "ticks", 200);

        item = McTsonUtils.itemStackOfTson(map.get("item"));
        name = map.getStrSafe("name");
    }


    @Override
    public void call(double x, double y, double z, EntityPlayerMP player) {
        int id = player.world.nextEntityId();
        CameraAnimEngine.VrStand entity = new CameraAnimEngine.VrStand(id);

        entity.set(this.x+x, this.y+y, this.z+z,0,0);
        entity.sendSpawn(player);
        if(rx != 0 && ry != 0 && rz != 0)
            entity.rotateHead(player, rx, ry, rz);
        if(!item.isEmpty())
            entity.sendHead(player, item);
        if(name != null && !name.isEmpty())
            entity.sendName(player, name);

        player.server.ticker().service.schedule(
                () -> entity.sendDespawn(player), ticks*50L, TimeUnit.MILLISECONDS
        );
    }
}