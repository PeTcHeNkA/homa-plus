package net.minecraft.world;

import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import plus.fserver.ElcaConfig;
import plus.fserver.utls.math.FastRandom;
import plus.tson.utl.Te4HashSet;


public final class WorldEntitySpawner {
    private static final EnumCreatureType[] TYPES = EnumCreatureType.values();
    private static final int MOB_COUNT_DIV = (int)Math.pow(17.0D, 2.0D);
    private final Set<ChunkPos> eligibleChunksForSpawning = new Te4HashSet<>();
    private final int[] countCache = new int[TYPES.length];
    private int recheck = 0;

    public WorldEntitySpawner(){
        Arrays.fill(countCache, -1);
    }

    public int findChunksForSpawning(WorldServer world,
                                     boolean spawnHostileMobs,
                                     boolean spawnPeacefulMobs,
                                     boolean spawnOnSetTickRate
    ) {
        if (!spawnHostileMobs && !spawnPeacefulMobs) {
            return 0;
        } else {
            this.eligibleChunksForSpawning.clear();
            int factor = 0;

            for (EntityPlayer entityplayer : world.playerEntities) {
                if (!entityplayer.isSpectator()) {
                    int j = MathHelper.floor(entityplayer.posX / 16.0D);
                    int k = MathHelper.floor(entityplayer.posZ / 16.0D);

                    for (int i1 = -8; i1 <= 8; ++i1) {
                        for (int j1 = -8; j1 <= 8; ++j1) {
                            boolean flag = i1 == -8 || i1 == 8 || j1 == -8 || j1 == 8;
                            ChunkPos chunkpos = new ChunkPos(i1 + j, j1 + k);

                            if (!this.eligibleChunksForSpawning.contains(chunkpos)) {
                                ++factor;

                                if (!flag && world.getWorldBorder().contains(chunkpos)) {
                                    PlayerChunkMapEntry playerchunkmapentry = world.getPlayerChunkMap().getEntry(chunkpos.x, chunkpos.z);

                                    if (playerchunkmapentry != null && playerchunkmapentry.isSentToPlayers()) {
                                        this.eligibleChunksForSpawning.add(chunkpos);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            int spawned = 0, cursor = 0;

            if(recheck++>ElcaConfig.RECHECK_SPAWNER_RATE){
                Arrays.fill(countCache, -1);
                recheck = 0;
            }

            SpawnPos spawnPos = null;
            //BlockPos worldSpawn = world.getSpawnPoint();

            for (EnumCreatureType enumcreaturetype : TYPES) {
                if ((!enumcreaturetype.getPeacefulCreature() || spawnPeacefulMobs) && (enumcreaturetype.getPeacefulCreature() || spawnHostileMobs) && (!enumcreaturetype.getAnimal() || spawnOnSetTickRate)) {
                    int count = countCache[cursor];
                    if(count == -1)
                        count = countCache[cursor] = world.countEntities(enumcreaturetype, true);

                    int maxCount = enumcreaturetype.getMaxNumberOfCreature() * factor / MOB_COUNT_DIV;

                    if (count <= maxCount) {
                        if(spawnPos==null) spawnPos = new SpawnPos(world);
                        ChunkPos[] shuffled = this.eligibleChunksForSpawning.toArray(new ChunkPos[0]);
                        FastRandom.INST.shuffle(shuffled);

                        inner:

                        for (ChunkPos pos : shuffled) {
                            spawnPos.setRandomChunkPos(pos.x, pos.z);
                            int sX = spawnPos.getX();
                            int sY = spawnPos.getY();
                            int sZ = spawnPos.getZ();
                            IBlockState iblockstate = spawnPos.chunk.getBlockState(spawnPos);

                            if (!iblockstate.isNormalCube()) {
                                int pack = 0;

                                for (int k2 = 0; k2 < 3; ++k2) {
                                    int l2 = sX;
                                    int j3 = sZ;
                                    Biome.SpawnListEntry entry = null;
                                    IEntityLivingData ientitylivingdata = null;
                                    int l3 = MathHelper.ceil(world.rand.nextFloat() * 4);

                                    for (int i4 = 0; i4 < l3; ++i4) {
                                        l2 += world.rand.nextInt(6) - world.rand.nextInt(6);
                                        j3 += world.rand.nextInt(6) - world.rand.nextInt(6);
                                        spawnPos.setPos(l2, sY, j3);
                                        float f = (float)l2 + 0.5F;
                                        float f1 = (float)j3 + 0.5F;

                                        if (!world.isAnyPlayerWithinRangeAt(f, sY, f1, 24.0D)/* && worldSpawn.distanceSq(f, sY, f1) >= 576.0D*/) {
                                            if (entry == null) {
                                                entry = world.getSpawnListEntryForTypeAt(enumcreaturetype, spawnPos);

                                                if (entry == null) break;
                                            }
                                            if (world.canCreatureTypeSpawnHere(enumcreaturetype, entry, spawnPos) && canCreatureTypeSpawnAtLocation(EntitySpawnPlacementRegistry.getPlacementForEntity(entry.entityClass), world, spawnPos)) {
                                                EntityLiving entityliving;

                                                try {
                                                    entityliving = entry.newInstance(world);
                                                }
                                                catch (Exception exception) {
                                                    exception.printStackTrace();
                                                    return spawned;
                                                }

                                                entityliving.setLocationAndAngles(f, sY, f1, world.rand.nextFloat() * 360.0F, 0.0F);

                                                //todo
                                                Event.Result canSpawn = world.canEntitySpawn(entityliving, f, sY, f1);
                                                if (canSpawn == Event.Result.ALLOW || (canSpawn == net.minecraftforge.fml.common.eventhandler.Event.Result.DEFAULT && (entityliving.getCanSpawnHere() && entityliving.isNotColliding()))) {
                                                    if (!ForgeEventFactory.doSpecialSpawn(entityliving, world, f, sY, f1))
                                                        ientitylivingdata = entityliving.onInitialSpawn(
                                                                world.getDifficultyForLocation(new BlockPos(entityliving)), ientitylivingdata
                                                        );
                                                    if (entityliving.isNotColliding()) {
                                                        ++pack;
                                                        world.spawnEntity(entityliving);
                                                        ++countCache[cursor];
                                                    } else {
                                                        entityliving.setDead();
                                                    }

                                                    if (pack >= ForgeEventFactory.getMaxSpawnPackSize(entityliving)) {
                                                        continue inner;
                                                    }
                                                }
                                                spawned += pack;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                ++cursor;
            }
            return spawned;
        }
    }


    private static void getRandomChunkPosition(SpawnPos pos, int x, int z) {
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


    public static boolean canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType spawnPlacementTypeIn, World worldIn, BlockPos pos) {
        if (!worldIn.getWorldBorder().contains(pos)) {
            return false;
        } else {
            return spawnPlacementTypeIn.canSpawnAt(worldIn, pos);
        }
    }


    public static boolean canCreatureTypeSpawnBody(EntityLiving.SpawnPlacementType spawnPlacementTypeIn, World worldIn, BlockPos pos) {{
            IBlockState iblockstate = worldIn.getBlockState(pos);

            if (spawnPlacementTypeIn == EntityLiving.SpawnPlacementType.IN_WATER) {
                return iblockstate.getMaterial() == Material.WATER && worldIn.getBlockState(pos.down()).getMaterial() == Material.WATER && !worldIn.getBlockState(pos.up()).isNormalCube();
            } else {
                BlockPos blockpos = pos.down();
                IBlockState state = worldIn.getBlockState(blockpos);

                if (!state.getBlock().canCreatureSpawn(state, worldIn, blockpos, spawnPlacementTypeIn)) {
                    return false;
                } else {
                    Block block = worldIn.getBlockState(blockpos).getBlock();
                    boolean flag = block != Blocks.BEDROCK && block != Blocks.BARRIER;
                    return flag && isValidEmptySpawnBlock(iblockstate) && isValidEmptySpawnBlock(worldIn.getBlockState(pos.up()));
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