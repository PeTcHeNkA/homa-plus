package net.minecraft.village;

import com.google.common.collect.*;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraftforge.event.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import java.util.*;
import net.minecraft.world.*;
import net.minecraft.util.*;
import javax.annotation.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.block.material.*;
import net.minecraft.block.state.*;
import net.minecraft.block.*;
import com.mojang.authlib.*;
import net.minecraft.util.math.*;
import net.minecraft.nbt.*;
import net.minecraftforge.common.capabilities.*;


public class Village implements ICapabilitySerializable<NBTTagCompound> {
    private World world;
    private final List<VillageDoorInfo> villageDoorInfoList;
    private BlockPos centerHelper;
    private BlockPos center;
    private int villageRadius;
    private int lastAddDoorTimestamp;
    private int tickCounter;
    private int numVillagers;
    private int noBreedTicks;
    private Object2IntMap<UUID> playerReputation;
    private final List<VillageAggressor> villageAgressors;
    private int numIronGolems;
    private CapabilityDispatcher capabilities;

    public Village() {
        this.villageDoorInfoList = Lists.newArrayList();
        this.centerHelper = BlockPos.ORIGIN;
        this.center = BlockPos.ORIGIN;

        this.villageAgressors = Lists.newArrayList();
        this.capabilities = ForgeEventFactory.gatherCapabilities(this);
    }


    public Village(final World worldIn) {
        this.villageDoorInfoList = Lists.newArrayList();
        this.centerHelper = BlockPos.ORIGIN;
        this.center = BlockPos.ORIGIN;

        this.villageAgressors = Lists.newArrayList();
        this.world = worldIn;
        this.capabilities = ForgeEventFactory.gatherCapabilities(this);
    }


    public void setWorld(final World worldIn) {
        this.world = worldIn;
    }


    public void tick(final int tickCounterIn) {
        this.tickCounter = tickCounterIn;
        this.removeDeadAndOutOfRangeDoors();
        this.removeDeadAndOldAgressors();
        if (tickCounterIn % 20 == 0) {
            this.updateNumVillagers();
        }
        if (tickCounterIn % 30 == 0) {
            this.updateNumIronGolems();
        }
        final int i = this.numVillagers / 10;
        if (this.numIronGolems < i && this.villageDoorInfoList.size() > 20 && this.world.rand.nextInt(7000) == 0) {
            final Vec3d vec3d = this.findRandomSpawnPos(this.center, 2, 4, 2);
            if (vec3d != null) {
                final EntityIronGolem entityirongolem = new EntityIronGolem(this.world);
                entityirongolem.setPosition(vec3d.x, vec3d.y, vec3d.z);
                this.world.spawnEntity(entityirongolem);
                ++this.numIronGolems;
            }
        }
    }


    @Nullable
    private Vec3d findRandomSpawnPos(final BlockPos pos, final int x, final int y, final int z) {
        for (int i = 0; i < 10; ++i) {
            final BlockPos blockpos = pos.add(this.world.rand.nextInt(16) - 8, this.world.rand.nextInt(6) - 3, this.world.rand.nextInt(16) - 8);
            if (this.isBlockPosWithinSqVillageRadius(blockpos) && this.isAreaClearAround(new BlockPos(x, y, z), blockpos)) {
                return new Vec3d(blockpos.getX(), blockpos.getY(), blockpos.getZ());
            }
        }
        return null;
    }


