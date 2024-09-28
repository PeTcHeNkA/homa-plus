package net.minecraftforge.common;

import java.net.*;
import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.client.event.*;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.event.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraftforge.fml.common.registry.*;
import net.minecraftforge.fml.common.gameevent.*;
import net.minecraftforge.fml.common.discovery.json.*;
import org.apache.commons.io.*;
import net.minecraft.crash.*;
import net.minecraftforge.fml.common.discovery.*;
import net.minecraftforge.fml.common.network.*;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.common.network.*;
import net.minecraftforge.common.config.*;
import java.io.*;
import com.google.common.eventbus.*;
import net.minecraftforge.items.*;
import net.minecraftforge.fluids.capability.*;
import net.minecraftforge.common.model.animation.*;
import net.minecraftforge.energy.*;
import net.minecraftforge.fml.relauncher.*;
import net.minecraftforge.client.*;
import net.minecraftforge.fluids.*;
import net.minecraft.world.biome.*;
import net.minecraftforge.event.terraingen.*;
import net.minecraftforge.server.command.*;
import net.minecraft.command.*;
import net.minecraft.world.storage.*;
import net.minecraft.nbt.*;
import java.util.*;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.oredict.*;
import net.minecraft.stats.*;
import net.minecraft.item.crafting.*;
import net.minecraftforge.classloading.*;
import net.minecraftforge.fml.client.*;
import com.google.common.collect.*;
import java.security.cert.*;
import javax.annotation.*;
import org.apache.logging.log4j.*;
import plus.fserver.api.ElcaBiomeRegistrator;
import plus.fserver.plugin.SSPatchLoader;


@Mod.EventBusSubscriber(modid = "forge")
public class ForgeModContainer extends DummyModContainer implements WorldAccessContainer {
    public static final String VERSION_CHECK_CAT = "version_checking";
    public static int clumpingThreshold;
    public static boolean removeErroringEntities;
    public static boolean removeErroringTileEntities;
    public static boolean fullBoundingBoxLadders;
    public static double zombieSummonBaseChance;
    public static int[] blendRanges;
    public static float zombieBabyChance;
    public static boolean shouldSortRecipies;
    public static boolean disableVersionCheck;
    public static boolean forgeLightPipelineEnabled;
    public static boolean selectiveResourceReloadEnabled;
    @Deprecated
    public static boolean replaceVanillaBucketModel;
    public static boolean zoomInMissingModelTextInGui;
    public static boolean forgeCloudsEnabled;
    public static boolean disableStairSlabCulling;
    public static boolean alwaysSetupTerrainOffThread;
    public static boolean allowEmissiveItems;
    public static int dimensionUnloadQueueDelay;
    public static boolean logCascadingWorldGeneration;
    public static boolean fixVanillaCascading;
    static final Logger log;
    private static Configuration config;
    private static ForgeModContainer INSTANCE;
    private URL updateJSONUrl;
    public UniversalBucket universalBucket;

    public static ForgeModContainer getInstance() {
        return ForgeModContainer.INSTANCE;
    }

    public ForgeModContainer() {
        super(new ModMetadata());
        this.updateJSONUrl = null;
        final ModMetadata meta = this.getMetadata();
        meta.modId = "forge";
        meta.name = "Minecraft Forge -> " + SSPatchLoader.ELCA_RELEASE;
        meta.version = ForgeVersion.getVersion();
        meta.credits = "Made possible with help from many people";
        meta.authorList = Arrays.asList("HomaPlus", "LexManos", "cpw", "fry");
        meta.description = "Minecraft " + SSPatchLoader.ELCA_RELEASE + " height performance forge server mod";
        meta.url = "http://minecraftforge.net";
        meta.screenshots = new String[0];
        meta.logoFile = "/forge_logo.png";
        try {
            this.updateJSONUrl = new URL("http://files.minecraftforge.net/maven/net/minecraftforge/forge/promotions_slim.json");
        }
        catch (MalformedURLException ex) {}
        ForgeModContainer.config = null;
        final File cfgFile = new File(Loader.instance().getConfigDir(), "forge.cfg");
        ForgeModContainer.config = new Configuration(cfgFile);
        syncConfig(true);
        ForgeModContainer.INSTANCE = this;
    }


    @Override
    public String getGuiClassName() {
        return "net.minecraftforge.client.gui.ForgeGuiFactory";
    }


