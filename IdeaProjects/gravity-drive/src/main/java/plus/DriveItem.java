package plus;

import net.minecraft.item.Item;


public class DriveItem extends Item {
    public static DriveItem INST = new DriveItem();
    public DriveItem() {
        this.setRegistryName("test_i");
        this.setTranslationKey("test_i");
    }
}