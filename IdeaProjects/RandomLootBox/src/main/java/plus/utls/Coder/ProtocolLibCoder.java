package plus.utls.Coder;

import com.comphenix.protocol.utility.StreamSerializer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import plus.main;
import plus.utls.Coder.Coder;

import java.io.IOException;

public class ProtocolLibCoder extends BaseWriter implements Coder {
    @Override
    public String codeItem(ItemStack item){
        try {
            return StreamSerializer.getDefault().serializeItemStack(item);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    public ItemStack decodeItem(String code){
        try {
            return StreamSerializer.getDefault().deserializeItemStack(code);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }



}
