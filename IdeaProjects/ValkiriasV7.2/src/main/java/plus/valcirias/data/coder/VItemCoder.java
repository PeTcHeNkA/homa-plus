package plus.valcirias.data.coder;

import org.bukkit.inventory.ItemStack;
import plus.item.BaseInfo;
import plus.item.BoostStats;
import plus.Main;


public class VItemCoder implements AbstractItemCoder {

    public static final String s = " qwertyuiopasdfghjklzxcvbnm1234567890QWERTYUIOPASDFGHJKLZXCVBNM#$";

    @Override
    public ItemStack encode(String value) {
        if(value.isEmpty())return null;

        if(value.contains("=")){
            String[] args = value.split("=");
            BaseInfo info = new BaseInfo(args[0]);

            if(info.TYPE == 6){
                int amount = s.indexOf(value.charAt(0));

                ItemStack item = Main.generator.getNIItem(info, args[1]);
                if(amount>1){
                    item.setAmount(amount);
                }
                return item;
            }

            if(info.TYPE == 7) {
                int amount = s.indexOf(value.charAt(0));
                ItemStack item = Main.generator.getNIBox(info, args[1]);
                if(amount>1){
                    item.setAmount(amount);
                }
                return item;
            }

            else return Main.generator.getBoostable(
                        info,
                        args[1].isEmpty()?null:new BoostStats(args[1])
                );
        } else {
            int amount = s.indexOf(value.charAt(0));
            if(amount>1) {
                ItemStack result = Main.generator.buildStandartItem(value.substring(1));
                result.setAmount(amount);
                return result;
            }else return Main.generator.buildStandartItem(value.substring(1));
        }
    }


    @Override
    public String code(ItemStack item) {
        BaseInfo info = Main.generator.getBaseInfo(item);
        if(info==null)return "";
        if(info.TYPE == 1 || info.TYPE == 2){
            return info+"="+Main.generator.getBoostStats(item).toString();
        } else {
            if(info.TYPE == 6 || info.TYPE == 7){
                return s.charAt(item.getAmount())+info.toString()+"="+Main.generator.getNITagValue(item);
            }
            return s.charAt(item.getAmount())+info.toString();
        }
    }
}