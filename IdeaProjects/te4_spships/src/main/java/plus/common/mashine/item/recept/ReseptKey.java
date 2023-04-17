package plus.common.mashine.item.recept;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class ReseptKey {
    private final ItemStack[] realItems;
    private final ItemStack[] items;
    private final int hashCode;

    public static int defHashCalc(ItemStack[] items){
        int result = 0;
        for(ItemStack item:items){
            result = 31 * result + (item.isEmpty()?0:calcItemHash(item));
        }
        return result;
    }

    public static int calcItemHash(ItemStack item){
        return item.getItem().getRegistryName().hashCode()+31*item.getCount();
    }

    public static boolean eqItemArray(ItemStack[] arr1, ItemStack[] arr2){
        if(arr1.length == arr2.length){
            for(int i = 0;i<arr1.length;i++){
                if(!eqItemStack(arr1[i], arr2[i])){
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }


    public static boolean eqItemStack(ItemStack item, ItemStack other){
        if(item == other || item.isEmpty() == other.isEmpty())return true;
        if(item.getItem() == other.getItem()) {
            if (item.getCount() == other.getCount()){
                NBTTagCompound tag = item.getTagCompound();
                NBTTagCompound tag2 = other.getTagCompound();
                if(tag == null){
                    return tag2 == null;
                } else if(tag2 == null){
                    return false;
                }
                return tag.equals(tag2);
            }
        }
        return false;
    }


    public ReseptKey(IReceptIndexator ind, ItemStack... items){
        this.realItems = items;
        this.items = new ItemStack[items.length];
        for(int i=0;i<items.length;i++){
            ItemStack temp = items[i].copy();
            temp.setCount(1);
            this.items[i] = temp;
        }
        hashCode = ind.calcHash(items);
    }


    public int getSize(){
        return realItems.length;
    }


    public ItemStack[] getItems(){
        return realItems;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReseptKey that = (ReseptKey) o;
        return eqItemArray(items, that.items);
    }


    @Override
    public int hashCode() {
        return hashCode;
    }
}