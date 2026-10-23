package plus.craft;

import org.bukkit.inventory.ItemStack;
import plus.item.BaseInfo;
import plus.Main;


public class CraftItem implements CraftThing{

    private final ItemStack item;
    protected final BaseInfo info;

    public CraftItem(String uid){
        if(uid.contains("*")){
            String[] cs = uid.split("\\*");
            uid = cs[0];
            int count = 0;
            try { count = Integer.parseInt(cs[1]); } catch (Exception ignored){}
            item = Main.generator.buildStandartItem(uid);
            item.setAmount(count);
        } else {
            item = Main.generator.buildStandartItem(uid);
        }
        info = new BaseInfo(uid);
    }


    @Override
    public ItemStack toItem(){
        return item;
    }


    @Override
    public ItemStack view() {
        return item;
    }


    public boolean isSuccessful(ItemStack item) {
        if(item.getAmount()>=this.item.getAmount())
        return info.equals(Main.generator.getBaseInfo(item));
        else return false;
    }
}