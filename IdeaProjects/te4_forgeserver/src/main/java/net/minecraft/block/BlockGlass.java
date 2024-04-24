package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class BlockGlass extends BlockBreakableFs {
    public BlockGlass(Material materialIn, boolean ignoreSimilarity) {
        super(materialIn, ignoreSimilarity);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }


    @Override
    public int quantityDropped(Random random) {
        return 0;
    }


    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }


    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }


    @Override
    protected boolean canSilkHarvest() {
        return true;
    }
}