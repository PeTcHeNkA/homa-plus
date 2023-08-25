package plus.common.mashine.item.recept;

import net.minecraft.item.ItemStack;


public interface ICraftMatrix {
    ItemStack[] getItems();
    int getSize();
    void takeItem(int index, int count);
    ItemStack getCurResult();
    void editResult(ItemStack result);

    default ReseptKey toKey(IReceptIndexator ind){
        return new ReseptKey(ind, getItems());
    }
}