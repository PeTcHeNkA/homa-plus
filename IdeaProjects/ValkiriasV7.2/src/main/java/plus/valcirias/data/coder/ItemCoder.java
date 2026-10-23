package plus.valcirias.data.coder;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;


public class ItemCoder implements AbstractItemCoder {

    private final AbstractItemCoder c;

    public ItemCoder(AbstractItemCoder coder){
        c = coder;
    }


    public String codeItems(ItemStack[] itemStacks){
        if(itemStacks != null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < itemStacks.length; i++) {
                if (i > 0) {
                    stringBuilder.append(";");
                }
                if ((itemStacks[i] != null) && (itemStacks[i].getType() != Material.AIR)) {
                    try {
                        stringBuilder.append(code(itemStacks[i]));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return stringBuilder.toString();
        }
        return null;
    }


    public ItemStack[] decodeItems(String data) {
        String[] strings = data.split(";");
        ItemStack[] itemStacks = new ItemStack[strings.length];

        for (int i = 0; i < strings.length; i++) {
            if (!strings[i].equals("")) {
                try {
                    itemStacks[i] = encode(strings[i]);
                } catch (Exception ignored){}
            }
        }
        return itemStacks;
    }


    @Override
    public ItemStack encode(String value) {
        return c.encode(value);
    }


    @Override
    public String code(ItemStack itemStack) {
        return c.code(itemStack);
    }
}