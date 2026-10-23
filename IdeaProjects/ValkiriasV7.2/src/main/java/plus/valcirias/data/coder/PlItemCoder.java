package plus.valcirias.data.coder;

import org.bukkit.inventory.ItemStack;
import com.comphenix.protocol.utility.StreamSerializer;
import java.io.IOException;


public class PlItemCoder implements AbstractItemCoder {
    @Override
    public ItemStack encode(String value) {
        try {
            return StreamSerializer.getDefault().deserializeItemStack(value);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    public String code(ItemStack itemStack) {
        try {
            return StreamSerializer.getDefault().serializeItemStack(itemStack);
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }
}
