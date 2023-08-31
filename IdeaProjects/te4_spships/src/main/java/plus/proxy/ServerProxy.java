package plus.proxy;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import plus.SpMain;
import plus.common.amm.SpBullet;
import plus.common.block.CrystalOre;
import plus.common.block.SimpleBlock;
import plus.common.mashine.block.SpMehModule;
import plus.common.block.SimpleOre;
import plus.common.mashine.capicator.SpCapacitorTileEntity;
import plus.common.mashine.furnace.SpFurnaceTileEntity;
import plus.common.mashine.item.*;
import plus.common.mashine.item.recept.ISimpleSmeling;
import plus.common.mashine.lincker.SpCraftTileEntity;
import plus.common.mashine.oxy.SpOxyChargerTileEntity;
import plus.common.mashine.press.SpPressTileEntity;
import plus.common.mashine.react.SpReactTileEntity;
import plus.common.network.PacketSyncRenderItem;
import plus.common.network.spship.SpModePacket;
import plus.common.network.spship.SpShootPacket;
import plus.common.spship.entity.OffSpShip;
import plus.common.spship.item.*;
import java.util.HashSet;
import static plus.SpMain.preLoadClasses;


public class ServerProxy {
    public static SimpleNetworkWrapper network;

    public void preInit() {
        preLoadClasses(
                //BasicItem.class,
                ReactCore.class,
                SpMehModule.class,
                ItemMssCapacitor.class,
                CrystalOre.class,
                TehSubcraftItem.class,
                ItemCorpus.class,
                ItemSpArmor.class,
                //ItemModule.class,
                ItemAccelerator.class,
                ItemRegenShield.class,
                ItemProtector.class,
                ItemGun.class
        );
        network = NetworkRegistry.INSTANCE.newSimpleChannel(SpMain.MOD_ID);
        network.registerMessage(
                SpShootPacket.ServerHandler.class, SpShootPacket.class,0, Side.SERVER);
        network.registerMessage(
                SpModePacket.ServerHandler.class,  SpModePacket.class, 1, Side.SERVER);

        MinecraftForge.EVENT_BUS.register(this);
    }


    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget().world.isRemote) return;
        if (!(event.getTarget() instanceof EntityPlayerMP)) return;
        PacketSyncRenderItem.sendPlayerCacheOf(
                (EntityPlayerMP) event.getEntityPlayer(), event.getTarget().getEntityId()
        );
    }


    @SubscribeEvent
    public void onRegistryItem(RegistryEvent.Register<Item> e) {
        for(Object o:toReg){
            if (o instanceof Item) {
                e.getRegistry().register((Item) o);
            }
        }
    }


    @SubscribeEvent
    public void onRegistryBock(RegistryEvent.Register<Block> e) {
        for(Object o:toReg){
            if (o instanceof Block) {
                e.getRegistry().register((Block) o);
            }
        }
    }


    protected HashSet<Object> toReg = new HashSet<>();
    public void addToRegObj(Item o){
        toReg.add(o);
    }
    public void addToRegObj(Block block){
        toReg.add(block);
        int countLores;
        if(block instanceof SimpleOre || (block instanceof SimpleBlock &&
                !(block instanceof SpMehModule))){
            countLores = 0;
        } else {
            countLores = 2;
        }
        addToRegObj(new EItemBlock(block, countLores));
    }


    public void init() {
        ISimpleSmeling.regAll();

        regTile(SpCraftTileEntity.class, "sp_craft_table");
        regTile(SpFurnaceTileEntity.class, "sp_furnace");
        regTile(SpReactTileEntity.class, "sp_react");
        regTile(SpPressTileEntity.class, "sp_press");
        regTile(SpCapacitorTileEntity.class, "sp_capacitor");
        regTile(SpOxyChargerTileEntity.class, "sp_oxy_charger");
    }


    protected void regTile(Class<? extends TileEntity> clazz, String key){
        GameRegistry.registerTileEntity(clazz, key);
    }


    public void postInit() {
        toReg = null;
    }


    public void registryEntity(RegistryEvent.Register<EntityEntry> event){
        event.getRegistry().register(
                EntityEntryBuilder.create().entity(SpBullet.class).name("sp_bullet")
                        .tracker(64,2,true)
                        .id("sp_bullet",1).build()
        );
        event.getRegistry().register(
                EntityEntryBuilder.create().entity(OffSpShip.class).name("ot_spship")
                        .tracker(64,4,false)
                        .id("ot_spship",2).build()
        );
    }


    public void runIfClient(Runnable r){}
}