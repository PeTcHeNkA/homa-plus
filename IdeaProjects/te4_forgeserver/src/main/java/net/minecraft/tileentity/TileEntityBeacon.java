package net.minecraft.tileentity;

import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.*;
import javax.annotation.*;
import net.minecraft.potion.*;
import net.minecraft.item.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.advancements.*;
import net.minecraft.block.state.*;
import net.minecraft.block.*;
import net.minecraft.network.play.server.*;
import net.minecraft.nbt.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.util.*;
import net.minecraft.init.*;
import com.google.common.collect.*;
import plus.fserver.ElcaConfig;
import plus.fserver.api.IBlockEAccess;
import plus.fserver.utls.collections.ArrayListE2;
import plus.fserver.utls.collections.ArrayListE6;
import java.util.*;


public class TileEntityBeacon extends TileEntityLockable implements ITickable, ISidedInventory {
    public static final Potion[][] EFFECTS_LIST;
    private static final Set<Potion> VALID_EFFECTS;
    private final ArrayListE6<BeamSegment> beamSegments;
    @SideOnly(Side.CLIENT)
    private long beamRenderCounter;
    @SideOnly(Side.CLIENT)
    private float beamRenderScale;
    private boolean isComplete;
    private int levels;
    @Nullable
    private Potion primaryEffect;
    @Nullable
    private Potion secondaryEffect;
    private ItemStack payment;
    private String customName;

    public TileEntityBeacon() {
        this.beamSegments = new ArrayListE2<>();
        this.levels = -1;
        this.payment = ItemStack.EMPTY;
    }


    @Override
    public void update() {
        if (this.world.getTotalWorldTime() % ElcaConfig.BEACON_TICK_RATE == 0L) {
            this.updateBeacon();
        }
    }


    public void updateBeacon() {
        if (this.world != null) {
            this.updateSegmentColors();
            this.addEffectsToPlayers();
        }
    }


    private void addEffectsToPlayers() {
        if (this.isComplete && this.levels > 0 && !this.world.isRemote && this.primaryEffect != null) {
            final double d0 = this.levels * 10 + 10;
            int i = 0;
            if (this.levels >= 4 && this.primaryEffect == this.secondaryEffect) {
                i = 1;
            }
            final int j = (9 + this.levels * 2) * 20;
            final int posX = this.pos.getX();
            final int posY = this.pos.getY();
            final int posZ = this.pos.getZ();
            final AxisAlignedBB box = new AxisAlignedBB(
                    posX, posY-256, posZ,
                    posX + 1, posY+256, posZ + 1
            );
            box.growTo(d0, d0, d0, box);

            final List<EntityPlayer> list = this.world.getEntitiesWithinAABB(EntityPlayer.class, box);

            for (final EntityPlayer entityplayer : list) {
                entityplayer.addPotionEffect(new PotionEffect(this.primaryEffect, j, i, true, true));
            }

            if (this.levels >= 4 && this.primaryEffect != this.secondaryEffect && this.secondaryEffect != null) {
                for (final EntityPlayer player : list) {
                    player.addPotionEffect(new PotionEffect(this.secondaryEffect, j, 0, true, true));
                }
            }
        }
    }


