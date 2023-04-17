package plus.common.mashine.item.recept;

import net.minecraft.item.ItemStack;


public interface ICraftMatrix {
    ItemStack[] getItems();
    int getSize();
    void takeItem(int index, int count);

    default ReseptKey toKey(IReceptIndexator ind){
        return new ReseptKey(ind, getItems());
    }
}