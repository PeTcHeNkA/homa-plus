package com.sk89q.worldedit.forge;

import net.minecraftforge.event.CommandEvent;
import org.apache.logging.log4j.*;
import java.io.*;
import net.minecraftforge.common.*;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.*;
import com.sk89q.worldedit.event.platform.*;
import net.minecraft.entity.player.*;
import com.sk89q.worldedit.extension.platform.*;
import net.minecraftforge.event.entity.player.*;
import com.sk89q.worldedit.forge.net.*;
import net.minecraftforge.fml.common.network.simpleimpl.*;
import net.minecraftforge.fml.common.eventhandler.*;
import com.sk89q.worldedit.entity.*;
import com.sk89q.worldedit.internal.*;
import com.sk89q.worldedit.world.*;
import com.sk89q.worldedit.blocks.*;
import net.minecraft.item.*;
import net.minecraft.enchantment.*;
import java.util.*;
import com.google.common.base.*;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.session.*;


@Mod(modid = "worldedit", name = "com.sk89q.worldedit.WorldEdit", version = "6.1.10", acceptableRemoteVersions = "*")
public class ForgeWorldEdit {
    public static Logger logger;
    public static final String MOD_ID = "worldedit";
    public static final String CUI_PLUGIN_CHANNEL = "WECUI";
    private ForgePermissionsProvider provider;
    @Mod.Instance("worldedit")
    public static ForgeWorldEdit inst;
    @SidedProxy(serverSide = "com.sk89q.worldedit.forge.CommonProxy", clientSide = "com.sk89q.worldedit.forge.ClientProxy")
    public static CommonProxy proxy;
    private ForgePlatform platform;
    private ForgeConfiguration config;
    private File workingDir;

    @Mod.EventHandler
    public void preInit(final FMLPreInitializationEvent event) {
        ForgeWorldEdit.logger = event.getModLog();
        (this.workingDir = new File(event.getModConfigurationDirectory() + File.separator + "worldedit")).mkdir();
        (this.config = new ForgeConfiguration(this)).load();
        MinecraftForge.EVENT_BUS.register(ThreadSafeCache.getInstance());
    }


    @Mod.EventHandler
    public void init(final FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        WECUIPacketHandler.init();
        InternalPacketHandler.init();
        ForgeWorldEdit.proxy.registerHandlers();
    }


    @Mod.EventHandler
    public void postInit(final FMLPostInitializationEvent event) {
        ForgeWorldEdit.logger.info("com.sk89q.worldedit.WorldEdit for Forge (version " + this.getInternalVersion() + ") is loaded");
    }


    @Mod.EventHandler
    public void serverAboutToStart(final FMLServerAboutToStartEvent event) {
        if (this.platform != null) {
            ForgeWorldEdit.logger.warn("FMLServerStartingEvent occurred when FMLServerStoppingEvent hasn't");
            WorldEdit.getInstance().getPlatformManager().unregister(this.platform);
        }
        this.platform = new ForgePlatform(this);
        WorldEdit.getInstance().getPlatformManager().register(this.platform);
        if (Loader.isModLoaded("sponge")) {
            this.provider = new ForgePermissionsProvider.SpongePermissionsProvider();
        }
        else {
            this.provider = new ForgePermissionsProvider.VanillaPermissionsProvider(this.platform);
        }
    }


    @Mod.EventHandler
    public void serverStopping(final FMLServerStoppingEvent event) {
        WorldEdit.getInstance().getPlatformManager().unregister((Platform)this.platform);
    }


    @Mod.EventHandler
    public void serverStarted(final FMLServerStartedEvent event) {
        WorldEdit.getInstance().getEventBus().post(new PlatformReadyEvent());
    }


