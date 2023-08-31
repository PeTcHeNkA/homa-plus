package plus.common.block;

import net.minecraft.block.BlockOre;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import plus.SpMain;
import plus.common.mashine.item.BasicItem;
import plus.common.mashine.item.recept.ISimpleSmeling;
import java.util.Random;


public class SimpleOre extends BlockOre implements ISimpleSmeling<SimpleOre> {
    public static SimpleIronBlock COPPER_BLOCK = new SimpleIronBlock("copper_block");
    public static SimpleIronBlock ALUMINE_BLOCK = new SimpleIronBlock("alumine_block");
    public static SimpleIronBlock MEFRIL_BLOCK = new SimpleIronBlock("mefril_block");
    public static SimpleIronBlock ARMORED_BLOCK = new SimpleIronBlock("armored_block");
    public static SimpleBlock METEOR_STONE = new SimpleBlock(Material.ROCK, "stone_met")
            .setHardness(3f).setResistance(5f).setSoundType(SoundType.STONE)
            .setHarvestLvl("pickaxe", 1);

    public static final SimpleOre COPPER_ORE = new SimpleOre("copper_ore", 1)
            .addSmeling(new ItemStack(BasicItem.COPPER_INGOT));
    public static final SimpleOre COPPER_ORE_MET = new SimpleOre("copper_ore_met", 2)
            .addSmeling(new ItemStack(BasicItem.COPPER_INGOT));
    public static final SimpleOre ALUMINE_ORE = new SimpleOre("alumine_ore", 1)
            .addSmeling(new ItemStack(BasicItem.COPPER_INGOT));
    public static final SimpleOre ALUMINE_ORE_MET = new SimpleOre("alumine_ore_met", 2)
            .addSmeling(new ItemStack(BasicItem.COPPER_INGOT));
    public static final SimpleOre MEFRIL_ORE = new SimpleOre("mefril_ore", 3)
            .addSmeling(new ItemStack(BasicItem.MEFRIL_INGOT));
    public static final SimpleOre MEFRIL_ORE_MET = new SimpleOre("mefril_ore_met", 3)
            .addSmeling(new ItemStack(BasicItem.MEFRIL_INGOT));

    public static final SimpleOre GOLD_ORE_MET = new SimpleOre("gold_ore_met", 2)
            .addSmeling(new ItemStack(Items.GOLD_INGOT));
    public static final SimpleOre IRON_ORE_MET = new SimpleOre("iron_ore_met", 2)
            .addSmeling(new ItemStack(Items.IRON_INGOT));



    

    public SimpleOre(String regName, int lvl){
        setRegistryName(regName);
        setTranslationKey(regName);
        setCreativeTab(SpMain.SP_TAB);
        setHardness(3f);
        setResistance(5f);
        setSoundType(SoundType.STONE);
        setHarvestLevel("pickaxe", lvl);
        SpMain.proxy.addToRegObj(this);
    }


    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(this);
    }


    @Override
    public int quantityDropped(Random random) {
        return 1;
    }


    @Override
    public int getExpDrop(IBlockState state, net.minecraft.world.IBlockAccess world, BlockPos pos, int fortune){
        return 0;
    }
}