package plus.fserver.plugin;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import static plus.fserver.event.ElcaCV.*;


public class SSPatchLoader extends TweakEngine implements IClassTransformer, ITweaker{
    public static final String ELCA_RELEASE = "Elca Enterprise Edition";
    public static final String ELCA_VERSION;

    static {
        String version = "2.3.7.04";
        boolean isDev = true;

        int num  = 0;
        boolean[] flags = {
            ENABLE_COLLISION_EVENTS       ,
            ENABLE_EXPLOSION_EVENT        ,
            AABB_MEMORY_IMPROVEMENT       ,
            ENABLE_NEIGHBOR_NOTIFY_EVENT  ,
            DEBUG_NO_SERVER               ,
            CHECK_LEGAL_CHUNK_CHANGE      ,
            MORE_MOBS_OPTIMIZER           ,
        };
        for (int i=0;i< flags.length;i++)
            if(flags[i]) num |= 1<<i;

        version += "-x"+Integer.toHexString(num);
        if(isDev){
            version += "-dev";
        }
        ELCA_VERSION = version;
    }

    public SSPatchLoader(){
        super("plus/fserver/plugin/SSPatchLoader.class");
        addPatch();
        addOverrides();
    }


    private void addPatch(){
        add("net.minecraft.block.BlockDynamicLiquid");
        add("net.minecraft.block.BlockGrass");
        add("net.minecraft.block.BlockLiquid");
        add("net.minecraft.block.BlockMushroom");
        add("net.minecraft.block.BlockPistonBase$1");
        add("net.minecraft.block.BlockPistonBase");
        add("net.minecraft.block.BlockRedstoneDiode");
        add("net.minecraft.block.BlockRedstoneWire$1");
        add("net.minecraft.block.BlockRedstoneWire$EnumAttachPosition");
        add("net.minecraft.block.BlockRedstoneWire");
        add("net.minecraft.block.BlockStaticLiquid");
        add("net.minecraft.block.BlockUtils");
        add("net.minecraft.block.material.Material$1");
        add("net.minecraft.block.material.Material");
        add("net.minecraft.block.properties.PropertyBool");
        add("net.minecraft.block.properties.PropertyEnum");
        add("net.minecraft.block.properties.PropertyHelper");
        add("net.minecraft.block.properties.PropertyInteger");
        add("net.minecraft.block.state.BlockPistonStructureHelper");
        add("net.minecraft.block.state.BlockStateBase$1");
        add("net.minecraft.block.state.BlockStateBase");
        add("net.minecraft.block.state.BlockStateContainer$1");
        add("net.minecraft.block.state.BlockStateContainer$Builder");
        add("net.minecraft.block.state.BlockStateContainer$StateImplementation");
        add("net.minecraft.block.state.BlockStateContainer");
        add("net.minecraft.block.state.IBlockState");
        add("net.minecraft.command.CommandResultStats$1");
        add("net.minecraft.command.CommandResultStats$Type");
        add("net.minecraft.command.CommandResultStats");
        add("net.minecraft.entity.ai.attributes.AttributeMap");
        add("net.minecraft.entity.ai.attributes.ModifiableAttributeInstance");
        add("net.minecraft.entity.ai.EntityAIEatGrass");
        add("net.minecraft.entity.ai.EntityAIMoveToBlock");
        add("net.minecraft.entity.ai.EntityAINearestAttackableTarget$1");
        add("net.minecraft.entity.ai.EntityAINearestAttackableTarget$Player2Double");
        add("net.minecraft.entity.ai.EntityAINearestAttackableTarget$Sorter");
        add("net.minecraft.entity.ai.EntityAINearestAttackableTarget");
        add("net.minecraft.entity.ai.EntityAITasks$EntityAITaskEntry");
        add("net.minecraft.entity.ai.EntityAITasks");
        add("net.minecraft.entity.Entity$1");
        add("net.minecraft.entity.Entity$2");
        add("net.minecraft.entity.Entity$3");
        add("net.minecraft.entity.Entity$4");
        add("net.minecraft.entity.Entity$5");
        add("net.minecraft.entity.Entity$6");
        add("net.minecraft.entity.Entity");
        add("net.minecraft.entity.EntityLiving$1");
        add("net.minecraft.entity.EntityLiving$SpawnPlacementType");
        add("net.minecraft.entity.EntityLiving");
        add("net.minecraft.entity.EntityLivingBase$1");
        add("net.minecraft.entity.EntityLivingBase");
        add("net.minecraft.entity.EntityTracker");
        add("net.minecraft.entity.EntityTrackerEntry");
        add("net.minecraft.entity.item.EntityBoat$1");
        add("net.minecraft.entity.item.EntityBoat$Status");
        add("net.minecraft.entity.item.EntityBoat$Type");
        add("net.minecraft.entity.item.EntityBoat");
        add("net.minecraft.entity.item.EntityMinecart$1");
        add("net.minecraft.entity.item.EntityMinecart$Type");
        add("net.minecraft.entity.item.EntityMinecart");
        add("net.minecraft.entity.player.EntityPlayer$1");
        add("net.minecraft.entity.player.EntityPlayer$2");
        add("net.minecraft.entity.player.EntityPlayer$EnumChatVisibility");
        add("net.minecraft.entity.player.EntityPlayer$SleepEnemyPredicate");
        add("net.minecraft.entity.player.EntityPlayer$SleepResult");
        add("net.minecraft.entity.player.EntityPlayer");
        add("net.minecraft.entity.player.EntityPlayerMP$1");
        add("net.minecraft.entity.player.EntityPlayerMP");
        add("net.minecraft.inventory.Container");
        add("net.minecraft.inventory.InventoryCrafting");
        add("net.minecraft.item.crafting.CraftingManager");
        add("net.minecraft.item.Item$1");
        add("net.minecraft.item.Item$10");
        add("net.minecraft.item.Item$11");
        add("net.minecraft.item.Item$12");
        add("net.minecraft.item.Item$13");
        add("net.minecraft.item.Item$14");
        add("net.minecraft.item.Item$15");
        add("net.minecraft.item.Item$16");
        add("net.minecraft.item.Item$17");
        add("net.minecraft.item.Item$18");
        add("net.minecraft.item.Item$19");
        add("net.minecraft.item.Item$2");
        add("net.minecraft.item.Item$20");
        add("net.minecraft.item.Item$21");
        add("net.minecraft.item.Item$22");
        add("net.minecraft.item.Item$3");
        add("net.minecraft.item.Item$4");
        add("net.minecraft.item.Item$5");
        add("net.minecraft.item.Item$6");
        add("net.minecraft.item.Item$7");
        add("net.minecraft.item.Item$8");
        add("net.minecraft.item.Item$9");
        add("net.minecraft.item.Item$ToolMaterial");
        add("net.minecraft.item.Item");
        add("net.minecraft.nbt.NBTTagCompound");
        add("net.minecraft.network.datasync.EntityDataManager$DataEntry");
        add("net.minecraft.network.datasync.EntityDataManager");
        add("net.minecraft.network.NetHandlerPlayServer$1");
        add("net.minecraft.network.NetHandlerPlayServer$2");
        add("net.minecraft.network.NetHandlerPlayServer");
        add("net.minecraft.network.NetworkManager$1");
        add("net.minecraft.network.NetworkManager$2");
        add("net.minecraft.network.NetworkManager$3");
        add("net.minecraft.network.NetworkManager$4");
        add("net.minecraft.network.NetworkManager$5");
        add("net.minecraft.network.NetworkManager$InboundHandlerTuplePacketListener");
        add("net.minecraft.network.NetworkManager");
        add("net.minecraft.network.play.client.CPacketClientSettings");
        add("net.minecraft.pathfinding.FlyingNodeProcessor");
        add("net.minecraft.pathfinding.NodeProcessor");
        add("net.minecraft.pathfinding.PathHeap");
        add("net.minecraft.pathfinding.PathNavigate");
        add("net.minecraft.pathfinding.PathNavigateGround");
        add("net.minecraft.pathfinding.PathNodeType$Set");
        add("net.minecraft.pathfinding.PathNodeType");
        add("net.minecraft.pathfinding.PathWorldListener");
        add("net.minecraft.pathfinding.SwimNodeProcessor");
        add("net.minecraft.pathfinding.WalkNodeProcessor");
        add("net.minecraft.scoreboard.Scoreboard");
        add("net.minecraft.server.dedicated.DedicatedServer$1");
        add("net.minecraft.server.dedicated.DedicatedServer");
        add("net.minecraft.server.integrated.IntegratedServer");
        add("net.minecraft.server.management.PlayerChunkMap$1");
        add("net.minecraft.server.management.PlayerChunkMap");
        add("net.minecraft.server.management.PlayerChunkMapEntry");
        add("net.minecraft.server.management.PlayerList$1");
        add("net.minecraft.server.management.PlayerList");
        add("net.minecraft.server.MinecraftServer$1");
        add("net.minecraft.server.MinecraftServer$2");
        add("net.minecraft.server.MinecraftServer");
        add("net.minecraft.util.ArrayListE1V");
        add("net.minecraft.util.ArrayListEA");
        add("net.minecraft.util.BitArray");
        add("net.minecraft.util.BlockIDIndexes$Index");
        add("net.minecraft.util.BlockIDIndexes");
        add("net.minecraft.util.ByClass");
        add("net.minecraft.util.ClassInheritanceMultiMap$FastIter");
        add("net.minecraft.util.ClassInheritanceMultiMap");
        add("net.minecraft.util.EmptyByClass");
        add("net.minecraft.util.math.AxisAlignedBB");
        add("net.minecraft.util.math.BlockPos$1$1");
        add("net.minecraft.util.math.BlockPos$1");
        add("net.minecraft.util.math.BlockPos$2$1");
        add("net.minecraft.util.math.BlockPos$2");
        add("net.minecraft.util.math.BlockPos$3");
        add("net.minecraft.util.math.BlockPos$MutableBlockPos");
        add("net.minecraft.util.math.BlockPos$PooledMutableBlockPos$1");
        add("net.minecraft.util.math.BlockPos$PooledMutableBlockPos");
        add("net.minecraft.util.math.BlockPos");
        add("net.minecraft.util.math.Vec3i");
        add("net.minecraft.world.biome.BiomeProvider");
        add("net.minecraft.world.biome.BiomeProviderSingle");
        add("net.minecraft.world.border.WorldBorder");
        add("net.minecraft.world.chunk.BlockStateContainer");
        add("net.minecraft.world.chunk.BlockStatePaletteHashMap");
        add("net.minecraft.world.chunk.BlockStatePaletteLinear");
        add("net.minecraft.world.chunk.BlockStatePaletteRegistry");
        add("net.minecraft.world.chunk.Chunk$EnumCreateEntityType");
        add("net.minecraft.world.chunk.Chunk");
        add("net.minecraft.world.chunk.ChunkPrimer");
        add("net.minecraft.world.chunk.IBlockStatePalette");
        add("net.minecraft.world.chunk.storage.ChunkBuffer");
        add("net.minecraft.world.chunk.storage.ExtendedBlockStorage");
        add("net.minecraft.world.DimensionType");
        add("net.minecraft.world.Explosion");
        add("net.minecraft.world.GameRules$Value");
        add("net.minecraft.world.GameRules$ValueType");
        add("net.minecraft.world.GameRules");
        add("net.minecraft.world.gen.ChunkGeneratorHell$GenSession");
        add("net.minecraft.world.gen.ChunkGeneratorHell");
        add("net.minecraft.world.gen.ChunkGeneratorOverworld");
        add("net.minecraft.world.gen.ChunkProviderServer");
        add("net.minecraft.world.gen.feature.CWorldGenerator");
        add("net.minecraft.world.gen.feature.StonePredicateV2");
        add("net.minecraft.world.gen.feature.WorldGenAbstractTree");
        add("net.minecraft.world.gen.feature.WorldGenBigMushroom");
        add("net.minecraft.world.gen.feature.WorldGenBigTree$1");
        add("net.minecraft.world.gen.feature.WorldGenBigTree$FoliageCoordinates");
        add("net.minecraft.world.gen.feature.WorldGenBigTree$GenSession");
        add("net.minecraft.world.gen.feature.WorldGenBigTree");
        add("net.minecraft.world.gen.feature.WorldGenBirchTree");
        add("net.minecraft.world.gen.feature.WorldGenBlockBlob");
        add("net.minecraft.world.gen.feature.WorldGenCactus");
        add("net.minecraft.world.gen.feature.WorldGenClay");
        add("net.minecraft.world.gen.feature.WorldGenDeadBush");
        add("net.minecraft.world.gen.feature.WorldGenDungeons");
        add("net.minecraft.world.gen.feature.WorldGenerator");
        add("net.minecraft.world.gen.feature.WorldGenFire");
        add("net.minecraft.world.gen.feature.WorldGenFlowers");
        add("net.minecraft.world.gen.feature.WorldGenGlowStone1");
        add("net.minecraft.world.gen.feature.WorldGenGlowStone2");
        add("net.minecraft.world.gen.feature.WorldGenHugeTrees");
        add("net.minecraft.world.gen.feature.WorldGenIcePath");
        add("net.minecraft.world.gen.feature.WorldGenLakes");
        add("net.minecraft.world.gen.feature.WorldGenLiquids");
        add("net.minecraft.world.gen.feature.WorldGenMegaJungle");
        add("net.minecraft.world.gen.feature.WorldGenMegaPineTree");
        add("net.minecraft.world.gen.feature.WorldGenMinable");
        add("net.minecraft.world.gen.feature.WorldGenPumpkin");
        add("net.minecraft.world.gen.feature.WorldGenSand");
        add("net.minecraft.world.gen.feature.WorldGenSavannaTree");
        add("net.minecraft.world.gen.feature.WorldGenShrub");
        add("net.minecraft.world.gen.feature.WorldGenSwamp");
        add("net.minecraft.world.gen.feature.WorldGenTaiga1");
        add("net.minecraft.world.gen.feature.WorldGenTaiga2");
        add("net.minecraft.world.gen.feature.WorldGenTallGrass");
        add("net.minecraft.world.gen.feature.WorldGenTrees");
        add("net.minecraft.world.gen.feature.WorldGenVines");
        add("net.minecraft.world.gen.feature.WorldGenWaterlily");
        add("net.minecraft.world.gen.IChunkGenerator");
        add("net.minecraft.world.gen.NoiseGeneratorImproved");
        add("net.minecraft.world.gen.NoiseGeneratorOctaves");
        add("net.minecraft.world.gen.structure.StructureComponent$1");
        add("net.minecraft.world.gen.structure.StructureComponent$BlockSelector");
        add("net.minecraft.world.gen.structure.StructureComponent");
        add("net.minecraft.world.gen.structure.StructureVillagePieces$1");
        add("net.minecraft.world.gen.structure.StructureVillagePieces$Church");
        add("net.minecraft.world.gen.structure.StructureVillagePieces$Field1");
        add("net.minecraft.world.gen.structure.StructureVillagePieces$Field2");
        add("net.minecraft.world.gen.structure.StructureVillagePieces$Hall");
        add("net.minecraft.world.gen.structure.StructureVillagePieces$House1");
        add("net.minecraft.world.gen.structure.StructureVillagePieces$House2");
        add("net.minecraft.world.gen.structure.StructureVillagePieces$House3");
        add("net.minecraft.world.gen.structure.StructureVillagePieces$House4Garden");
        add("net.minecraft.world.gen.structure.StructureVillagePieces$Path");
        add("net.minecraft.world.gen.structure.StructureVillagePieces$PieceWeight");
        add("net.minecraft.world.gen.structure.StructureVillagePieces$Road");
        add("net.minecraft.world.gen.structure.StructureVillagePieces$Start");
        add("net.minecraft.world.gen.structure.StructureVillagePieces$Torch");
        add("net.minecraft.world.gen.structure.StructureVillagePieces$Village");
        add("net.minecraft.world.gen.structure.StructureVillagePieces$Well");
        add("net.minecraft.world.gen.structure.StructureVillagePieces$WoodHut");
        add("net.minecraft.world.gen.structure.StructureVillagePieces");
        add("net.minecraft.world.IBlockAccess");
        add("net.minecraft.world.ServerWorldEventHandler");
        add("net.minecraft.world.SpawnPos");
        add("net.minecraft.world.World$1");
        add("net.minecraft.world.World$2");
        add("net.minecraft.world.World$3");
        add("net.minecraft.world.World$4");
        add("net.minecraft.world.World$5");
        add("net.minecraft.world.World");
        add("net.minecraft.world.WorldEntitySpawner");
        add("net.minecraft.world.WorldProviderHell$1");
        add("net.minecraft.world.WorldProviderHell");
        add("net.minecraft.world.WorldServer$1");
        add("net.minecraft.world.WorldServer$ServerBlockEventList");
        add("net.minecraft.world.WorldServer");
        add("net.minecraftforge.common.chunkio.ChunkIOExecutor");
        add("net.minecraftforge.common.DimensionManager$1");
        add("net.minecraftforge.common.DimensionManager$Dimension");
        add("net.minecraftforge.common.DimensionManager");
        add("net.minecraftforge.event.entity.EntityEvent$CanUpdate");
        add("net.minecraftforge.event.entity.EntityEvent$EnteringChunk");
        add("net.minecraftforge.event.entity.EntityEvent$EntityConstructing");
        add("net.minecraftforge.event.entity.EntityEvent");
        add("net.minecraftforge.event.entity.living.LivingAttackEvent");
        add("net.minecraftforge.event.entity.living.LivingDamageEvent");
        add("net.minecraftforge.event.entity.living.LivingEvent$LivingJumpEvent");
        add("net.minecraftforge.event.entity.living.LivingEvent$LivingUpdateEvent");
        add("net.minecraftforge.event.entity.living.LivingEvent");
        add("net.minecraftforge.event.entity.living.LivingHurtEvent");
        add("net.minecraftforge.event.world.ChunkWatchEvent$UnWatch");
        add("net.minecraftforge.event.world.ChunkWatchEvent$Watch");
        add("net.minecraftforge.event.world.ChunkWatchEvent");
        add("net.minecraftforge.event.world.GetCollisionBoxesEvent");
    }


