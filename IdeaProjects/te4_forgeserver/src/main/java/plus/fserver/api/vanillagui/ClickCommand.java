package plus.fserver.api.vanillagui;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import plus.fserver.api.IPlayerDataModel;
import plus.fserver.api.placeholders.PlayerVarProvider;
import plus.tson.TsonFunc;
import plus.tson.TsonMap;
import plus.tson.format.IVarGetter;
import plus.tson.format.TextFormatter;


public class ClickCommand implements TsonFunc {
    private final IVarGetter<IPlayerDataModel> format;
    private final boolean fromServer;

    public ClickCommand(TsonMap map){
        this(map.getStr("msg"), map.getOrDefaultBool("from-server", false));
    }


    public ClickCommand(String format){
        this(format, false);
    }


    public ClickCommand(String format, boolean fromServer){
        if(fromServer && format.charAt(0) == '/'){
            format = format.substring(1);
        }
        this.format = TextFormatter.compile(format, PlayerVarProvider.PROVIDER).simplify();
        this.fromServer = fromServer;
    }


    @Override
    public Object call(Object... args) {
        return run((ChestGui.VrContainer) args[0], (Integer) args[1], (Integer) args[2], (ClickType) args[3], (EntityPlayer) args[4]);
    }


    public ItemStack run(ChestGui.VrContainer cont, Integer slotId, Integer dType, ClickType cType, EntityPlayer player){
        ItemStack result = ChestGui.canselAll(cont, slotId, dType, cType, player);
        try {
            String msg;
            if(!format.isConst()) {
                ImmutableMap<String, String> map = ImmutableMap.of("slot", String.valueOf(cont.getSlot(slotId).slotNumber), "type", cType.name());

                msg = format.get(PlayerVarProvider.model((EntityPlayerMP) player, map));
            } else {
                msg = format.get(null);
            }

            MinecraftServer server = player.world.getMinecraftServer();

            if(fromServer){
                server.getCommandManager().executeCommand(server, msg);
            } else {
                if(msg.charAt(0) == '/'){
                    msg = msg.substring(1);
                    server.getCommandManager().executeCommand(player, msg);
                } else {
                    ((EntityPlayerMP)player).connection.processChatMessage(msg);
                }
            }
        } catch (Throwable e){
            e.printStackTrace();
        }
        return result;
    }
}