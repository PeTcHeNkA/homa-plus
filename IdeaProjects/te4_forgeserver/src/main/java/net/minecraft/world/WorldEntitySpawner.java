package net.minecraft.world;

import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.init.Blocks;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.eventhandler.Event;
import plus.fserver.api.Keep;


public class WorldEntitySpawner {
    @Keep
    public static final EnumCreatureType[] TYPES = EnumCreatureType.values();

    @Keep
    public int findChunksForSpawning(WorldServer world,
                                     boolean spawnHostileMobs,
                                     boolean spawnPeacefulMobs,
                                     boolean spawnOnSetTickRate
    ){return 0;}


    public static void getRandomChunkPosition(SpawnPos pos, int x, int z) {
        Chunk chunk = pos.chunk = pos.world.getChunk(x, z);
        int rand = pos.world.rand.nextInt();
        int nX = x >> 4 + (rand & 15);
        int nZ = z >> 4 + ((rand >> 4) & 15);
        int k = MathHelper.roundUp(chunk.getHeight(new BlockPos(nX, 0, nZ)) + 1, 16);
        int nY = pos.world.rand.nextInt(k > 0 ? k : chunk.getTopFilledSegment() + 16 - 1);
        pos.setPos(nX, nY, nZ);
    }


    public static boolean isValidEmptySpawnBlock(IBlockState state) {
        if (state.isBlockNormalCube()) {
            return false;
        } else if (state.canProvidePower()) {
            return false;
        } else if (state.getMaterial().isLiquid()) {
            return false;
        } else {
            return !BlockRailBase.isRailBlock(state);
        }
    }


    public static boolean canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType type, World world, BlockPos pos) {
        if (!world.getWorldBorder().contains(pos)) {
            return false;
        } else {
            return type.canSpawnAt(world, pos);
        }
    }


    public static boolean canCreatureTypeSpawnBody(EntityLiving.SpawnPlacementType type, World world, BlockPos pos) {{
            IBlockState state = world.access().getBlockState(pos);

            if (type == EntityLiving.SpawnPlacementType.IN_WATER) {
                return state.getMaterial() == Material.WATER &&
                        world.access().getBlockState(pos.getX(), pos.getY()-1, pos.getZ()).getMaterial() == Material.WATER
                        && !world.access().getBlockState(pos.getX(), pos.getY()+1, pos.getZ()).isNormalCube();
            } else {
                BlockPos down = pos.down();
                IBlockState downState = world.getBlockState(down);

                if (!downState.getBlock().canCreatureSpawn(downState, world, down, type)) {
                    return false;
                } else {
                    Block block = world.access().getBlockState(down).getBlock();
                    return block != Blocks.BEDROCK && block != Blocks.BARRIER && isValidEmptySpawnBlock(state)
                            && isValidEmptySpawnBlock(world.access().getBlockState(pos.getX(), pos.getY()+1, pos.getZ()));
                }
            }
        }
    }


    public static void performWorldGenSpawning(World worldIn, Biome biomeIn, int centerX, int centerZ, int diameterX, int diameterZ, Random randomIn) {
        List<Biome.SpawnListEntry> list = biomeIn.getSpawnableList(EnumCreatureType.CREATURE);

        if (!list.isEmpty()) {
            while (randomIn.nextFloat() < biomeIn.getSpawningChance()) {
                Biome.SpawnListEntry listEntry = WeightedRandom.getRandomItem(worldIn.rand, list);
                int i = listEntry.minGroupCount + randomIn.nextInt(1 + listEntry.maxGroupCount - listEntry.minGroupCount);
                IEntityLivingData ientitylivingdata = null;
                int j = centerX + randomIn.nextInt(diameterX);
                int k = centerZ + randomIn.nextInt(diameterZ);
                int l = j;
                int i1 = k;

                for (int j1 = 0; j1 < i; ++j1) {
                    boolean flag = false;

                    for (int k1 = 0; !flag && k1 < 4; ++k1) {
                        BlockPos blockpos = worldIn.getTopSolidOrLiquidBlock(new BlockPos(j, 0, k));

                        if (canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType.ON_GROUND, worldIn, blockpos)) {
                            EntityLiving entityliving;

                            try {
                                entityliving = listEntry.newInstance(worldIn);
                            } catch (Exception exception) {
                                exception.printStackTrace();
                                continue;
                            }

                            if (worldIn.canEntitySpawn(entityliving, j + 0.5f, (float) blockpos.getY(), k +0.5f) == Event.Result.DENY) continue;
                            entityliving.setLocationAndAngles((float)j + 0.5F, blockpos.getY(), (float)k + 0.5F, randomIn.nextFloat() * 360.0F, 0.0F);
                            worldIn.spawnEntity(entityliving);
                            ientitylivingdata = entityliving.onInitialSpawn(worldIn.getDifficultyForLocation(new BlockPos(entityliving)), ientitylivingdata);
                            flag = true;
                        }

                        j += randomIn.nextInt(5) - randomIn.nextInt(5);

                        for (k += randomIn.nextInt(5) - randomIn.nextInt(5); j < centerX || j >= centerX + diameterX || k < centerZ || k >= centerZ + diameterX; k = i1 + randomIn.nextInt(5) - randomIn.nextInt(5)) {
                            j = l + randomIn.nextInt(5) - randomIn.nextInt(5);
                        }
                    }
                }
            }
        }
    }
}