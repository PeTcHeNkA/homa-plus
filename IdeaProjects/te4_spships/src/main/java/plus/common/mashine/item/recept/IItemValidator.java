package plus.common.mashine.item.recept;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public interface IItemValidator {
    boolean isEqual(ItemStack input);
    int getCount();
    @SideOnly(Side.CLIENT)
//    default ItemStack preView(){
//        throw new UnsupportedOperationException();
//    };

    @FunctionalInterface
    interface Single extends IItemValidator{
        @Override
        default int getCount() {
            return 1;
        }
    }

    class Strong implements Single{
        private final ItemStack item;
        public Strong(ItemStack item) {
            this.item = item;
        }

        @Override
        public boolean isEqual(ItemStack input) {
            return item.equals(input);
        }
//
//        @Override
//        @SideOnly(Side.CLIENT)
//        public ItemStack preView() {
//            return item;
//        }
    }

    class Basic implements IItemValidator{
        private final Item item;
        private final int count;
        public Basic(ItemStack item) {
            if(item != null && !item.isEmpty()) {
                this.item = item.getItem();
                count = item.getCount();
            } else {
                this.item = Items.AIR;
                count = 0;
            }
        }
        @Override
        public boolean isEqual(ItemStack input) {
            return input.getItem() == item;
        }

        @Override
        public int getCount() {
            //return item == Items.AIR?0:1;
            return count;
        }

//        @Override
//        @SideOnly(Side.CLIENT)
//        public ItemStack preView() {
//            return null;
//        }
    }

    static IItemValidator[] buildBasic(ItemStack[] items, int size){
        items = ReceptBook.normalize(items, size);
        IItemValidator[] vls = new IItemValidator[items.length];
        for(int i=0;i< vls.length;i++){
            vls[i] = new IItemValidator.Basic(items[i]);
        }
        return vls;
    }
}