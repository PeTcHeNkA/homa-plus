package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.block.BlockPrismarine;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntityElderGuardian;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockIDIndexes;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.TemplateManager;
import plus.fserver.server.elca.ChunkSelector;


public class StructureOceanMonumentPieces {
    public static void registerOceanMonumentPieces() {
        MapGenStructureIO.registerStructureComponent(MonumentBuilding.class, "OMB");
        MapGenStructureIO.registerStructureComponent(MonumentCoreRoom.class, "OMCR");
        MapGenStructureIO.registerStructureComponent(DoubleXRoom.class, "OMDXR");
        MapGenStructureIO.registerStructureComponent(DoubleXYRoom.class, "OMDXYR");
        MapGenStructureIO.registerStructureComponent(DoubleYRoom.class, "OMDYR");
        MapGenStructureIO.registerStructureComponent(DoubleYZRoom.class, "OMDYZR");
        MapGenStructureIO.registerStructureComponent(DoubleZRoom.class, "OMDZR");
        MapGenStructureIO.registerStructureComponent(EntryRoom.class, "OMEntry");
        MapGenStructureIO.registerStructureComponent(Penthouse.class, "OMPenthouse");
        MapGenStructureIO.registerStructureComponent(SimpleRoom.class, "OMSimple");
        MapGenStructureIO.registerStructureComponent(SimpleTopRoom.class, "OMSimpleT");
    }

    static class YZDoubleRoomFitHelper implements MonumentRoomFitHelper {
        private YZDoubleRoomFitHelper() {}

        @Override
        public boolean fits(RoomDefinition room) {
            if (room.hasOpening[EnumFacing.NORTH.getIndex()] && !room.connections[EnumFacing.NORTH.getIndex()].claimed && room.hasOpening[EnumFacing.UP.getIndex()] && !room.connections[EnumFacing.UP.getIndex()].claimed) {
                RoomDefinition lvt_2_1_ = room.connections[EnumFacing.NORTH.getIndex()];
                return lvt_2_1_.hasOpening[EnumFacing.UP.getIndex()] && !lvt_2_1_.connections[EnumFacing.UP.getIndex()].claimed;
            } else {
                return false;
            }
        }

        @Override
        public Piece create(EnumFacing facing, RoomDefinition room, Random rand) {
            room.claimed = true;
            room.connections[EnumFacing.NORTH.getIndex()].claimed = true;
            room.connections[EnumFacing.UP.getIndex()].claimed = true;
            room.connections[EnumFacing.NORTH.getIndex()].connections[EnumFacing.UP.getIndex()].claimed = true;
            return new DoubleYZRoom(facing, room, rand);
        }
    }

    static class XYDoubleRoomFitHelper implements MonumentRoomFitHelper {
        private XYDoubleRoomFitHelper() {}

        @Override
        public boolean fits(RoomDefinition room) {
            if (room.hasOpening[EnumFacing.EAST.getIndex()] && !room.connections[EnumFacing.EAST.getIndex()].claimed && room.hasOpening[EnumFacing.UP.getIndex()] && !room.connections[EnumFacing.UP.getIndex()].claimed) {
                RoomDefinition lvt_2_1_ = room.connections[EnumFacing.EAST.getIndex()];
                return lvt_2_1_.hasOpening[EnumFacing.UP.getIndex()] && !lvt_2_1_.connections[EnumFacing.UP.getIndex()].claimed;
            } else {
                return false;
            }
        }

        @Override
        public Piece create(EnumFacing facing, RoomDefinition room, Random rand) {
            room.claimed = true;
            room.connections[EnumFacing.EAST.getIndex()].claimed = true;
            room.connections[EnumFacing.UP.getIndex()].claimed = true;
            room.connections[EnumFacing.EAST.getIndex()].connections[EnumFacing.UP.getIndex()].claimed = true;
            return new DoubleXYRoom(facing, room, rand);
        }
    }

    static class ZDoubleRoomFitHelper implements MonumentRoomFitHelper {
        private ZDoubleRoomFitHelper() {}

        @Override
        public boolean fits(RoomDefinition room) {
            return room.hasOpening[EnumFacing.NORTH.getIndex()] && !room.connections[EnumFacing.NORTH.getIndex()].claimed;
        }

        @Override
        public Piece create(EnumFacing facing, RoomDefinition room, Random rand) {
            RoomDefinition room1 = room;
            if (!room.hasOpening[EnumFacing.NORTH.getIndex()] || room.connections[EnumFacing.NORTH.getIndex()].claimed) {
                room1 = room.connections[EnumFacing.SOUTH.getIndex()];
            }

            room1.claimed = true;
            room1.connections[EnumFacing.NORTH.getIndex()].claimed = true;
            return new DoubleZRoom(facing, room1, rand);
        }
    }

    static class XDoubleRoomFitHelper implements MonumentRoomFitHelper {
        private XDoubleRoomFitHelper() {}

        @Override
        public boolean fits(RoomDefinition room) {
            return room.hasOpening[EnumFacing.EAST.getIndex()] && !room.connections[EnumFacing.EAST.getIndex()].claimed;
        }

        @Override
        public Piece create(EnumFacing facing, RoomDefinition room, Random rand) {
            room.claimed = true;
            room.connections[EnumFacing.EAST.getIndex()].claimed = true;
            return new DoubleXRoom(facing, room, rand);
        }
    }

    static class YDoubleRoomFitHelper implements MonumentRoomFitHelper {
        private YDoubleRoomFitHelper() {}

        @Override
        public boolean fits(RoomDefinition room) {
            return room.hasOpening[EnumFacing.UP.getIndex()] && !room.connections[EnumFacing.UP.getIndex()].claimed;
        }

        @Override
        public Piece create(EnumFacing facing, RoomDefinition room, Random rand) {
            room.claimed = true;
            room.connections[EnumFacing.UP.getIndex()].claimed = true;
            return new DoubleYRoom(facing, room, rand);
        }
    }

    static class FitSimpleRoomTopHelper implements MonumentRoomFitHelper {
        private FitSimpleRoomTopHelper() {}

        @Override
        public boolean fits(RoomDefinition room) {
            return !room.hasOpening[EnumFacing.WEST.getIndex()] && !room.hasOpening[EnumFacing.EAST.getIndex()] && !room.hasOpening[EnumFacing.NORTH.getIndex()] && !room.hasOpening[EnumFacing.SOUTH.getIndex()] && !room.hasOpening[EnumFacing.UP.getIndex()];
        }

        @Override
        public Piece create(EnumFacing facing, RoomDefinition room, Random rand) {
            room.claimed = true;
            return new SimpleTopRoom(facing, room, rand);
        }
    }

    static class FitSimpleRoomHelper implements MonumentRoomFitHelper {
        private FitSimpleRoomHelper() {}

        @Override
        public boolean fits(RoomDefinition room) {
            return true;
        }

        @Override
        public Piece create(EnumFacing facing, RoomDefinition room, Random rand) {
            room.claimed = true;
            return new SimpleRoom(facing, room, rand);
        }
    }

    interface MonumentRoomFitHelper {
        boolean fits(RoomDefinition var1);
        Piece create(EnumFacing var1, RoomDefinition var2, Random var3);
    }

    static class RoomDefinition {
        int index;
        RoomDefinition[] connections = new RoomDefinition[6];
        boolean[] hasOpening = new boolean[6];
        boolean claimed;
        boolean isSource;
        int scanIndex;

        public RoomDefinition(int index) {
            this.index = index;
        }

        public void setConnection(EnumFacing facing, RoomDefinition room) {
            this.connections[facing.getIndex()] = room;
            room.connections[facing.getOpposite().getIndex()] = this;
        }

        public void updateOpenings() {
            for(int i = 0; i < 6; ++i) {
                this.hasOpening[i] = this.connections[i] != null;
            }
        }

        public boolean findSource(int index) {
            if (this.isSource) {
                return true;
            } else {
                this.scanIndex = index;

                for(int i = 0; i < 6; ++i) {
                    if (this.connections[i] != null && this.hasOpening[i] && this.connections[i].scanIndex != index && this.connections[i].findSource(index)) {
                        return true;
                    }
                }
                return false;
            }
        }

        public boolean isSpecial() {
            return this.index >= 75;
        }

        public int countOpenings() {
            int count = 0;
            for(int i = 0; i < 6; ++i) {
                if (this.hasOpening[i]) ++count;
            }
            return count;
        }
    }

    public static class Penthouse extends Piece {
        public Penthouse() {}

        public Penthouse(EnumFacing facing, StructureBoundingBox box) {
            super(facing, box);
        }

        public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
            ChunkSelector selector = new ChunkSelector(world);

            this.fillWithBlocks(selector, box, 2 , -1, 2 , 11, -1, 11, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 0 , -1, 0 , 1 , -1, 11, ROUGH_PRISMARINE , false);
            this.fillWithBlocks(selector, box, 12, -1, 0 , 13, -1, 11, ROUGH_PRISMARINE , false);
            this.fillWithBlocks(selector, box, 2 , -1, 0 , 11, -1, 1 , ROUGH_PRISMARINE , false);
            this.fillWithBlocks(selector, box, 2 , -1, 12, 11, -1, 13, ROUGH_PRISMARINE , false);
            this.fillWithBlocks(selector, box, 0 ,  0, 0 , 0 ,  0, 13, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 13,  0, 0 , 13,  0, 13, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 1 ,  0, 0 , 12,  0, 0 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 1 ,  0, 13, 12,  0, 13, BRICKS_PRISMARINE, false);

            for(int i = 2; i <= 11; i += 3) {
                this.setBlockState(selector, SEA_LANTERN, 0, 0, i, box);
                this.setBlockState(selector, SEA_LANTERN, 13, 0, i, box);
                this.setBlockState(selector, SEA_LANTERN, i, 0, 0, box);
            }

            this.fillWithBlocks(selector, box, 2, 0, 3, 4, 0, 9 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 9, 0, 3, 11, 0, 9, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 4, 0, 9, 9, 0, 11, BRICKS_PRISMARINE, false);
            this.setBlockState(selector, BRICKS_PRISMARINE, 5, 0, 8, box);
            this.setBlockState(selector, BRICKS_PRISMARINE, 8, 0, 8, box);
            this.setBlockState(selector, BRICKS_PRISMARINE, 10, 0, 10, box);
            this.setBlockState(selector, BRICKS_PRISMARINE, 3, 0, 10, box);
            this.fillWithBlocks(selector, box, 3, 0, 3, 3, 0, 7  , DARK_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 10, 0, 3, 10, 0, 7, DARK_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 6, 0, 10, 7, 0, 10, DARK_PRISMARINE, false);

            int xMin = 3;

            for(int i = 0; i < 2; ++i) {
                for(int zMin = 2; zMin <= 8; zMin += 3) {
                    this.fillWithBlocks(selector, box, xMin, 0, zMin, xMin, 2, zMin, BRICKS_PRISMARINE, false);
                }
                xMin = 10;
            }