    private void updateSegmentColors() {
        final int posX = this.pos.getX();
        final int posY = this.pos.getY();
        final int posZ = this.pos.getZ();
        final int prevLvls = this.levels;
        this.levels = 0;
        ArrayListE6<BeamSegment> beamSegments = this.beamSegments;
        int prevSize = beamSegments.sizeC();
        beamSegments.fclear();
        this.isComplete = true;

        //reuse exist segment
        BeamSegment segment = beamSegments.getSafe(0);
        if(segment == null){
            segment = new BeamSegment(EnumDyeColor.WHITE.getColorComponentValues());
        } else {
            segment.init(EnumDyeColor.WHITE.getColorComponentValues());
        }
        beamSegments.add(segment);

        boolean flag = true;

        World world = this.world;
        IBlockEAccess access = world.access();
        BlockPos.MutableBlockPos mut = null;
        Chunk chunk = world.getChunk(posX>>4, posZ>>4);

        for (int curY = posY + 1; curY < 256; ++curY) {
            final IBlockState state = chunk.getBlockStateUnchecked(posX, curY, posZ);
            float[] afloat;
            if (state.getBlock() == Blocks.STAINED_GLASS) {
                afloat = state.getValue(BlockStainedGlass.COLOR).getColorComponentValues();
            }
            else if (state.getBlock() != Blocks.STAINED_GLASS_PANE) {
                if(mut == null)mut = new BlockPos.MutableBlockPos(posX, curY, posZ);
                else mut.setPos(posX, curY, posZ);
                if (state.getLightOpacity(access, mut) >= 15 && state.getBlock() != Blocks.BEDROCK) {
                    this.isComplete = false;
                    this.beamSegments.clear();
                    break;
                }
                final float[] customColor = state.getBlock().getBeaconColorMultiplier(state, world, mut, this.getPos());
                if (customColor == null) {
                    segment.incrementHeight();
                    continue;
                }
                afloat = customColor;
            }
            else {
                afloat = state.getValue(BlockStainedGlassPane.COLOR).getColorComponentValues();
            }
            if (!flag) {
                afloat = new float[] { (segment.getColors()[0] + afloat[0]) / 2.0f, (segment.getColors()[1] + afloat[1]) / 2.0f, (segment.getColors()[2] + afloat[2]) / 2.0f };
            }
            if (Arrays.equals(afloat, segment.getColors())) {
                segment.incrementHeight();
            }
            else {
                segment = beamSegments.getSafe(beamSegments.sizeC());
                if(segment == null) segment = new BeamSegment(afloat);
                else segment.init(afloat);
                beamSegments.add(segment);
            }
            flag = false;
        }
        //clear tp work GC
        while (prevSize > beamSegments.sizeC()){
            --prevSize;
            beamSegments.set(prevSize, null);
        }

        if (this.isComplete) {
            final BlockPos curPos = this.getPos();
            if(mut == null)mut = new BlockPos.MutableBlockPos();
            for (int l2 = 1; l2 <= 4; this.levels = l2++) {
                final int ay = posY - l2;
                if (ay < 0) {
                    break;
                }
                boolean flag2 = true;
                for (int ax = posX - l2; ax <= posX + l2 && flag2; ++ax) {
                    for (int az = posZ - l2; az <= posZ + l2; ++az) {
                        final Block block = access.getBlockState(ax, ay, az).getBlock();
                        if (!block.isBeaconBase(world, mut.setPos(ax, ay, az), curPos)) {
                            flag2 = false;
                            break;
                        }
                    }
                }
                if (!flag2) {
                    break;
                }
            }
            if (this.levels == 0) {
                this.isComplete = false;
            }
        }
        if (!this.world.isRemote && prevLvls < this.levels) {
            for (final EntityPlayerMP player : world.getEntitiesWithinAABB(EntityPlayerMP.class, new AxisAlignedBB(posX, posY, posZ, posX, posY - 4, posZ).grow(10.0, 5.0, 10.0))) {
                CriteriaTriggers.CONSTRUCT_BEACON.trigger(player, this);
            }
        }
    }


    @SideOnly(Side.CLIENT)
    public List<BeamSegment> getBeamSegments() {
        return this.beamSegments;
    }


    @SideOnly(Side.CLIENT)
    public float shouldBeamRender() {
        if (!this.isComplete) {
            return 0.0f;
        }
        final int i = (int)(this.world.getTotalWorldTime() - this.beamRenderCounter);
        this.beamRenderCounter = this.world.getTotalWorldTime();
        if (i > 1) {
            this.beamRenderScale -= i / 40.0f;
            if (this.beamRenderScale < 0.0f) {
                this.beamRenderScale = 0.0f;
            }
        }
        this.beamRenderScale += 0.025f;
        if (this.beamRenderScale > 1.0f) {
            this.beamRenderScale = 1.0f;
        }
        return this.beamRenderScale;
    }


