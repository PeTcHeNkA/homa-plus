package plus.inv.proxy;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import plus.inv.AInventoryClientRenderer;
import plus.inv.AInventoryContainer;
import plus.invlib.event.GuiHandler;
import plus.invlib.event.CAPEventHandler;
import plus.invlib.cap.CAPInvStorage;
import plus.invlib.cap.CAPInventory;
import plus.invlib.cap.ICAPInventory;
import plus.invlib.network.PacketOpenInventory;
import plus.main;


public class ServerProxy {
	public static SimpleNetworkWrapper network;


	public static class Handler implements IMessageHandler<PacketOpenInventory, IMessage> {
		@Override
		public IMessage onMessage(PacketOpenInventory message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			player.openGui(
					main.INSTANCE,
					0,
					player.getEntityWorld(),
					(int)player.posX, (int)player.posY, (int)player.posZ
			);
			return null;
		}
	}


	public void preInit() {
		network = NetworkRegistry.INSTANCE.newSimpleChannel(main.MOD_ID);
		network.registerMessage(Handler.class, PacketOpenInventory.class, 0, Side.SERVER);
	}


	public void init() {
		CapabilityManager.INSTANCE.register(ICAPInventory.class, new CAPInvStorage(), CAPInventory.class);
		NetworkRegistry.INSTANCE.registerGuiHandler(
				main.INSTANCE, new GuiHandler(
						(playerInventory, cInventory, player) -> new AInventoryContainer(playerInventory, cInventory, player),
						(playerInventory, cInventory, player) -> new AInventoryClientRenderer(playerInventory, cInventory, player),
						0
				)
		);

		CAPEventHandler.register();
	}

	
	public void postInit() {}
}
