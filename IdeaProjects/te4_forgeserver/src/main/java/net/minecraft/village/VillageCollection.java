package net.minecraft.village;

import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.storage.WorldSavedData;
import plus.fserver.utls.collections.ArrayListE6;


public class VillageCollection extends WorldSavedData {
    private World world;
    private final ArrayListE6<BlockPos>        villagerPositionsList = new ArrayListE6<>();
    private final ArrayListE6<VillageDoorInfo> newDoors              = new ArrayListE6<>();
    private final ArrayListE6<Village>         villageList           = new ArrayListE6<>();
    private int tickCounter;

    public VillageCollection(String name) {
        super(name);
    }


    public VillageCollection(World world) {
        super(fileNameForProvider(world.provider));
        this.world = world;
        this.markDirty();
    }


    public void setWorldsForAll(World p_82566_1_) {
        this.world = p_82566_1_;
        ArrayListE6.Itr var2 = this.villageList.iterator();

        while(var2.hasNext()) {
            Village village = (Village)var2.nextObj();
            village.setWorld(p_82566_1_);
        }
    }


    public void addToVillagerPositionList(BlockPos pos) {
        if (this.villagerPositionsList.sizeC() <= 64 && !this.positionInList(pos)) {
            this.villagerPositionsList.addF(pos);
        }
    }


    public void tick() {
        ++this.tickCounter;
        ArrayListE6.Itr var1 = this.villageList.iterator();

        while(var1.hasNext()) {
            Village village = (Village)var1.nextObj();
            village.tick(this.tickCounter);
        }
        this.removeAnnihilatedVillages();

        if(this.tickCounter%30==0)
            this.dropOldestVillagerPosition();

        this.addNewDoorsToVillageOrCreateVillage();

        if (this.tickCounter % 400 == 0) {
            this.markDirty();
        }
    }


    private void removeAnnihilatedVillages() {
        ArrayListE6.Itr iterator = this.villageList.iterator();

        while(iterator.hasNext()) {
            Village village = (Village)iterator.nextObj();
            if (village.isAnnihilated()) {
                iterator.remove();
                this.markDirty();
            }
        }
    }


    public List<Village> getVillageList() {
        return this.villageList;
    }


    public Village getNearestVillage(BlockPos p_176056_1_, int p_176056_2_) {
        Village village = null;
        double d0 = 3.4028234663852886E38;
        ArrayListE6.Itr var6 = this.villageList.iterator();

        while(var6.hasNext()) {
            Village village1 = (Village)var6.nextObj();
            double d1 = village1.getCenter().distanceSq(p_176056_1_);
            if (d1 < d0) {
                float f = (float)(p_176056_2_ + village1.getVillageRadius());
                if (d1 <= (double)(f * f)) {
                    village = village1;
                    d0 = d1;
                }
            }
        }
        return village;
    }


    private void dropOldestVillagerPosition() {
        if (!this.villagerPositionsList.isEmpty()) {
            this.addDoorsAround(this.villagerPositionsList.remove(0));
        }
    }

    private void addNewDoorsToVillageOrCreateVillage() {
        for(int i = 0; i < this.newDoors.size(); ++i) {
            VillageDoorInfo villagedoorinfo = this.newDoors.get(i);
            Village village = this.getNearestVillage(villagedoorinfo.getDoorBlockPos(), 32);
            if (village == null) {
                village = new Village(this.world);
                this.villageList.add(village);
                this.markDirty();
            }

            village.addVillageDoorInfo(villagedoorinfo);
        }

        this.newDoors.clear();
    }

    private void addDoorsAround(BlockPos pos) {
        if (this.world.isAreaLoaded(pos, 16)) {

            for(int l = -16; l < 16; ++l) {
                for(int i1 = -4; i1 < 4; ++i1) {
                    for(int j1 = -16; j1 < 16; ++j1) {
                        BlockPos blockpos = pos.add(l, i1, j1);
                        if (this.isWoodDoor(blockpos)) {
                            VillageDoorInfo info = this.checkDoorExistence(blockpos);
                            if (info == null) {
                                this.addToNewDoorsList(blockpos);
                            } else {
                                info.setLastActivityTimestamp(this.tickCounter);
                            }
                        }
                    }
                }
            }

        }
    }

    @Nullable
    private VillageDoorInfo checkDoorExistence(BlockPos pos) {
        ArrayListE6.Itr var2 = this.newDoors.iterator();

        VillageDoorInfo info;
        do {
            if (!var2.hasNext()) {
                var2 = this.villageList.iterator();

                VillageDoorInfo villagedoorinfo1;
                do {
                    if (!var2.hasNext()) {
                        return null;
                    }

                    Village village = (Village)var2.nextObj();
                    villagedoorinfo1 = village.getExistedDoor(pos);
                } while(villagedoorinfo1 == null);

                return villagedoorinfo1;
            }

            info = (VillageDoorInfo)var2.nextObj();
        } while(info.getDoorBlockPos().getX() != pos.getX() || info.getDoorBlockPos().getZ() != pos.getZ() || Math.abs(info.getDoorBlockPos().getY() - pos.getY()) > 1);

        return info;
    }


    private void addToNewDoorsList(BlockPos pos) {
        EnumFacing enumfacing = BlockDoor.getFacing(this.world, pos);
        EnumFacing enumfacing1 = enumfacing.getOpposite();
        int i = this.countBlocksCanSeeSky(pos, enumfacing, 5);
        int j = this.countBlocksCanSeeSky(pos, enumfacing1, i + 1);
        if (i != j) {
            this.newDoors.add(new VillageDoorInfo(pos, i < j ? enumfacing : enumfacing1, this.tickCounter));
        }
    }


    private int countBlocksCanSeeSky(BlockPos pos, EnumFacing facing, int num) {
        int i = 0;
        for(int j = 1; j <= 5; ++j) {
            if (this.world.canSeeSky(pos.offset(facing, j))) {
                ++i;
                if (i >= num) return i;
            }
        }
        return i;
    }


    private boolean positionInList(BlockPos pos) {
        ArrayListE6.Itr var2 = this.villagerPositionsList.iterator();

        BlockPos blockpos;
        do {
            if (!var2.hasNext()) return false;

            blockpos = (BlockPos)var2.nextObj();
        } while(!blockpos.equals(pos));
        return true;
    }


    private boolean isWoodDoor(BlockPos pos) {
        IBlockState iblockstate = this.world.getBlockState(pos);
        Block block = iblockstate.getBlock();
        if (block instanceof BlockDoor) {
            return iblockstate.getMaterial() == Material.WOOD;
        } else {
            return false;
        }
    }


    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.tickCounter = nbt.getInteger("Tick");
        NBTTagList nbttaglist = nbt.getTagList("Villages", 10);

        for(int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            Village village = new Village();
            village.readVillageDataFromNBT(nbttagcompound);
            this.villageList.add(village);
        }

    }


    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("Tick", this.tickCounter);
        NBTTagList nbttaglist = new NBTTagList();
        Iterator var3 = this.villageList.iterator();

        while(var3.hasNext()) {
            Village village = (Village)var3.next();
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            village.writeVillageDataToNBT(nbttagcompound);
            nbttaglist.appendTag(nbttagcompound);
        }

        nbt.setTag("Villages", nbttaglist);
        return nbt;
    }


    public static String fileNameForProvider(WorldProvider provider) {
        return "villages" + provider.getDimensionType().getSuffix();
    }
}