    public int getLevels() {
        return this.levels;
    }


    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 3, this.getUpdateTag());
    }


    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }


    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0;
    }


    @Nullable
    private static Potion isBeaconEffect(final int p_184279_0_) {
        final Potion potion = Potion.getPotionById(p_184279_0_);
        return TileEntityBeacon.VALID_EFFECTS.contains(potion) ? potion : null;
    }


    @Override
    public void readFromNBT(final NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.primaryEffect = isBeaconEffect(compound.getInteger("Primary"));
        this.secondaryEffect = isBeaconEffect(compound.getInteger("Secondary"));
        this.levels = compound.getInteger("Levels");
    }


    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setIntegerU("Primary", Potion.getIdFromPotion(this.primaryEffect));
        compound.setIntegerU("Secondary", Potion.getIdFromPotion(this.secondaryEffect));
        compound.setIntegerU("Levels", this.levels);
        return compound;
    }


    @Override
    public int getSizeInventory() {
        return 1;
    }


    @Override
    public boolean isEmpty() {
        return this.payment.isEmpty();
    }


    @Override
    public ItemStack getStackInSlot(final int index) {
        return (index == 0) ? this.payment : ItemStack.EMPTY;
    }


    @Override
    public ItemStack decrStackSize(final int index, final int count) {
        if (index != 0 || this.payment.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (count >= this.payment.getCount()) {
            final ItemStack itemstack = this.payment;
            this.payment = ItemStack.EMPTY;
            return itemstack;
        }
        return this.payment.splitStack(count);
    }


    @Override
    public ItemStack removeStackFromSlot(final int index) {
        if (index == 0) {
            final ItemStack itemstack = this.payment;
            this.payment = ItemStack.EMPTY;
            return itemstack;
        }
        return ItemStack.EMPTY;
    }


    @Override
    public void setInventorySlotContents(final int index, final ItemStack stack) {
        if (index == 0) {
            this.payment = stack;
        }
    }


    @Override
    public String getName() {
        return this.hasCustomName() ? this.customName : "container.beacon";
    }


    @Override
    public boolean hasCustomName() {
        return this.customName != null && !this.customName.isEmpty();
    }


    public void setName(final String name) {
        this.customName = name;
    }


    @Override
    public int getInventoryStackLimit() {
        return 1;
    }


    @Override
    public boolean isUsableByPlayer(final EntityPlayer player) {
        return this.world.getTileEntity(this.pos) == this && player.getDistanceSq(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5) <= 64.0;
    }


    @Override
    public void openInventory(final EntityPlayer player) {}


    @Override
    public void closeInventory(final EntityPlayer player) {}


    @Override
    public boolean isItemValidForSlot(final int index, final ItemStack stack) {
        return stack.getItem() != null && stack.getItem().isBeaconPayment(stack);
    }


    @Override
    public String getGuiID() {
        return "minecraft:beacon";
    }


    @Override
    public Container createContainer(final InventoryPlayer playerInventory, final EntityPlayer playerIn) {
        return new ContainerBeacon(playerInventory, this);
    }


    @Override
    public int getField(final int id) {
        switch (id) {
            case 0: {
                return this.levels;
            }
            case 1: {
                return Potion.getIdFromPotion(this.primaryEffect);
            }
            case 2: {
                return Potion.getIdFromPotion(this.secondaryEffect);
            }
            default: {
                return 0;
            }
        }
    }


    @Override
    public void setField(final int id, final int value) {
        switch (id) {
            case 0: {
                this.levels = value;
                break;
            }
            case 1: {
                this.primaryEffect = isBeaconEffect(value);
                break;
            }
            case 2: {
                this.secondaryEffect = isBeaconEffect(value);
                break;
            }
        }
    }


    @Override
    public int getFieldCount() {
        return 3;
    }


    @Override
    public void clear() {
        this.payment = ItemStack.EMPTY;
    }


    @Override
    public boolean receiveClientEvent(final int id, final int type) {
        if (id == 1) {
            this.updateBeacon();
            return true;
        }
        return super.receiveClientEvent(id, type);
    }


    @Override
    public int[] getSlotsForFace(final EnumFacing side) {
        return new int[0];
    }


    @Override
    public boolean canInsertItem(final int index, final ItemStack itemStackIn, final EnumFacing direction) {
        return false;
    }


    @Override
    public boolean canExtractItem(final int index, final ItemStack stack, final EnumFacing direction) {
        return false;
    }


    static {
        EFFECTS_LIST = new Potion[][] { { MobEffects.SPEED, MobEffects.HASTE }, { MobEffects.RESISTANCE, MobEffects.JUMP_BOOST }, { MobEffects.STRENGTH }, { MobEffects.REGENERATION } };
        VALID_EFFECTS = Sets.newHashSet();
        for (final Potion[] apotion : TileEntityBeacon.EFFECTS_LIST) {
            Collections.addAll(TileEntityBeacon.VALID_EFFECTS, apotion);
        }
    }


    public static class BeamSegment {
        private float[] colors;
        private int height;

        public BeamSegment(final float[] colorsIn) {
            this.colors = colorsIn;
            this.height = 1;
        }

        public void init(final float[] colorsIn){
            this.colors = colorsIn;
            this.height = 1;
        }


        protected void incrementHeight() {
            ++this.height;
        }


        public float[] getColors() {
            return this.colors;
        }


        @SideOnly(Side.CLIENT)
        public int getHeight() {
            return this.height;
        }
    }
}
