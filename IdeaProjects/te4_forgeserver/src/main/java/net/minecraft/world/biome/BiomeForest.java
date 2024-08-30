package net.minecraft.world.biome;

import net.minecraft.entity.passive.*;
import net.minecraft.init.*;
import java.util.*;
import net.minecraft.world.*;
import net.minecraft.util.math.*;
import net.minecraftforge.event.terraingen.*;
import net.minecraft.world.gen.feature.*;
import net.minecraft.block.*;
import net.minecraftforge.fml.relauncher.*;
import plus.fserver.server.elca.ChunkSelector;


public class BiomeForest extends Biome {
    protected static final WorldGenBirchTree SUPER_BIRCH_TREE;
    protected static final WorldGenBirchTree BIRCH_TREE;
    protected static final WorldGenCanopyTree ROOF_TREE;
    private final Type type;

    public BiomeForest(final Type typeIn, final BiomeProperties properties) {
        super(properties);
        this.type = typeIn;
        this.decorator.treesPerChunk = 10;
        this.decorator.grassPerChunk = 2;
        if (this.type == Type.FLOWER) {
            this.decorator.treesPerChunk = 6;
            this.decorator.flowersPerChunk = 100;
            this.decorator.grassPerChunk = 1;
            this.spawnableCreatureList.add(new SpawnListEntry(EntityRabbit.class, 4, 2, 3));
        }
        if (this.type == Type.NORMAL) {
            this.spawnableCreatureList.add(new SpawnListEntry(EntityWolf.class, 5, 4, 4));
        }
        if (this.type == Type.ROOFED) {
            this.decorator.treesPerChunk = -999;
        }
        if (this.type == Type.FLOWER) {
            this.flowers.clear();
            for (BlockFlower.EnumFlowerType type : BlockFlower.EnumFlowerType.values()) {
                if (type.getBlockType() != BlockFlower.EnumFlowerColor.YELLOW) {
                    if (type == BlockFlower.EnumFlowerType.BLUE_ORCHID) {
                        type = BlockFlower.EnumFlowerType.POPPY;
                    }
                    this.addFlower(Blocks.RED_FLOWER.getDefaultState()
                            .withProperty(Blocks.RED_FLOWER.getTypeProperty(), type), 10);
                }
            }
        }
    }

    public WorldGenAbstractTree getRandomTreeFeature(final Random rand) {
        if (this.type == Type.ROOFED && rand.nextInt(3) > 0) {
            return BiomeForest.ROOF_TREE;
        }
        if (this.type != Type.BIRCH && rand.nextInt(5) != 0) {
            return (rand.nextInt(10) == 0) ? BiomeForest.BIG_TREE_FEATURE : BiomeForest.TREE_FEATURE;
        }
        return BiomeForest.BIRCH_TREE;
    }

    public BlockFlower.EnumFlowerType pickRandomFlower(final Random rand, final BlockPos pos) {
        if (this.type == Type.FLOWER) {
            final double d0 = MathHelper.clamp((1.0 + BiomeForest.GRASS_COLOR_NOISE.getValue(pos.getX() / 48.0, pos.getZ() / 48.0)) / 2.0, 0.0, 0.9999);
            final BlockFlower.EnumFlowerType blockflower$enumflowertype = BlockFlower.EnumFlowerType.values()[(int)(d0 * BlockFlower.EnumFlowerType.values().length)];
            return (blockflower$enumflowertype == BlockFlower.EnumFlowerType.BLUE_ORCHID) ? BlockFlower.EnumFlowerType.POPPY : blockflower$enumflowertype;
        }
        return super.pickRandomFlower(rand, pos);
    }


    @Override
    public void decorate(final World worldIn, final Random rand, final BlockPos pos) {
        if (this.type == Type.ROOFED) {
            this.addMushrooms(worldIn, rand, pos);
        }
        if (TerrainGen.decorate(worldIn, rand, new ChunkPos(pos), DecorateBiomeEvent.Decorate.EventType.FLOWERS)) {
            int i = rand.nextInt(5) - 3;
            if (this.type == Type.FLOWER) {
                i += 2;
            }
            this.addDoublePlants(worldIn, rand, pos, i);
        }
        super.decorate(worldIn, rand, pos);
    }


    public void addMushrooms(final World world, final Random rand, final BlockPos pos) {
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        ChunkSelector selector = new ChunkSelector(world);
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 4; ++j) {
                final int fx = i * 4 + 1 + 8 + rand.nextInt(3);
                final int fz = j * 4 + 1 + 8 + rand.nextInt(3);

                mut.setPosB(fx, selector.getHeight(fx, fz), fz);

                if (rand.nextInt(20) == 0 && TerrainGen.decorate(world, rand, new ChunkPos(pos), mut, DecorateBiomeEvent.Decorate.EventType.BIG_SHROOM)) {
                    final WorldGenBigMushroom mushroom = new WorldGenBigMushroom();
                    mushroom.generate(world, selector, rand, mut);
                }
                else if (TerrainGen.decorate(world, rand, new ChunkPos(pos), mut, DecorateBiomeEvent.Decorate.EventType.TREE)) {
                    final WorldGenAbstractTree tree = this.getRandomTreeFeature(rand);
                    tree.setDecorationDefaults();
                    if (tree.generate(world, selector, rand, mut)) {
                        tree.generateSaplings(world, rand, mut);
                    }
                }
            }
        }
    }


    public void addDoublePlants(final World world, final Random rand, final BlockPos pos, final int count) {
        if(count == 0)return;
        ChunkSelector selector = new ChunkSelector(world);
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        BlockDoublePlant.EnumPlantType type;
        for (int i = 0; i < count; ++i) {
            final int j = rand.nextInt(3);
            if (j == 0) {
                type = BlockDoublePlant.EnumPlantType.SYRINGA;
            }
            else if (j == 1) {
                type = BlockDoublePlant.EnumPlantType.ROSE;
            }
            else {
                type = BlockDoublePlant.EnumPlantType.PAEONIA;
            }
            for (int k = 0; k < 5; ++k) {
                final int l = rand.nextInt(16) + 8;
                final int i2 = rand.nextInt(16) + 8;
                final int j2 = rand.nextInt(world.getHeight(pos.add(l, 0, i2)).getY() + 32);

                if (WorldGenDoublePlant.generate(
                        world, selector, rand, mut.setPos(pos.getX() + l, j2, pos.getZ() + i2), type)) {
                    break;
                }
            }
        }
    }


    @Override
    public Class<? extends Biome> getBiomeClass() {
        return BiomeForest.class;
    }


    @SideOnly(Side.CLIENT)
    public int getGrassColorAtPos(final BlockPos pos) {
        final int i = super.getGrassColorAtPos(pos);
        return (this.type == Type.ROOFED) ? ((i & 0xFEFEFE) + 2634762 >> 1) : i;
    }


    static {
        SUPER_BIRCH_TREE = new WorldGenBirchTree(false, true);
        BIRCH_TREE = new WorldGenBirchTree(false, false);
        ROOF_TREE = new WorldGenCanopyTree(false);
    }


    public enum Type {
        NORMAL,
        FLOWER,
        BIRCH,
        ROOFED;
    }
}