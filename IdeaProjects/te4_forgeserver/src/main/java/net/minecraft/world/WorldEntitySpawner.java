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


    @Keep
    public static void getRandomChunkPosition(SpawnPos pos, int x, int z) {
        Chunk chunk = pos.chunk = pos.world.getChunk(x, z);
        int rand = pos.world.rand.nextInt();
        int nX = x >> 4 + (rand & 15);
        int nZ = z >> 4 + ((rand >> 4) & 15);
        int k = MathHelper.roundUp(chunk.getHeight(new BlockPos(nX, 0, nZ)) + 1, 16);
        int nY = pos.world.rand.nextInt(k > 0 ? k : chunk.getTopFilledSegment() + 16 - 1);
        pos.setPos(nX, nY, nZ);
    }


    @Keep
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


    @Keep
    public static boolean canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType type, World world, BlockPos pos) {
        if(world.isInWorldBorder(pos.getX(), pos.getZ())){
            return type.canSpawnAt(world, pos);
        } else {
            return false;
        }
    }


    @Keep
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


    @Keep
    public static void performWorldGenSpawning(World worldIn, Biome biomeIn, int centerX, int centerZ, int diameterX, int diameterZ, Random rand) {
        List<Biome.SpawnListEntry> list = biomeIn.getSpawnableList(EnumCreatureType.CREATURE);

        if (!list.isEmpty()) {
            while (rand.nextFloat() < biomeIn.getSpawningChance()) {
                Biome.SpawnListEntry listEntry = WeightedRandom.getRandomItem(worldIn.rand, list);
                EntityLiving prev = null;
                int i = listEntry.minGroupCount + rand.nextInt(1 + listEntry.maxGroupCount - listEntry.minGroupCount);
                IEntityLivingData ientitylivingdata = null;

                int px = centerX + rand.nextInt(diameterX);
                int pz = centerZ + rand.nextInt(diameterZ);

                int px2 = px;
                int pz2 = pz;

                for (int j1 = 0; j1 < i; ++j1) {
                    boolean flag = false;

                    for (int k1 = 0; !flag && k1 < 4; ++k1) {
                        BlockPos pos = worldIn.getTopSolidOrLiquidBlock(new BlockPos(px, 0, pz));

                        if (canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType.ON_GROUND, worldIn, pos)) {
                            EntityLiving toSpawn;

                            try {
                                if(prev != null) toSpawn = prev;
                                else toSpawn = prev = listEntry.newInstance(worldIn);
                            } catch (Exception exception) {
                                exception.printStackTrace();
                                continue;
                            }

                            if (worldIn.canEntitySpawn(toSpawn, px + 0.5f, (float) pos.getY(), pz +0.5f) == Event.Result.DENY) continue;
                            prev = null;
                            toSpawn.setLocationAndAngles((float)px + 0.5F, pos.getY(), (float)pz + 0.5F, rand.nextFloat() * 360.0F, 0.0F);
                            worldIn.spawnEntity(toSpawn);
                            ientitylivingdata = toSpawn.onInitialSpawn(worldIn.getDifficultyForLocation(new BlockPos(toSpawn)), ientitylivingdata);
                            flag = true;
                        }

                        int bits = rand.nextInt();

                        px += (((bits & 7) % 5) - ((bits>>3 & 7) % 5));
                        //px += rand.nextInt(5) - rand.nextInt(5);

                        for (pz += rand.nextInt(5) - rand.nextInt(5); px < centerX || px >= centerX + diameterX || pz < centerZ || pz >= centerZ + diameterX; pz = pz2 + rand.nextInt(5) - rand.nextInt(5)) {
                            bits = rand.nextInt();
                            px = px2 + (((bits & 7) % 5) - ((bits>>3 & 7) % 5));
                        }
                    }
                }
            }
        }
    }
}