            this.fillWithBlocks(selector, box, 5, 0, 10, 5, 2, 10, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 8, 0, 10, 8, 2, 10, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 6, -1, 7, 7, -1, 8, DARK_PRISMARINE, false);
            this.generateWaterBox(selector, box, 6, -1, 3, 7, -1, 4, false);
            this.spawnElder(world, box, 6, 1, 6);
            return true;
        }
    }


    public static class WingRoom extends Piece {
        private int mainDesign;
        public WingRoom() {}

        public WingRoom(EnumFacing facing, StructureBoundingBox box, int mainDesign) {
            super(facing, box);
            this.mainDesign = mainDesign & 1;
        }


        @Override
        public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
            ChunkSelector selector = new ChunkSelector(world);

            if (this.mainDesign == 0) {
                int i;
                for(i = 0; i < 4; ++i) {
                    this.fillWithBlocks(selector, box, 10 - i, 3 - i, 20 - i, 12 + i, 3 - i, 20, BRICKS_PRISMARINE, false);
                }

                this.fillWithBlocks(selector, box, 7 , 0, 6 , 15, 0, 16, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 6 , 0, 6 , 6 , 3, 20, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 16, 0, 6 , 16, 3, 20, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 7 , 1, 7 , 7 , 1, 20, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 15, 1, 7 , 15, 1, 20, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 7 , 1, 6 , 9 , 3, 6 , BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 13, 1, 6 , 15, 3, 6 , BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 8 , 1, 7 , 9 , 1, 7 , BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 13, 1, 7 , 14, 1, 7 , BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 9 , 0, 5 , 13, 0, 5 , BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 10, 0, 7 , 12, 0, 7 , DARK_PRISMARINE  , false);
                this.fillWithBlocks(selector, box, 8 , 0, 10, 8 , 0, 12, DARK_PRISMARINE  , false);
                this.fillWithBlocks(selector, box, 14, 0, 10, 14, 0, 12, DARK_PRISMARINE  , false);

                for(i = 18; i >= 7; i -= 3) {
                    this.setBlockState(selector, SEA_LANTERN, 6, 3, i, box);
                    this.setBlockState(selector, SEA_LANTERN, 16, 3, i, box);
                }

                this.setBlockState(selector, SEA_LANTERN, 10, 0, 10, box);
                this.setBlockState(selector, SEA_LANTERN, 12, 0, 10, box);
                this.setBlockState(selector, SEA_LANTERN, 10, 0, 12, box);
                this.setBlockState(selector, SEA_LANTERN, 12, 0, 12, box);
                this.setBlockState(selector, SEA_LANTERN, 8, 3, 6, box);
                this.setBlockState(selector, SEA_LANTERN, 14, 3, 6, box);
                this.setBlockState(selector, BRICKS_PRISMARINE, 4, 2, 4, box);
                this.setBlockState(selector, SEA_LANTERN, 4, 1, 4, box);
                this.setBlockState(selector, BRICKS_PRISMARINE, 4, 0, 4, box);
                this.setBlockState(selector, BRICKS_PRISMARINE, 18, 2, 4, box);
                this.setBlockState(selector, SEA_LANTERN, 18, 1, 4, box);
                this.setBlockState(selector, BRICKS_PRISMARINE, 18, 0, 4, box);
                this.setBlockState(selector, BRICKS_PRISMARINE, 4, 2, 18, box);
                this.setBlockState(selector, SEA_LANTERN, 4, 1, 18, box);
                this.setBlockState(selector, BRICKS_PRISMARINE, 4, 0, 18, box);
                this.setBlockState(selector, BRICKS_PRISMARINE, 18, 2, 18, box);
                this.setBlockState(selector, SEA_LANTERN, 18, 1, 18, box);
                this.setBlockState(selector, BRICKS_PRISMARINE, 18, 0, 18, box);
                this.setBlockState(selector, BRICKS_PRISMARINE, 9, 7, 20, box);
                this.setBlockState(selector, BRICKS_PRISMARINE, 13, 7, 20, box);
                this.fillWithBlocks(selector, box, 6, 0, 21, 7, 4, 21, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 15, 0, 21, 16, 4, 21, BRICKS_PRISMARINE, false);
                this.spawnElder(world, box, 11, 2, 16);
            } else if (this.mainDesign == 1) {
                this.fillWithBlocks(selector, box, 9, 3, 18, 13, 3, 20, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 9, 0, 18, 9, 2, 18, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 13, 0, 18, 13, 2, 18, BRICKS_PRISMARINE, false);
                int dx = 9;
//                int lvt_5_1_ = true;
//                int lvt_6_1_ = true;

                BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

                int i;
                for(i = 0; i < 2; ++i) {
                    this.setBlockStateM(mut, selector, BRICKS_PRISMARINE, dx, 6, 20, box);
                    this.setBlockStateM(mut, selector, SEA_LANTERN, dx, 5, 20, box);
                    this.setBlockStateM(mut, selector, BRICKS_PRISMARINE, dx, 4, 20, box);
                    dx = 13;
                }

                this.fillWithBlocks(selector, box, 7, 3, 7, 15, 3, 14, BRICKS_PRISMARINE, false);
                dx = 10;

                for(i = 0; i < 2; ++i) {
                    this.fillWithBlocks(selector, box, dx, 0, 10, dx, 6, 10, BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, dx, 0, 12, dx, 6, 12, BRICKS_PRISMARINE, false);

                    this.setBlockStateM(mut, selector, SEA_LANTERN, dx, 0, 10, box);
                    this.setBlockStateM(mut, selector, SEA_LANTERN, dx, 0, 12, box);
                    this.setBlockStateM(mut, selector, SEA_LANTERN, dx, 4, 10, box);
                    this.setBlockStateM(mut, selector, SEA_LANTERN, dx, 4, 12, box);
                    dx = 12;
                }

                dx = 8;

                for(i = 0; i < 2; ++i) {
                    this.fillWithBlocks(selector, box, dx, 0, 7, dx, 2, 7, BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, dx, 0, 14, dx, 2, 14, BRICKS_PRISMARINE, false);
                    dx = 14;
                }

                this.fillWithBlocks(selector, box, 8, 3, 8, 8, 3, 13, DARK_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 14, 3, 8, 14, 3, 13, DARK_PRISMARINE, false);

                this.spawnElder(world, box, 11, 5, 13);
            }
            return true;
        }
    }

    public static class MonumentCoreRoom extends Piece {
        public MonumentCoreRoom() {}

        public MonumentCoreRoom(EnumFacing facing, RoomDefinition room, Random rand) {
            super(1, facing, room, 2, 2, 2);
        }


        @Override
        public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
            ChunkSelector selector = new ChunkSelector(world);

            this.generateBoxOnFillOnly(selector, box, 1, 8, 0, 14, 8, 14, ROUGH_PRISMARINE);
            //int lvt_4_1_ = true;
            IBlockState state = BRICKS_PRISMARINE;
            this.fillWithBlocks(selector, box, 0 , 7, 0 , 0 , 7, 15, state, false);
            this.fillWithBlocks(selector, box, 15, 7, 0 , 15, 7, 15, state, false);
            this.fillWithBlocks(selector, box, 1 , 7, 0 , 15, 7, 0 , state, false);
            this.fillWithBlocks(selector, box, 1 , 7, 15, 14, 7, 15, state, false);

            int yMin;
            for(yMin = 1; yMin <= 6; ++yMin) {
                state = BRICKS_PRISMARINE;
                if (yMin == 2 || yMin == 6) {
                    state = ROUGH_PRISMARINE;
                }

                for(int xMin = 0; xMin <= 15; xMin += 15) {
                    this.fillWithBlocks(selector, box, xMin, yMin, 0 , xMin, yMin, 1 , state, false);
                    this.fillWithBlocks(selector, box, xMin, yMin, 6 , xMin, yMin, 9 , state, false);
                    this.fillWithBlocks(selector, box, xMin, yMin, 14, xMin, yMin, 15, state, false);
                }

                this.fillWithBlocks(selector, box, 1 , yMin, 0 , 1 , yMin, 0 , state, false);
                this.fillWithBlocks(selector, box, 6 , yMin, 0 , 9 , yMin, 0 , state, false);
                this.fillWithBlocks(selector, box, 14, yMin, 0 , 14, yMin, 0 , state, false);
                this.fillWithBlocks(selector, box, 1 , yMin, 15, 14, yMin, 15, state, false);
            }

            this.fillWithBlocks(selector, box, 6, 3, 6, 9, 6, 9, DARK_PRISMARINE                    , false);
            this.fillWithBlocks(selector, box, 7, 4, 7, 8, 5, 8, Blocks.GOLD_BLOCK.getDefaultState(), false);

            for(yMin = 3; yMin <= 6; yMin += 3) {
                for(int xMin = 6; xMin <= 9; xMin += 3) {
                    this.setBlockState(selector, SEA_LANTERN, xMin, yMin, 6, box);
                    this.setBlockState(selector, SEA_LANTERN, xMin, yMin, 9, box);
                }
            }

            this.fillWithBlocks(selector, box, 5 , 1, 6 , 5 , 2, 6 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 5 , 1, 9 , 5 , 2, 9 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 10, 1, 6 , 10, 2, 6 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 10, 1, 9 , 10, 2, 9 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 6 , 1, 5 , 6 , 2, 5 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 9 , 1, 5 , 9 , 2, 5 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 6 , 1, 10, 6 , 2, 10, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 9 , 1, 10, 9 , 2, 10, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 5 , 2, 5 , 5 , 6, 5 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 5 , 2, 10, 5 , 6, 10, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 10, 2, 5 , 10, 6, 5 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 10, 2, 10, 10, 6, 10, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 5 , 7, 1 , 5 , 7, 6 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 10, 7, 1 , 10, 7, 6 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 5 , 7, 9 , 5 , 7, 14, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 10, 7, 9 , 10, 7, 14, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 1 , 7, 5 , 6 , 7, 5 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 1 , 7, 10, 6 , 7, 10, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 9 , 7, 5 , 14, 7, 5 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 9 , 7, 10, 14, 7, 10, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 2 , 1, 2 , 2 , 1, 3 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 3 , 1, 2 , 3 , 1, 2 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 13, 1, 2 , 13, 1, 3 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 12, 1, 2 , 12, 1, 2 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 2 , 1, 12, 2 , 1, 13, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 3 , 1, 13, 3 , 1, 13, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 13, 1, 12, 13, 1, 13, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 12, 1, 13, 12, 1, 13, BRICKS_PRISMARINE, false);
            return true;
        }
    }

    public static class DoubleYZRoom extends Piece {
        public DoubleYZRoom() {
        }

        public DoubleYZRoom(EnumFacing facing, RoomDefinition room, Random rand) {
            super(1, facing, room, 1, 2, 2);
        }

        @Override
        public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
            ChunkSelector selector = new ChunkSelector(world);

            RoomDefinition room1 = this.roomDefinition.connections[EnumFacing.NORTH.getIndex()];
            RoomDefinition room2 = this.roomDefinition;
            RoomDefinition room3 = room1.connections[EnumFacing.UP.getIndex()];
            RoomDefinition room4 = room2.connections[EnumFacing.UP.getIndex()];
            if (this.roomDefinition.index / 25 > 0) {
                this.generateDefaultFloor(selector, box, 0, 8, room1.hasOpening[EnumFacing.DOWN.getIndex()]);
                this.generateDefaultFloor(selector, box, 0, 0, room2.hasOpening[EnumFacing.DOWN.getIndex()]);
            }

            if (room4.connections[EnumFacing.UP.getIndex()] == null) {
                this.generateBoxOnFillOnly(selector, box, 1, 8, 1, 6, 8, 7, ROUGH_PRISMARINE);
            }

            if (room3.connections[EnumFacing.UP.getIndex()] == null) {
                this.generateBoxOnFillOnly(selector, box, 1, 8, 8, 6, 8, 14, ROUGH_PRISMARINE);
            }

            int i;
            IBlockState state;
            for(i = 1; i <= 7; ++i) {
                state = BRICKS_PRISMARINE;
                if (i == 2 || i == 6) {
                    state = ROUGH_PRISMARINE;
                }

                this.fillWithBlocks(selector, box, 0, i, 0 , 0, i, 15, state, false);
                this.fillWithBlocks(selector, box, 7, i, 0 , 7, i, 15, state, false);
                this.fillWithBlocks(selector, box, 1, i, 0 , 6, i, 0 , state, false);
                this.fillWithBlocks(selector, box, 1, i, 15, 6, i, 15, state, false);
            }

            for(i = 1; i <= 7; ++i) {
                state = DARK_PRISMARINE;
                if (i == 2 || i == 6) {
                    state = SEA_LANTERN;
                }

                this.fillWithBlocks(selector, box, 3, i, 7, 4, i, 8, state, false);
            }

            if (room2.hasOpening[EnumFacing.SOUTH.getIndex()]) {
                this.generateWaterBox(selector, box, 3, 1, 0, 4, 2, 0, false);
            }

            if (room2.hasOpening[EnumFacing.EAST.getIndex()]) {
                this.generateWaterBox(selector, box, 7, 1, 3, 7, 2, 4, false);
            }

            if (room2.hasOpening[EnumFacing.WEST.getIndex()]) {
                this.generateWaterBox(selector, box, 0, 1, 3, 0, 2, 4, false);
            }

            if (room1.hasOpening[EnumFacing.NORTH.getIndex()]) {
                this.generateWaterBox(selector, box, 3, 1, 15, 4, 2, 15, false);
            }

            if (room1.hasOpening[EnumFacing.WEST.getIndex()]) {
                this.generateWaterBox(selector, box, 0, 1, 11, 0, 2, 12, false);
            }

            if (room1.hasOpening[EnumFacing.EAST.getIndex()]) {
                this.generateWaterBox(selector, box, 7, 1, 11, 7, 2, 12, false);
            }

            if (room4.hasOpening[EnumFacing.SOUTH.getIndex()]) {
                this.generateWaterBox(selector, box, 3, 5, 0, 4, 6, 0, false);
            }

            if (room4.hasOpening[EnumFacing.EAST.getIndex()]) {
                this.generateWaterBox(selector, box, 7, 5, 3, 7, 6, 4, false);
                this.fillWithBlocks(selector, box, 5, 4, 2, 6, 4, 5, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 6, 1, 2, 6, 3, 2, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 6, 1, 5, 6, 3, 5, BRICKS_PRISMARINE, false);
            }

            if (room4.hasOpening[EnumFacing.WEST.getIndex()]) {
                this.generateWaterBox(selector, box, 0, 5, 3, 0, 6, 4, false);
                this.fillWithBlocks(selector, box, 1, 4, 2, 2, 4, 5, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 1, 1, 2, 1, 3, 2, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 1, 1, 5, 1, 3, 5, BRICKS_PRISMARINE, false);
            }

            if (room3.hasOpening[EnumFacing.NORTH.getIndex()]) {
                this.generateWaterBox(selector, box, 3, 5, 15, 4, 6, 15, false);
            }

            if (room3.hasOpening[EnumFacing.WEST.getIndex()]) {
                this.generateWaterBox(selector, box, 0, 5, 11, 0, 6, 12, false);
                this.fillWithBlocks(selector, box, 1, 4, 10, 2, 4, 13, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 1, 1, 10, 1, 3, 10, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 1, 1, 13, 1, 3, 13, BRICKS_PRISMARINE, false);
            }

            if (room3.hasOpening[EnumFacing.EAST.getIndex()]) {
                this.generateWaterBox(selector, box, 7, 5, 11, 7, 6, 12, false);
                this.fillWithBlocks(selector, box, 5, 4, 10, 6, 4, 13, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 6, 1, 10, 6, 3, 10, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 6, 1, 13, 6, 3, 13, BRICKS_PRISMARINE, false);
            }

            return true;
        }
    }

    public static class DoubleXYRoom extends Piece {
        public DoubleXYRoom() {}

        public DoubleXYRoom(EnumFacing facing, RoomDefinition room, Random rand) {
            super(1, facing, room, 2, 2, 1);
        }

        @Override
        public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
            ChunkSelector selector = new ChunkSelector(world);

            RoomDefinition room1 = this.roomDefinition.connections[EnumFacing.EAST.getIndex()];
            RoomDefinition room2 = this.roomDefinition;
            RoomDefinition room3 = room2.connections[EnumFacing.UP.getIndex()];
            RoomDefinition room4 = room1.connections[EnumFacing.UP.getIndex()];
            if (this.roomDefinition.index / 25 > 0) {
                this.generateDefaultFloor(selector, box, 8, 0, room1.hasOpening[EnumFacing.DOWN.getIndex()]);
                this.generateDefaultFloor(selector, box, 0, 0, room2.hasOpening[EnumFacing.DOWN.getIndex()]);
            }

            if (room3.connections[EnumFacing.UP.getIndex()] == null) {
                this.generateBoxOnFillOnly(selector, box, 1, 8, 1, 7, 8, 6, ROUGH_PRISMARINE);
            }

            if (room4.connections[EnumFacing.UP.getIndex()] == null) {
                this.generateBoxOnFillOnly(selector, box, 8, 8, 1, 14, 8, 6, ROUGH_PRISMARINE);
            }

            for(int i = 1; i <= 7; ++i) {
                IBlockState state = BRICKS_PRISMARINE;
                if (i == 2 || i == 6) {
                    state = ROUGH_PRISMARINE;
                }

                this.fillWithBlocks(selector, box, 0 , i, 0, 0 , i, 7, state, false);
                this.fillWithBlocks(selector, box, 15, i, 0, 15, i, 7, state, false);
                this.fillWithBlocks(selector, box, 1 , i, 0, 15, i, 0, state, false);
                this.fillWithBlocks(selector, box, 1 , i, 7, 14, i, 7, state, false);
            }

            this.fillWithBlocks(selector, box, 2 , 1, 3, 2 , 7, 4, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 3 , 1, 2, 4 , 7, 2, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 3 , 1, 5, 4 , 7, 5, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 13, 1, 3, 13, 7, 4, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 11, 1, 2, 12, 7, 2, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 11, 1, 5, 12, 7, 5, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 5 , 1, 3, 5 , 3, 4, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 10, 1, 3, 10, 3, 4, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 5 , 7, 2, 10, 7, 5, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 5 , 5, 2, 5 , 7, 2, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 10, 5, 2, 10, 7, 2, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 5 , 5, 5, 5 , 7, 5, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 10, 5, 5, 10, 7, 5, BRICKS_PRISMARINE, false);
            this.setBlockState(selector, BRICKS_PRISMARINE, 6, 6, 2, box);
            this.setBlockState(selector, BRICKS_PRISMARINE, 9, 6, 2, box);
            this.setBlockState(selector, BRICKS_PRISMARINE, 6, 6, 5, box);
            this.setBlockState(selector, BRICKS_PRISMARINE, 9, 6, 5, box);
            this.fillWithBlocks(selector, box, 5, 4, 3, 6 , 4, 4, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 9, 4, 3, 10, 4, 4, BRICKS_PRISMARINE, false);
            this.setBlockState(selector, SEA_LANTERN, 5, 4, 2, box);
            this.setBlockState(selector, SEA_LANTERN, 5, 4, 5, box);
            this.setBlockState(selector, SEA_LANTERN, 10, 4, 2, box);
            this.setBlockState(selector, SEA_LANTERN, 10, 4, 5, box);
            if (room2.hasOpening[EnumFacing.SOUTH.getIndex()]) {
                this.generateWaterBox(selector, box, 3, 1, 0, 4, 2, 0, false);
            }

            if (room2.hasOpening[EnumFacing.NORTH.getIndex()]) {
                this.generateWaterBox(selector, box, 3, 1, 7, 4, 2, 7, false);
            }

            if (room2.hasOpening[EnumFacing.WEST.getIndex()]) {
                this.generateWaterBox(selector, box, 0, 1, 3, 0, 2, 4, false);
            }

            if (room1.hasOpening[EnumFacing.SOUTH.getIndex()]) {
                this.generateWaterBox(selector, box, 11, 1, 0, 12, 2, 0, false);
            }

            if (room1.hasOpening[EnumFacing.NORTH.getIndex()]) {
                this.generateWaterBox(selector, box, 11, 1, 7, 12, 2, 7, false);
            }

            if (room1.hasOpening[EnumFacing.EAST.getIndex()]) {
                this.generateWaterBox(selector, box, 15, 1, 3, 15, 2, 4, false);
            }

            if (room3.hasOpening[EnumFacing.SOUTH.getIndex()]) {
                this.generateWaterBox(selector, box, 3, 5, 0, 4, 6, 0, false);
            }

            if (room3.hasOpening[EnumFacing.NORTH.getIndex()]) {
                this.generateWaterBox(selector, box, 3, 5, 7, 4, 6, 7, false);
            }

            if (room3.hasOpening[EnumFacing.WEST.getIndex()]) {
                this.generateWaterBox(selector, box, 0, 5, 3, 0, 6, 4, false);
            }

            if (room4.hasOpening[EnumFacing.SOUTH.getIndex()]) {
                this.generateWaterBox(selector, box, 11, 5, 0, 12, 6, 0, false);
            }

            if (room4.hasOpening[EnumFacing.NORTH.getIndex()]) {
                this.generateWaterBox(selector, box, 11, 5, 7, 12, 6, 7, false);
            }

            if (room4.hasOpening[EnumFacing.EAST.getIndex()]) {
                this.generateWaterBox(selector, box, 15, 5, 3, 15, 6, 4, false);
            }
            return true;
        }
    }

    public static class DoubleZRoom extends Piece {
        public DoubleZRoom() {}

        public DoubleZRoom(EnumFacing facing, RoomDefinition room, Random rand) {
            super(1, facing, room, 1, 1, 2);
        }

        @Override
        public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
            ChunkSelector selector = new ChunkSelector(world);

            RoomDefinition room1 = this.roomDefinition.connections[EnumFacing.NORTH.getIndex()];
            RoomDefinition room2 = this.roomDefinition;
            if (this.roomDefinition.index / 25 > 0) {
                this.generateDefaultFloor(selector, box, 0, 8, room1.hasOpening[EnumFacing.DOWN.getIndex()]);
                this.generateDefaultFloor(selector, box, 0, 0, room2.hasOpening[EnumFacing.DOWN.getIndex()]);
            }

            if (room2.connections[EnumFacing.UP.getIndex()] == null) {
                this.generateBoxOnFillOnly(selector, box, 1, 4, 1, 6, 4, 7, ROUGH_PRISMARINE);
            }

            if (room1.connections[EnumFacing.UP.getIndex()] == null) {
                this.generateBoxOnFillOnly(selector, box, 1, 4, 8, 6, 4, 14, ROUGH_PRISMARINE);
            }

            this.fillWithBlocks(selector, box, 0, 3, 0 , 0, 3, 15, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 7, 3, 0 , 7, 3, 15, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 1, 3, 0 , 7, 3, 0 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 1, 3, 15, 6, 3, 15, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 0, 2, 0 , 0, 2, 15, ROUGH_PRISMARINE , false);
            this.fillWithBlocks(selector, box, 7, 2, 0 , 7, 2, 15, ROUGH_PRISMARINE , false);
            this.fillWithBlocks(selector, box, 1, 2, 0 , 7, 2, 0 , ROUGH_PRISMARINE , false);
            this.fillWithBlocks(selector, box, 1, 2, 15, 6, 2, 15, ROUGH_PRISMARINE , false);
            this.fillWithBlocks(selector, box, 0, 1, 0 , 0, 1, 15, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 7, 1, 0 , 7, 1, 15, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 1, 1, 0 , 7, 1, 0 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 1, 1, 15, 6, 1, 15, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 1, 1, 1 , 1, 1, 2 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 6, 1, 1 , 6, 1, 2 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 1, 3, 1 , 1, 3, 2 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 6, 3, 1 , 6, 3, 2 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 1, 1, 13, 1, 1, 14, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 6, 1, 13, 6, 1, 14, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 1, 3, 13, 1, 3, 14, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 6, 3, 13, 6, 3, 14, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 2, 1, 6 , 2, 3, 6 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 5, 1, 6 , 5, 3, 6 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 2, 1, 9 , 2, 3, 9 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 5, 1, 9 , 5, 3, 9 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 3, 2, 6 , 4, 2, 6 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 3, 2, 9 , 4, 2, 9 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 2, 2, 7 , 2, 2, 8 , BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 5, 2, 7 , 5, 2, 8 , BRICKS_PRISMARINE, false);
            this.setBlockState(selector, SEA_LANTERN, 2, 2, 5, box);
            this.setBlockState(selector, SEA_LANTERN, 5, 2, 5, box);
            this.setBlockState(selector, SEA_LANTERN, 2, 2, 10, box);
            this.setBlockState(selector, SEA_LANTERN, 5, 2, 10, box);
            this.setBlockState(selector, BRICKS_PRISMARINE, 2, 3, 5, box);
            this.setBlockState(selector, BRICKS_PRISMARINE, 5, 3, 5, box);
            this.setBlockState(selector, BRICKS_PRISMARINE, 2, 3, 10, box);
            this.setBlockState(selector, BRICKS_PRISMARINE, 5, 3, 10, box);

            if (room2.hasOpening[EnumFacing.SOUTH.getIndex()]) {
                this.generateWaterBox(selector, box, 3, 1, 0, 4, 2, 0, false);
            }

            if (room2.hasOpening[EnumFacing.EAST.getIndex()]) {
                this.generateWaterBox(selector, box, 7, 1, 3, 7, 2, 4, false);
            }

            if (room2.hasOpening[EnumFacing.WEST.getIndex()]) {
                this.generateWaterBox(selector, box, 0, 1, 3, 0, 2, 4, false);
            }

            if (room1.hasOpening[EnumFacing.NORTH.getIndex()]) {
                this.generateWaterBox(selector, box, 3, 1, 15, 4, 2, 15, false);
            }

            if (room1.hasOpening[EnumFacing.WEST.getIndex()]) {
                this.generateWaterBox(selector, box, 0, 1, 11, 0, 2, 12, false);
            }

            if (room1.hasOpening[EnumFacing.EAST.getIndex()]) {
                this.generateWaterBox(selector, box, 7, 1, 11, 7, 2, 12, false);
            }
            return true;
        }
    }


    public static class DoubleXRoom extends Piece {
        public DoubleXRoom() {}

        public DoubleXRoom(EnumFacing facing, RoomDefinition room, Random rand) {
            super(1, facing, room, 2, 1, 1);
        }

        @Override
        public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
            ChunkSelector selector = new ChunkSelector(world);

            RoomDefinition room1 = this.roomDefinition.connections[EnumFacing.EAST.getIndex()];
            RoomDefinition room2 = this.roomDefinition;

            if (this.roomDefinition.index / 25 > 0) {
                this.generateDefaultFloor(selector, box, 8, 0, room1.hasOpening[EnumFacing.DOWN.getIndex()]);
                this.generateDefaultFloor(selector, box, 0, 0, room2.hasOpening[EnumFacing.DOWN.getIndex()]);
            }

            if (room2.connections[EnumFacing.UP.getIndex()] == null) {
                this.generateBoxOnFillOnly(selector, box, 1, 4, 1, 7, 4, 6, ROUGH_PRISMARINE);
            }

            if (room1.connections[EnumFacing.UP.getIndex()] == null) {
                this.generateBoxOnFillOnly(selector, box, 8, 4, 1, 14, 4, 6, ROUGH_PRISMARINE);
            }

            this.fillWithBlocks(selector, box, 0 , 3, 0, 0 , 3, 7, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 15, 3, 0, 15, 3, 7, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 1 , 3, 0, 15, 3, 0, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 1 , 3, 7, 14, 3, 7, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 0 , 2, 0, 0 , 2, 7, ROUGH_PRISMARINE , false);
            this.fillWithBlocks(selector, box, 15, 2, 0, 15, 2, 7, ROUGH_PRISMARINE , false);
            this.fillWithBlocks(selector, box, 1 , 2, 0, 15, 2, 0, ROUGH_PRISMARINE , false);
            this.fillWithBlocks(selector, box, 1 , 2, 7, 14, 2, 7, ROUGH_PRISMARINE , false);
            this.fillWithBlocks(selector, box, 0 , 1, 0, 0 , 1, 7, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 15, 1, 0, 15, 1, 7, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 1 , 1, 0, 15, 1, 0, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 1 , 1, 7, 14, 1, 7, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 5 , 1, 0, 10, 1, 4, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 6 , 2, 0, 9 , 2, 3, ROUGH_PRISMARINE , false);
            this.fillWithBlocks(selector, box, 5 , 3, 0, 10, 3, 4, BRICKS_PRISMARINE, false);
            this.setBlockState(selector, SEA_LANTERN, 6, 2, 3, box);
            this.setBlockState(selector, SEA_LANTERN, 9, 2, 3, box);

            if (room2.hasOpening[EnumFacing.SOUTH.getIndex()]) {
                this.generateWaterBox(selector, box, 3, 1, 0, 4, 2, 0, false);
            }

            if (room2.hasOpening[EnumFacing.NORTH.getIndex()]) {
                this.generateWaterBox(selector, box, 3, 1, 7, 4, 2, 7, false);
            }

            if (room2.hasOpening[EnumFacing.WEST.getIndex()]) {
                this.generateWaterBox(selector, box, 0, 1, 3, 0, 2, 4, false);
            }

            if (room1.hasOpening[EnumFacing.SOUTH.getIndex()]) {
                this.generateWaterBox(selector, box, 11, 1, 0, 12, 2, 0, false);
            }

            if (room1.hasOpening[EnumFacing.NORTH.getIndex()]) {
                this.generateWaterBox(selector, box, 11, 1, 7, 12, 2, 7, false);
            }

            if (room1.hasOpening[EnumFacing.EAST.getIndex()]) {
                this.generateWaterBox(selector, box, 15, 1, 3, 15, 2, 4, false);
            }
            return true;
        }
    }


    public static class DoubleYRoom extends Piece {
        public DoubleYRoom() {}

        public DoubleYRoom(EnumFacing facing, RoomDefinition room, Random rand) {
            super(1, facing, room, 1, 2, 1);
        }

        @Override
        public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
            ChunkSelector selector = new ChunkSelector(world);

            if (this.roomDefinition.index / 25 > 0) {
                this.generateDefaultFloor(selector, box, 0, 0, this.roomDefinition.hasOpening[EnumFacing.DOWN.getIndex()]);
            }

            RoomDefinition room = this.roomDefinition.connections[EnumFacing.UP.getIndex()];
            if (room.connections[EnumFacing.UP.getIndex()] == null) {
                this.generateBoxOnFillOnly(selector, box, 1, 8, 1, 6, 8, 6, ROUGH_PRISMARINE);
            }

            this.fillWithBlocks(selector, box, 0, 4, 0, 0, 4, 7, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 7, 4, 0, 7, 4, 7, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 1, 4, 0, 6, 4, 0, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 1, 4, 7, 6, 4, 7, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 2, 4, 1, 2, 4, 2, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 1, 4, 2, 1, 4, 2, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 5, 4, 1, 5, 4, 2, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 6, 4, 2, 6, 4, 2, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 2, 4, 5, 2, 4, 6, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 1, 4, 5, 1, 4, 5, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 5, 4, 5, 5, 4, 6, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 6, 4, 5, 6, 4, 5, BRICKS_PRISMARINE, false);

            RoomDefinition room2 = this.roomDefinition;

            for(int i = 1; i <= 5; i += 4) {
                int zMin = 0;
                if (room2.hasOpening[EnumFacing.SOUTH.getIndex()]) {
                    this.fillWithBlocks(selector, box, 2, i, zMin, 2, i + 2, zMin, BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, 5, i, zMin, 5, i + 2, zMin, BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, 3, i + 2, zMin, 4, i + 2, zMin, BRICKS_PRISMARINE, false);
                } else {
                    this.fillWithBlocks(selector, box, 0, i, zMin, 7, i + 2, zMin, BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, 0, i + 1, zMin, 7, i + 1, zMin, ROUGH_PRISMARINE, false);
                }

                zMin = 7;
                if (room2.hasOpening[EnumFacing.NORTH.getIndex()]) {
                    this.fillWithBlocks(selector, box, 2, i, zMin, 2, i + 2, zMin, BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, 5, i, zMin, 5, i + 2, zMin, BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, 3, i + 2, zMin, 4, i + 2, zMin, BRICKS_PRISMARINE, false);
                } else {
                    this.fillWithBlocks(selector, box, 0, i, zMin, 7, i + 2, zMin, BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, 0, i + 1, zMin, 7, i + 1, zMin, ROUGH_PRISMARINE, false);
                }

                int xMin = 0;
                if (room2.hasOpening[EnumFacing.WEST.getIndex()]) {
                    this.fillWithBlocks(selector, box, xMin, i, 2, xMin, i + 2, 2, BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, xMin, i, 5, xMin, i + 2, 5, BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, xMin, i + 2, 3, xMin, i + 2, 4, BRICKS_PRISMARINE, false);
                } else {
                    this.fillWithBlocks(selector, box, xMin, i, 0, xMin, i + 2, 7, BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, xMin, i + 1, 0, xMin, i + 1, 7, ROUGH_PRISMARINE, false);
                }

                xMin = 7;
                if (room2.hasOpening[EnumFacing.EAST.getIndex()]) {
                    this.fillWithBlocks(selector, box, xMin, i, 2, xMin, i + 2, 2, BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, xMin, i, 5, xMin, i + 2, 5, BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, xMin, i + 2, 3, xMin, i + 2, 4, BRICKS_PRISMARINE, false);
                } else {
                    this.fillWithBlocks(selector, box, xMin, i, 0, xMin, i + 2, 7, BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, xMin, i + 1, 0, xMin, i + 1, 7, ROUGH_PRISMARINE, false);
                }

                room2 = room;
            }
            return true;
        }
    }


    public static class SimpleTopRoom extends Piece {
        public SimpleTopRoom() {}

        public SimpleTopRoom(EnumFacing facing, RoomDefinition room, Random rand) {
            super(1, facing, room, 1, 1, 1);
        }

        @Override
        public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
            ChunkSelector selector = new ChunkSelector(world);
            if (this.roomDefinition.index / 25 > 0) {
                this.generateDefaultFloor(selector, box, 0, 0, this.roomDefinition.hasOpening[EnumFacing.DOWN.getIndex()]);
            }

            if (this.roomDefinition.connections[EnumFacing.UP.getIndex()] == null) {
                this.generateBoxOnFillOnly(selector, box, 1, 4, 1, 6, 4, 6, ROUGH_PRISMARINE);
            }

            for(int x = 1; x <= 6; ++x) {
                for(int z = 1; z <= 6; ++z) {
                    if (rand.nextInt(3) != 0) {
                        int y = 2 + (rand.nextInt(4) == 0 ? 0 : 1);
                        this.fillWithBlocks(selector, box, x, y, z, x, 3, z, Blocks.SPONGE.getStateFromMeta(1), Blocks.SPONGE.getStateFromMeta(1), false);
                    }
                }
            }

            this.fillWithBlocks(selector, box, 0, 1, 0, 0, 1, 7, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 7, 1, 0, 7, 1, 7, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 1, 1, 0, 6, 1, 0, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 1, 1, 7, 6, 1, 7, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 0, 2, 0, 0, 2, 7, DARK_PRISMARINE  , false);
            this.fillWithBlocks(selector, box, 7, 2, 0, 7, 2, 7, DARK_PRISMARINE  , false);
            this.fillWithBlocks(selector, box, 1, 2, 0, 6, 2, 0, DARK_PRISMARINE  , false);
            this.fillWithBlocks(selector, box, 1, 2, 7, 6, 2, 7, DARK_PRISMARINE  , false);
            this.fillWithBlocks(selector, box, 0, 3, 0, 0, 3, 7, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 7, 3, 0, 7, 3, 7, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 1, 3, 0, 6, 3, 0, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 1, 3, 7, 6, 3, 7, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 0, 1, 3, 0, 2, 4, DARK_PRISMARINE  , false);
            this.fillWithBlocks(selector, box, 7, 1, 3, 7, 2, 4, DARK_PRISMARINE  , false);
            this.fillWithBlocks(selector, box, 3, 1, 0, 4, 2, 0, DARK_PRISMARINE  , false);
            this.fillWithBlocks(selector, box, 3, 1, 7, 4, 2, 7, DARK_PRISMARINE  , false);

            if (this.roomDefinition.hasOpening[EnumFacing.SOUTH.getIndex()]) {
                this.generateWaterBox(selector, box, 3, 1, 0, 4, 2, 0, false);
            }
            return true;
        }
    }

    public static class SimpleRoom extends Piece {
        private int mainDesign;
        public SimpleRoom() {}

        public SimpleRoom(EnumFacing facing, RoomDefinition room, Random rand) {
            super(1, facing, room, 1, 1, 1);
            this.mainDesign = rand.nextInt(3);
        }

        @Override
        public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
            ChunkSelector selector = new ChunkSelector(world);
            BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

            if (this.roomDefinition.index / 25 > 0) {
                this.generateDefaultFloor(selector, box, 0, 0, this.roomDefinition.hasOpening[EnumFacing.DOWN.getIndex()]);
            }

            if (this.roomDefinition.connections[EnumFacing.UP.getIndex()] == null) {
                this.generateBoxOnFillOnly(selector, box, 1, 4, 1, 6, 4, 6, ROUGH_PRISMARINE);
            }

            boolean flag = this.mainDesign != 0 && rand.nextBoolean() && !this.roomDefinition.hasOpening[EnumFacing.DOWN.getIndex()] && !this.roomDefinition.hasOpening[EnumFacing.UP.getIndex()] && this.roomDefinition.countOpenings() > 1;
            if (this.mainDesign == 0) {
                this.fillWithBlocks(selector, box, 0, 1, 0, 2, 1, 2, BRICKS_PRISMARINE,false);
                this.fillWithBlocks(selector, box, 0, 3, 0, 2, 3, 2, BRICKS_PRISMARINE,false);
                this.fillWithBlocks(selector, box, 0, 2, 0, 0, 2, 2, ROUGH_PRISMARINE ,false);
                this.fillWithBlocks(selector, box, 1, 2, 0, 2, 2, 0, ROUGH_PRISMARINE ,false);
                this.setBlockStateM(mut, selector, SEA_LANTERN, 1, 2, 1, box);
                this.fillWithBlocks(selector, box, 5, 1, 0, 7, 1, 2, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 5, 3, 0, 7, 3, 2, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 7, 2, 0, 7, 2, 2, ROUGH_PRISMARINE , false);
                this.fillWithBlocks(selector, box, 5, 2, 0, 6, 2, 0, ROUGH_PRISMARINE , false);
                this.setBlockStateM(mut, selector, SEA_LANTERN, 6, 2, 1, box);
                this.fillWithBlocks(selector, box, 0, 1, 5, 2, 1, 7, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 0, 3, 5, 2, 3, 7, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 0, 2, 5, 0, 2, 7, ROUGH_PRISMARINE , false);
                this.fillWithBlocks(selector, box, 1, 2, 7, 2, 2, 7, ROUGH_PRISMARINE , false);
                this.setBlockStateM(mut, selector, SEA_LANTERN, 1, 2, 6, box);
                this.fillWithBlocks(selector, box, 5, 1, 5, 7, 1, 7, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 5, 3, 5, 7, 3, 7, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 7, 2, 5, 7, 2, 7, ROUGH_PRISMARINE , false);
                this.fillWithBlocks(selector, box, 5, 2, 7, 6, 2, 7, ROUGH_PRISMARINE , false);
                this.setBlockStateM(mut, selector, SEA_LANTERN, 6, 2, 6, box);
                if (this.roomDefinition.hasOpening[EnumFacing.SOUTH.getIndex()]) {
                    this.fillWithBlocks(selector, box, 3, 3, 0, 4, 3, 0, BRICKS_PRISMARINE, false);
                } else {
                    this.fillWithBlocks(selector, box, 3, 3, 0, 4, 3, 1, BRICKS_PRISMARINE,false);
                    this.fillWithBlocks(selector, box, 3, 2, 0, 4, 2, 0, ROUGH_PRISMARINE ,false);
                    this.fillWithBlocks(selector, box, 3, 1, 0, 4, 1, 1, BRICKS_PRISMARINE, false);
                }

                if (this.roomDefinition.hasOpening[EnumFacing.NORTH.getIndex()]) {
                    this.fillWithBlocks(selector, box, 3, 3, 7, 4, 3, 7, BRICKS_PRISMARINE, false);
                } else {
                    this.fillWithBlocks(selector, box, 3, 3, 6, 4, 3, 7, BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, 3, 2, 7, 4, 2, 7, ROUGH_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, 3, 1, 6, 4, 1, 7, BRICKS_PRISMARINE, false);
                }

                if (this.roomDefinition.hasOpening[EnumFacing.WEST.getIndex()]) {
                    this.fillWithBlocks(selector, box, 0, 3, 3, 0, 3, 4, BRICKS_PRISMARINE, false);
                } else {
                    this.fillWithBlocks(selector, box, 0, 3, 3, 1, 3, 4, BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, 0, 2, 3, 0, 2, 4, ROUGH_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, 0, 1, 3, 1, 1, 4, BRICKS_PRISMARINE, false);
                }

                if (this.roomDefinition.hasOpening[EnumFacing.EAST.getIndex()]) {
                    this.fillWithBlocks(selector, box, 7, 3, 3, 7, 3, 4, BRICKS_PRISMARINE, false);
                } else {
                    this.fillWithBlocks(selector, box, 6, 3, 3, 7, 3, 4, BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, 7, 2, 3, 7, 2, 4, ROUGH_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, 6, 1, 3, 7, 1, 4, BRICKS_PRISMARINE, false);
                }
            } else if (this.mainDesign == 1) {
                this.fillWithBlocks(selector, box, 2, 1, 2, 2, 3, 2, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 2, 1, 5, 2, 3, 5, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 5, 1, 5, 5, 3, 5, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 5, 1, 2, 5, 3, 2, BRICKS_PRISMARINE, false);
                this.setBlockStateM(mut, selector, SEA_LANTERN, 2, 2, 2, box);
                this.setBlockStateM(mut, selector, SEA_LANTERN, 2, 2, 5, box);
                this.setBlockStateM(mut, selector, SEA_LANTERN, 5, 2, 5, box);
                this.setBlockStateM(mut, selector, SEA_LANTERN, 5, 2, 2, box);
                this.fillWithBlocks(selector, box, 0, 1, 0, 1, 3, 0, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 0, 1, 1, 0, 3, 1, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 0, 1, 7, 1, 3, 7, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 0, 1, 6, 0, 3, 6, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 6, 1, 7, 7, 3, 7, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 7, 1, 6, 7, 3, 6, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 6, 1, 0, 7, 3, 0, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 7, 1, 1, 7, 3, 1, BRICKS_PRISMARINE, false);
                this.setBlockStateM(mut, selector, ROUGH_PRISMARINE, 1, 2, 0, box);
                this.setBlockStateM(mut, selector, ROUGH_PRISMARINE, 0, 2, 1, box);
                this.setBlockStateM(mut, selector, ROUGH_PRISMARINE, 1, 2, 7, box);
                this.setBlockStateM(mut, selector, ROUGH_PRISMARINE, 0, 2, 6, box);
                this.setBlockStateM(mut, selector, ROUGH_PRISMARINE, 6, 2, 7, box);
                this.setBlockStateM(mut, selector, ROUGH_PRISMARINE, 7, 2, 6, box);
                this.setBlockStateM(mut, selector, ROUGH_PRISMARINE, 6, 2, 0, box);
                this.setBlockStateM(mut, selector, ROUGH_PRISMARINE, 7, 2, 1, box);

                if (!this.roomDefinition.hasOpening[EnumFacing.SOUTH.getIndex()]) {
                    this.fillWithBlocks(selector, box, 1, 3, 0, 6, 3, 0, BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, 1, 2, 0, 6, 2, 0, ROUGH_PRISMARINE , false);
                    this.fillWithBlocks(selector, box, 1, 1, 0, 6, 1, 0, BRICKS_PRISMARINE, false);
                }

                if (!this.roomDefinition.hasOpening[EnumFacing.NORTH.getIndex()]) {
                    this.fillWithBlocks(selector, box, 1, 3, 7, 6, 3, 7, BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, 1, 2, 7, 6, 2, 7, ROUGH_PRISMARINE , false);
                    this.fillWithBlocks(selector, box, 1, 1, 7, 6, 1, 7, BRICKS_PRISMARINE, false);
                }

                if (!this.roomDefinition.hasOpening[EnumFacing.WEST.getIndex()]) {
                    this.fillWithBlocks(selector, box, 0, 3, 1, 0, 3, 6, BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, 0, 2, 1, 0, 2, 6, ROUGH_PRISMARINE , false);
                    this.fillWithBlocks(selector, box, 0, 1, 1, 0, 1, 6, BRICKS_PRISMARINE, false);
                }

                if (!this.roomDefinition.hasOpening[EnumFacing.EAST.getIndex()]) {
                    this.fillWithBlocks(selector, box, 7, 3, 1, 7, 3, 6, BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, 7, 2, 1, 7, 2, 6, ROUGH_PRISMARINE , false);
                    this.fillWithBlocks(selector, box, 7, 1, 1, 7, 1, 6, BRICKS_PRISMARINE, false);
                }
            } else if (this.mainDesign == 2) {
                this.fillWithBlocks(selector, box, 0, 1, 0, 0, 1, 7, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 7, 1, 0, 7, 1, 7, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 1, 1, 0, 6, 1, 0, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 1, 1, 7, 6, 1, 7, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 0, 2, 0, 0, 2, 7, DARK_PRISMARINE  , false);
                this.fillWithBlocks(selector, box, 7, 2, 0, 7, 2, 7, DARK_PRISMARINE  , false);
                this.fillWithBlocks(selector, box, 1, 2, 0, 6, 2, 0, DARK_PRISMARINE  , false);
                this.fillWithBlocks(selector, box, 1, 2, 7, 6, 2, 7, DARK_PRISMARINE  , false);
                this.fillWithBlocks(selector, box, 0, 3, 0, 0, 3, 7, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 7, 3, 0, 7, 3, 7, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 1, 3, 0, 6, 3, 0, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 1, 3, 7, 6, 3, 7, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 0, 1, 3, 0, 2, 4, DARK_PRISMARINE  , false);
                this.fillWithBlocks(selector, box, 7, 1, 3, 7, 2, 4, DARK_PRISMARINE  , false);
                this.fillWithBlocks(selector, box, 3, 1, 0, 4, 2, 0, DARK_PRISMARINE  , false);
                this.fillWithBlocks(selector, box, 3, 1, 7, 4, 2, 7, DARK_PRISMARINE  , false);

                if (this.roomDefinition.hasOpening[EnumFacing.SOUTH.getIndex()]) {
                    this.generateWaterBox(selector, box, 3, 1, 0, 4, 2, 0, false);
                }

                if (this.roomDefinition.hasOpening[EnumFacing.NORTH.getIndex()]) {
                    this.generateWaterBox(selector, box, 3, 1, 7, 4, 2, 7, false);
                }

                if (this.roomDefinition.hasOpening[EnumFacing.WEST.getIndex()]) {
                    this.generateWaterBox(selector, box, 0, 1, 3, 0, 2, 4, false);
                }

                if (this.roomDefinition.hasOpening[EnumFacing.EAST.getIndex()]) {
                    this.generateWaterBox(selector, box, 7, 1, 3, 7, 2, 4, false);
                }
            }

            if (flag) {
                this.fillWithBlocks(selector, box, 3, 1, 3, 4, 1, 4, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 3, 2, 3, 4, 2, 4, ROUGH_PRISMARINE , false);
                this.fillWithBlocks(selector, box, 3, 3, 3, 4, 3, 4, BRICKS_PRISMARINE, false);
            }
            return true;
        }
    }

    public static class EntryRoom extends Piece {
        public EntryRoom() {}

        public EntryRoom(EnumFacing facing, RoomDefinition room) {
            super(1, facing, room, 1, 1, 1);
        }

        @Override
        public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
            ChunkSelector selector = new ChunkSelector(world);

            this.fillWithBlocks(selector, box, 0, 3, 0, 2, 3, 7, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 5, 3, 0, 7, 3, 7, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 0, 2, 0, 1, 2, 7, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 6, 2, 0, 7, 2, 7, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 0, 1, 0, 0, 1, 7, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 7, 1, 0, 7, 1, 7, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 0, 1, 7, 7, 3, 7, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 1, 1, 0, 2, 3, 0, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(selector, box, 5, 1, 0, 6, 3, 0, BRICKS_PRISMARINE, false);

            if (this.roomDefinition.hasOpening[EnumFacing.NORTH.getIndex()]) {
                this.generateWaterBox(selector, box, 3, 1, 7, 4, 2, 7, false);
            }

            if (this.roomDefinition.hasOpening[EnumFacing.WEST.getIndex()]) {
                this.generateWaterBox(selector, box, 0, 1, 3, 1, 2, 4, false);
            }

            if (this.roomDefinition.hasOpening[EnumFacing.EAST.getIndex()]) {
                this.generateWaterBox(selector, box, 6, 1, 3, 7, 2, 4, false);
            }
            return true;
        }
    }

    public static class MonumentBuilding extends Piece {
        private RoomDefinition sourceRoom;
        private RoomDefinition coreRoom;
        private final List<Piece> childPieces = Lists.newArrayList();

        public MonumentBuilding() {}

        public MonumentBuilding(Random rand, int x, int z, EnumFacing facing) {
            super(0);
            this.setCoordBaseMode(facing);
            EnumFacing lvt_5_1_ = this.getCoordBaseMode();
            if (lvt_5_1_.getAxis() == Axis.Z) {
                this.boundingBox = new StructureBoundingBox(x, 39, z, x + 58 - 1, 61, z + 58 - 1);
            } else {
                this.boundingBox = new StructureBoundingBox(x, 39, z, x + 58 - 1, 61, z + 58 - 1);
            }

            List<RoomDefinition> rooms = this.generateRoomGraph(rand);
            this.sourceRoom.claimed = true;
            this.childPieces.add(new EntryRoom(lvt_5_1_, this.sourceRoom));
            this.childPieces.add(new MonumentCoreRoom(lvt_5_1_, this.coreRoom, rand));
            List<MonumentRoomFitHelper> helpers = Lists.newArrayList();
            helpers.add(new XYDoubleRoomFitHelper());
            helpers.add(new YZDoubleRoomFitHelper());
            helpers.add(new ZDoubleRoomFitHelper());
            helpers.add(new XDoubleRoomFitHelper());
            helpers.add(new YDoubleRoomFitHelper());
            helpers.add(new FitSimpleRoomTopHelper());
            helpers.add(new FitSimpleRoomHelper());
            Iterator var8 = rooms.iterator();

            while(true) {
                while(true) {
                    RoomDefinition lvt_9_1_;
                    do {
                        do {
                            if (!var8.hasNext()) {
                                int lvt_8_1_ = this.boundingBox.minY;
                                int lvt_9_2_ = this.getXWithOffset(9, 22);
                                int lvt_10_1_ = this.getZWithOffset(9, 22);
                                Iterator var18 = this.childPieces.iterator();

                                while(var18.hasNext()) {
                                    Piece lvt_12_1_ = (Piece)var18.next();
                                    lvt_12_1_.getBoundingBox().offset(lvt_9_2_, lvt_8_1_, lvt_10_1_);
                                }

                                StructureBoundingBox lvt_11_2_ = StructureBoundingBox.createProper(this.getXWithOffset(1, 1), this.getYWithOffset(1), this.getZWithOffset(1, 1), this.getXWithOffset(23, 21), this.getYWithOffset(8), this.getZWithOffset(23, 21));
                                StructureBoundingBox lvt_12_2_ = StructureBoundingBox.createProper(this.getXWithOffset(34, 1), this.getYWithOffset(1), this.getZWithOffset(34, 1), this.getXWithOffset(56, 21), this.getYWithOffset(8), this.getZWithOffset(56, 21));
                                StructureBoundingBox lvt_13_1_ = StructureBoundingBox.createProper(this.getXWithOffset(22, 22), this.getYWithOffset(13), this.getZWithOffset(22, 22), this.getXWithOffset(35, 35), this.getYWithOffset(17), this.getZWithOffset(35, 35));
                                int lvt_14_1_ = rand.nextInt();
                                this.childPieces.add(new WingRoom(lvt_5_1_, lvt_11_2_, lvt_14_1_++));
                                this.childPieces.add(new WingRoom(lvt_5_1_, lvt_12_2_, lvt_14_1_++));
                                this.childPieces.add(new Penthouse(lvt_5_1_, lvt_13_1_));
                                return;
                            }

                            lvt_9_1_ = (RoomDefinition)var8.next();
                        } while(lvt_9_1_.claimed);
                    } while(lvt_9_1_.isSpecial());

                    Iterator var10 = helpers.iterator();

                    while(var10.hasNext()) {
                        MonumentRoomFitHelper lvt_11_1_ = (MonumentRoomFitHelper)var10.next();
                        if (lvt_11_1_.fits(lvt_9_1_)) {
                            this.childPieces.add(lvt_11_1_.create(lvt_5_1_, lvt_9_1_, rand));
                            break;
                        }
                    }
                }
            }
        }

        private List<RoomDefinition> generateRoomGraph(Random p_175836_1_) {
            RoomDefinition[] lvt_2_1_ = new RoomDefinition[75];

            int lvt_3_4_;
            int lvt_4_4_;
            boolean lvt_5_3_;
            int lvt_6_4_;
            for(lvt_3_4_ = 0; lvt_3_4_ < 5; ++lvt_3_4_) {
                for(lvt_4_4_ = 0; lvt_4_4_ < 4; ++lvt_4_4_) {
                    lvt_5_3_ = false;
                    lvt_6_4_ = getRoomIndex(lvt_3_4_, 0, lvt_4_4_);
                    lvt_2_1_[lvt_6_4_] = new RoomDefinition(lvt_6_4_);
                }
            }

            for(lvt_3_4_ = 0; lvt_3_4_ < 5; ++lvt_3_4_) {
                for(lvt_4_4_ = 0; lvt_4_4_ < 4; ++lvt_4_4_) {
                    lvt_5_3_ = true;
                    lvt_6_4_ = getRoomIndex(lvt_3_4_, 1, lvt_4_4_);
                    lvt_2_1_[lvt_6_4_] = new RoomDefinition(lvt_6_4_);
                }
            }

            for(lvt_3_4_ = 1; lvt_3_4_ < 4; ++lvt_3_4_) {
                for(lvt_4_4_ = 0; lvt_4_4_ < 2; ++lvt_4_4_) {
                    lvt_5_3_ = true;
                    lvt_6_4_ = getRoomIndex(lvt_3_4_, 2, lvt_4_4_);
                    lvt_2_1_[lvt_6_4_] = new RoomDefinition(lvt_6_4_);
                }
            }

            this.sourceRoom = lvt_2_1_[GRIDROOM_SOURCE_INDEX];

            int var8;
            int var9;
            int lvt_11_1_;
            int lvt_12_2_;
            int lvt_13_1_;
            for(lvt_3_4_ = 0; lvt_3_4_ < 5; ++lvt_3_4_) {
                for(lvt_4_4_ = 0; lvt_4_4_ < 5; ++lvt_4_4_) {
                    for(int lvt_5_4_ = 0; lvt_5_4_ < 3; ++lvt_5_4_) {
                        lvt_6_4_ = getRoomIndex(lvt_3_4_, lvt_5_4_, lvt_4_4_);
                        if (lvt_2_1_[lvt_6_4_] != null) {
                            EnumFacing[] var7 = EnumFacing.values();
                            var8 = var7.length;

                            for(var9 = 0; var9 < var8; ++var9) {
                                EnumFacing lvt_10_1_ = var7[var9];
                                lvt_11_1_ = lvt_3_4_ + lvt_10_1_.getXOffset();
                                lvt_12_2_ = lvt_5_4_ + lvt_10_1_.getYOffset();
                                lvt_13_1_ = lvt_4_4_ + lvt_10_1_.getZOffset();
                                if (lvt_11_1_ >= 0 && lvt_11_1_ < 5 && lvt_13_1_ >= 0 && lvt_13_1_ < 5 && lvt_12_2_ >= 0 && lvt_12_2_ < 3) {
                                    int lvt_14_1_ = getRoomIndex(lvt_11_1_, lvt_12_2_, lvt_13_1_);
                                    if (lvt_2_1_[lvt_14_1_] != null) {
                                        if (lvt_13_1_ == lvt_4_4_) {
                                            lvt_2_1_[lvt_6_4_].setConnection(lvt_10_1_, lvt_2_1_[lvt_14_1_]);
                                        } else {
                                            lvt_2_1_[lvt_6_4_].setConnection(lvt_10_1_.getOpposite(), lvt_2_1_[lvt_14_1_]);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            RoomDefinition lvt_3_5_ = new RoomDefinition(1003);
            RoomDefinition lvt_4_5_ = new RoomDefinition(1001);
            RoomDefinition lvt_5_5_ = new RoomDefinition(1002);
            lvt_2_1_[GRIDROOM_TOP_CONNECT_INDEX].setConnection(EnumFacing.UP, lvt_3_5_);
            lvt_2_1_[GRIDROOM_LEFTWING_CONNECT_INDEX].setConnection(EnumFacing.SOUTH, lvt_4_5_);
            lvt_2_1_[GRIDROOM_RIGHTWING_CONNECT_INDEX].setConnection(EnumFacing.SOUTH, lvt_5_5_);
            lvt_3_5_.claimed = true;
            lvt_4_5_.claimed = true;
            lvt_5_5_.claimed = true;
            this.sourceRoom.isSource = true;
            this.coreRoom = lvt_2_1_[getRoomIndex(p_175836_1_.nextInt(4), 0, 2)];
            this.coreRoom.claimed = true;
            this.coreRoom.connections[EnumFacing.EAST.getIndex()].claimed = true;
            this.coreRoom.connections[EnumFacing.NORTH.getIndex()].claimed = true;
            this.coreRoom.connections[EnumFacing.EAST.getIndex()].connections[EnumFacing.NORTH.getIndex()].claimed = true;
            this.coreRoom.connections[EnumFacing.UP.getIndex()].claimed = true;
            this.coreRoom.connections[EnumFacing.EAST.getIndex()].connections[EnumFacing.UP.getIndex()].claimed = true;
            this.coreRoom.connections[EnumFacing.NORTH.getIndex()].connections[EnumFacing.UP.getIndex()].claimed = true;
            this.coreRoom.connections[EnumFacing.EAST.getIndex()].connections[EnumFacing.NORTH.getIndex()].connections[EnumFacing.UP.getIndex()].claimed = true;
            List<RoomDefinition> lvt_6_5_ = Lists.newArrayList();
            RoomDefinition[] var20 = lvt_2_1_;
            var8 = lvt_2_1_.length;

            for(var9 = 0; var9 < var8; ++var9) {
                RoomDefinition lvt_10_2_ = var20[var9];
                if (lvt_10_2_ != null) {
                    lvt_10_2_.updateOpenings();
                    lvt_6_5_.add(lvt_10_2_);
                }
            }

            lvt_3_5_.updateOpenings();
            Collections.shuffle(lvt_6_5_, p_175836_1_);
            int lvt_7_1_ = 1;
            Iterator var22 = lvt_6_5_.iterator();

            label95:
            while(var22.hasNext()) {
                RoomDefinition lvt_9_1_ = (RoomDefinition)var22.next();
                int lvt_10_3_ = 0;
                lvt_11_1_ = 0;

                while(true) {
                    while(true) {
                        do {
                            if (lvt_10_3_ >= 2 || lvt_11_1_ >= 5) {
                                continue label95;
                            }

                            ++lvt_11_1_;
                            lvt_12_2_ = p_175836_1_.nextInt(6);
                        } while(!lvt_9_1_.hasOpening[lvt_12_2_]);

                        lvt_13_1_ = EnumFacing.byIndex(lvt_12_2_).getOpposite().getIndex();
                        lvt_9_1_.hasOpening[lvt_12_2_] = false;
                        lvt_9_1_.connections[lvt_12_2_].hasOpening[lvt_13_1_] = false;
                        if (lvt_9_1_.findSource(lvt_7_1_++) && lvt_9_1_.connections[lvt_12_2_].findSource(lvt_7_1_++)) {
                            ++lvt_10_3_;
                        } else {
                            lvt_9_1_.hasOpening[lvt_12_2_] = true;
                            lvt_9_1_.connections[lvt_12_2_].hasOpening[lvt_13_1_] = true;
                        }
                    }
                }
            }

            lvt_6_5_.add(lvt_3_5_);
            lvt_6_5_.add(lvt_4_5_);
            lvt_6_5_.add(lvt_5_5_);
            return lvt_6_5_;
        }

        public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
            ChunkSelector selector = new ChunkSelector(world);

            int lvl = Math.max(world.getSeaLevel(), 64) - this.boundingBox.minY;
            this.generateWaterBox(selector, box, 0, 0, 0, 58, lvl, 58, false);

            this.generateWing(false, 0, selector, rand, box);
            this.generateWing(true, 33, selector, rand, box);

            this.generateEntranceArchs(selector, rand, box);
            this.generateEntranceWall(selector, box);
            this.generateRoofPiece(selector, box);
            this.generateLowerWall(selector, box);
            this.generateMiddleWall(selector, rand, box);
            this.generateUpperWall(selector, box);

            int minX;
            label72:
            for(minX = 0; minX < 7; ++minX) {
                int i1 = 0;

                while(true) {
                    while(true) {
                        if (i1 >= 7) {
                            continue label72;
                        }

                        if (i1 == 0 && minX == 3) {
                            i1 = 6;
                        }

                        int ox = minX * 9;
                        int oz = i1 * 9;

                        for(int dx = 0; dx < 4; ++dx) {
                            for(int dz = 0; dz < 4; ++dz) {
                                this.setBlockState(selector, BRICKS_PRISMARINE, ox + dx, 0, oz + dz, box);
                                this.replaceAirAndLiquidDownwards(selector, BRICKS_PRISMARINE, ox + dx, -1, oz + dz, box);
                            }
                        }

                        if (minX != 0 && minX != 6) {
                            i1 += 6;
                        } else {
                            ++i1;
                        }
                    }
                }
            }

            for(minX = 0; minX < 5; ++minX) {
                this.generateWaterBox(selector, box, -1 - minX, 0 + minX * 2, -1 - minX, -1 - minX, 23, 58 + minX, false);
                this.generateWaterBox(selector, box, 58 + minX, 0 + minX * 2, -1 - minX, 58 + minX, 23, 58 + minX, false);
                this.generateWaterBox(selector, box, 0 - minX, 0 + minX * 2, -1 - minX, 57 + minX, 23, -1 - minX, false);
                this.generateWaterBox(selector, box, 0 - minX, 0 + minX * 2, 58 + minX, 57 + minX, 23, 58 + minX, false);
            }

            for (Piece piece : this.childPieces) {
                if (piece.getBoundingBox().intersectsWith(box)) {
                    piece.addComponentParts(world, rand, box);
                }
            }
            return true;
        }


        private void generateWing(boolean b, int xMin, ChunkSelector selector, Random rand, StructureBoundingBox box) {
            //int lvt_6_1_ = true;
            if (this.doesChunkIntersect(box, xMin, 0, xMin + 23, 20)) {
                this.fillWithBlocks(selector, box, xMin + 0, 0, 0, xMin + 24, 0, 20, ROUGH_PRISMARINE, false);
                this.generateWaterBox(selector, box, xMin + 0, 1, 0, xMin + 24, 10, 20, false);

                int dx1;
                for(dx1 = 0; dx1 < 4; ++dx1) {
                    this.fillWithBlocks(selector, box, xMin + dx1     , dx1 + 1, dx1          , xMin + dx1      , dx1 + 1, 20     , BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, xMin + dx1 + 7 , dx1 + 5, dx1 + 7 , xMin + dx1 + 7  , dx1 + 5, 20     , BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, xMin + 17 - dx1, dx1 + 5, dx1 + 7 , xMin + 17 - dx1 , dx1 + 5, 20     , BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, xMin + 24 - dx1, dx1 + 1, dx1          , xMin + 24 - dx1 , dx1 + 1, 20     , BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, xMin + dx1 + 1 , dx1 + 1, dx1          , xMin + 23 - dx1 , dx1 + 1,      dx1     , BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, xMin + dx1 + 8 , dx1 + 5, dx1 + 7 , xMin + 16 - dx1 , dx1 + 5, dx1 + 7, BRICKS_PRISMARINE, false);
                }

                this.fillWithBlocks(selector, box, xMin + 4, 4, 4, xMin + 6, 4, 20, ROUGH_PRISMARINE, false);
                this.fillWithBlocks(selector, box, xMin + 7, 4, 4, xMin + 17, 4, 6, ROUGH_PRISMARINE, false);
                this.fillWithBlocks(selector, box, xMin + 18, 4, 4, xMin + 20, 4, 20, ROUGH_PRISMARINE,false);
                this.fillWithBlocks(selector, box, xMin + 11, 8, 11, xMin + 13, 8, 20, ROUGH_PRISMARINE, false);

                BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

                this.setBlockStateM(mut, selector, DOT_DECO_DATA, xMin + 12, 9, 12, box);
                this.setBlockStateM(mut, selector, DOT_DECO_DATA, xMin + 12, 9, 15, box);
                this.setBlockStateM(mut, selector, DOT_DECO_DATA, xMin + 12, 9, 18, box);
                dx1 = xMin + (b ? 19 : 5);
                int dx2 = xMin + (b ? 5 : 19);

                int dz;
                for(dz = 20; dz >= 5; dz -= 3) {
                    this.setBlockStateM(mut, selector, DOT_DECO_DATA, dx1, 5, dz, box);
                }

                for(dz = 19; dz >= 7; dz -= 3) {
                    this.setBlockStateM(mut, selector, DOT_DECO_DATA, dx2, 5, dz, box);
                }

                for(dz = 0; dz < 4; ++dz) {
                    int lvt_10_1_ = b ? xMin + (24 - (17 - dz * 3)) : xMin + 17 - dz * 3;
                    this.setBlockStateM(mut, selector, DOT_DECO_DATA, lvt_10_1_, 5, 5, box);
                }

                this.setBlockStateM(mut, selector, DOT_DECO_DATA, dx2, 5, 5, box);
                this.fillWithBlocks(selector, box, xMin + 11, 1, 12, xMin + 13, 7, 12, ROUGH_PRISMARINE, false);
                this.fillWithBlocks(selector, box, xMin + 12, 1, 11, xMin + 12, 7, 13, ROUGH_PRISMARINE, false);
            }
        }


        private void generateEntranceArchs(ChunkSelector selector, Random rand, StructureBoundingBox box) {
            if (this.doesChunkIntersect(box, 22, 5, 35, 17)) {
                this.generateWaterBox(selector, box, 25, 0, 0, 32, 8, 20, false);

                BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

                for(int i = 0; i < 4; ++i) {
                    this.fillWithBlocks(selector, box, 24, 2, 5 + i * 4, 24, 4, 5 + i * 4, BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, 22, 4, 5 + i * 4, 23, 4, 5 + i * 4, BRICKS_PRISMARINE, false);
                    this.setBlockStateM(mut, selector, BRICKS_PRISMARINE, 25, 5, 5 + i * 4, box);
                    this.setBlockStateM(mut, selector, BRICKS_PRISMARINE, 26, 6, 5 + i * 4, box);
                    this.setBlockStateM(mut, selector, SEA_LANTERN, 26, 5, 5 + i * 4, box);
                    this.fillWithBlocks(selector, box, 33, 2, 5 + i * 4, 33, 4, 5 + i * 4, BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, 34, 4, 5 + i * 4, 35, 4, 5 + i * 4, BRICKS_PRISMARINE, false);
                    this.setBlockStateM(mut, selector, BRICKS_PRISMARINE, 32, 5, 5 + i * 4, box);
                    this.setBlockStateM(mut, selector, BRICKS_PRISMARINE, 31, 6, 5 + i * 4, box);
                    this.setBlockStateM(mut, selector, SEA_LANTERN, 31, 5, 5 + i * 4, box);
                    this.fillWithBlocks(selector, box, 27, 6, 5 + i * 4, 30, 6, 5 + i * 4, ROUGH_PRISMARINE, false);
                }
            }
        }


        private void generateEntranceWall(ChunkSelector selector, StructureBoundingBox box) {
            if (this.doesChunkIntersect(box, 15, 20, 42, 21)) {
                this.fillWithBlocks(selector, box, 15, 0, 21, 42, 0, 21, ROUGH_PRISMARINE, false);
                this.generateWaterBox(selector, box, 26, 1, 21, 31, 3, 21, false);
                this.fillWithBlocks(selector, box, 21, 12, 21, 36, 12, 21, ROUGH_PRISMARINE , false);
                this.fillWithBlocks(selector, box, 17, 11, 21, 40, 11, 21, ROUGH_PRISMARINE , false);
                this.fillWithBlocks(selector, box, 16, 10, 21, 41, 10, 21, ROUGH_PRISMARINE , false);
                this.fillWithBlocks(selector, box, 15, 7 , 21, 42, 9 , 21, ROUGH_PRISMARINE , false);
                this.fillWithBlocks(selector, box, 16, 6 , 21, 41, 6 , 21, ROUGH_PRISMARINE , false);
                this.fillWithBlocks(selector, box, 17, 5 , 21, 40, 5 , 21, ROUGH_PRISMARINE , false);
                this.fillWithBlocks(selector, box, 21, 4 , 21, 36, 4 , 21, ROUGH_PRISMARINE , false);
                this.fillWithBlocks(selector, box, 22, 3 , 21, 26, 3 , 21, ROUGH_PRISMARINE , false);
                this.fillWithBlocks(selector, box, 31, 3 , 21, 35, 3 , 21, ROUGH_PRISMARINE , false);
                this.fillWithBlocks(selector, box, 23, 2 , 21, 25, 2 , 21, ROUGH_PRISMARINE , false);
                this.fillWithBlocks(selector, box, 32, 2 , 21, 34, 2 , 21, ROUGH_PRISMARINE , false);
                this.fillWithBlocks(selector, box, 28, 4 , 20, 29, 4 , 21, BRICKS_PRISMARINE, false);

                BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

                this.setBlockStateM(mut, selector, BRICKS_PRISMARINE, 27, 3, 21, box);
                this.setBlockStateM(mut, selector, BRICKS_PRISMARINE, 30, 3, 21, box);
                this.setBlockStateM(mut, selector, BRICKS_PRISMARINE, 26, 2, 21, box);
                this.setBlockStateM(mut, selector, BRICKS_PRISMARINE, 31, 2, 21, box);
                this.setBlockStateM(mut, selector, BRICKS_PRISMARINE, 25, 1, 21, box);
                this.setBlockStateM(mut, selector, BRICKS_PRISMARINE, 32, 1, 21, box);

                int i;
                for(i = 0; i < 7; ++i) {
                    this.setBlockStateM(mut, selector, DARK_PRISMARINE, 28 - i, 6 + i, 21, box);
                    this.setBlockStateM(mut, selector, DARK_PRISMARINE, 29 + i, 6 + i, 21, box);
                }

                for(i = 0; i < 4; ++i) {
                    this.setBlockStateM(mut, selector, DARK_PRISMARINE, 28 - i, 9 + i, 21, box);
                    this.setBlockStateM(mut, selector, DARK_PRISMARINE, 29 + i, 9 + i, 21, box);
                }

                this.setBlockStateM(mut, selector, DARK_PRISMARINE, 28, 12, 21, box);
                this.setBlockStateM(mut, selector, DARK_PRISMARINE, 29, 12, 21, box);

                for(i = 0; i < 3; ++i) {
                    this.setBlockStateM(mut, selector, DARK_PRISMARINE, 22 - i * 2, 8, 21, box);
                    this.setBlockStateM(mut, selector, DARK_PRISMARINE, 22 - i * 2, 9, 21, box);
                    this.setBlockStateM(mut, selector, DARK_PRISMARINE, 35 + i * 2, 8, 21, box);
                    this.setBlockStateM(mut, selector, DARK_PRISMARINE, 35 + i * 2, 9, 21, box);
                }

                this.generateWaterBox(selector, box, 15, 13, 21, 42, 15, 21, false);
                this.generateWaterBox(selector, box, 15, 1, 21, 15, 6, 21, false);
                this.generateWaterBox(selector, box, 16, 1, 21, 16, 5, 21, false);
                this.generateWaterBox(selector, box, 17, 1, 21, 20, 4, 21, false);
                this.generateWaterBox(selector, box, 21, 1, 21, 21, 3, 21, false);
                this.generateWaterBox(selector, box, 22, 1, 21, 22, 2, 21, false);
                this.generateWaterBox(selector, box, 23, 1, 21, 24, 1, 21, false);
                this.generateWaterBox(selector, box, 42, 1, 21, 42, 6, 21, false);
                this.generateWaterBox(selector, box, 41, 1, 21, 41, 5, 21, false);
                this.generateWaterBox(selector, box, 37, 1, 21, 40, 4, 21, false);
                this.generateWaterBox(selector, box, 36, 1, 21, 36, 3, 21, false);
                this.generateWaterBox(selector, box, 33, 1, 21, 34, 1, 21, false);
                this.generateWaterBox(selector, box, 35, 1, 21, 35, 2, 21, false);
            }
        }


        private void generateRoofPiece(ChunkSelector selector, StructureBoundingBox box) {
            if (this.doesChunkIntersect(box, 21, 21, 36, 36)) {
                this.fillWithBlocks(selector, box, 21, 0, 22, 36, 0, 36, ROUGH_PRISMARINE, false);
                this.generateWaterBox(selector, box, 21, 1, 22, 36, 23, 36, false);

                for(int dx = 0; dx < 4; ++dx) {
                    this.fillWithBlocks(selector, box, 21 + dx, 13 + dx, 21 + dx, 36 - dx, 13 + dx, 21 + dx, BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, 21 + dx, 13 + dx, 36 - dx, 36 - dx, 13 + dx, 36 - dx, BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, 21 + dx, 13 + dx, 22 + dx, 21 + dx, 13 + dx, 35 - dx, BRICKS_PRISMARINE, false);
                    this.fillWithBlocks(selector, box, 36 - dx, 13 + dx, 22 + dx, 36 - dx, 13 + dx, 35 - dx, BRICKS_PRISMARINE, false);
                }

                this.fillWithBlocks(selector, box, 25, 16, 25, 32, 16, 32, ROUGH_PRISMARINE , false);
                this.fillWithBlocks(selector, box, 25, 17, 25, 25, 19, 25, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 32, 17, 25, 32, 19, 25, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 25, 17, 32, 25, 19, 32, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 32, 17, 32, 32, 19, 32, BRICKS_PRISMARINE, false);

                BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

                this.setBlockStateM(mut, selector, BRICKS_PRISMARINE, 26, 20, 26, box);
                this.setBlockStateM(mut, selector, BRICKS_PRISMARINE, 27, 21, 27, box);
                this.setBlockStateM(mut, selector, SEA_LANTERN, 27, 20, 27, box);
                this.setBlockStateM(mut, selector, BRICKS_PRISMARINE, 26, 20, 31, box);
                this.setBlockStateM(mut, selector, BRICKS_PRISMARINE, 27, 21, 30, box);
                this.setBlockStateM(mut, selector, SEA_LANTERN, 27, 20, 30, box);
                this.setBlockStateM(mut, selector, BRICKS_PRISMARINE, 31, 20, 31, box);
                this.setBlockStateM(mut, selector, BRICKS_PRISMARINE, 30, 21, 30, box);
                this.setBlockStateM(mut, selector, SEA_LANTERN, 30, 20, 30, box);
                this.setBlockStateM(mut, selector, BRICKS_PRISMARINE, 31, 20, 26, box);
                this.setBlockStateM(mut, selector, BRICKS_PRISMARINE, 30, 21, 27, box);
                this.setBlockStateM(mut, selector, SEA_LANTERN, 30, 20, 27, box);

                this.fillWithBlocks(selector, box, 28, 21, 27, 29, 21, 27, ROUGH_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 27, 21, 28, 27, 21, 29, ROUGH_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 28, 21, 30, 29, 21, 30, ROUGH_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 30, 21, 28, 30, 21, 29, ROUGH_PRISMARINE, false);
            }
        }


        private void generateLowerWall(ChunkSelector selector, StructureBoundingBox box) {
            BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
            int i;
            if (this.doesChunkIntersect(box, 0, 21, 6, 58)) {
                this.fillWithBlocks(selector, box, 0, 0, 21, 6, 0, 57, ROUGH_PRISMARINE, false);
                this.generateWaterBox(selector, box, 0, 1, 21, 6, 7, 57, false);
                this.fillWithBlocks(selector, box, 4, 4, 21, 6, 4, 53, ROUGH_PRISMARINE, false);

                for(i = 0; i < 4; ++i) {
                    this.fillWithBlocks(selector, box, i, i + 1, 21, i, i + 1, 57 - i, BRICKS_PRISMARINE, false);
                }

                for(i = 23; i < 53; i += 3) {
                    this.setBlockStateM(mut, selector, DOT_DECO_DATA, 5, 5, i, box);
                }

                this.setBlockStateM(mut, selector, DOT_DECO_DATA, 5, 5, 52, box);

                for(i = 0; i < 4; ++i) {
                    this.fillWithBlocks(selector, box, i, i + 1, 21, i, i + 1, 57 - i, BRICKS_PRISMARINE, false);
                }

                this.fillWithBlocks(selector, box, 4, 1, 52, 6, 3, 52, ROUGH_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 5, 1, 51, 5, 3, 53, ROUGH_PRISMARINE, false);
            }

            if (this.doesChunkIntersect(box, 51, 21, 58, 58)) {
                this.fillWithBlocks(selector, box, 51, 0, 21, 57, 0, 57, ROUGH_PRISMARINE, false);
                this.generateWaterBox(selector, box, 51, 1, 21, 57, 7, 57, false);
                this.fillWithBlocks(selector, box, 51, 4, 21, 53, 4, 53, ROUGH_PRISMARINE, false);

                for(i = 0; i < 4; ++i) {
                    this.fillWithBlocks(selector, box, 57 - i, i + 1, 21, 57 - i, i + 1, 57 - i, BRICKS_PRISMARINE, false);
                }

                for(i = 23; i < 53; i += 3) {
                    this.setBlockStateM(mut, selector, DOT_DECO_DATA, 52, 5, i, box);
                }

                this.setBlockStateM(mut, selector, DOT_DECO_DATA, 52, 5, 52, box);
                this.fillWithBlocks(selector, box, 51, 1, 52, 53, 3, 52, ROUGH_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 52, 1, 51, 52, 3, 53, ROUGH_PRISMARINE, false);
            }

            if (this.doesChunkIntersect(box, 0, 51, 57, 57)) {
                this.fillWithBlocks(selector, box, 7, 0, 51, 50, 0, 57, ROUGH_PRISMARINE, false);
                this.generateWaterBox(selector, box, 7, 1, 51, 50, 10, 57, false);

                for(i = 0; i < 4; ++i) {
                    this.fillWithBlocks(selector, box, i + 1, i + 1, 57 - i, 56 - i, i + 1, 57 - i, BRICKS_PRISMARINE, false);
                }
            }
        }


        private void generateMiddleWall(ChunkSelector selector, Random rand, StructureBoundingBox box) {
            int z;
            BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
            if (this.doesChunkIntersect(box, 7, 21, 13, 50)) {
                this.fillWithBlocks(selector, box, 7, 0, 21, 13, 0, 50, ROUGH_PRISMARINE, false);
                this.generateWaterBox(selector, box, 7, 1, 21, 13, 10, 50, false);
                this.fillWithBlocks(selector, box, 11, 8, 21, 13, 8, 53, ROUGH_PRISMARINE, false);

                for(z = 0; z < 4; ++z) {
                    this.fillWithBlocks(selector, box, z + 7, z + 5, 21, z + 7, z + 5, 54, BRICKS_PRISMARINE, false);
                }

                for(z = 21; z <= 45; z += 3) {
                    this.setBlockStateM(mut, selector, DOT_DECO_DATA, 12, 9, z, box);
                }
            }

            if (this.doesChunkIntersect(box, 44, 21, 50, 54)) {
                this.fillWithBlocks(selector, box, 44, 0, 21, 50, 0, 50, ROUGH_PRISMARINE, false);
                this.generateWaterBox(selector, box, 44, 1, 21, 50, 10, 50, false);
                this.fillWithBlocks(selector, box, 44, 8, 21, 46, 8, 53, ROUGH_PRISMARINE, false);

                for(z = 0; z < 4; ++z) {
                    this.fillWithBlocks(selector, box, 50 - z, z + 5, 21, 50 - z, z + 5, 54, BRICKS_PRISMARINE, false);
                }

                for(z = 21; z <= 45; z += 3) {
                    this.setBlockStateM(mut, selector, DOT_DECO_DATA, 45, 9, z, box);
                }
            }

            if (this.doesChunkIntersect(box, 8, 44, 49, 54)) {
                this.fillWithBlocks(selector, box, 14, 0, 44, 43, 0, 50, ROUGH_PRISMARINE, false);
                this.generateWaterBox(selector, box, 14, 1, 44, 43, 10, 50, false);

                for(z = 12; z <= 45; z += 3) {
                    this.setBlockStateM(mut, selector, DOT_DECO_DATA, z, 9, 45, box);
                    this.setBlockStateM(mut, selector, DOT_DECO_DATA, z, 9, 52, box);
                    if (z == 12 || z == 18 || z == 24 || z == 33 || z == 39 || z == 45) {
                        this.setBlockStateM(mut, selector, DOT_DECO_DATA, z, 9, 47, box);
                        this.setBlockStateM(mut, selector, DOT_DECO_DATA, z, 9, 50, box);
                        this.setBlockStateM(mut, selector, DOT_DECO_DATA, z, 10, 45, box);
                        this.setBlockStateM(mut, selector, DOT_DECO_DATA, z, 10, 46, box);
                        this.setBlockStateM(mut, selector, DOT_DECO_DATA, z, 10, 51, box);
                        this.setBlockStateM(mut, selector, DOT_DECO_DATA, z, 10, 52, box);
                        this.setBlockStateM(mut, selector, DOT_DECO_DATA, z, 11, 47, box);
                        this.setBlockStateM(mut, selector, DOT_DECO_DATA, z, 11, 50, box);
                        this.setBlockStateM(mut, selector, DOT_DECO_DATA, z, 12, 48, box);
                        this.setBlockStateM(mut, selector, DOT_DECO_DATA, z, 12, 49, box);
                    }
                }

                for(z = 0; z < 3; ++z) {
                    this.fillWithBlocks(selector, box, 8 + z, 5 + z, 54, 49 - z, 5 + z, 54, ROUGH_PRISMARINE, false);
                }

                this.fillWithBlocks(selector, box, 11, 8, 54, 46, 8, 54, BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 14, 8, 44, 43, 8, 53, ROUGH_PRISMARINE, false);
            }
        }


        private void generateUpperWall(ChunkSelector selector, StructureBoundingBox box) {
            int dz;
            BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
            if (this.doesChunkIntersect(box, 14, 21, 20, 43)) {
                this.fillWithBlocks(selector, box, 14, 0, 21, 20, 0, 43, ROUGH_PRISMARINE, false);
                this.generateWaterBox(selector, box, 14, 1, 22, 20, 14, 43, false);
                this.fillWithBlocks(selector, box, 18, 12, 22, 20, 12, 39, ROUGH_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 18, 12, 21, 20, 12, 21, BRICKS_PRISMARINE, false);

                for(dz = 0; dz < 4; ++dz) {
                    this.fillWithBlocks(selector, box, dz + 14, dz + 9, 21, dz + 14, dz + 9, 43 - dz, BRICKS_PRISMARINE, false);
                }

                for(dz = 23; dz <= 39; dz += 3) {
                    this.setBlockStateM(mut, selector, DOT_DECO_DATA, 19, 13, dz, box);
                }
            }

            if (this.doesChunkIntersect(box, 37, 21, 43, 43)) {
                this.fillWithBlocks(selector, box, 37, 0, 21, 43, 0, 43, ROUGH_PRISMARINE, false);
                this.generateWaterBox(selector, box, 37, 1, 22, 43, 14, 43, false);
                this.fillWithBlocks(selector, box, 37, 12, 22, 39, 12, 39, ROUGH_PRISMARINE, false);
                this.fillWithBlocks(selector, box, 37, 12, 21, 39, 12, 21, BRICKS_PRISMARINE, false);

                for(dz = 0; dz < 4; ++dz) {
                    this.fillWithBlocks(selector, box, 43 - dz, dz + 9, 21, 43 - dz, dz + 9, 43 - dz, BRICKS_PRISMARINE, false);
                }

                for(dz = 23; dz <= 39; dz += 3) {
                    this.setBlockStateM(mut, selector, DOT_DECO_DATA, 38, 13, dz, box);
                }
            }

            if (this.doesChunkIntersect(box, 15, 37, 42, 43)) {
                this.fillWithBlocks(selector, box, 21, 0, 37, 36, 0, 43, ROUGH_PRISMARINE, false);
                this.generateWaterBox(selector, box, 21, 1, 37, 36, 14, 43, false);
                this.fillWithBlocks(selector, box, 21, 12, 37, 36, 12, 39, ROUGH_PRISMARINE, false);

                for(dz = 0; dz < 4; ++dz) {
                    this.fillWithBlocks(selector, box, 15 + dz, dz + 9, 43 - dz, 42 - dz, dz + 9, 43 - dz, BRICKS_PRISMARINE, false);
                }

                for(dz = 21; dz <= 36; dz += 3) {
                    this.setBlockState(selector, DOT_DECO_DATA, dz, 13, 38, box);
                }
            }
        }
    }


    public abstract static class Piece extends StructureComponent {
        protected static final IBlockState ROUGH_PRISMARINE;
        protected static final IBlockState BRICKS_PRISMARINE;
        protected static final IBlockState DARK_PRISMARINE;
        protected static final IBlockState DOT_DECO_DATA;
        protected static final IBlockState SEA_LANTERN;
        protected static final IBlockState WATER;
        protected static final int GRIDROOM_SOURCE_INDEX;
        protected static final int GRIDROOM_TOP_CONNECT_INDEX;
        protected static final int GRIDROOM_LEFTWING_CONNECT_INDEX;
        protected static final int GRIDROOM_RIGHTWING_CONNECT_INDEX;
        protected RoomDefinition roomDefinition;

        protected static final int getRoomIndex(int p_175820_0_, int p_175820_1_, int p_175820_2_) {
            return p_175820_1_ * 25 + p_175820_2_ * 5 + p_175820_0_;
        }

        public Piece() {
            super(0);
        }

        public Piece(int type) {
            super(type);
        }

        public Piece(EnumFacing facing, StructureBoundingBox box) {
            super(1);
            this.setCoordBaseMode(facing);
            this.boundingBox = box;
        }

        protected Piece(int type, EnumFacing facing, RoomDefinition room, int x, int y, int z) {
            super(type);
            this.setCoordBaseMode(facing);
            this.roomDefinition = room;
            int index = room.index;
            int index2 = index % 5;
            int index3 = index / 5 % 5;
            int index4 = index / 25;
            if (facing != EnumFacing.NORTH && facing != EnumFacing.SOUTH) {
                this.boundingBox = new StructureBoundingBox(0, 0, 0, z * 8 - 1, y * 4 - 1, x * 8 - 1);
            } else {
                this.boundingBox = new StructureBoundingBox(0, 0, 0, x * 8 - 1, y * 4 - 1, z * 8 - 1);
            }

            switch (facing) {
                case NORTH:
                    this.boundingBox.offset(index2 * 8, index4 * 4, -(index3 + z) * 8 + 1);
                    break;
                case SOUTH:
                    this.boundingBox.offset(index2 * 8, index4 * 4, index3 * 8);
                    break;
                case WEST:
                    this.boundingBox.offset(-(index3 + z) * 8 + 1, index4 * 4, index2 * 8);
                    break;
                default:
                    this.boundingBox.offset(index3 * 8, index4 * 4, index2 * 8);
            }

        }


        @Override
        protected void writeStructureToNBT(NBTTagCompound nbt) {}

        @Override
        protected void readStructureFromNBT(NBTTagCompound nbt, TemplateManager manager) {}


        protected void generateWaterBox(ChunkSelector selector, StructureBoundingBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean flag) {
            final int seaLvl = selector.getWorld().getSeaLevel();
            final BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
            for(int dx = minX; dx <= maxX; ++dx) {
                    for(int dz = minZ; dz <= maxZ; ++dz) {
                        for(int dy = minY; dy <= maxY; ++dy) {
                        if (!flag || this.getBlockStateFromPos(selector, dx, dy, dz, box).getMaterial() != Material.AIR) {
                            if (this.getYWithOffset(dy) >= seaLvl) {
                                this.setBlockStateM(mut, selector, BlockIDIndexes.AIR_STATE, dx, dy, dz, box);
                            } else {
                                this.setBlockStateM(mut, selector, WATER, dx, dy, dz, box);
                            }
                        }
                    }
                }
            }
        }


        protected void generateWaterBox(World world, StructureBoundingBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean p_181655_9_) {
            for(int dy = minY; dy <= maxY; ++dy) {
                for(int dx = minX; dx <= maxX; ++dx) {
                    for(int dz = minZ; dz <= maxZ; ++dz) {
                        if (!p_181655_9_ || this.getBlockStateFromPos(world, dx, dy, dz, box).getMaterial() != Material.AIR) {
                            if (this.getYWithOffset(dy) >= world.getSeaLevel()) {
                                this.setBlockState(world, BlockIDIndexes.AIR_STATE, dx, dy, dz, box);
                            } else {
                                this.setBlockState(world, WATER, dx, dy, dz, box);
                            }
                        }
                    }
                }
            }
        }


        protected void generateDefaultFloor(ChunkSelector selector, StructureBoundingBox box, int xMin, int zMin, boolean b) {
            if (b) {
                this.fillWithBlocks(selector, box, xMin + 0, 0, zMin + 0, xMin + 2    , 0, zMin + 8 - 1, ROUGH_PRISMARINE , false);
                this.fillWithBlocks(selector, box, xMin + 5, 0, zMin + 0, xMin + 8 - 1, 0, zMin + 8 - 1, ROUGH_PRISMARINE , false);
                this.fillWithBlocks(selector, box, xMin + 3, 0, zMin + 0, xMin + 4    , 0, zMin + 2    , ROUGH_PRISMARINE , false);
                this.fillWithBlocks(selector, box, xMin + 3, 0, zMin + 5, xMin + 4    , 0, zMin + 8 - 1, ROUGH_PRISMARINE , false);
                this.fillWithBlocks(selector, box, xMin + 3, 0, zMin + 2, xMin + 4    , 0, zMin + 2    , BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, xMin + 3, 0, zMin + 5, xMin + 4    , 0, zMin + 5    , BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, xMin + 2, 0, zMin + 3, xMin + 2    , 0, zMin + 4    , BRICKS_PRISMARINE, false);
                this.fillWithBlocks(selector, box, xMin + 5, 0, zMin + 3, xMin + 5    , 0, zMin + 4    , BRICKS_PRISMARINE, false);
            } else {
                this.fillWithBlocks(selector, box, xMin + 0, 0, zMin + 0, xMin + 8 - 1, 0, zMin + 8 - 1, ROUGH_PRISMARINE, false);
            }
        }


        protected void generateDefaultFloor(World world, StructureBoundingBox box, int xMin, int zMin, boolean b) {
            generateDefaultFloor(new ChunkSelector(world), box, xMin, zMin, b);
        }


        protected void generateBoxOnFillOnly(ChunkSelector selector, StructureBoundingBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, IBlockState state) {
            BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
            for(int dx = minX; dx <= maxX; ++dx) {
                for(int dz = minZ; dz <= maxZ; ++dz) {
                    for(int dy = minY; dy <= maxY; ++dy) {
                        if (this.getBlockStateFromPos(selector, dx, dy, dz, box) == WATER) {
                            this.setBlockStateM(mut, selector, state, dx, dy, dz, box);
                        }
                    }
                }
            }
        }


        protected void generateBoxOnFillOnly(World world, StructureBoundingBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, IBlockState state) {
            generateBoxOnFillOnly(new ChunkSelector(world), box, minX, minY, minZ, maxX, maxY, maxZ, state);
        }


        protected boolean doesChunkIntersect(StructureBoundingBox box, int x, int z, int x2, int z2) {
            int xOff = this.getXWithOffset(x, z);
            int zOff = this.getZWithOffset(x, z);
            int xOff2 = this.getXWithOffset(x2, z2);
            int zOff2 = this.getZWithOffset(x2, z2);
            return box.intersectsWith(Math.min(xOff, xOff2), Math.min(zOff, zOff2), Math.max(xOff, xOff2), Math.max(zOff, zOff2));
        }


        protected boolean spawnElder(World world, StructureBoundingBox box, int x, int y, int z) {
            int xOff = this.getXWithOffset(x, z);
            int yOff = this.getYWithOffset(y);
            int zOff = this.getZWithOffset(x, z);
            if (box.isVecInside(xOff, yOff, zOff)) {
                EntityElderGuardian guardian = new EntityElderGuardian(world);
                guardian.heal(guardian.getMaxHealth());
                guardian.setLocationAndAngles((double)xOff + 0.5, yOff, (double)zOff + 0.5, 0.0F, 0.0F);
                guardian.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(guardian)), null);
                world.spawnEntity(guardian);
                return true;
            } else {
                return false;
            }
        }


        static {
            ROUGH_PRISMARINE = Blocks.PRISMARINE.getStateFromMeta(BlockPrismarine.ROUGH_META);
            BRICKS_PRISMARINE = Blocks.PRISMARINE.getStateFromMeta(BlockPrismarine.BRICKS_META);
            DARK_PRISMARINE = Blocks.PRISMARINE.getStateFromMeta(BlockPrismarine.DARK_META);
            DOT_DECO_DATA = BRICKS_PRISMARINE;
            SEA_LANTERN = Blocks.SEA_LANTERN.getDefaultState();
            WATER = Blocks.WATER.getDefaultState();
            GRIDROOM_SOURCE_INDEX = getRoomIndex(2, 0, 0);
            GRIDROOM_TOP_CONNECT_INDEX = getRoomIndex(2, 2, 0);
            GRIDROOM_LEFTWING_CONNECT_INDEX = getRoomIndex(0, 1, 0);
            GRIDROOM_RIGHTWING_CONNECT_INDEX = getRoomIndex(4, 1, 0);
        }
    }
}