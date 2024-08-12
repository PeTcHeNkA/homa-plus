package net.minecraft.tileentity;

import net.minecraft.nbt.*;
import net.minecraftforge.common.*;
import net.minecraft.world.*;
import net.minecraft.util.math.*;
import net.minecraft.block.material.*;
import net.minecraft.init.*;
import net.minecraft.block.state.*;
import net.minecraft.block.*;


public class TileEntityNote extends TileEntity {
    public byte note;
    public boolean previousRedstoneState;

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setByteU("note", this.note);
        compound.setBoolean("powered", this.previousRedstoneState);
        return compound;
    }


    @Override
    public void readFromNBT(final NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.note = compound.getByte("note");
        this.note = (byte)MathHelper.clamp(this.note, 0, 24);
        this.previousRedstoneState = compound.getBoolean("powered");
    }


    public void changePitch() {
        final byte old = this.note;
        this.note = (byte)((this.note + 1) % 25);
        if (!ForgeHooks.onNoteChange(this, old)) {
            return;
        }
        this.markDirty();
    }


    public void triggerNote(final World worldIn, final BlockPos posIn) {
        if (worldIn.getBlockState(posIn.up()).getMaterial() == Material.AIR) {
            final IBlockState iblockstate = worldIn.getBlockState(posIn.down());
            final Material material = iblockstate.getMaterial();
            int i = 0;
            if (material == Material.ROCK) {
                i = 1;
            }
            if (material == Material.SAND) {
                i = 2;
            }
            if (material == Material.GLASS) {
                i = 3;
            }
            if (material == Material.WOOD) {
                i = 4;
            }
            final Block block = iblockstate.getBlock();
            if (block == Blocks.CLAY) {
                i = 5;
            }
            if (block == Blocks.GOLD_BLOCK) {
                i = 6;
            }
            if (block == Blocks.WOOL) {
                i = 7;
            }
            if (block == Blocks.PACKED_ICE) {
                i = 8;
            }
            if (block == Blocks.BONE_BLOCK) {
                i = 9;
            }
            worldIn.addBlockEvent(posIn, Blocks.NOTEBLOCK, i, this.note);
        }
    }
}