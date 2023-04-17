package plus.common.block;

import net.minecraft.block.BlockOre;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
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

    public static final SimpleOre COPPER_ORE = new SimpleOre("copper_ore", 1)
            .addSmeling(new ItemStack(BasicItem.COPPER_INGOT));
    public static final SimpleOre ALUMINE_ORE = new SimpleOre("alumine_ore", 1)
            .addSmeling(new ItemStack(BasicItem.COPPER_INGOT));
    public static final SimpleOre MEFRIL_ORE = new SimpleOre("mefril_ore", 3)
            .addSmeling(new ItemStack(BasicItem.MEFRIL_INGOT));


    public static final SimpleOre AETHER_ORE = new SimpleOre("aether_ore", 3){
        @Override
        public Item getItemDropped(IBlockState state, Random rand, int fortune) {
            return BasicItem.AETHER_CRYSTAL;
        }
        @Override
        public int quantityDropped(Random random) {
            return 2 + random.nextInt(3);
        }
        @Override
        public int getExpDrop(IBlockState state, net.minecraft.world.IBlockAccess world, BlockPos pos, int fortune){
            Random rand = world instanceof World ? ((World)world).rand : RANDOM;
            return MathHelper.getInt(rand, 3, 7);
        }
    }.addSmeling(new ItemStack(BasicItem.AETHER_CRYSTAL, 3));
    

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