    private boolean isAreaClearAround(final BlockPos blockSize, final BlockPos blockLocation) {
        if (!this.world.getBlockState(blockLocation.down()).isTopSolid()) {
            return false;
        }
        final int i = blockLocation.getX() - blockSize.getX() / 2;
        final int j = blockLocation.getZ() - blockSize.getZ() / 2;
        for (int k = i; k < i + blockSize.getX(); ++k) {
            for (int l = blockLocation.getY(); l < blockLocation.getY() + blockSize.getY(); ++l) {
                for (int i2 = j; i2 < j + blockSize.getZ(); ++i2) {
                    if (this.world.getBlockState(new BlockPos(k, l, i2)).isNormalCube()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    private void updateNumIronGolems() {
        final List<EntityIronGolem> list = this.world.getEntitiesWithinAABB(EntityIronGolem.class, new AxisAlignedBB((double)(this.center.getX() - this.villageRadius), (double)(this.center.getY() - 4), (double)(this.center.getZ() - this.villageRadius), (double)(this.center.getX() + this.villageRadius), (double)(this.center.getY() + 4), (double)(this.center.getZ() + this.villageRadius)));
        this.numIronGolems = list.size();
    }


    private void updateNumVillagers() {
        final List<EntityVillager> list = this.world.getEntitiesWithinAABB(EntityVillager.class, new AxisAlignedBB((double)(this.center.getX() - this.villageRadius), (double)(this.center.getY() - 4), (double)(this.center.getZ() - this.villageRadius), (double)(this.center.getX() + this.villageRadius), (double)(this.center.getY() + 4), (double)(this.center.getZ() + this.villageRadius)));
        this.numVillagers = list.size();
        if (this.numVillagers == 0) {
            Map<UUID, Integer> pl = this.playerReputation;
            if(pl != null) pl.clear();
        }
    }


    public BlockPos getCenter() {
        return this.center;
    }


    public int getVillageRadius() {
        return this.villageRadius;
    }


    public int getNumVillageDoors() {
        return this.villageDoorInfoList.size();
    }


    public int getTicksSinceLastDoorAdding() {
        return this.tickCounter - this.lastAddDoorTimestamp;
    }


    public int getNumVillagers() {
        return this.numVillagers;
    }


    public boolean isBlockPosWithinSqVillageRadius(final BlockPos pos) {
        return this.center.distanceSq(pos) < this.villageRadius * this.villageRadius;
    }


    public List<VillageDoorInfo> getVillageDoorInfoList() {
        return this.villageDoorInfoList;
    }


    @Nullable
    public VillageDoorInfo getNearestDoor(final BlockPos pos) {
        VillageDoorInfo villagedoorinfo = null;
        int i = Integer.MAX_VALUE;
        for (final VillageDoorInfo villagedoorinfo2 : this.villageDoorInfoList) {
            final int j = villagedoorinfo2.getDistanceToDoorBlockSq(pos);
            if (j < i) {
                villagedoorinfo = villagedoorinfo2;
                i = j;
            }
        }
        return villagedoorinfo;
    }


    @Nullable
    public VillageDoorInfo getDoorInfo(final BlockPos pos) {
        VillageDoorInfo villagedoorinfo = null;
        int i = Integer.MAX_VALUE;
        for (final VillageDoorInfo villagedoorinfo2 : this.villageDoorInfoList) {
            int j = villagedoorinfo2.getDistanceToDoorBlockSq(pos);
            if (j > 256) {
                j *= 1000;
            }
            else {
                j = villagedoorinfo2.getDoorOpeningRestrictionCounter();
            }
            if (j < i) {
                final BlockPos blockpos = villagedoorinfo2.getDoorBlockPos();
                final EnumFacing enumfacing = villagedoorinfo2.getInsideDirection();
                if (!this.world.getBlockState(blockpos.offset(enumfacing, 1)).getBlock().isPassable((IBlockAccess)this.world, blockpos.offset(enumfacing, 1)) || !this.world.getBlockState(blockpos.offset(enumfacing, -1)).getBlock().isPassable((IBlockAccess)this.world, blockpos.offset(enumfacing, -1)) || !this.world.getBlockState(blockpos.up().offset(enumfacing, 1)).getBlock().isPassable((IBlockAccess)this.world, blockpos.up().offset(enumfacing, 1)) || !this.world.getBlockState(blockpos.up().offset(enumfacing, -1)).getBlock().isPassable((IBlockAccess)this.world, blockpos.up().offset(enumfacing, -1))) {
                    continue;
                }
                villagedoorinfo = villagedoorinfo2;
                i = j;
            }
        }
        return villagedoorinfo;
    }


    @Nullable
    public VillageDoorInfo getExistedDoor(final BlockPos doorBlock) {
        if (this.center.distanceSq(doorBlock) > this.villageRadius * this.villageRadius) {
            return null;
        }
        for (final VillageDoorInfo villagedoorinfo : this.villageDoorInfoList) {
            if (villagedoorinfo.getDoorBlockPos().getX() == doorBlock.getX() && villagedoorinfo.getDoorBlockPos().getZ() == doorBlock.getZ() && Math.abs(villagedoorinfo.getDoorBlockPos().getY() - doorBlock.getY()) <= 1) {
                return villagedoorinfo;
            }
        }
        return null;
    }


    public void addVillageDoorInfo(final VillageDoorInfo doorInfo) {
        this.villageDoorInfoList.add(doorInfo);
        this.centerHelper = this.centerHelper.add(doorInfo.getDoorBlockPos());
        this.updateVillageRadiusAndCenter();
        this.lastAddDoorTimestamp = doorInfo.getLastActivityTimestamp();
    }


    public boolean isAnnihilated() {
        return this.villageDoorInfoList.isEmpty();
    }


    public void addOrRenewAgressor(final EntityLivingBase entitylivingbaseIn) {
        for (final VillageAggressor village$villageaggressor : this.villageAgressors) {
            if (village$villageaggressor.agressor == entitylivingbaseIn) {
                village$villageaggressor.agressionTime = this.tickCounter;
                return;
            }
        }
        this.villageAgressors.add(new VillageAggressor(entitylivingbaseIn, this.tickCounter));
    }


    @Nullable
    public EntityLivingBase findNearestVillageAggressor(final EntityLivingBase entitylivingbaseIn) {
        double d0 = Double.MAX_VALUE;
        VillageAggressor village$villageaggressor = null;
        for (int i = 0; i < this.villageAgressors.size(); ++i) {
            final VillageAggressor village$villageaggressor2 = this.villageAgressors.get(i);
            final double d2 = village$villageaggressor2.agressor.getDistanceSq(entitylivingbaseIn);
            if (d2 <= d0) {
                village$villageaggressor = village$villageaggressor2;
                d0 = d2;
            }
        }
        return (village$villageaggressor == null) ? null : village$villageaggressor.agressor;
    }


    public EntityPlayer getNearestTargetPlayer(final EntityLivingBase villageDefender) {
        Map<UUID, Integer> rep = this.playerReputation;
        if(rep == null)return null;
        World world = this.world;

        double d0 = Double.MAX_VALUE;
        EntityPlayer entityplayer = null;

        for (final UUID s : rep.keySet()) {
            if (this.isPlayerReputationTooLow(s)) {
                final EntityPlayer entityplayer2 = world.getPlayerEntityByUUID(s);
                if (entityplayer2 == null) {
                    continue;
                }
                final double d2 = entityplayer2.getDistanceSq(villageDefender);
                if (d2 > d0) {
                    continue;
                }
                entityplayer = entityplayer2;
                d0 = d2;
            }
        }
        return entityplayer;
    }


    private void removeDeadAndOldAgressors() {
        final Iterator<VillageAggressor> iterator = this.villageAgressors.iterator();
        while (iterator.hasNext()) {
            final VillageAggressor village$villageaggressor = iterator.next();
            if (!village$villageaggressor.agressor.isEntityAlive() || Math.abs(this.tickCounter - village$villageaggressor.agressionTime) > 300) {
                iterator.remove();
            }
        }
    }


    private void removeDeadAndOutOfRangeDoors() {
        boolean flag = false;
        final boolean flag2 = this.world.rand.nextInt(50) == 0;
        final Iterator<VillageDoorInfo> iterator = this.villageDoorInfoList.iterator();
        while (iterator.hasNext()) {
            final VillageDoorInfo villagedoorinfo = iterator.next();
            if (flag2) {
                villagedoorinfo.resetDoorOpeningRestrictionCounter();
            }
            if (this.world.isBlockLoaded(villagedoorinfo.getDoorBlockPos()) && (!this.isWoodDoor(villagedoorinfo.getDoorBlockPos()) || Math.abs(this.tickCounter - villagedoorinfo.getLastActivityTimestamp()) > 1200)) {
                this.centerHelper = this.centerHelper.subtract((Vec3i)villagedoorinfo.getDoorBlockPos());
                flag = true;
                villagedoorinfo.setIsDetachedFromVillageFlag(true);
                iterator.remove();
            }
        }
        if (flag) {
            this.updateVillageRadiusAndCenter();
        }
    }

    private boolean isWoodDoor(final BlockPos pos) {
        final IBlockState iblockstate = this.world.getBlockState(pos);
        final Block block = iblockstate.getBlock();
        return block instanceof BlockDoor && iblockstate.getMaterial() == Material.WOOD;
    }


    private void updateVillageRadiusAndCenter() {
        final int i = this.villageDoorInfoList.size();
        if (i == 0) {
            this.center = BlockPos.ORIGIN;
            this.villageRadius = 0;
        }
        else {
            this.center = new BlockPos(this.centerHelper.getX() / i, this.centerHelper.getY() / i, this.centerHelper.getZ() / i);
            int j = 0;
            for (final VillageDoorInfo villagedoorinfo : this.villageDoorInfoList) {
                j = Math.max(villagedoorinfo.getDistanceToDoorBlockSq(this.center), j);
            }
            this.villageRadius = Math.max(32, (int)Math.sqrt(j) + 1);
        }
    }


    @Deprecated
    public int getPlayerReputation(final String playerName) {
        return this.getPlayerReputation(this.findUUID(playerName));
    }


    public int getPlayerReputation(final UUID uuid) {
        Object2IntMap<UUID> rep = this.playerReputation;
        if(rep == null)return 0;
        //final Integer integer = this.playerReputation.get(uuid);
        return rep.getInt(uuid);
    }


    private UUID findUUID(final String uidStr) {
        World world = this.world;
        if (world == null || world.getMinecraftServer() == null) {
            return EntityPlayer.getOfflineUUID(uidStr);
        }
        final GameProfile profile = world.getMinecraftServer().getPlayerProfileCache().getGameProfileForUsername(uidStr);
        return (profile == null) ? EntityPlayer.getOfflineUUID(uidStr) : profile.getId();
    }


    @Deprecated
    public int modifyPlayerReputation(final String playerName, final int reputation) {
        return this.modifyPlayerReputation(this.findUUID(playerName), reputation);
    }


    public int modifyPlayerReputation(final UUID uuid, final int amount) {
        final int i = this.getPlayerReputation(uuid);
        final int j = MathHelper.clamp(i + amount, -30, 10);

        Object2IntMap<UUID> map = this.playerReputation;
        if(map == null){
            map = this.playerReputation = new Object2IntOpenHashMap<>(4);
        }
        map.put(uuid, j);
        return j;
    }


    @Deprecated
    public boolean isPlayerReputationTooLow(final String playerName) {
        return this.isPlayerReputationTooLow(this.findUUID(playerName));
    }


    public boolean isPlayerReputationTooLow(final UUID p_isPlayerReputationTooLow_1_) {
        return this.getPlayerReputation(p_isPlayerReputationTooLow_1_) <= -15;
    }


    public void readVillageDataFromNBT(final NBTTagCompound nbt) {
        this.numVillagers = nbt.getInteger("PopSize");
        this.villageRadius = nbt.getInteger("Radius");
        this.numIronGolems = nbt.getInteger("Golems");
        this.lastAddDoorTimestamp = nbt.getInteger("Stable");
        this.tickCounter = nbt.getInteger("Tick");
        this.noBreedTicks = nbt.getInteger("MTick");
        this.center = new BlockPos(nbt.getInteger("CX"), nbt.getInteger("CY"), nbt.getInteger("CZ"));
        this.centerHelper = new BlockPos(nbt.getInteger("ACX"), nbt.getInteger("ACY"), nbt.getInteger("ACZ"));
        final NBTTagList nbttaglist = nbt.getTagList("Doors", 10);

        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            final NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            final VillageDoorInfo villagedoorinfo = new VillageDoorInfo(new BlockPos(nbttagcompound.getInteger("X"), nbttagcompound.getInteger("Y"), nbttagcompound.getInteger("Z")), nbttagcompound.getInteger("IDX"), nbttagcompound.getInteger("IDZ"), nbttagcompound.getInteger("TS"));
            this.villageDoorInfoList.add(villagedoorinfo);
        }

        final NBTTagList players = nbt.getTagList("Players", 10);
        if(!players.isEmpty()) {
            Object2IntMap<UUID> rep = this.playerReputation = new Object2IntOpenHashMap<>(players.tagCount());
            for (int j = 0; j < players.tagCount(); ++j) {
                final NBTTagCompound nbttagcompound2 = players.getCompoundTagAt(j);
                if (nbttagcompound2.hasKey("UUID")) {
                    rep.put(UUID.fromString(nbttagcompound2.getString("UUID")), nbttagcompound2.getInteger("S"));
                } else {
                    rep.put(this.findUUID(nbttagcompound2.getString("Name")), nbttagcompound2.getInteger("S"));
                }
            }
        }

        if (this.capabilities != null && nbt.hasKey("ForgeCaps")) {
            this.capabilities.deserializeNBT(nbt.getCompoundTag("ForgeCaps"));
        }
    }


    public void writeVillageDataToNBT(final NBTTagCompound nbt) {
        nbt.setIntegerU("PopSize", this.numVillagers);
        nbt.setIntegerU("Radius", this.villageRadius);
        nbt.setIntegerU("Golems", this.numIronGolems);
        nbt.setIntegerU("Stable", this.lastAddDoorTimestamp);
        nbt.setIntegerU("Tick", this.tickCounter);
        nbt.setIntegerU("MTick", this.noBreedTicks);
        nbt.setInteger("CX", this.center.getX());
        nbt.setIntegerU("CY", this.center.getY());
        nbt.setInteger("CZ", this.center.getZ());
        nbt.setInteger("ACX", this.centerHelper.getX());
        nbt.setIntegerU("ACY", this.centerHelper.getY());
        nbt.setInteger("ACZ", this.centerHelper.getZ());

        final NBTTagList nbttaglist = new NBTTagList();
        for (final VillageDoorInfo villagedoorinfo : this.villageDoorInfoList) {
            final NBTTagCompound nbttagcompound = new NBTTagCompound(8,1);
            BlockPos door = villagedoorinfo.getDoorBlockPos();

            nbttagcompound.setInteger("X", door.getX());
            nbttagcompound.setIntegerU("Y", door.getY());
            nbttagcompound.setInteger("Z", door.getZ());
            nbttagcompound.setIntegerU("IDX", villagedoorinfo.getInsideOffsetX());
            nbttagcompound.setIntegerU("IDZ", villagedoorinfo.getInsideOffsetZ());
            nbttagcompound.setInteger("TS", villagedoorinfo.getLastActivityTimestamp());
            nbttaglist.appendTag(nbttagcompound);
        }
        nbt.setTag("Doors", nbttaglist);
        if(playerReputation != null) {
            final NBTTagList nbttaglist2 = new NBTTagList();
            for (final UUID s : this.playerReputation.keySet()) {
                final NBTTagCompound inf = new NBTTagCompound(4, 1);
                try {
                    inf.setString("UUID", s.toString());
                    inf.setIntegerU("S", this.playerReputation.get(s));
                    nbttaglist2.appendTag(inf);
                } catch (RuntimeException ex) {
                }
            }
            nbt.setTag("Players", nbttaglist2);
        }
        if (this.capabilities != null) {
            nbt.setTag("ForgeCaps", this.capabilities.serializeNBT());
        }
    }

    public void endMatingSeason() {
        this.noBreedTicks = this.tickCounter;
    }


    public boolean isMatingSeason() {
        return this.noBreedTicks == 0 || this.tickCounter - this.noBreedTicks >= 3600;
    }


    public void setDefaultPlayerReputation(final int defaultReputation) {
        for (final UUID s : this.playerReputation.keySet()) {
            this.modifyPlayerReputation(s, defaultReputation);
        }
    }


    @Override
    public boolean hasCapability(final Capability<?> p_hasCapability_1_, @Nullable final EnumFacing p_hasCapability_2_) {
        return this.capabilities != null && this.capabilities.hasCapability(p_hasCapability_1_, p_hasCapability_2_);
    }


    @Nullable
    @Override
    public <T> T getCapability(final Capability<T> p_getCapability_1_, @Nullable final EnumFacing p_getCapability_2_) {
        return (this.capabilities == null) ? null : this.capabilities.getCapability(p_getCapability_1_, p_getCapability_2_);
    }


    @Override
    public void deserializeNBT(final NBTTagCompound p_deserializeNBT_1_) {
        this.readVillageDataFromNBT(p_deserializeNBT_1_);
    }


    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound ret = new NBTTagCompound();
        this.writeVillageDataToNBT(ret);
        return ret;
    }


    class VillageAggressor {
        public EntityLivingBase agressor;
        public int agressionTime;

        VillageAggressor(final EntityLivingBase agressorIn, final int agressionTimeIn) {
            this.agressor = agressorIn;
            this.agressionTime = agressionTimeIn;
        }
    }
}