package plus.fserver.api.anim;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.MathHelper;
import plus.tson.TsonList;


public class RelBlockBreakFunc implements IRelativeFunc {
    private final CameraAnimEngine.BlockEditEntry[] entries;

    public RelBlockBreakFunc(TsonList list){
        CameraAnimEngine.BlockEditEntry[] entries = this.entries
                = new CameraAnimEngine.BlockEditEntry[list.size()];

        for (int i = 0; i<entries.length; i++){
            entries[i] = new CameraAnimEngine.BlockEditEntry(list.getList(i));
        }
    }


    @Override
    public void call(double x, double y, double z, EntityPlayerMP player) {
        CameraAnimEngine.MultiEditBuilder b = new CameraAnimEngine.MultiEditBuilder();

        int ix = MathHelper.floor(x), iy = MathHelper.floor(y), iz = MathHelper.floor(z);

        for (CameraAnimEngine.BlockEditEntry entry:entries){
            b.add(entry.x+ix, entry.y+iy, entry.z+iz, entry.state);
        }

        b.build().sendSync(player);
    }
}