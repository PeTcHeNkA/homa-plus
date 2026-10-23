package plus.invlib.event;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import plus.inv.AInventory;
import plus.inv.GuiContainer;
import plus.invlib.cap.CAPInventoryProvider;
import plus.invlib.cap.ICAPInventory;


public class GuiHandler implements IGuiHandler {
    private final Builder<Container> serverBuilder;
    private final Builder<GuiContainer> clientBuilder;
    private final int id;

    public interface Builder<T>{
        T build(InventoryPlayer playerInventory, AInventory cInventory, EntityPlayer player);
    }

    public GuiHandler(Builder<Container> serverBuilder, Builder<GuiContainer> clientBuilder, int index){
        this.serverBuilder = serverBuilder;
        this.clientBuilder = clientBuilder;
        this.id = index;
    }


    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        ICAPInventory inv = player.getCapability(CAPInventoryProvider.INVENTORY_CAP, null);
        if(id == this.id) {
            return serverBuilder.build(player.inventory, inv.getInventory(), player);
            //return new AInventoryContainer(player.inventory, inv.getInventory(), player);
        }
        return null;
    }


    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        ICAPInventory inv = player.getCapability(CAPInventoryProvider.INVENTORY_CAP, null);
        if(id == this.id) {
            return clientBuilder.build(player.inventory, inv.getInventory(), player);
            //return new AInventoryClientRenderer(player, player.inventory, inv.getInventory());
        }
        return null;
    }
}