package plus.inv.proxy;

import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;
import plus.invlib.slot.BodyASlotRenderer;
import plus.invlib.event.KeyHandler;
import plus.invlib.network.PacketSyncRenderItem;
import plus.invlib.network.PacketOpenInventory;

public class ClientProxy extends ServerProxy {

	@Override
	public void preInit() {
		super.preInit();
		//синхронизация отображения инвентаря
		network.registerMessage(PacketSyncRenderItem.ClientHandler.class,
				PacketSyncRenderItem.class, 1, Side.CLIENT);
	}


	@Override
	public void init() {
		new KeyHandler(network, PacketOpenInventory::new,
				"Custom Inventory Keys", "Open Inventory", Keyboard.KEY_E, true);

		new BodyASlotRenderer(0.85f, 180f,0f,0f,20f, 9);
		super.init();
	}


	@Override
	public void postInit() {
		super.postInit();
	}
}