package plus.common.mashine.item;

import net.minecraft.item.ItemStack;


public class ItemMssCapacitor extends ItemCapacitor {
    public static ItemCapacitor BATTERY_L1 = new ItemCapacitor("battery_l1", 1, 2000);
    public static ItemCapacitor BATTERY_L2 = new ItemCapacitor("battery_l2", 2, 3500);
    public static ItemCapacitor BATTERY_L3 = new ItemCapacitor("battery_l3", 3, 5700);
    public static ItemCapacitor BATTERY_L4 = new ItemCapacitor("battery_l4", 4, 7200);

    public static ItemMssCapacitor MASSIVE_L1
            = new ItemMssCapacitor("massive_l1", 1, 12500);
    public static ItemMssCapacitor MASSIVE_L2
            = new ItemMssCapacitor("massive_l2", 2, 18000);
    public static ItemMssCapacitor MASSIVE_L3
            = new ItemMssCapacitor("massive_l3", 3, 25000);
    public static ItemMssCapacitor MASSIVE_L4
            = new ItemMssCapacitor("massive_l4", 4, 32000);

    public static BasicItem MASSIVE_FRAME_L1 = new BasicItem("massive_frame_l1");
    public static BasicItem MASSIVE_FRAME_L2 = new BasicItem("massive_frame_l2");


    protected ItemMssCapacitor(String name, int lvl, int basicCap) {
        super(name, lvl, basicCap);
    }


    @Override
    public int getMaxDur(){
        return 8000;
    }


    @Override
    public ItemStack create(float damage, float power, int tehProcess) {
        return super.create(damage, power, tehProcess);
    }
}