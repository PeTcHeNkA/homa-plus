package plus.fserver.api.bind;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import java.util.Locale;


public class CommandBindFunc implements ItemBind.Func{
    private final String cmd;
    private final EnumActionResult result;

    public CommandBindFunc(String cmd){
        this(cmd, EnumActionResult.PASS);
    }


    public CommandBindFunc(String cmd, String result) {
        this(cmd, EnumActionResult.valueOf(result.toUpperCase(Locale.ROOT)));
    }


    public CommandBindFunc(String cmd, EnumActionResult result) {
        this.cmd = cmd;
        this.result = result;
    }


    @Override
    public EnumActionResult call(ItemStack stack, EntityPlayer playerIn, EnumHand hand) {
        playerIn.world.getMinecraftServer().getCommandManager().executeCommand(playerIn, cmd);
        return result;
    }
}