package plus.fserver.api.bind;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import plus.fserver.utls.NbtUtils;
import plus.tson.*;
import java.io.File;


public class ItemBind {
    public static final String ITEM_BIND_ID = "\\tBIND";
    private static final Int2ObjectOpenHashMap<Func> REGISTRY = new Int2ObjectOpenHashMap<>();

    public interface Func{
        EnumActionResult call(ItemStack stack, EntityPlayer playerIn, EnumHand hand);
    }


    public static void bindFunc(int id, ItemStack stack){
        if(stack.isEmpty())return;
        stack.setTagInfo(ITEM_BIND_ID, NbtUtils.create(id));
    }


    public static EnumActionResult call(int id, ItemStack stack, EntityPlayer playerIn, EnumHand hand){
        if(id == 0)return null;
        Func func = REGISTRY.get(id);
        if(func == null)return null;
        return func.call(stack, playerIn, hand);
    }


    public static synchronized boolean isRegistred(int id){
        return REGISTRY.containsKey(id);
    }


    public static synchronized void register(int id, Func func){
        REGISTRY.put(id, func);
    }


    public static void load(){
        load(new File("item-binds.tson"));
    }


    public static void load(File file){
        String data = TsonFile.read(file, "{}");
        if(data == null)return;
        data = TsonFile.remBadChars(data);
        STsonParser parser = new STsonParser(data);

        TsonObj bind = parser.getMap().get("binds");
        if(bind == null)return;
        loadFrom(bind.getList());
    }


    public static synchronized void loadFrom(TsonList binds){
        for (TsonObj obj:binds){
            TsonMap map = obj.getMap();
            int id = map.getInt("id");
            if(id != 0) register(id, map.getCustom("func"));
        }
    }
}