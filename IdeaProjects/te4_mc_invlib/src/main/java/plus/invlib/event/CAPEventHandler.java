package plus.invlib.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import plus.inv.AInventory;
import plus.inv.AInventoryContainer;
import plus.inv.AllPlayerInventoryProxy;
import plus.invlib.cap.CAPInventoryProvider;
import plus.invlib.cap.ICAPInventory;
import plus.invlib.network.PacketSyncRenderItem;
import plus.main;


public class CAPEventHandler {
	
	public static void register(){
		MinecraftForge.EVENT_BUS.register(new CAPEventHandler());
	}
	
	public static final ResourceLocation INVENTORY_CAP =
			new ResourceLocation(main.MOD_ID, "inventory");


	@SubscribeEvent
	public void attachCapability(AttachCapabilitiesEvent event) {
		if (event.getObject() instanceof EntityPlayer) {
			event.addCapability(INVENTORY_CAP, new CAPInventoryProvider());
		}
	}


	@SubscribeEvent
	public void onLoadFromFile(PlayerEvent.LoadFromFile event) {
//		ICAPInventory inv = event.getEntityPlayer().getCapability(
//				CAPInventoryProvider.INVENTORY_CAP, null);
//		if(inv==null)return;
//		for(int i:ItemSynchonolizer.syncIndexes){
//			ItemSynchonolizer packet = new ItemSynchonolizer(
//					inv.getInventory().getStackInSlot(i),
//					event.getEntity().getEntityId(), i
//			);
//			ItemSynchonolizer.serverCache.add(packet.id, packet.slot, packet);
//		}
	}


	@SubscribeEvent
	public void onStartTracking(PlayerEvent.StartTracking event) {
		if (event.getTarget().world.isRemote) {
			return;
		}
		if (!(event.getTarget() instanceof EntityPlayer)) {
			return;
		}
		PacketSyncRenderItem.sendPlayerCacheOf(
				(EntityPlayerMP) event.getEntityPlayer(), event.getTarget().getEntityId()
		);
	}


	@SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        EntityPlayer player = event.getEntityPlayer();
        ICAPInventory newCap = player.getCapability(CAPInventoryProvider.INVENTORY_CAP, null);
        ICAPInventory oldCap = event.getOriginal().getCapability(CAPInventoryProvider.INVENTORY_CAP, null);
        newCap.copyInventory(oldCap);
    }


	@SubscribeEvent
	public void onPlayerJoin(final EntityJoinWorldEvent event) {
		if (event.getWorld().isRemote) {
			return;
		}
		if (!(event.getEntity() instanceof EntityPlayer)) {
			return;
		}
//		System.out.println("NEN________");
//		ICAPInventory inv = event.getEntity().getCapability(CAPInventoryProvider.INVENTORY_CAP, null);
//		AllPlayerInventoryProxy.INST.getStackInSlot(event.getEntity(),i);
//		if(inv==null)return;

		for(int i: AInventoryContainer.syncIndexes) {
			PacketSyncRenderItem packet = new PacketSyncRenderItem(
					AllPlayerInventoryProxy.INST.getStackInSlot((EntityPlayer) event.getEntity(),i),
//					inv.getInventory().getStackInSlot(i),
					event.getEntity().getEntityId(), i
			);
//			System.out.println(packet);
			PacketSyncRenderItem.serverCache.add(packet.id, packet.slot, packet);
		}


//		ItemSynchonolizer.sendToAllTrackingCacheOf(event.getEntity());
	}


	@SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
		if(event.getEntity() instanceof EntityPlayer) {

			EntityPlayer player = (EntityPlayer)event.getEntity();
			ICAPInventory cap = player.getCapability(CAPInventoryProvider.INVENTORY_CAP, null);
			AInventory inv = cap.getInventory();

			dropAllItems(player, inv);
			inv.clear();
		}	 
    }


	private static void dropAllItems(EntityPlayer player, AInventory inventory){
		for (ItemStack itemStack : inventory.getStacks()) {
			if (!itemStack.isEmpty()) {
				player.dropItem(itemStack, true, false);
			}
		}
	}
}