    @SubscribeEvent
    public void onCommandEvent(final CommandEvent event) {
        if (event.getSender() instanceof EntityPlayerMP) {
            if (((EntityPlayerMP)event.getSender()).world.isRemote) {
                return;
            }
            final String[] split = new String[event.getParameters().length + 1];
            System.arraycopy(event.getParameters(), 0, split, 1, event.getParameters().length);
            split[0] = event.getCommand().getName();
            final com.sk89q.worldedit.event.platform.CommandEvent weEvent = new com.sk89q.worldedit.event.platform.CommandEvent((Actor)this.wrap((EntityPlayerMP)event.getSender()), Joiner.on(" ").join((Object[])split));
            WorldEdit.getInstance().getEventBus().post(weEvent);
        }
    }


    @SubscribeEvent
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (this.platform == null) {
            return;
        }
        if (!this.platform.isHookingEvents()) {
            return;
        }
        if (event.getWorld().isRemote && event instanceof PlayerInteractEvent.LeftClickEmpty) {
            InternalPacketHandler.CHANNEL.sendToServer((IMessage)new LeftClickAirEventMessage());
            return;
        }
        final boolean isLeftDeny = event instanceof PlayerInteractEvent.LeftClickBlock && ((PlayerInteractEvent.LeftClickBlock)event).getUseItem() == Event.Result.DENY;
        final boolean isRightDeny = event instanceof PlayerInteractEvent.RightClickBlock && ((PlayerInteractEvent.RightClickBlock)event).getUseItem() == Event.Result.DENY;
        if (isLeftDeny || isRightDeny || event.getEntity().world.isRemote) {
            return;
        }
        final WorldEdit we = WorldEdit.getInstance();
        final ForgePlayer player = this.wrap((EntityPlayerMP)event.getEntityPlayer());
        final ForgeWorld world = this.getWorld(event.getEntityPlayer().world);
        if (event instanceof PlayerInteractEvent.LeftClickEmpty) {
            if (we.handleArmSwing((Player)player)) {}
        }
        else if (event instanceof PlayerInteractEvent.LeftClickBlock) {
            final WorldVector pos = new WorldVector(LocalWorldAdapter.adapt((World)world), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());
            if (we.handleBlockLeftClick((Player)player, pos)) {
                event.setCanceled(true);
            }
            if (we.handleArmSwing((Player)player)) {
                event.setCanceled(true);
            }
        }
        else if (event instanceof PlayerInteractEvent.RightClickBlock) {
            final WorldVector pos = new WorldVector(LocalWorldAdapter.adapt(world), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());
            if (we.handleBlockRightClick(player, pos)) {
                event.setCanceled(true);
            }
            if (we.handleRightClick(player)) {
                event.setCanceled(true);
            }
        }
        else if (event instanceof PlayerInteractEvent.RightClickItem && we.handleRightClick(player)) {
            event.setCanceled(true);
        }
    }


    public static ItemStack toForgeItemStack(final BaseItemStack item) {
        final ItemStack ret = new ItemStack(Item.getItemById(item.getType()), item.getAmount(), item.getData());
        for (final Map.Entry<Integer, Integer> entry : item.getEnchantments().entrySet()) {
            ret.addEnchantment(Enchantment.getEnchantmentByID(entry.getKey()), entry.getValue());
        }
        return ret;
    }


    ForgeConfiguration getConfig() {
        return this.config;
    }


    public ForgePlayer wrap(final EntityPlayerMP player) {
        Preconditions.checkNotNull((Object)player);
        return new ForgePlayer(player);
    }


    public LocalSession getSession(final EntityPlayerMP player) {
        Preconditions.checkNotNull((Object)player);
        return WorldEdit.getInstance().getSessionManager().get((SessionOwner)this.wrap(player));
    }


    public ForgeWorld getWorld(final net.minecraft.world.World world) {
        Preconditions.checkNotNull((Object)world);
        return new ForgeWorld(world);
    }


    public Platform getPlatform() {
        return (Platform)this.platform;
    }


    public File getWorkingDir() {
        return this.workingDir;
    }


    String getInternalVersion() {
        return ForgeWorldEdit.class.getAnnotation(Mod.class).version();
    }


    public void setPermissionsProvider(final ForgePermissionsProvider provider) {
        this.provider = provider;
    }


    public ForgePermissionsProvider getPermissionsProvider() {
        return this.provider;
    }
}