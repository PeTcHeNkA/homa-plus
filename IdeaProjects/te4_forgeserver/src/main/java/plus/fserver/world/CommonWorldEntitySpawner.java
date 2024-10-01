package plus.fserver.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.SpawnPos;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingPackSizeEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import plus.fserver.ElcaConfig;
import plus.fserver.api.Keep;
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.utls.collections.FlatMap;
import plus.fserver.utls.math.FastRandom;
import java.util.Arrays;
import java.util.Random;


@Keep
public class CommonWorldEntitySpawner extends WorldEntitySpawner {
    private static final int MOB_COUNT_DIV = (int)Math.pow(17.0D, 2.0D);
    private final FlatMap<Chunk> eligibleChunksForSpawning = new FlatMap<>(128);
    private final int[] countCache = new int[TYPES.length];
    private int recheck = 0, prevFactor;
    private Chunk[] shuffled;

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
            if(this.shuffled != null) {
                this.shuffled = null;
            }
            if(this.eligibleChunksForSpawning.size() > 0) {
                this.eligibleChunksForSpawning.clear();
            }
            return 0;
        } else {
            FlatMap<Chunk> chunkSet = this.eligibleChunksForSpawning;

            final int factor;
            Chunk[] shuffled;
            if((recheck & ElcaConfig.RECHECK_CHUNKS_SPAWNER_RATE)==0) {
                shuffled = this.shuffled = null;
                int preFactor = 0;
                chunkSet.clear();
                ChunkProviderServer map = world.chunkProviderServer;
                int range = ElcaConfig.MAX_WORLD_SPAWNER_RANGE;

                for (EntityPlayer player : world.playerEntities) {
                    if (!player.isSpectator()) {
                        int cX = MathHelper.floor(player.posX * 0.0625); // 0.0625 = 1/16
                        int cZ = MathHelper.floor(player.posZ * 0.0625);

                        for (int dx = -range; dx <= range; ++dx) {
                            for (int dz = -range; dz <= range; ++dz) {
                                //ChunkPos pos = new ChunkPos(dx + cX, dz + cZ);
                                int chunkX = dx + cX, chunkZ = dz + cZ;

                                if (!chunkSet.contains(chunkX, chunkZ)) {
                                    ++preFactor;

                                    if (!(dx == -range || dx == range || dz == -range || dz == range) && world.isInWorldBorderChunk(chunkX, chunkZ)) {
                                        Chunk chunk = map.getLoadedChunk(dx + cX, dz + cZ);

                                        if (chunk != null && chunk.isSentToPlayers()) {
                                            chunkSet.put(chunkX, chunkZ, chunk);
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
            ChunkSelector selector = world.localSelector;
            Random rand = world.rand;
            LivingPackSizeEvent.EventProxy proxy = null;

            for (EnumCreatureType creatureType : TYPES) {
                if ((!creatureType.getPeacefulCreature() || spawnPeacefulMobs) && (creatureType.getPeacefulCreature() || spawnHostileMobs) && (!creatureType.getAnimal() || spawnOnSetTickRate)) {
                    int count = countCache[cursor];
                    if(count == -1)
                        count = countCache[cursor] = world.countEntities(creatureType, true);

                    int maxCount = creatureType.getMaxNumberOfCreature() * factor / MOB_COUNT_DIV;

                    if (count <= maxCount) {
                        if(spawnPos == null) spawnPos = new SpawnPos(world);
                        if(shuffled == null) {
                            shuffled = this.shuffled = chunkSet.toArray(new Chunk[chunkSet.size()]);
                            //todo error NULL
                        }
                        FastRandom.INST.shuffle(shuffled);

                        inner:
                        for (Chunk chunk : shuffled) {
                            //todo remove this and fix
                            if(chunk == null)continue;
                            spawnPos.setRandomChunkPos(chunk);
                            selector.setChunk(chunk);
                            int sX, sY, sZ;
                            IBlockState state = chunk.getBlockStateUnchecked(sX = spawnPos.getX(), sY = spawnPos.getY(), sZ = spawnPos.getZ());

                            if (!state.isNormalCube()) {
                                int pack = 0;
                                int bits = rand.nextInt();

                                for (int attempts = 0; attempts < 3; ++attempts) {
                                    bits = bits * 17 + attempts;
                                    int sx2 = sX;
                                    int sz2 = sZ;
                                    Biome.SpawnListEntry entry = null;
                                    EntityLiving prev = null;
                                    IEntityLivingData data = null;
                                    int max = MathHelper.ceil(rand.nextFloat() * 4);

                                    for (int mob = 0; mob < max; ++mob) {
                                        bits >>= mob;

                                        sx2 += (((bits & 7) % 5) - ((bits>>3 & 7) % 5));
                                        sz2 += (((bits>>6 & 7) % 5) - ((bits>>9 & 7) % 5));

                                        spawnPos.setPos(sx2, sY, sz2);
                                        float px = (float)sx2 + 0.5F;
                                        float pz = (float)sz2 + 0.5F;

                                        if (entry != null || !world.isAnyPlayerWithinRangeAt(px, sY, pz, 24.0D)) {
                                            if (entry == null) {
                                                entry = world.getSpawnListEntryForTypeAt(creatureType, spawnPos);

                                                if (entry == null) break;
                                            }
                                            if (canCreatureTypeSpawnAtLocation(EntitySpawnPlacementRegistry.getPlacementForEntity(entry.entityClass), world, spawnPos)) {
                                                EntityLiving entity;

                                                try {
                                                    if (prev != null) entity = prev;
                                                    else entity = prev = entry.newInstance(world);
                                                } catch (Exception exception) {
                                                    world.getMinecraftServer().ticker().onError(world, exception);
                                                    return spawned;
                                                }

                                                entity.setLocationAndAngles(px, sY, pz, rand.nextFloat() * 360.0F, 0.0F);

                                                //todo
                                                Event.Result canSpawn = world.canEntitySpawn(entity, px, sY, pz);
                                                if (canSpawn == Event.Result.ALLOW || (canSpawn == Event.Result.DEFAULT && (entity.getCanSpawnHere() && entity.isNotColliding()))) {
                                                    prev = null;
                                                    if (!world.doSpecialSpawn(entity, px, sY, pz))
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

                                                    if(proxy == null){
                                                        proxy = new LivingPackSizeEvent().forBus(MinecraftForge.EVENT_BUS);
                                                    }

                                                    if (pack >= proxy.post(entity)) {
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
