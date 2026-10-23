package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import plus.fserver.api.Keep;
import plus.fserver.block.BlockFx;


@Keep
public class BlockNetherrack extends BlockFx {
    public BlockNetherrack() {
        super(Material.ROCK, MapColor.NETHERRACK);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }
}