    public static Configuration getConfig() {
        return ForgeModContainer.config;
    }


    private static void remapGeneralPropertyToClient(final String key) {
        final ConfigCategory GENERAL = ForgeModContainer.config.getCategory("general");
        if (GENERAL.containsKey(key)) {
            FMLLog.log.debug("Remapping property {} from category general to client", (Object)key);
            final Property property = GENERAL.get(key);
            GENERAL.remove(key);
            ForgeModContainer.config.getCategory("client").put(key, property);
        }
    }

    private static void syncConfig(final boolean load) {
        List<String> propOrder = new ArrayList<String>();
        if (!ForgeModContainer.config.isChild) {
            if (load) {
                ForgeModContainer.config.load();
            }
            final Property enableGlobalCfg = ForgeModContainer.config.get("general", "enableGlobalConfig", false).setShowInGui(false);
            if (enableGlobalCfg.getBoolean(false)) {
                Configuration.enableGlobalConfig();
            }
        }
        if (ForgeModContainer.config.getCategory("general").containsKey("defaultSpawnFuzz")) {
            ForgeModContainer.config.getCategory("general").remove((Object)"defaultSpawnFuzz");
        }
        if (ForgeModContainer.config.getCategory("general").containsKey("spawnHasFuzz")) {
            ForgeModContainer.config.getCategory("general").remove((Object)"spawnHasFuzz");
        }
        if (ForgeModContainer.config.getCategory("general").containsKey("disableStitchedFileSaving")) {
            ForgeModContainer.config.getCategory("general").remove((Object)"disableStitchedFileSaving");
        }
        if (ForgeModContainer.config.getCategory("client").containsKey("java8Reminder")) {
            ForgeModContainer.config.getCategory("client").remove((Object)"java8Reminder");
        }
        if (ForgeModContainer.config.getCategory("client").containsKey("replaceVanillaBucketModel")) {
            ForgeModContainer.config.getCategory("client").remove((Object)"replaceVanillaBucketModel");
        }
        remapGeneralPropertyToClient("biomeSkyBlendRange");
        remapGeneralPropertyToClient("forgeLightPipelineEnabled");
        Property prop = ForgeModContainer.config.get("general", "disableVersionCheck", false);
        prop.setComment("Set to true to disable Forge's version check mechanics. Forge queries a small json file on our server for version information. For more details see the ForgeVersion class in our github.");
        prop.setLanguageKey("forge.configgui.disableVersionCheck");
        ForgeModContainer.disableVersionCheck = prop.getBoolean(ForgeModContainer.disableVersionCheck);
        propOrder.add(prop.getName());
        prop = ForgeModContainer.config.get("general", "clumpingThreshold", 64, "Controls the number threshold at which Packet51 is preferred over Packet52, default and minimum 64, maximum 1024", 64, 1024);
        prop.setLanguageKey("forge.configgui.clumpingThreshold").setRequiresWorldRestart(true);
        ForgeModContainer.clumpingThreshold = prop.getInt(64);
        if (ForgeModContainer.clumpingThreshold > 1024 || ForgeModContainer.clumpingThreshold < 64) {
            prop.set(ForgeModContainer.clumpingThreshold = 64);
        }
        propOrder.add(prop.getName());
        prop = ForgeModContainer.config.get("general", "sortRecipies", true);
        prop.setComment("Set to true to enable the post initialization sorting of crafting recipes using Forge's sorter. May cause desyncing on conflicting recipes. MUST RESTART MINECRAFT IF CHANGED FROM THE CONFIG GUI.");
        prop.setLanguageKey("forge.configgui.sortRecipies").setRequiresMcRestart(true);
        ForgeModContainer.shouldSortRecipies = prop.getBoolean(true);
        propOrder.add(prop.getName());
        prop = ForgeModContainer.config.get("general", "removeErroringEntities", false);
        prop.setComment("Set this to true to remove any Entity that throws an error in its update method instead of closing the server and reporting a crash log. BE WARNED THIS COULD SCREW UP EVERYTHING USE SPARINGLY WE ARE NOT RESPONSIBLE FOR DAMAGES.");
        prop.setLanguageKey("forge.configgui.removeErroringEntities").setRequiresWorldRestart(true);
        ForgeModContainer.removeErroringEntities = prop.getBoolean(false);
        propOrder.add(prop.getName());
        if (ForgeModContainer.removeErroringEntities) {
            FMLLog.log.warn("Enabling removal of erroring Entities - USE AT YOUR OWN RISK");
        }
        prop = ForgeModContainer.config.get("general", "removeErroringTileEntities", false);
        prop.setComment("Set this to true to remove any TileEntity that throws an error in its update method instead of closing the server and reporting a crash log. BE WARNED THIS COULD SCREW UP EVERYTHING USE SPARINGLY WE ARE NOT RESPONSIBLE FOR DAMAGES.");
        prop.setLanguageKey("forge.configgui.removeErroringTileEntities").setRequiresWorldRestart(true);
        ForgeModContainer.removeErroringTileEntities = prop.getBoolean(false);
        propOrder.add(prop.getName());
        if (ForgeModContainer.removeErroringTileEntities) {
            FMLLog.log.warn("Enabling removal of erroring Tile Entities - USE AT YOUR OWN RISK");
        }
        prop = ForgeModContainer.config.get("general", "fullBoundingBoxLadders", false);
        prop.setComment("Set this to true to check the entire entity's collision bounding box for ladders instead of just the block they are in. Causes noticeable differences in mechanics so default is vanilla behavior. Default: false");
        prop.setLanguageKey("forge.configgui.fullBoundingBoxLadders").setRequiresWorldRestart(true);
        ForgeModContainer.fullBoundingBoxLadders = prop.getBoolean(false);
        propOrder.add(prop.getName());
        prop = ForgeModContainer.config.get("general", "zombieBaseSummonChance", 0.1, "Base zombie summoning spawn chance. Allows changing the bonus zombie summoning mechanic.", 0.0, 1.0);
        prop.setLanguageKey("forge.configgui.zombieBaseSummonChance").setRequiresWorldRestart(true);
        ForgeModContainer.zombieSummonBaseChance = prop.getDouble(0.1);
        propOrder.add(prop.getName());
        prop = ForgeModContainer.config.get("general", "zombieBabyChance", 0.05, "Chance that a zombie (or subclass) is a baby. Allows changing the zombie spawning mechanic.", 0.0, 1.0);
        prop.setLanguageKey("forge.configgui.zombieBabyChance").setRequiresWorldRestart(true);
        ForgeModContainer.zombieBabyChance = (float)prop.getDouble(0.05);
        propOrder.add(prop.getName());
        prop = ForgeModContainer.config.get("general", "logCascadingWorldGeneration", true, "Log cascading chunk generation issues during terrain population.");
        ForgeModContainer.logCascadingWorldGeneration = prop.getBoolean();
        prop.setLanguageKey("forge.configgui.logCascadingWorldGeneration");
        propOrder.add(prop.getName());
        prop = ForgeModContainer.config.get("general", "fixVanillaCascading", false, "Fix vanilla issues that cause worldgen cascading. This DOES change vanilla worldgen so DO NOT report bugs related to world differences if this flag is on.");
        ForgeModContainer.fixVanillaCascading = prop.getBoolean();
        prop.setLanguageKey("forge.configgui.fixVanillaCascading");
        propOrder.add(prop.getName());
        prop = ForgeModContainer.config.get("general", "dimensionUnloadQueueDelay", 0, "The time in ticks the server will wait when a dimension was queued to unload. This can be useful when rapidly loading and unloading dimensions, like e.g. throwing items through a nether portal a few time per second.");
        ForgeModContainer.dimensionUnloadQueueDelay = prop.getInt(0);
        prop.setLanguageKey("forge.configgui.dimensionUnloadQueueDelay");
        propOrder.add(prop.getName());
        ForgeModContainer.config.setCategoryPropertyOrder("general", (List)propOrder);
        propOrder = new ArrayList<String>();
        prop = ForgeModContainer.config.get("version_checking", "Global", true, "Enable the entire mod update check system. This only applies to mods using the Forge system.");
        propOrder.add("Global");
        ForgeModContainer.config.setCategoryPropertyOrder("version_checking", (List)propOrder);
        propOrder = new ArrayList<String>();
        prop = ForgeModContainer.config.get("client", "zoomInMissingModelTextInGui", false, "Toggle off to make missing model text in the gui fit inside the slot.");
        ForgeModContainer.zoomInMissingModelTextInGui = prop.getBoolean(false);
        prop.setLanguageKey("forge.configgui.zoomInMissingModelTextInGui");
        propOrder.add(prop.getName());
        prop = ForgeModContainer.config.get("client", "forgeCloudsEnabled", true, "Enable uploading cloud geometry to the GPU for faster rendering.");
        prop.setLanguageKey("forge.configgui.forgeCloudsEnabled");
        ForgeModContainer.forgeCloudsEnabled = prop.getBoolean();
        propOrder.add(prop.getName());
        prop = ForgeModContainer.config.get("client", "disableStairSlabCulling", false, "Disable culling of hidden faces next to stairs and slabs. Causes extra rendering, but may fix some resource packs that exploit this vanilla mechanic.");
        ForgeModContainer.disableStairSlabCulling = prop.getBoolean(false);
        prop.setLanguageKey("forge.configgui.disableStairSlabCulling");
        propOrder.add(prop.getName());
        prop = ForgeModContainer.config.get("client", "alwaysSetupTerrainOffThread", false, "Enable forge to queue all chunk updates to the Chunk Update thread. May increase FPS significantly, but may also cause weird rendering lag. Not recommended for computers without a significant number of cores available.");
        ForgeModContainer.alwaysSetupTerrainOffThread = prop.getBoolean(false);
        prop.setLanguageKey("forge.configgui.alwaysSetupTerrainOffThread");
        propOrder.add(prop.getName());
        prop = ForgeModContainer.config.get("client", "allowEmissiveItems", true, "Allow item rendering to detect emissive quads and draw them properly. This allows glowing blocks to look the same in item form, but incurs a very slight performance hit.");
        ForgeModContainer.allowEmissiveItems = prop.getBoolean(true);
        prop.setLanguageKey("forge.configgui.allowEmissiveItems");
        propOrder.add(prop.getName());
        prop = ForgeModContainer.config.get("client", "biomeSkyBlendRange", new int[] { 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34 });
        prop.setComment("Control the range of sky blending for colored skies in biomes.");
        prop.setLanguageKey("forge.configgui.biomeSkyBlendRange");
        ForgeModContainer.blendRanges = prop.getIntList();
        propOrder.add(prop.getName());
        prop = ForgeModContainer.config.get("client", "forgeLightPipelineEnabled", true, "Enable the forge block rendering pipeline - fixes the lighting of custom models.");
        ForgeModContainer.forgeLightPipelineEnabled = prop.getBoolean(true);
        prop.setLanguageKey("forge.configgui.forgeLightPipelineEnabled");
        propOrder.add(prop.getName());
        prop = ForgeModContainer.config.get("client", "selectiveResourceReloadEnabled", false, "When enabled, makes specific reload tasks such as language changing quicker to run.");
        ForgeModContainer.selectiveResourceReloadEnabled = prop.getBoolean(false);
        prop.setLanguageKey("forge.configgui.selectiveResourceReloadEnabled");
        propOrder.add(prop.getName());
        ForgeModContainer.config.setCategoryPropertyOrder("client", (List)propOrder);
        if (ForgeModContainer.config.hasChanged()) {
            ForgeModContainer.config.save();
        }
    }

