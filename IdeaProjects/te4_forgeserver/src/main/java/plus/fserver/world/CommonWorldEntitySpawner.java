package plus.fserver.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.SpawnPos;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.eventhandler.Event;
import plus.fserver.ElcaConfig;
import plus.fserver.api.Keep;
import plus.fserver.utls.math.FastRandom;
import plus.tson.utl.Te4HashSet;
import java.util.Arrays;
import java.util.Set;


@Keep
public class CommonWorldEntitySpawner extends WorldEntitySpawner {
    private static final int MOB_COUNT_DIV = (int)Math.pow(17.0D, 2.0D);
    private final Set<ChunkPos> eligibleChunksForSpawning = new Te4HashSet<>();
    private final int[] countCache = new int[TYPES.length];
    private int recheck = 0;

    public CommonWorldEntitySpawner(){
        Arrays.fill(countCache, -1);
    }


    @Override
    public int findChunksForSpawning(WorldServer world,
                                     boolean spawnHostileMobs,
                                     boolean spawnPeacefulMobs,
                                     boolean spawnOnSetTickRate
    ) {
        if (!spawnHostileMobs && !spawnPeacefulMobs) {
            return 0;
        } else {
            Set<ChunkPos> posSet = this.eligibleChunksForSpawning;
            PlayerChunkMap map = world.getPlayerChunkMap();
            posSet.clear();
            int factor = 0;
            int range = ElcaConfig.MAX_WORLD_SPAWNER_RANGE;

            for (EntityPlayer player : world.playerEntities) {
                if (!player.isSpectator()) {
                    int j = MathHelper.floor(player.posX * 0.0625); // 0.0625 = 1/16
                    int k = MathHelper.floor(player.posZ * 0.0625);

                    for (int i1 = -range; i1 <= range; ++i1) {
                        for (int j1 = -range; j1 <= range; ++j1) {
                            ChunkPos pos = new ChunkPos(i1 + j, j1 + k);

                            if (!posSet.contains(pos)) {
                                ++factor;

                                if (!(i1 == -range || i1 == range || j1 == -range || j1 == range) && world.inInWorldBorder(pos)) {
                                    PlayerChunkMapEntry entry = map.getEntry(pos.x, pos.z);

                                    if (entry != null && entry.isSentToPlayers()) {
                                        posSet.add(pos);
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

            for (EnumCreatureType creatureType : TYPES) {
                if ((!creatureType.getPeacefulCreature() || spawnPeacefulMobs) && (creatureType.getPeacefulCreature() || spawnHostileMobs) && (!creatureType.getAnimal() || spawnOnSetTickRate)) {
                    int count = countCache[cursor];
                    if(count == -1)
                        count = countCache[cursor] = world.countEntities(creatureType, true);

                    int maxCount = creatureType.getMaxNumberOfCreature() * factor / MOB_COUNT_DIV;

                    if (count <= maxCount) {
                        if(spawnPos==null) spawnPos = new SpawnPos(world);
                        ChunkPos[] shuffled = posSet.toArray(new ChunkPos[0]);
                        FastRandom.INST.shuffle(shuffled);

                        inner:

                        for (ChunkPos pos : shuffled) {
                            spawnPos.setRandomChunkPos(pos.x, pos.z);
                            int sX, sY, sZ;
                            IBlockState state = spawnPos.chunk.getBlockStateUnchecked(sX = spawnPos.getX(), sY = spawnPos.getY(), sZ = spawnPos.getZ());

                            if (!state.isNormalCube()) {
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
                                                entry = world.getSpawnListEntryForTypeAt(creatureType, spawnPos);

                                                if (entry == null) break;
                                            }
                                            if (world.canCreatureTypeSpawnHere(creatureType, entry, spawnPos) && canCreatureTypeSpawnAtLocation(EntitySpawnPlacementRegistry.getPlacementForEntity(entry.entityClass), world, spawnPos)) {
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
                                                if (canSpawn == Event.Result.ALLOW || (canSpawn == Event.Result.DEFAULT && (entityliving.getCanSpawnHere() && entityliving.isNotColliding()))) {
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
}
