package plus.fserver.utls.debug;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.ElcaCV;


@SideOnly(Side.CLIENT)
public class MutDetectPos extends BlockPos.MutableBlockPos {
    private Thread thread;
    static {
        if(!ElcaCV.DEBUG_ASYNC_ERRORS)throw new RuntimeException("Async debug is disabled!");
    }

    @Override
    public MutableBlockPos setPos(Vec3i vec) {
        check();
        return super.setPos(vec);
    }
    @Override
    public MutableBlockPos setPos(int xIn, int yIn, int zIn) {
        check();
        return super.setPos(xIn, yIn, zIn);
    }
    @Override
    public void setPosB(int xIn, int yIn, int zIn) {
        check();
        super.setPosB(xIn, yIn, zIn);
    }


    public void lock(){
        thread = Thread.currentThread();
    }


    public void free(){
        thread = null;
    }


    public void check(){
        Thread cur = thread;
        if(cur == null)return;
        if(cur != Thread.currentThread())throw new RuntimeException();
    }


    public static void lock(World world){
        MutDetectPos p1 = (MutDetectPos) world.localTempPos;
        MutDetectPos p2 = (MutDetectPos) world.localTempPos2;
        MutDetectPos p3 = (MutDetectPos) world.localTempPos3;

        p1.lock();
        p2.lock();
        p3.lock();
    }
}