    @SubscribeEvent
    public void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
        if (this.getMetadata().modId.equals(event.getModID())) {
            if ("chunkLoader".equals(event.getConfigID())) {
                ForgeChunkManager.syncConfigDefaults();
                ForgeChunkManager.loadConfiguration();
            }
            else {
                final boolean tmpStairs = ForgeModContainer.disableStairSlabCulling;
                syncConfig(false);
                if (event.isWorldRunning() && tmpStairs != ForgeModContainer.disableStairSlabCulling) {
                    FMLCommonHandler.instance().reloadRenderers();
                }
            }
        }
    }

    @SubscribeEvent
    public void missingMapping(final RegistryEvent.MissingMappings<Item> event) {
        for (final RegistryEvent.MissingMappings.Mapping<Item> entry : event.getAllMappings()) {
            if (entry.key.toString().equals("minecraft:totem")) {
                final ResourceLocation newTotem = new ResourceLocation("minecraft:totem_of_undying");
                entry.remap(ForgeRegistries.ITEMS.getValue(newTotem));
            }
        }
    }

    @SubscribeEvent
    public void playerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        UsernameCache.setUsername(event.player.getPersistentID(), event.player.getGameProfile().getName());
    }

    @Override
    public boolean registerBus(final EventBus bus, final LoadController controller) {
        bus.register((Object)this);
        return true;
    }

    @Subscribe
    public void modConstruction(final FMLConstructionEvent evt) {
        final InputStream is = ForgeModContainer.class.getResourceAsStream("/META-INF/vanilla_annotations.json");
        try {
            if (is != null) {
                JsonAnnotationLoader.loadJson(is, null, evt.getASMHarvestedData());
            }
            ForgeModContainer.log.debug("Loading Vanilla annotations: " + is);
        }
        finally {
            IOUtils.closeQuietly(is);
        }
        final List<String> all = Lists.newArrayList();
        for (final ASMDataTable.ASMData asm : evt.getASMHarvestedData().getAll(ICrashReportDetail.class.getName().replace('.', '/'))) {
            all.add(asm.getClassName());
        }
        for (final ASMDataTable.ASMData asm : evt.getASMHarvestedData().getAll(ICrashCallable.class.getName().replace('.', '/'))) {
            all.add(asm.getClassName());
        }
        all.add("net/minecraftforge/common/util/TextTable");
        all.add("net/minecraftforge/common/util/TextTable$Column");
        all.add("net/minecraftforge/common/util/TextTable$Row");
        all.add("net/minecraftforge/common/util/TextTable$Alignment");
        all.removeIf(cls -> !cls.startsWith("net/minecraft/") && !cls.startsWith("net/minecraftforge/"));
        ForgeModContainer.log.debug("Preloading CrashReport Classes");
        Collections.sort(all);
        for (final String name : all) {
            ForgeModContainer.log.debug("\t{}", (Object)name);
            try {
                Class.forName(name.replace('/', '.'), false, MinecraftForge.class.getClassLoader());
            }
            catch (Exception e) {
                ForgeModContainer.log.error("Could not find class for name '{}'.", (Object)name, (Object)e);
            }
        }
        NetworkRegistry.INSTANCE.register(this, this.getClass(), "*", evt.getASMHarvestedData());
        ForgeNetworkHandler.registerChannel(this, evt.getSide());
        ConfigManager.sync(this.getModId(), Config.Type.INSTANCE);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Subscribe
    public void preInit(final FMLPreInitializationEvent evt) {
        CapabilityItemHandler.register();
        CapabilityFluidHandler.register();
        CapabilityAnimation.register();
        CapabilityEnergy.register();
        MinecraftForge.EVENT_BUS.register(MinecraftForge.INTERNAL_HANDLER);
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            MinecraftForge.EVENT_BUS.register(ForgeClientHandler.class);
        }
        ForgeChunkManager.captureConfig(evt.getModConfigurationDirectory());
        MinecraftForge.EVENT_BUS.register(this);
    }


    @SubscribeEvent
    public void registrItems(final RegistryEvent.Register<Item> event) {
        if (FluidRegistry.isUniversalBucketEnabled()) {
            (this.universalBucket = new UniversalBucket()).setTranslationKey("forge.bucketFilled");
            event.getRegistry().register((Item)this.universalBucket.setRegistryName("forge", "bucketFilled"));
            MinecraftForge.EVENT_BUS.register(this.universalBucket);
        }
    }


    @SubscribeEvent
    public void registerElcaBiomes(final RegistryEvent.Register<Biome> event){
        ElcaBiomeRegistrator.registerAll(event);
    }


    @Subscribe
    public void postInit(final FMLPostInitializationEvent evt) {
        registerAllBiomesAndGenerateEvents();
        ForgeChunkManager.loadConfiguration();
    }


    private static void registerAllBiomesAndGenerateEvents() {
        for (final Biome biome : ForgeRegistries.BIOMES.getValuesCollection()) {
            if (biome.decorator instanceof DeferredBiomeDecorator) {
                final DeferredBiomeDecorator decorator = (DeferredBiomeDecorator)biome.decorator;
                decorator.fireCreateEventAndReplace(biome);
            }
            BiomeDictionary.ensureHasTypes(biome);
        }
    }


    @Subscribe
    public void onAvailable(final FMLLoadCompleteEvent evt) {
        if (ForgeModContainer.shouldSortRecipies) {
            RecipeSorter.sortCraftManager();
        }
        FluidRegistry.validateFluidRegistry();
    }


    @Subscribe
    public void serverStarting(final FMLServerStartingEvent evt) {
        evt.registerServerCommand(new ForgeCommand());
    }


    @Subscribe
    public void serverStopping(final FMLServerStoppingEvent evt) {
        WorldWorkerManager.clear();
    }


    @Override
    public NBTTagCompound getDataForWriting(final SaveHandler handler, final WorldInfo info) {
        final NBTTagCompound forgeData = new NBTTagCompound();
        final NBTTagCompound dimData = DimensionManager.saveDimensionDataMap();
        forgeData.setTag("DimensionData", dimData);
        FluidRegistry.writeDefaultFluidList(forgeData);
        return forgeData;
    }


    @Override
    public void readData(final SaveHandler handler, final WorldInfo info, final Map<String, NBTBase> propertyMap, final NBTTagCompound tag) {
        DimensionManager.loadDimensionDataMap(tag.hasKey("DimensionData") ? tag.getCompoundTag("DimensionData") : null);
        FluidRegistry.loadFluidDefaults(tag);
    }


    @Subscribe
    public void mappingChanged(final FMLModIdMappingEvent evt) {
        OreDictionary.rebakeMap();
        StatList.reinit();
        Ingredient.invalidateAll();
        FMLCommonHandler.instance().resetClientRecipeBook();
        FMLCommonHandler.instance().reloadSearchTrees();
        FMLCommonHandler.instance().reloadCreativeSettings();
    }


    @Override
    public File getSource() {
        return FMLForgePlugin.forgeLocation;
    }


    @Override
    public Class<?> getCustomResourcePackClass() {
        if (this.getSource().isDirectory()) {
            return FMLFolderResourcePack.class;
        }
        return FMLFileResourcePack.class;
    }


    @Override
    public List<String> getOwnedPackages() {
        return ImmutableList.of(
                "net.minecraftforge.classloading", "net.minecraftforge.client",
                "net.minecraftforge.client.event", "net.minecraftforge.client.event.sound",
                "net.minecraftforge.client.model", "net.minecraftforge.client.model.obj",
                "net.minecraftforge.client.model.techne", "net.minecraftforge.common",
                "net.minecraftforge.common.config", "net.minecraftforge.common.network",
                "net.minecraftforge.common.util", "net.minecraftforge.event",
                "net.minecraftforge.event.brewing", "net.minecraftforge.event.entity",
                "net.minecraftforge.event.entity.item", "net.minecraftforge.event.entity.living",
                "net.minecraftforge.event.entity.minecart", "net.minecraftforge.event.entity.player",
                "net.minecraftforge.event.terraingen", "net.minecraftforge.event.world",
                "net.minecraftforge.fluids", "net.minecraftforge.oredict",
                "net.minecraftforge.server", "net.minecraftforge.server.command", "net.minecraftforge.transformers"
        );
    }


    @Nullable
    @Override
    public Certificate getSigningCertificate() {
        final Certificate[] certificates = this.getClass().getProtectionDomain().getCodeSource().getCertificates();
        return (certificates != null) ? certificates[0] : null;
    }


    @Override
    public URL getUpdateUrl() {
        return this.updateJSONUrl;
    }


    static {
        ForgeModContainer.clumpingThreshold = 64;
        ForgeModContainer.removeErroringEntities = false;
        ForgeModContainer.removeErroringTileEntities = false;
        ForgeModContainer.fullBoundingBoxLadders = false;
        ForgeModContainer.zombieSummonBaseChance = 0.1;
        ForgeModContainer.blendRanges = new int[] { 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34 };
        ForgeModContainer.zombieBabyChance = 0.05f;
        ForgeModContainer.shouldSortRecipies = true;
        ForgeModContainer.disableVersionCheck = true;
        ForgeModContainer.forgeLightPipelineEnabled = true;
        ForgeModContainer.selectiveResourceReloadEnabled = false;
        ForgeModContainer.replaceVanillaBucketModel = true;
        ForgeModContainer.zoomInMissingModelTextInGui = false;
        ForgeModContainer.forgeCloudsEnabled = true;
        ForgeModContainer.disableStairSlabCulling = false;
        ForgeModContainer.alwaysSetupTerrainOffThread = false;
        ForgeModContainer.dimensionUnloadQueueDelay = 0;
        ForgeModContainer.logCascadingWorldGeneration = true;
        ForgeModContainer.fixVanillaCascading = false;
        log = LogManager.getLogger("forge");
    }
}