    private void addOverrides(){
        add("net.minecraft.server.MinecraftServer", true);
        add("net.minecraft.server.MinecraftServer$1", true);
        add("net.minecraft.server.MinecraftServer$2", true);
        add("net.minecraft.server.dedicated.DedicatedServer", true);
        add("net.minecraft.server.dedicated.DedicatedServer$1", true);
        add("net.minecraft.server.dedicated.DedicatedServer$2", true);
        add("net.minecraftforge.common.DimensionManager", true);
        add("net.minecraftforge.common.chunkio.ChunkIOExecutor", true);
    }


    public static void printLogo(){
        String logo = "\n" +
                "     _____  _             \n" +
                "    | ____|| |  ___  __ _ \n" +
                "    |  _|  | | / __|/ _` |\n" +
                "    | |___ | || (__| (_| |\n" +
                "    |_____||_| \\___|\\__,_|\n" +
                "\n" +
                "  ___ ___  ____ _  __ ___  ____\n" +
                " (_-</ -_)/ __/| |/ // -_)/ __/\n" +
                "/___/\\__//_/   |___/ \\__//_/   v"+ ELCA_VERSION +"\n\n";
        LogManager.getLogger("ELCA").info(logo);
    }


    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {}


    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.fserver.json");
        try {
            editTransformers(classLoader);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            classLoader.registerTransformer("plus.fserver.plugin.SSPatchLoader");
        }
        preLoadClasses();
        try {
            ElcaBiomes.registerIfNeed();
        } catch (Throwable t){
            t.printStackTrace();
        }
    }


    private List<IClassTransformer> transformers(LaunchClassLoader classLoader) throws NoSuchFieldException, IllegalAccessException {
        Field field = LaunchClassLoader.class.getDeclaredField("transformers");
        field.setAccessible(true);
        return  (List<IClassTransformer>) field.get(classLoader);
    }


    private void editTransformers(LaunchClassLoader classLoader) throws NoSuchFieldException, IllegalAccessException {
        List<IClassTransformer> res = transformers(classLoader);
        res.add(this);
    }


    @Override
    public String getLaunchTarget() {
        return "net.minecraft.server.MinecraftServer";
    }


    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }
}