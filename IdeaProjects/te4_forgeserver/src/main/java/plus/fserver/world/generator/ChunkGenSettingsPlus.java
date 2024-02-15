package plus.fserver.world.generator;

import net.minecraft.block.BlockStone;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;


public class ChunkGenSettingsPlus {
    public final ChunkGeneratorSettings settings;

    public final WorldGenMinable dirtGen;
    public final WorldGenMinable gravelOreGen;
    public final WorldGenMinable graniteGen;
    public final WorldGenMinable dioriteGen;
    public final WorldGenMinable andesiteGen;
    public final WorldGenMinable coalGen;
    public final WorldGenMinable ironGen;
    public final WorldGenMinable goldGen;
    public final WorldGenMinable redstoneGen;
    public final WorldGenMinable diamondGen;
    public final WorldGenMinable lapisGen;

    public ChunkGenSettingsPlus(ChunkGeneratorSettings settings) {
        this.settings = settings;
        this.dirtGen      = new WorldGenMinable(Blocks.DIRT.getDefaultState(), settings.dirtSize);
        this.gravelOreGen = new WorldGenMinable(Blocks.GRAVEL.getDefaultState(), settings.gravelSize);
        this.graniteGen   = new WorldGenMinable(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE), settings.graniteSize);
        this.dioriteGen   = new WorldGenMinable(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE), settings.dioriteSize);
        this.andesiteGen  = new WorldGenMinable(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE), settings.andesiteSize);
        this.coalGen      = new WorldGenMinable(Blocks.COAL_ORE.getDefaultState(), settings.coalSize);
        this.ironGen      = new WorldGenMinable(Blocks.IRON_ORE.getDefaultState(), settings.ironSize);
        this.goldGen      = new WorldGenMinable(Blocks.GOLD_ORE.getDefaultState(), settings.goldSize);
        this.redstoneGen  = new WorldGenMinable(Blocks.REDSTONE_ORE.getDefaultState(), settings.redstoneSize);
        this.diamondGen   = new WorldGenMinable(Blocks.DIAMOND_ORE.getDefaultState(), settings.diamondSize);
        this.lapisGen     = new WorldGenMinable(Blocks.LAPIS_ORE.getDefaultState(), settings.lapisSize);
    }
}
