package plus.client;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.asyutl.shoulder.TaskConsumer;


@SideOnly(Side.CLIENT)
public class CTaskShoulder extends TaskConsumer{
    public static final CTaskShoulder INST = new CTaskShoulder();
    @Override
    public void tick() {
        super.tick();
    }
}