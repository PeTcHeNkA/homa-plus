package plus.space.server.gen;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.*;
import net.minecraftforge.event.terraingen.TerrainGen;

import plus.fserver.utls.TsonThreeRegistry;
import plus.space.common.world.biome.IWorldGenThreesPatch;
import plus.space.server.biome.TsonBiomeServer;

import java.util.ArrayList;
import java.util.Random;


public class CustomThreeGen {
    public void generateTree(World worldIn, TsonBiomeServer biome, BlockPos pos, IBlockState state, Random rand){
        if (!TerrainGen.saplingGrowTree(worldIn, rand, pos)) return;

        BlockPlanks.EnumType type = state.getValue(BlockSapling.TYPE);
        Result res = isSquare(worldIn, pos, type);
        WorldGenerator worldgenerator = getEqGen(worldIn, biome, type, res.big);
        if(worldgenerator==null) {
            switch (type) {
                case SPRUCE:
                    if (res.big) {
                        worldgenerator = new WorldGenMegaPineTree(false, rand.nextBoolean());
                    } else {
                        worldgenerator = new WorldGenTaiga2(true);
                    }
                    break;
                case BIRCH:
                    worldgenerator = new WorldGenBirchTree(true, false);
                    break;
                case JUNGLE:
                    IBlockState iblockstate = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE);
                    IBlockState iblockstate1 = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE).withProperty(BlockLeaves.CHECK_DECAY, Boolean.FALSE);
                    if (res.big) {
                        worldgenerator = new WorldGenMegaJungle(true, 10, 20, iblockstate, iblockstate1);
                    } else {
                        worldgenerator = new WorldGenTrees(true, 4 + rand.nextInt(7), iblockstate, iblockstate1, false);
                    }
                    break;
                case ACACIA:
                    worldgenerator = new WorldGenSavannaTree(true);
                    break;
                case DARK_OAK:
                    if (res.big) {
                        worldgenerator = new WorldGenSavannaTree(true);
                    }
                    break;
                case OAK:
                    break;
            }
            if(worldgenerator==null){
                worldgenerator = rand.nextInt(10) == 0 ? new WorldGenBigTree(true) : new WorldGenTrees(true);
            }
        }

        IBlockState iblockstate2 = Blocks.AIR.getDefaultState();

        if (res.big) {
            worldIn.setBlockState(pos.add(res.dx, 0, res.dz), iblockstate2, 4);
            worldIn.setBlockState(pos.add(res.dx+1, 0, res.dz), iblockstate2, 4);
            worldIn.setBlockState(pos.add(res.dx, 0, res.dz+1), iblockstate2, 4);
            worldIn.setBlockState(pos.add(res.dx+1, 0, res.dz+1), iblockstate2, 4);
        } else {
            worldIn.setBlockState(pos, iblockstate2, 4);
        }

        if (!worldgenerator.generate(worldIn, rand, pos.add(res.dx, 0, res.dz))) {
            if (res.big) {
                worldIn.setBlockState(pos.add(res.dx, 0, res.dz), state, 4);
                worldIn.setBlockState(pos.add(res.dx+1, 0, res.dz), state, 4);
                worldIn.setBlockState(pos.add(res.dx, 0, res.dz+1), state, 4);
                worldIn.setBlockState(pos.add(res.dx+1, 0, res.dz+1), state, 4);
            } else {
                worldIn.setBlockState(pos, state, 4);
            }
        }
    }


    private Result isSquare(World worldIn, BlockPos pos, BlockPlanks.EnumType type){
        if(!this.isTypeAt(worldIn, pos, type))return new Result(false,0,0);
        if(isTypeAt(worldIn, pos.add(1,0,0), type)){
            if (isTypeAt(worldIn, pos.add(0,0,1), type)) {
                return new Result(isTypeAt(worldIn, pos.add(1,0,1), type), 0, 0);
            } else if (isTypeAt(worldIn, pos.add(0,0,-1), type)) {
                return new Result(isTypeAt(worldIn, pos.add(1,0,-1), type), 0, -1);
            }
        } else if (isTypeAt(worldIn, pos.add(-1,0,0), type)) {
            if (isTypeAt(worldIn, pos.add(0,0,1), type)) {
                return new Result(isTypeAt(worldIn, pos.add(-1,0,1), type), -1, 0);
            } else if (isTypeAt(worldIn, pos.add(0,0,-1), type)) {
                return new Result(isTypeAt(worldIn, pos.add(-1,0,-1), type), -1, 0);
            }
        }
        return new Result(false,0,0);
    }


    private static final class Result {
        final boolean big;
        final int dx, dz;
        public Result(boolean big, int dx, int dz) {
            this.big = big;
            this.dx = dx;
            this.dz = dz;
        }
    }


    public boolean isTypeAt(World worldIn, BlockPos pos, BlockPlanks.EnumType type) {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        return iblockstate.getBlock() == Blocks.SAPLING && iblockstate.getValue(BlockSapling.TYPE) == type;
    }


    public WorldGenAbstractTree getEqGen(World worldIn, TsonBiomeServer biome, BlockPlanks.EnumType type, boolean big){
        if(biome.threeGen==null)return null;
        int[] ids = biome.threeGen.ids;
        ArrayList<WorldGenAbstractTree> list = new ArrayList<>(ids.length);
        IntOpenHashSet set = new IntOpenHashSet();
        for (int id : ids) {
            WorldGenAbstractTree gen = TsonThreeRegistry.getById(id);
            if(set.add(id)){
                list.add(gen);
            } else if (isEqual(gen, type, big)) {
                list.add(gen);
            }
        }
        if(list.isEmpty())return null;
        if(list.size()==1)return list.get(0);
        else return list.get(worldIn.rand.nextInt(list.size()));
    }


    private boolean isEqBlocks(IBlockState s1, IBlockState s2){
        if(s1.getBlock() instanceof BlockNewLog){
            return s1.getValue(BlockNewLog.VARIANT) == s2.getValue(BlockNewLog.VARIANT);
        } else {
            return s1.getValue(BlockOldLog.VARIANT) == s2.getValue(BlockOldLog.VARIANT);
        }
    }


    private boolean isEqual(WorldGenAbstractTree gen, BlockPlanks.EnumType type, boolean big) {
        IBlockState check = null;
        if (big && (gen instanceof WorldGenHugeTrees)) {
            check = WorldGenHugeTreesPatch.getWoodState((WorldGenHugeTrees) gen);
        } else if (!big && (gen instanceof IWorldGenThreesPatch)) {
            check = ((IWorldGenThreesPatch) gen).getWood();
        }
        if (check == null) return false;

        switch (type) {
            case SPRUCE:
                if (isEqBlocks(check, TsonThreeRegistry.SPRUCE_LOG)) return true;
                break;
            case JUNGLE:
                if (isEqBlocks(check, TsonThreeRegistry.JUNGLE_LOG)) return true;
                break;
            case OAK:
                if (isEqBlocks(check, TsonThreeRegistry.DEFAULT_LOG)) return true;
                break;
            case ACACIA:
                if (isEqBlocks(check, TsonThreeRegistry.ACACIA_LOG)) return true;
                break;
            case BIRCH:
                if (isEqBlocks(check, TsonThreeRegistry.BIRCH_LOG)) return true;
                break;
            case DARK_OAK:
                if (isEqBlocks(check, TsonThreeRegistry.DARK_LOG)) return true;
                break;
        }
        return false;
    }
}


