package plus.fserver.api.anim;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import plus.fserver.utls.McTsonUtils;
import plus.tson.TsonFunc;
import plus.tson.TsonList;
import plus.tson.TsonObj;


public class BlockBreakFunc implements TsonFunc {
    private final CameraAnimEngine.MultiPacketSender sender;

    public BlockBreakFunc(TsonList list) {
        CameraAnimEngine.MultiEditBuilder b = new CameraAnimEngine.MultiEditBuilder();
        for (TsonObj obj:list){
            TsonList pos = obj.getList();
            IBlockState state = McTsonUtils.blockOfTson(pos.get(3));
            b.add(pos.getInt(0), pos.getInt(1), pos.getInt(2), state);
        }
        this.sender = b.build();
    }


    @Override
    public Object call(Object... args) {
        return call(args[0]);
    }


    @Override
    public Object call(Object arg) {
        EntityPlayerMP player = (EntityPlayerMP) arg;
        sender.sendAsync(player);
        return null;
    }


    @Override
    public int countArgs() {
        return 1;
    }
}
