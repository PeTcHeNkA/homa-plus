package plus.fserver.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
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


@Keep
public class CommonWorldEntitySpawner extends WorldEntitySpawner {
    private static final int MOB_COUNT_DIV = (int)Math.pow(17.0D, 2.0D);
    private final Te4HashSet<ChunkPos> eligibleChunksForSpawning = new Te4HashSet<>();
    private final int[] countCache = new int[TYPES.length];
    private int recheck = 0, prevFactor;
    private ChunkPos[] shuffled;

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
            Te4HashSet<ChunkPos> posSet = this.eligibleChunksForSpawning;

            final int factor;
            ChunkPos[] shuffled;
            if((recheck & ElcaConfig.RECHECK_CHUNKS_SPAWNER_RATE)==0) {
                shuffled = this.shuffled = null;
                int preFactor = 0;
                posSet.clear();
                PlayerChunkMap map = world.getPlayerChunkMap();
                int range = ElcaConfig.MAX_WORLD_SPAWNER_RANGE;

                for (EntityPlayer player : world.playerEntities) {
                    if (!player.isSpectator()) {
                        int j = MathHelper.floor(player.posX * 0.0625); // 0.0625 = 1/16
                        int k = MathHelper.floor(player.posZ * 0.0625);

                        for (int i1 = -range; i1 <= range; ++i1) {
                            for (int j1 = -range; j1 <= range; ++j1) {
                                ChunkPos pos = new ChunkPos(i1 + j, j1 + k);

                                if (!posSet.contains(pos)) {
                                    ++preFactor;

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
                prevFactor = factor = preFactor;
            } else {
                factor = prevFactor;
                shuffled = this.shuffled;
            }
            int spawned = 0, cursor = 0;

            if(recheck++>ElcaConfig.RECHECK_SPAWNER_RATE){
                Arrays.fill(countCache, -1);
                recheck = 0;
            }

            SpawnPos spawnPos = null;

            for (EnumCreatureType creatureType : TYPES) {
                if ((!creatureType.getPeacefulCreature() || spawnPeacefulMobs) && (creatureType.getPeacefulCreature() || spawnHostileMobs) && (!creatureType.getAnimal() || spawnOnSetTickRate)) {
                    int count = countCache[cursor];
                    if(count == -1)
                        count = countCache[cursor] = world.countEntities(creatureType, true);

                    int maxCount = creatureType.getMaxNumberOfCreature() * factor / MOB_COUNT_DIV;

                    if (count <= maxCount) {
                        if(spawnPos==null) spawnPos = new SpawnPos(world);
                        if(shuffled == null) {
                            shuffled = this.shuffled = posSet.toArray(new ChunkPos[0]);
                        }
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
                                    EntityLiving prev = null;
                                    IEntityLivingData data = null;
                                    int l3 = MathHelper.ceil(world.rand.nextFloat() * 4);

                                    for (int i4 = 0; i4 < l3; ++i4) {
                                        l2 += world.rand.nextInt(6) - world.rand.nextInt(6);
                                        j3 += world.rand.nextInt(6) - world.rand.nextInt(6);
                                        spawnPos.setPos(l2, sY, j3);
                                        float f = (float)l2 + 0.5F;
                                        float f1 = (float)j3 + 0.5F;

                                        if (!world.isAnyPlayerWithinRangeAt(f, sY, f1, 24.0D)) {
                                            if (entry == null) {
                                                entry = world.getSpawnListEntryForTypeAt(creatureType, spawnPos);

                                                if (entry == null) break;
                                            }
                                            if (world.canCreatureTypeSpawnHere(creatureType, entry, spawnPos) && canCreatureTypeSpawnAtLocation(EntitySpawnPlacementRegistry.getPlacementForEntity(entry.entityClass), world, spawnPos)) {
                                                EntityLiving entity;

                                                try {
                                                    if (prev != null) entity = prev;
                                                    else entity = prev = entry.newInstance(world);
                                                } catch (Exception exception) {
                                                    world.getMinecraftServer().ticker().onError(world, exception);
                                                    return spawned;
                                                }

                                                entity.setLocationAndAngles(f, sY, f1, world.rand.nextFloat() * 360.0F, 0.0F);

                                                //todo
                                                Event.Result canSpawn = world.canEntitySpawn(entity, f, sY, f1);
                                                if (canSpawn == Event.Result.ALLOW || (canSpawn == Event.Result.DEFAULT && (entity.getCanSpawnHere() && entity.isNotColliding()))) {
                                                    prev = null;
                                                    if (!world.doSpecialSpawn(entity, f, sY, f1))
                                                        data = entity.onInitialSpawn(
                                                                world.getDifficultyForLocation(
                                                                        MathHelper.floor(entity.posX), MathHelper.floor(entity.posY)
                                                                ), data
                                                        );
                                                    if (entity.isNotColliding()) {
                                                        ++pack;
                                                        world.spawnEntity(entity);
                                                        ++countCache[cursor];
                                                    } else {
                                                        entity.setDead();
                                                    }

                                                    if (pack >= ForgeEventFactory.getMaxSpawnPackSize(entity)) {
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
