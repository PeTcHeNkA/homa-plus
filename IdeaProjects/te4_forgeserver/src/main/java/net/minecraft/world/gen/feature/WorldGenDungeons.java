package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.BlockIDIndexes;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.DungeonHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.fserver.utls.math.BlockPosDelta;
import plus.fserver.server.elca.ChunkSelector;


public class WorldGenDungeons extends WorldGenerator {
    private static final IBlockState MOSSY_COBBLESTONE = Blocks.MOSSY_COBBLESTONE.getDefaultState();
    private static final IBlockState COBBLESTONE       = Blocks.COBBLESTONE.getDefaultState();
    private static final Logger LOGGER = LogManager.getLogger();


    private static final ResourceLocation[] SPAWNERTYPES = new ResourceLocation[] {
            EntityList.getKey(EntitySkeleton.class),
            EntityList.getKey(EntityZombie.class),
            EntityList.getKey(EntityZombie.class),
            EntityList.getKey(EntitySpider.class)
    };


    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position0) {
        if(position0.getY() < 1 || position0.getY() > 255)return false;
        int j = rand.nextInt(2) + 2;
        int k = -j - 1;
        int l = j + 1;
        int k1 = rand.nextInt(2) + 2;
        int l1 = -k1 - 1;
        int i2 = k1 + 1;
        int j2 = 0;

        ChunkSelector selector = new ChunkSelector(worldIn);
        BlockPosDelta delta = new BlockPosDelta(position0);
        for (int k2 = k; k2 <= l; ++k2) {
            for (int posY = -1; posY <= 4; ++posY) {

                for (int i3 = l1; i3 <= i2; ++i3) {
                    if(delta.delta(k2, posY, i3).getY()<1 || delta.getY()>255)continue;

                    Material material = selector.getState(delta).getMaterial();
                    boolean flag = material.isSolid();
                    if (posY == -1 && !flag || posY == 4 && !flag) return false;


                    if ((k2 == k || k2 == l || i3 == l1 || i3 == i2) && posY == 0 && selector.isAirBlock(delta)
                            && selector.isAirBlock(delta.up())) {
                        ++j2;
                    }
                }
            }
        }
        IBlockState tempState;
        if (j2 >= 1 && j2 <= 5) {
            for (int k3 = k; k3 <= l; ++k3) {
                for (int i4 = 3; i4 >= -1; --i4) {
                    for (int k4 = l1; k4 <= i2; ++k4) {
                        delta.delta(k3, i4, k4);

                        if (k3 != k && i4 != -1 && k4 != l1 && k3 != l && i4 != 4 && k4 != i2) {
                            if (selector.getState(delta).getBlock() != Blocks.CHEST) {
                                selector.setBlockState(delta, BlockIDIndexes.AIR_STATE, 3);
                            }
                        } else if (delta.getY() >= 0 && !
                                selector.getState(delta.down()).getMaterial().isSolid()) {
                            selector.setBlockState(delta, BlockIDIndexes.AIR_STATE, 3);
                        } else if ((tempState = selector.getState(delta)).getMaterial().isSolid()
                                && tempState.getBlock() != Blocks.CHEST) {

                            selector.setCurStateFast(delta.getX(), delta.getY(), delta.getZ(),
                                    i4 == -1 && rand.nextInt(4) != 0?MOSSY_COBBLESTONE:COBBLESTONE
                            );
                        }
                    }
                }
            }

            for (int l3 = 0; l3 < 2; ++l3) {
                for (int j4 = 0; j4 < 3; ++j4) {
                    int l4 = position0.getX() + rand.nextInt(j * 2 + 1) - j;
                    int i5 = position0.getY();
                    int j5 = position0.getZ() + rand.nextInt(k1 * 2 + 1) - k1;
                    BlockPos blockpos2 = new BlockPos(l4, i5, j5);

                    if (selector.isAirBlock(blockpos2)) {
                        int j3 = 0;

                        for (EnumFacing enumfacing : BlockIDIndexes.FACING_HORIZONTAL) {
                            if (selector.getState(blockpos2.offset(enumfacing)).getMaterial().isSolid()) {
                                ++j3;
                            }
                        }

                        if (j3 == 1) {
                            selector.setBlockState(blockpos2, Blocks.CHEST.correctFacing(worldIn, blockpos2, Blocks.CHEST.getDefaultState()), 2);
                            TileEntity tileentity1 = worldIn.getTileEntity(blockpos2);

                            if (tileentity1 instanceof TileEntityChest) {
                                ((TileEntityChest)tileentity1).setLootTable(LootTableList.CHESTS_SIMPLE_DUNGEON, rand.nextLong());
                            }
                            break;
                        }
                    }
                }
            }

            selector.setBlockState(position0, Blocks.MOB_SPAWNER.getDefaultState(), 2);
            TileEntity tileentity = worldIn.getTileEntity(position0);

            if (tileentity instanceof TileEntityMobSpawner) {
                ((TileEntityMobSpawner)tileentity).getSpawnerBaseLogic().setEntityId(this.pickMobSpawner(rand));
            } else {
                LOGGER.error("Failed to fetch mob spawner entity at ({}, {}, {})",
                        position0.getX(), position0.getY(), position0.getZ());
            }
            return true;
        } else {
            return false;
        }
    }


    private ResourceLocation pickMobSpawner(Random rand) {
        return DungeonHooks.getRandomDungeonMob(rand);
    }
}