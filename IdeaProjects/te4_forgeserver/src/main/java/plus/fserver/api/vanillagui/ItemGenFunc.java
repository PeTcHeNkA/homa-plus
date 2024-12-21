package plus.fserver.api.vanillagui;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import plus.format.IVarGetter;
import plus.fserver.api.IPlayerDataModel;
import plus.fserver.api.placeholders.PlayerVarProvider;
import plus.fserver.utls.McTsonUtils;
import plus.tson.TsonFunc;
import plus.tson.TsonObj;
import java.io.*;
import java.nio.charset.StandardCharsets;


public class ItemGenFunc implements TsonFunc {
    protected final IVarGetter<IPlayerDataModel> format;
    private final ItemStack constResult;

    public ItemGenFunc(TsonObj item) {
        try {
            IVarGetter<IPlayerDataModel> format = createOfItem(item);
            if(format.isConst()){
                this.constResult = create(format, null);
                this.format = null;
            } else {
                this.format = format;
                this.constResult = null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static IVarGetter<IPlayerDataModel> createOfItem(TsonObj obj) throws Exception{
        NBTTagCompound nbt = McTsonUtils.itemStackOfTson(obj).writeToNBT(new NBTTagCompound());

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(bytes);

        CompressedStreamTools.write(nbt, dataOut);

        return PlayerVarProvider.buildFormat(
                new String(bytes.toByteArray(), StandardCharsets.UTF_8)
        );
    }


    public ItemStack create(IPlayerDataModel model){
        ItemStack result;
        if((result = this.constResult) == null)return create(format, model);
        return result.copy();
    }


    private static ItemStack create(IVarGetter<IPlayerDataModel> format, IPlayerDataModel model){
        byte[] bytes0 = format.getStr(model).getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream isBytes = new ByteArrayInputStream(bytes0);
        DataInputStream is = new DataInputStream(isBytes);
        try {
            return new ItemStack(CompressedStreamTools.read(is));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Object call(Object... args) {
        return call(args[0]);
    }


    @Override
    public Object call(Object arg) {
        return create((IPlayerDataModel) arg);
    }


    @Override
    public int countArgs() {
        return 1;
    }
}