package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;


public class MapGenNetherBridge extends MapGenStructure {
    private final ArrayList<Biome.SpawnListEntry> spawnList;

    public MapGenNetherBridge() {
        ArrayList<Biome.SpawnListEntry> spawnList = this.spawnList = new ArrayList<>(5);

        spawnList.add(new Biome.SpawnListEntry(EntityBlaze.class, 10, 2, 3));
        spawnList.add(new Biome.SpawnListEntry(EntityPigZombie.class, 5, 4, 4));
        spawnList.add(new Biome.SpawnListEntry(EntityWitherSkeleton.class, 8, 5, 5));
        spawnList.add(new Biome.SpawnListEntry(EntitySkeleton.class, 2, 5, 5));
        spawnList.add(new Biome.SpawnListEntry(EntityMagmaCube.class, 3, 4, 4));
    }


    @Override
    public String getStructureName() {
        return "Fortress";
    }


    public List<Biome.SpawnListEntry> getSpawnList() {
        return this.spawnList;
    }


    @Override
    protected boolean canSpawnStructureAtCoords(int blockX, int blockZ) {
        int cX = blockX >> 4;
        int cZ = blockZ >> 4;
        this.rand.setSeed((long)(cX ^ cZ << 4) ^ this.world.getSeed());
        this.rand.nextInt();
        if (this.rand.nextInt(3) != 0) {
            return false;
        } else if (blockX != (cX << 4) + 4 + this.rand.nextInt(8)) {
            return false;
        } else {
            return blockZ == (cZ << 4) + 4 + this.rand.nextInt(8);
        }
    }


    @Override
    protected StructureStart getStructureStart(int x, int z) {
        return new Start(this.world, this.rand, x, z);
    }


    @Override
    public BlockPos getNearestStructurePos(World world, BlockPos pos, boolean flag) {
        int cX = pos.getX() >> 4;
        int cZ = pos.getZ() >> 4;

        for(int at = 0; at <= 1000; ++at) {
            for(int aX = -at; aX <= at; ++aX) {
                boolean flag2 = aX == -at || aX == at;

                for(int aZ = -at; aZ <= at; ++aZ) {
                    boolean flag3 = aZ == -at || aZ == at;
                    if (flag2 || flag3) {
                        int dX = cX + aX;
                        int dZ = cZ + aZ;
                        if (this.canSpawnStructureAtCoords(dX, dZ)
                                && (!flag || !world.isChunkGeneratedAt(dX, dZ))) {

                            return new BlockPos((dX << 4) + 8, 64, (dZ << 4) + 8);
                        }
                    }
                }
            }
        }
        return null;
    }


    public static class Start extends StructureStart {
        public Start() {}


        public Start(World world, Random rand, int x, int z) {
            super(x, z);
            StructureNetherBridgePieces.Start start = new StructureNetherBridgePieces.Start(rand, (x << 4) + 2, (z << 4) + 2);
            this.components.add(start);
            start.buildComponent(start, this.components, rand);
            List<StructureComponent> childs = start.pendingChildren;

            while(!childs.isEmpty()) {
                int cursor = rand.nextInt(childs.size());
                StructureComponent comp = childs.remove(cursor);
                comp.buildComponent(start, this.components, rand);
            }

            this.updateBoundingBox();
            this.setRandomHeight(world, rand, 48, 70);
        }
    }
}
